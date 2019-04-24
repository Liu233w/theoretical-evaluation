package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormula;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestSuitSubsetResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestSuitSubsetForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestSuitSubsetJam;
import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.ProgramDefination;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CacheHandler;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import lombok.Cleanup;
import me.tongfei.progressbar.ProgressBar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 获取划分之后的测试用例子集
 */
public class ResolveTestSuitSubset {

    // 结果的输出路径
    private static final String resultOutputDir = "./target/outputs/test-suit-subset";

    public static void main(String[] args) throws IOException {

        for (String name : ProgramDefination.PROGRAM_LIST) {

            final RunResultJam imports = Run.getResultFromFile(name);
            final Set<Map.Entry<String, SuspiciousnessFactorFormula>> entrySet = SuspiciousnessFactorFormulas.getAllFormulas().entrySet();

            @Cleanup final ProgressBar progressBar = new ProgressBar("", imports.getRunResultForPrograms().size() * entrySet.size());

            for (Map.Entry<String, SuspiciousnessFactorFormula> entry :
                entrySet) {

                final String formulaTitle = entry.getKey();
                final SuspiciousnessFactorFormula formula = entry.getValue();

                // 跳过已经计算出的结果
                if (Files.exists(Paths.get(resolveResultFilePath(name, formulaTitle)))) {
                    LogUtils.logInfo("skip " + name + "-" + formulaTitle);
                    progressBar.maxHint(progressBar.getMax() - imports.getRunResultForPrograms().size());
                    continue;
                }

                LogUtils.logInfo("working on " + name + "-" + formulaTitle);

                // 缓存中间结果
                final CacheHandler cache = new CacheHandler("testsuit-subset-" + name + "-" + formulaTitle);
                final TestSuitSubsetResolver.Reporter reporter = item -> {
                    final String key = item.getProgramTitle();
                    LogUtils.logFine("saving cache: " + key);
                    cache.saveCache(key, item);
                    progressBar.step();
                };
                final TestSuitSubsetResolver.Provider provider = (resolver, input) -> {
                    final String key = input.getProgramTitle();
                    final Optional<TestSuitSubsetForProgram> result = cache.tryLoadCache(key, TestSuitSubsetForProgram.class);
                    if (result.isPresent()) {

                        // 避免缓存影响对剩余时间的计算
                        progressBar.maxHint(progressBar.getMax() - 1);
                        progressBar.stepBy(-1);

                        LogUtils.logFine("loaded item from cache: " + key);
                    }
                    return result;
                };

                final TestSuitSubsetResolver resolver = TestSuitSubsetResolver
                    .builder()
                    .sfFormula(formula)
                    .formulaTitle(formulaTitle)
                    .provider(provider)
                    .reporter(reporter)
                    .useParallel(true)
                    .build();
                final TestSuitSubsetJam result = resolver.resolve(imports);

                FileUtils.saveObject(resolveResultFilePath(name, formulaTitle), result);

                cache.deleteAllCaches();
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
