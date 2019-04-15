package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestcaseWeightHelper;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestcaseWeightResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
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
import java.util.List;
import java.util.Optional;

public class ResolveTestcaseWeight {

    private static final String resultDir = "./target/outputs/testcase-weights";

    private static final String[] MAIN_LIST = new String[]{
        "my_sort",
        "schedule2",
        "tcas",
        "tot_info",
        "replace",
        "print_tokens",
    };

    private static final boolean USE_PARALLEL = !"1".equals(System.getenv("DISABLE_PARALLEL"));

    public static void main(String[] args) throws IOException {

        LogUtils.logInfo("USE_PARALLEL=" + USE_PARALLEL);

        for (String name : MAIN_LIST) {

            // 跳过已经计算出的结果
            if (Files.exists(Paths.get(resolveResultFilePath(name)))) {
                continue;
            }

            LogUtils.logInfo("Running on " + name);

            final CacheHandler cache = new CacheHandler("testcase-weights-" + name);

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
                    LogUtils.logFine("loaded item from cache: " + key);
                }
                return result;
            };

            final List<TestcaseWeightResolver> resolvers = TestcaseWeightResolver.of(
                SuspiciousnessFactorFormulas.getAllFormulas(),
                TestcaseWeightResolver
                    .builder()
                    .provider(cacheLoader)
                    .reporter(cacheSaver)
                    .useParallel(USE_PARALLEL)
            );

            final RunResultJam imports = Run.getResultFromFile(name);
            progressBar.maxHint(resolvers.size() * imports.getRunResultForPrograms().size());

            final TestcaseWeightJam result = TestcaseWeightHelper
                .runOnAllResolvers(imports, resolvers, USE_PARALLEL);

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
