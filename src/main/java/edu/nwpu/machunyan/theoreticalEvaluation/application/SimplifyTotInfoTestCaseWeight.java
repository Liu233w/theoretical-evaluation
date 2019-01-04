package edu.nwpu.machunyan.theoreticalEvaluation.application;

import com.google.gson.JsonElement;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestCaseWeightResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestCaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.interData.TestCaseWeightJsonProcessor;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.GsonUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

public class SimplifyTotInfoTestCaseWeight {

    private static String outputPath = "./target/outputs/tot_info-testcase-weight-simplified.json";

    public static void main(String[] args) throws IOException {
//        final TestCaseWeightJam testCaseWeightJam = ResolveTotInfoTestCaseWeight.loadFromFile();
//        final TestCaseWeightJam result = TestCaseWeightResolver.simplifyResult(testCaseWeightJam);
//
//        final JsonElement jsonElement = TestCaseWeightJsonProcessor.dumpToJson(result);
//        FileUtils.printJsonToFile(Paths.get(outputPath), jsonElement);
    }
}
