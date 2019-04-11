package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestSuitSubsetResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestSuitSubsetJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ResolveTotInfoTestSuitSubset {

    // 结果的输出路径
    private static final String resultOutputPath = "./target/outputs/tot_info-test-suit-subset-op.json";

    public static void main(String[] args) throws IOException {

        final RunResultJam imports = RunTotInfo.getRunResultsFromSavedFile();
        final TestSuitSubsetResolver resolver = new TestSuitSubsetResolver(SuspiciousnessFactorFormulas::op);

        final TestSuitSubsetJam result = resolver.resolve(imports);

        FileUtils.saveObject(resultOutputPath, result);
    }

    /**
     * 从运行结果读取数据，必须已经存在
     *
     * @return
     */
    public static TestSuitSubsetJam loadFromFile() throws FileNotFoundException {
        return FileUtils.loadObject(resultOutputPath, TestSuitSubsetJam.class);
    }
}
