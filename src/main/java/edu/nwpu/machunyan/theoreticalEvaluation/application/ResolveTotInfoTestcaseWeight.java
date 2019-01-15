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

public class ResolveTotInfoTestcaseWeight {

    // 结果的输出路径
    private static final String resultOutputPath = "./target/outputs/tot_info-testcase-weight.json";
    // 结果的加权值
    private static final double weightMultiple = 5.0;

    public static void main(String[] args) throws IOException {

        final RunResultJam imports = RunTotInfo.getRunResultsFromSavedFile();
        final List<TestcaseWeightResolver> resolver = TestcaseWeightResolver.of(
            SuspiciousnessFactorFormulas.getAllFormulas(),
            weightMultiple);
        final TestcaseWeightJam result = TestcaseWeightHelper.runOnAllResolvers(imports, resolver);

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
