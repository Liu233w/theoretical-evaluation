package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestcaseWeightHelper;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestcaseWeightResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CacheHandler;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import lombok.Cleanup;
import me.tongfei.progressbar.ProgressBar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class ResolveTestcaseWeight {

    private static final String resultDir = "./target/outputs/testcase-weights";

    private static final String[] MAIN_LIST = new String[]{
        "my_sort",
        "schedule2",
        "tcas",
        "tot_info",
    };

    public static void main(String[] args) throws IOException {

        for (String name : MAIN_LIST) {

            // 跳过已经计算出的结果
            try {
                getResultFromFile(name);
                continue;
            } catch (FileNotFoundException ignored) {
            }

            System.out.println("Running on " + name);

            final CacheHandler cache = new CacheHandler("testcase-weights-" + name);

            @Cleanup final ProgressBar progressBar = new ProgressBar("", 0);

            final TestcaseWeightResolver.Reporter cacheSaver = item -> {
                final String key = item.getFormulaTitle() + "-" + item.getTitle();
                cache.saveCache(key, item);
                progressBar.step();
            };
            final TestcaseWeightResolver.Provider cacheLoader = (resolver, input) -> {
                final String key = resolver.getFormulaTitle() + "-" + input.getProgramTitle();
                return cache.tryLoadCache(key, TestcaseWeightForProgram.class);
            };

            final List<TestcaseWeightResolver> resolvers = TestcaseWeightResolver.of(
                SuspiciousnessFactorFormulas.getAllFormulas(),
                TestcaseWeightResolver
                    .builder()
                    .provider(cacheLoader)
                    .reporter(cacheSaver)
            );

            final RunResultJam imports = Run.getResultFromFile(name);
            progressBar.maxHint(resolvers.size() * imports.getRunResultForPrograms().size());

            final TestcaseWeightJam result = TestcaseWeightHelper
                .runOnAllResolvers(imports, resolvers);

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
