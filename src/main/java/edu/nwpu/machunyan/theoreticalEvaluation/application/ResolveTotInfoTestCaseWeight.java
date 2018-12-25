package edu.nwpu.machunyan.theoreticalEvaluation.application;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestCaseWeightResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.SingleRunResult;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.IntStream;

public class ResolveTotInfoTestCaseWeight {

    // 结果的输出路径
    private static final String resultOutputPath = "./target/outputs/tot_info-testcase-weight.json";

    public static void main(String[] args) throws IOException {

        final Map<String, ArrayList<SingleRunResult>> imports = RunTotInfo.getRunResultsFromSavedFile();

        final JsonArray result = imports.entrySet().stream()
                .parallel()
                .map(entry -> {
                    final ArrayList<Double> weight = TestCaseWeightResolver.resolveTestCaseWeight(entry.getValue());
                    final JsonArray weights = IntStream.range(0, weight.size())
                            .mapToObj(i -> {
                                final JsonObject jsonObject = new JsonObject();
                                jsonObject.add("testcase-index", new JsonPrimitive(i));
                                jsonObject.add("testcase-weight", new JsonPrimitive(weight.get(i)));
                                return jsonObject;
                            })
                            .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
                    final JsonObject recordForVersion = new JsonObject();
                    recordForVersion.add("version", new JsonPrimitive(entry.getKey()));
                    recordForVersion.add("weight-for-testcases", weights);
                    return recordForVersion;
                })
                .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);

        FileUtils.printJsonToFile(Paths.get(resultOutputPath), result);
    }
}
