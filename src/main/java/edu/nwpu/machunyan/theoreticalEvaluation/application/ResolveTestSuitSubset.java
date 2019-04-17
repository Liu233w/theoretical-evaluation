package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormula;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestSuitSubsetResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestcaseWeightResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestSuitSubsetJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import lombok.Cleanup;
import lombok.Value;
import me.tongfei.progressbar.ProgressBar;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 获取划分之后的测试用例子集
 */
public class ResolveTestSuitSubset {

    // 结果的输出路径
    private static final String resultOutputDir = "./target/outputs/test-suit-subset";

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

            final RunResultJam imports = Run.getResultFromFile(name);

            for (Map.Entry<String, SuspiciousnessFactorFormula> entry :
                SuspiciousnessFactorFormulas.getAllFormulas().entrySet()) {

                final String formulaTitle = entry.getKey();
                final SuspiciousnessFactorFormula formula = entry.getValue();

                // 跳过已经计算出的结果
                if (Files.exists(Paths.get(resolveResultFilePath(name, formulaTitle)))) {
                    LogUtils.logInfo("skip " + name + "-" + formulaTitle);
                    continue;
                }

                LogUtils.logInfo("working on " + name + "-" + formulaTitle);

                final TestSuitSubsetResolver resolver = new TestSuitSubsetResolver(formula, formulaTitle);
                final TestSuitSubsetJam result = resolver.resolve(imports);

                FileUtils.saveObject(resolveResultFilePath(name, formulaTitle), result);
            }
        }
    }

    /**
     * 从运行结果读取数据，必须已经存在
     *
     * @return
     */
    public static TestSuitSubsetJam getResultFromFile(
        String name,
        String formulaTitle) throws FileNotFoundException {

        return FileUtils.loadObject(resolveResultFilePath(name, formulaTitle), TestSuitSubsetJam.class);
    }

    private static String resolveResultFilePath(String programName, String formulaTitle) {
        return resultOutputDir + "/" + programName + "-" + formulaTitle + ".json";
    }
}
