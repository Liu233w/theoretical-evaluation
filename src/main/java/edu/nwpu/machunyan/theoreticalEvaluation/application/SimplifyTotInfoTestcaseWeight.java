package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestcaseWeight;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.IOException;

public class SimplifyTotInfoTestcaseWeight {

    private static String outputPath = "./target/outputs/tot_info-testcase-weight-simplified.json";

    public static void main(String[] args) throws IOException {

        final TestcaseWeight.Pojo.Jam input = ResolveTotInfoTestcaseWeight.loadFromFile();
        final TestcaseWeight.Pojo.Jam result = TestcaseWeight.Helper.simplifyTestcaseWeights(input);
        FileUtils.saveObject(outputPath, result);
    }
}
