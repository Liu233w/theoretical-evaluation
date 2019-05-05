package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormula;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ResolveTestcaseWeight {

    private static final String resultDir = "./target/outputs/testcase-weights";

    public static void main(String[] args) throws IOException {

        for (String name : ProgramDefination.PROGRAM_LIST) {

            LogUtils.logInfo("Running on " + name);

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
                final CacheHandler cache = new CacheHandler("testcase-weights-" + name + "-" + formulaTitle);
                final TestcaseWeightResolver.Reporter reporter = item -> {
                    final String key = item.getTitle();
                    LogUtils.logFine("saving cache: " + key);
                    cache.saveCache(key, item);
                    progressBar.step();
                };
                final TestcaseWeightResolver.Provider provider = (resolver, input) -> {
                    final String key = input.getProgramTitle();
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
                    .sfFormula(formula)
                    .formulaTitle(formulaTitle)
                    .provider(provider)
                    .reporter(reporter)
                    .useParallel(true)
                    .build();
                final TestcaseWeightJam result = resolver.resolve(imports);

                FileUtils.saveObject(resolveResultFilePath(name, formulaTitle), result);

                cache.deleteAllCaches();
            }
        }
    }

    public static TestcaseWeightJam getResultFromFile(
        String programName,
        String formulaName) throws FileNotFoundException {

        final String path = resolveResultFilePath(programName, formulaName);
        return FileUtils.loadObject(path, TestcaseWeightJam.class);
    }

    public static TestcaseWeightJam getResultFromFile(
        String programName) throws IOException {

        final Path[] fileList = getFileListOf(programName);

        // 合并多个文件
        final ArrayList<TestcaseWeightForProgram> result = new ArrayList<>();
        for (Path path : fileList) {
            LogUtils.logFine("merge results from " + path);
            final TestcaseWeightJam jam = FileUtils.loadObject(path, TestcaseWeightJam.class);
            result.addAll(jam.getTestcaseWeightForPrograms());
        }

        return new TestcaseWeightJam(result);
    }

    private static String resolveResultFilePath(
        String programName,
        String formulaName) {

        return resultDir + "/" + programName + "-" + formulaName + ".json";
    }

    private static Path[] getFileListOf(String programName) throws IOException {
        return Files.list(Paths.get(resultDir))
            .filter(a -> a.getFileName().startsWith(programName))
            .toArray(Path[]::new);
    }
}
