package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestcaseWeightResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
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
import java.util.Optional;

public class ResolveTestcaseWeightByOp {

    private static final String resultDir = "./target/outputs/testcase-weights-op";

    private static final boolean USE_PARALLEL = !"1".equals(System.getenv("DISABLE_PARALLEL"));

    public static void main(String[] args) throws IOException {

        LogUtils.logInfo("USE_PARALLEL=" + USE_PARALLEL);

        for (String name : ProgramDefination.PROGRAM_LIST) {

            // 跳过已经计算出的结果
            if (Files.exists(Paths.get(resolveResultFilePath(name)))) {
                continue;
            }

            LogUtils.logInfo("Running on " + name);

            final CacheHandler cache = new CacheHandler("testcase-weights-op-" + name);

            @Cleanup final ProgressBar progressBar = new ProgressBar("", 0);

            final TestcaseWeightResolver.Reporter cacheSaver = item -> {
                final String key = item.getFormulaTitle() + "-" + item.getTitle();
                LogUtils.logFine("saving cache: " + key);
                cache.saveCache(key, item);
                progressBar.step();
            };
            final TestcaseWeightResolver.Provider cacheLoader = (resolver, input) -> {
                final String key = resolver.getFormulaTitle() + "-" + input.getProgramTitle();
                final Optional<TestcaseWeightForProgram> result = cache.tryLoadCache(key, TestcaseWeightForProgram.class);
                if (result.isPresent()) {

                    // 避免缓存影响对剩余时间的计算
                    progressBar.maxHint(progressBar.getMax() - 1);
                    progressBar.stepBy(-1);

                    LogUtils.logFine("loaded item from cache: " + key);
                }
                return result;
            };

            final TestcaseWeightResolver resolver = TestcaseWeightResolver
                .builder()
                .provider(cacheLoader)
                .reporter(cacheSaver)
                .useParallel(USE_PARALLEL)
                .sfFormula(SuspiciousnessFactorFormulas::op)
                .formulaTitle("op")
                .build();
            final RunResultJam imports = Run.getResultFromFile(name);
            progressBar.maxHint(imports.getRunResultForPrograms().size());

            final TestcaseWeightJam result = resolver.resolve(imports);

            FileUtils.saveObject(resolveResultFilePath(name), result);

            cache.deleteAllCaches();
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
