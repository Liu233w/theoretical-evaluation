package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestcaseWeight;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class ResolveTotInfoTestcaseWeight {

    // 结果的输出路径
    private static final String resultOutputPath = "./target/outputs/tot_info-testcase-weight.json";

    public static void main(String[] args) throws IOException {

        final RunResultJam imports = RunTotInfo.getRunResultsFromSavedFile();
        final List<TestcaseWeight.Resolver> resolver = TestcaseWeight.Resolver.of(SuspiciousnessFactorFormulas.getAllFormulas());
        final TestcaseWeight.Pojo.Jam result = TestcaseWeight.Helper.runOnAllResolvers(imports, resolver);

        FileUtils.saveObject(resultOutputPath, result);
    }

    /**
     * 从运行结果读取数据，必须已经存在
     *
     * @return
     */
    public static TestcaseWeight.Pojo.Jam loadFromFile() throws FileNotFoundException {
        return FileUtils.loadObject(resultOutputPath, TestcaseWeight.Pojo.Jam.class);
    }
}
