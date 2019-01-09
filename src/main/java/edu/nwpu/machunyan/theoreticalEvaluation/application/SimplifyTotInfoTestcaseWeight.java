package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestcaseWeightHelper;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.IOException;

public class SimplifyTotInfoTestcaseWeight {

    private static String outputPath = "./target/outputs/tot_info-testcase-weight-simplified.json";

    public static void main(String[] args) throws IOException {

        final TestcaseWeightJam input = ResolveTotInfoTestcaseWeight.loadFromFile();
        final TestcaseWeightJam result = TestcaseWeightHelper.simplifyTestcaseWeights(input);
        FileUtils.saveObject(outputPath, result);
    }
}
