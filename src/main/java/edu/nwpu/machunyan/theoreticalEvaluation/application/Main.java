package edu.nwpu.machunyan.theoreticalEvaluation.application;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * 按顺序运行多个程序
 */
public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException {

        Run.main(args);
        ResolveSuspiciousnessFactor.main(args);
        ResolveTestcaseWeightByOp.main(args);
        ResolveTestSuitSubsetByOp.main(args);
        ResolveTestcaseWeight.main(args);
        ResolveTestSuitSubset.main(args);
        DiffSfWeightedByCertainFormula.main(args);
    }
}
