package edu.nwpu.machunyan.theoreticalEvaluation.application;

import com.google.gson.JsonElement;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestCaseWeightResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestCaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.interData.TestCaseWeightJsonProcessor;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.SingleRunResult;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class ResolveTotInfoTestCaseWeight {

    // 结果的输出路径
    private static final String resultOutputPath = "./target/outputs/tot_info-testcase-weight.json";

    public static void main(String[] args) throws IOException {

        final Map<String, ArrayList<SingleRunResult>> runResultsForVersion = RunTotInfo.getRunResultsFromSavedFile();
        final TestCaseWeightJam testCaseWeightJam = TestCaseWeightResolver.resolveFromRunResults(runResultsForVersion, true);
        final JsonElement result = TestCaseWeightJsonProcessor.dumpToJson(testCaseWeightJam);

        FileUtils.printJsonToFile(Paths.get(resultOutputPath), result);
    }

    /**
     * 从运行结果读取数据，必须已经存在
     *
     * @return
     */
    public static TestCaseWeightJam loadFromFile() throws FileNotFoundException {
        final JsonElement jsonFromFile = FileUtils.getJsonFromFile(resultOutputPath);
        return TestCaseWeightJsonProcessor.loadAllFromJson(jsonFromFile);
    }
}
