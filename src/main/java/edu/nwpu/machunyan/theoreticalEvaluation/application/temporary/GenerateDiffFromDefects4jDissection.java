package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvLine;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import lombok.Cleanup;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * 从 https://github.com/program-repair/defects4j-dissection 里提取出我需要的数据
 */
public class GenerateDiffFromDefects4jDissection {

    private static final String outputPath = "./target/outputs/defects4j-dissection.csv";

    public static void main(String[] args) throws IOException {

        @Cleanup final InputStreamReader urlReader
            = new InputStreamReader(new URL("https://raw.githubusercontent.com/program-repair/defects4j-dissection/master/defects4j-bugs.json").openStream());

        final JsonElement root = new JsonParser().parse(urlReader);

        final LinkedList<CsvLine> csvLines = new LinkedList<>();

        csvLines.add(new CsvLine(new Object[]{
            "program title", "version", "diff", "changed line",
        }));

        for (JsonElement element : root.getAsJsonArray()) {
            final JsonObject item = element.getAsJsonObject();

            final Set<Map.Entry<String, JsonElement>> changedFiles = item.getAsJsonObject("changedFiles").entrySet();
            final StringBuilder sb = new StringBuilder();
            EntryStream
                .of(changedFiles.iterator())
                .forKeyValue((file, value) -> {

                    sb.append(file).append(":");

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

                    StreamEx.of(res)
                        .sorted()
                        .forEach(lineNum -> sb.append(lineNum).append(","));
                    sb.deleteCharAt(sb.length() - 1);//移除末尾的逗号
                    sb.append("\n");
                });

            csvLines.add(new CsvLine(new Object[]{
                item.getAsJsonPrimitive("project").getAsString(),
                item.getAsJsonPrimitive("bugId").getAsInt() + "b",
                item.getAsJsonPrimitive("diff").getAsString(),
                sb.toString(),
            }));
        }

        FileUtils.saveString(outputPath, CsvExporter.toCsvString(csvLines));
    }
}
