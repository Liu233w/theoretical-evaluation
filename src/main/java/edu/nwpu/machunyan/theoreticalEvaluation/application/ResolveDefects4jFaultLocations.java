package edu.nwpu.machunyan.theoreticalEvaluation.application;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.FaultLocationForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.FaultLocationJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.StatementInfo;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.StatementMap;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvLine;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Data;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * 从 https://github.com/program-repair/defects4j-dissection 里提取出我需要的数据
 */
public class ResolveDefects4jFaultLocations {

    private static final String outputDir = "./target/outputs/defects4j-fault-locations/";

    public static void main(String[] args) throws IOException {

        final Map<String, List<Entity>> result = resolveProgramToEntities();

        saveSummary(result);
        saveEach(result);
    }

    private static void saveEach(Map<String, List<Entity>> result) throws IOException {

        for (Map.Entry<String, List<Entity>> entry : result.entrySet()) {
            String programName = entry.getKey();
            List<Entity> entities = entry.getValue();

            final RunResultJam runResults;
            try {
                runResults = Run.getResultFromFile(programName);
            } catch (FileNotFoundException e) {
                LogUtils.logInfo("Run result of " + programName + " does not exist, skipping");
                continue;
            }

            final Map<String, RunResultForProgram> versionToRunResult = StreamEx
                .of(runResults.getRunResultForPrograms())
                .mapToEntry(
                    RunResultForProgram::getProgramTitle,
                    a -> a)
                .toMap();

            final FaultLocationJam jam = StreamEx
                .of(entities)
                .map(entity -> {
                    final StatementMap statementMap = versionToRunResult.get(entity.getVersion()).getStatementMap();

                    // fileName -> lineNumber -> statementIndex
                    final Map<String, Map<Integer, Integer>> fileToLineNumberToStatementIndex = StreamEx
                        .of(statementMap.getMapList())
                        // 第一个元素是 null
                        .skip(1)
                        .mapToEntry(
                            StatementInfo::getFilePath,
                            a -> a)
                        .collapseKeys()
                        .mapValues(StreamEx::of)
                        .mapValues(a -> a.mapToEntry(
                            StatementInfo::getStartRow,
                            StatementInfo::getStatementIndex))
                        .mapValues(EntryStream::toMap)
                        .toMap();

                    final Set<Integer> locations = EntryStream
                        .of(entity.getFileToLineNumbers())
                        .flatMapKeyValue((file, lineNumbers) -> StreamEx.of(lineNumbers)
                            .map(lineNumber -> {
                                final Map<Integer, Integer> map = fileToLineNumberToStatementIndex.get(file);
                                if (map == null) {
                                    LogUtils.logError("File name not exist in run results, that can be an error\n" +
                                        "Project: " + programName + ", Version: " + entity.getVersion() + ", File name: " + file);
                                    return null;
                                }
                                return map.get(lineNumber);
                            })
                            .filter(Objects::nonNull))
                        .toSet();

                    return new FaultLocationForProgram(
                        entity.getVersion(),
                        locations,
                        entity.getDiff(),
                        "",
                        entity.isUsedInEffectSize());
                })
                .toListAndThen(FaultLocationJam::new);

            FileUtils.saveObject(outputDir + programName + ".json", jam);
        }
    }

    private static void saveSummary(Map<String, List<Entity>> result) throws IOException {

        final LinkedList<CsvLine> csvLines = new LinkedList<>();

        csvLines.add(new CsvLine(new Object[]{
            "program title", "version", "diff", "changed line",
        }));

        for (Map.Entry<String, List<Entity>> entry : result.entrySet()) {
            String programName = entry.getKey();
            List<Entity> entities = entry.getValue();

            for (Entity entity : entities) {

                final StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, Set<Integer>> e : entity.getFileToLineNumbers().entrySet()) {
                    String file = e.getKey();
                    Set<Integer> lineNumbers = e.getValue();

                    sb.append(file).append(":");
                    StreamEx.of(lineNumbers)
                        .sorted()
                        .forEach(lineNum -> sb.append(lineNum).append(","));
                    sb.deleteCharAt(sb.length() - 1);//移除末尾的逗号
                    sb.append("\n");
                }

                csvLines.add(new CsvLine(new Object[]{
                    programName,
                    entity.getVersion(),
                    entity.getDiff(),
                    sb.toString(),
                }));
            }
        }

        FileUtils.saveString(outputDir + "summary.csv", CsvExporter.toCsvString(csvLines));
    }

    private static Map<String, List<Entity>> resolveProgramToEntities() throws IOException {

        @Cleanup final InputStreamReader urlReader
            = new InputStreamReader(new URL("https://raw.githubusercontent.com/program-repair/defects4j-dissection/master/defects4j-bugs.json").openStream());

        final JsonElement root = new JsonParser().parse(urlReader);

        final Map<String, List<Entity>> result = new HashMap<>();

        for (JsonElement element : root.getAsJsonArray()) {
            final JsonObject item = element.getAsJsonObject();

            final String programName = item.getAsJsonPrimitive("project").getAsString();
            final String versionNum = item.getAsJsonPrimitive("bugId").getAsInt() + "b";
            final String diff = item.getAsJsonPrimitive("diff").getAsString();

            final Entity entity = new Entity(versionNum, diff, new HashMap<>(), true);

            result
                .computeIfAbsent(programName, k -> new LinkedList<>())
                .add(entity);

            final Set<Map.Entry<String, JsonElement>> changedFiles = item.getAsJsonObject("changedFiles").entrySet();
            EntryStream
                .of(changedFiles.iterator())
                .forKeyValue((file, value) -> {

                    final HashSet<Integer> res = new HashSet<>();
                    StreamEx<JsonElement> stream = StreamEx.empty();

                    final JsonObject object = value.getAsJsonObject();

                    JsonArray array = object.getAsJsonArray("changes");
                    if (array != null) {
                        stream = stream.append(StreamEx.of(array.iterator()));
                    }
                    array = object.getAsJsonArray("deletes");
                    if (array != null) {
                        stream = stream.append(StreamEx.of(array.iterator()));
                    }

                    stream
                        .map(JsonElement::getAsJsonArray)
                        .map(JsonArray::iterator)
                        .flatMap(StreamEx::of)
                        .map(JsonElement::getAsInt)
                        .toListAndThen(res::addAll);

                    // 如果要添加一行（代表bug版本少了一行），则将其前后的 RANGE 行也算作可疑代码行
                    final int RANGE = 1;
                    array = object.getAsJsonArray("inserts");
                    if (array != null) {

                        // 对于这种情况就不统计进 effect size 里了
                        entity.setUsedInEffectSize(false);

                        for (JsonElement elem : array) {

                            // 每个插入有多个可能的位置，这里只从第一个位置生成范围
                            // 剩下的可能位置直接加入
                            final Iterator<JsonElement> iterator = elem.getAsJsonArray().iterator();
                            final int first = iterator.next().getAsInt();
                            if (first == -1) {
                                continue;
                            }
                            for (int i = first - RANGE; i <= first + RANGE; i++) {
                                res.add(i);
                            }
                            iterator.forEachRemaining(a -> res.add(a.getAsInt()));
                        }
                    }

                    entity.getFileToLineNumbers().put(file, res);
                });
        }

        return result;
    }

    @Data
    @AllArgsConstructor
    private static class Entity {
        String version;
        String diff;
        Map<String, Set<Integer>> fileToLineNumbers;
        boolean usedInEffectSize;
    }
}
