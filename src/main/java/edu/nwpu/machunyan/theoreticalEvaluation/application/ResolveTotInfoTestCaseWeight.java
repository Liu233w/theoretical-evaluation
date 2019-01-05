package edu.nwpu.machunyan.theoreticalEvaluation.application;

import com.google.gson.JsonElement;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.interData.TestCaseWeightJsonProcessor;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ResolveTotInfoTestCaseWeight {

    // 结果的输出路径
    private static final String resultOutputPath = "./target/outputs/tot_info-testcase-weight.json";

    public static void main(String[] args) throws IOException {

//        final Map<String, ArrayList<RunResultFromRunner>> runResultsForVersion = RunTotInfo.getRunResultsFromSavedFile();
//        final TestcaseWeightJam testCaseWeightJam = TestCaseWeightResolver.resolveFromRunResults(runResultsForVersion, true);
//        final JsonElement result = TestCaseWeightJsonProcessor.dumpToJson(testCaseWeightJam);
//
//        FileUtils.printJsonToFile(Paths.get(resultOutputPath), result);
    }

    /**
     * 从运行结果读取数据，必须已经存在
     *
     * @return
     */
    public static TestcaseWeightJam loadFromFile() throws FileNotFoundException {
        final JsonElement jsonFromFile = FileUtils.getJsonFromFile(resultOutputPath);
        return TestCaseWeightJsonProcessor.loadAllFromJson(jsonFromFile);
    }
}
