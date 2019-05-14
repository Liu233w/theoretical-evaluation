package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

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
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
                .mapValues(a -> a.getAsJsonObject().getAsJsonArray("changes"))
                .filterValues(Objects::nonNull)
                .flatMapValues(a -> StreamEx.of(a.iterator()))
                .mapValues(JsonElement::getAsInt)
                .forKeyValue((file, lineNum) -> sb.append(file).append(":").append(lineNum).append("\n"));

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
