package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestcaseWeightHelper;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestcaseWeightResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class ResolveTestcaseWeight {

    private static final String resultDir = "./target/outputs/testcase-weights";

    private static final String[] MAIN_LIST = new String[]{
        "my_sort",
        "schedule2",
        "tcas",
        "tot_info",
    };

    public static void main(String[] args) throws IOException {

        for (String name : MAIN_LIST) {

            // 跳过已经计算出的结果
            try {
                getResultFromFile(name);
                break;
            } catch (FileNotFoundException ignored) {
            }

            System.out.println("Running on " + name);

            final RunResultJam imports = Run.getResultFromFile(name);
            final List<TestcaseWeightResolver> resolver = TestcaseWeightResolver.of(SuspiciousnessFactorFormulas.getAllFormulas());
            final TestcaseWeightJam result = TestcaseWeightHelper.runOnAllResolvers(imports, resolver);

            FileUtils.saveObject(resolveResultFilePath(name), result);
        }
    }

    public static TestcaseWeightJam getResultFromFile(String programName) throws FileNotFoundException {
        final String path = resolveResultFilePath(programName);
        return FileUtils.loadObject(path, TestcaseWeightJam.class);
    }


    private static String resolveResultFilePath(String programName) {
        return resultDir + "/" + programName + ".json";
    }

}
