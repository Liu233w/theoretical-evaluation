package edu.nwpu.machunyan.theoreticalEvaluation.application;

import com.google.gson.JsonArray;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestCaseWeightResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.interData.TestCaseWeightJsonProcessor;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.SingleRunResult;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResolveTotInfoTestCaseWeight {

    // 结果的输出路径
    private static final String resultOutputPath = "./target/outputs/tot_info-testcase-weight.json";

    public static void main(String[] args) throws IOException {

        final Map<String, ArrayList<SingleRunResult>> imports = RunTotInfo.getRunResultsFromSavedFile();

        final Map<String, List<Double>> result = imports.entrySet().stream()
                .parallel()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> TestCaseWeightResolver.resolveTestCaseWeight(entry.getValue())
                ));

        FileUtils.printJsonToFile(Paths.get(resultOutputPath), TestCaseWeightJsonProcessor.dumpToJson(result));
    }

    /**
     * 从运行结果读取数据，必须已经存在
     *
     * @return
     */
    public static Map<String, List<Double>> loadFromFile() throws FileNotFoundException {
        final JsonArray jsonArray = FileUtils.getJsonFromFile(resultOutputPath).getAsJsonArray();
        return TestCaseWeightJsonProcessor.loadAllFromJson(jsonArray);
    }
}
