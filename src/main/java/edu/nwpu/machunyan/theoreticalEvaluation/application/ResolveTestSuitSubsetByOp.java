package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestSuitSubsetResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestSuitSubsetJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 获取划分之后的测试用例子集，使用 Op 来划分
 */
public class ResolveTestSuitSubsetByOp {

    // 结果的输出路径
    private static final String resultOutputDir = "./target/outputs/test-suit-subset-op";

    private static final String[] MAIN_LIST = new String[]{
        "my_sort",
        "schedule2",
        "tcas",
        "tot_info",
        "replace",
        "print_tokens",
    };

    public static void main(String[] args) throws IOException {

        for (String name : MAIN_LIST) {

            // 跳过已经计算出的结果
            if (Files.exists(Paths.get(resolveResultFilePath(name)))) {
                continue;
            }

            LogUtils.logInfo("working on " + name);

            final RunResultJam imports = Run.getResultFromFile(name);
            final TestSuitSubsetResolver resolver = new TestSuitSubsetResolver(SuspiciousnessFactorFormulas::op);

            final TestSuitSubsetJam result = resolver.resolve(imports);

            FileUtils.saveObject(resolveResultFilePath(name), result);
        }
    }

    /**
     * 从运行结果读取数据，必须已经存在
     *
     * @return
     */
    public static TestSuitSubsetJam getResultFromFile(String name) throws FileNotFoundException {
        return FileUtils.loadObject(resolveResultFilePath(name), TestSuitSubsetJam.class);
    }

    private static String resolveResultFilePath(String programName) {
        return resultOutputDir + "/" + programName + ".json";
    }
}
