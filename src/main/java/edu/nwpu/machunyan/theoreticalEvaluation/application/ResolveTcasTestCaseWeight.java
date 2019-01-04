package edu.nwpu.machunyan.theoreticalEvaluation.application;

import com.google.gson.JsonElement;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestCaseWeightResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestCaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.interData.TestCaseWeightJsonProcessor;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.RunResultFromRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class ResolveTcasTestCaseWeight {

    // 结果的输出路径
    private static final String resultOutputPath = "./target/outputs/tcas-testcase-weight.json";

    public static void main(String[] args) throws IOException {

        final HashMap<String, ArrayList<RunResultFromRunner>> runResultsForVersion = RunTcas.getRunResultsFromSavedFile();
        final TestCaseWeightJam testCaseWeightJam = TestCaseWeightResolver.resolveFromRunResults(runResultsForVersion);
        final JsonElement result = TestCaseWeightJsonProcessor.dumpToJson(testCaseWeightJam);

        FileUtils.printJsonToFile(Paths.get(resultOutputPath), result);
    }
}
