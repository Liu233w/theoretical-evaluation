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

    private static final String[] sfRates = new String[]{
        "0.3",
        "0.5",
    };

    public static void main(String[] args) throws IOException {

        for (String name : ProgramDefination.PROGRAM_LIST) {
            resolve(name, null);
            for (String sfRate : sfRates) {
                resolve(name, sfRate);
            }
        }
    }

    private static void resolve(String name, String preLimitSfRate) throws IOException {

        /*
        sfRate: 实际用作计算的数值，为 0 时表示不取前百分之多少
        preLimitRate: 在显示和保存文件名时使用的，防止浮点数精度问题
            如果不取前百分之n，这个值为空字符串，便于复用之前的计算结果
         */
        final double sfRate;
        if (preLimitSfRate == null || preLimitSfRate.equals("")) {
            preLimitSfRate = "";
            sfRate = 0.0;
        } else {
            sfRate = Double.parseDouble(preLimitSfRate);
            preLimitSfRate = "-" + preLimitSfRate;
        }

        final RunResultJam imports = Run.getResultFromFile(name);
        final Set<Map.Entry<String, SuspiciousnessFactorFormula>> entrySet = SuspiciousnessFactorFormulas.getAllFormulas().entrySet();

        @Cleanup final ProgressBar progressBar = LogUtils.newProgressBarInstance("", imports.getRunResultForPrograms().size() * entrySet.size());

        for (Map.Entry<String, SuspiciousnessFactorFormula> entry :
            entrySet) {

            final String formulaTitle = entry.getKey();
            final SuspiciousnessFactorFormula formula = entry.getValue();

            // 跳过已经计算出的结果
            if (Files.exists(Paths.get(resolveResultFilePath(name, formulaTitle, preLimitSfRate)))) {
                LogUtils.logInfo("skip " + name + "-" + formulaTitle + preLimitSfRate);
                progressBar.maxHint(progressBar.getMax() - imports.getRunResultForPrograms().size());
                continue;
            }

            LogUtils.logInfo("working on " + name + "-" + formulaTitle + preLimitSfRate);

            // 缓存中间结果
            final CacheHandler cache = new CacheHandler("testsuit-subset-" + name + "-" + formulaTitle + preLimitSfRate);
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
                .preLimitSfRate(sfRate)
                .build();
            final TestSuitSubsetJam result = resolver.resolve(imports);

            FileUtils.saveObject(resolveResultFilePath(name, formulaTitle, preLimitSfRate), result);

            cache.deleteAllCaches();
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

    /**
     * 从运行结果读取数据，必须已经存在
     *
     * @return
     */
    public static TestSuitSubsetJam getResultFromFile(
        String name,
        String formulaTitle,
        String preLimitSfRate) throws FileNotFoundException {

        // 从外部调用的时候，这里的 preLimitSfRate 不带横线
        return FileUtils.loadObject(resolveResultFilePath(name, formulaTitle, "-" + preLimitSfRate), TestSuitSubsetJam.class);
    }

    private static String resolveResultFilePath(String programName, String formulaTitle) {
        return resolveResultFilePath(programName, formulaTitle, "");
    }

    private static String resolveResultFilePath(String programName, String formulaTitle, String preLimitSfRate) {
        // 这里的 preLimitSfRate 是已经带了横线的，比如 "-0.5" 或者 ""，后者表示不限制数量。
        return resultOutputDir + "/" + programName + "-" + formulaTitle + preLimitSfRate + ".json";
    }
}
