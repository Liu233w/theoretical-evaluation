package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestcaseWeightResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ResolveTotInfoTestCaseWeight {

    // 结果的输出路径
    private static final String resultOutputPath = "./target/outputs/tot_info-testcase-weight.json";

    public static void main(String[] args) throws IOException {

        final RunResultJam imports = RunTotInfo.getRunResultsFromSavedFile();
        final TestcaseWeightJam result = new TestcaseWeightResolver(SuspiciousnessFactorFormulas::o)
            .resolve(imports);
        FileUtils.saveObject(resultOutputPath, result);
    }

    /**
     * 从运行结果读取数据，必须已经存在
     *
     * @return
     */
    public static TestcaseWeightJam loadFromFile() throws FileNotFoundException {
        return FileUtils.loadObject(resultOutputPath, TestcaseWeightJam.class);
    }
}
