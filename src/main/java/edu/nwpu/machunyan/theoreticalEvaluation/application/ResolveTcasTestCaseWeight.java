package edu.nwpu.machunyan.theoreticalEvaluation.application;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestCaseWeightResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.SingleRunResult;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.IntStream;

public class ResolveTcasTestCaseWeight {

    // 结果的输出路径
    private static final String resultOutputPath = "./target/outputs/tcas-testcase-weight.json";

    public static void main(String[] args) throws IOException {

        final HashMap<String, ArrayList<SingleRunResult>> runResultsForVersion = RunTcas.getRunResultsFromSavedFile();
//        final int versionCount = runResultsForVersion.size();
        final int versionCount = 2;

        final JsonArray result = IntStream.range(1, versionCount)
                .mapToObj(i -> runResultsForVersion.get("v" + i))
                .parallel()
                .map(runResults -> {
                    final ArrayList<Double> testCaseWeight = TestCaseWeightResolver.resolveTestCaseWeight(runResults);
                    final JsonArray outputWeight = new JsonArray(testCaseWeight.size());
                    for (int i = 0; i < testCaseWeight.size(); i++) {
                        final JsonObject jsonObject = new JsonObject();
                        jsonObject.add("testcase-index", new JsonPrimitive(i));
                        jsonObject.add("testcase-weight", new JsonPrimitive(testCaseWeight.get(i)));
                        outputWeight.add(jsonObject);
                    }

                    final JsonObject singleResult = new JsonObject();
                    singleResult.add("version", new JsonPrimitive(runResults.get(0).getProgram().getTitle()));
                    singleResult.add("weight-for-testcases", outputWeight);
                    return singleResult;
                })
                .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);

        FileUtils.printJsonToFile(Paths.get(resultOutputPath), result);
    }
}
