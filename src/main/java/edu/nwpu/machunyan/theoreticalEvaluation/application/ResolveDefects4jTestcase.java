package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.ProgramDefination;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.CoverageRunnerException;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.Defects4jContainerExecutor;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.Defects4jTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CacheHandler;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 获取 defects4j 中的所有测试用例
 */
public class ResolveDefects4jTestcase {

    private static final Path OUTPUT_DIR = Paths.get("./target/outputs/defects4j-testcases");

    public static void main(String[] args) throws CoverageRunnerException, IOException {

        final boolean allDone = StreamEx
            .of(ProgramDefination.DEFECTS4J_RUN_LIST)
            .anyMatch(programName -> !Files.exists(resolveResultFilePath(programName)));
        if (allDone) {
            return;
        }

        @Cleanup final Defects4jContainerExecutor executor = Defects4jContainerExecutor.newInstance();
        Runtime.getRuntime().addShutdownHook(new Thread(executor::close));

        for (String programName : ProgramDefination.DEFECTS4J_RUN_LIST) {

            if (Files.exists(resolveResultFilePath(programName))) {
                continue;
            }
            LogUtils.logInfo("working on " + programName);

            @Cleanup ProgressBar progressBar = LogUtils.newProgressBarInstance("", 0);
            final CacheHandler cache = new CacheHandler("resolve-defects4j-testcases-" + programName);

            final Defects4jContainerExecutor.TestcaseResolvingProgressHandler progressHandler = new Defects4jContainerExecutor.TestcaseResolvingProgressHandler() {

                @Override
                public Optional<List<Defects4jTestcase>> tryGet(String version) {
                    final Optional<List<Defects4jTestcase>> res = cache.tryLoadCache(version, CacheItem.class)
                        .map(CacheItem::getTestcases);
                    if (res.isPresent()) {
                        progressBar.maxHint(progressBar.getMax() - 1);
                    }
                    return res;
                }

                @Override
                public void report(String version, List<Defects4jTestcase> testcases, int index) {
                    progressBar.step();
                    cache.saveCache(version, new CacheItem(testcases));
                }

                @Override
                public void reportVersionCount(int number) {
                    progressBar.maxHint(number);
                }
            };

            final Map<Program, List<Defects4jTestcase>> result = executor.resolveTestcases(programName, progressHandler);

            final List<TestcaseForProgram> list = EntryStream
                .of(result)
                .map(a -> new TestcaseForProgram(a.getKey(), a.getValue()))
                .toImmutableList();
            final TestcaseJam jam = new TestcaseJam(list);
            FileUtils.saveObject(resolveResultFilePath(programName), jam);

            cache.deleteAllCaches();
        }
    }

    /**
     * 从运行结果读取数据，必须已经存在
     *
     * @return
     */
    public static Map<Program, List<Defects4jTestcase>> getResultFromFile(
        String name) throws FileNotFoundException {

        final TestcaseJam testcaseJam = FileUtils.loadObject(resolveResultFilePath(name), TestcaseJam.class);
        return StreamEx
            .of(testcaseJam.getInner())
            .mapToEntry(TestcaseForProgram::getProgram, TestcaseForProgram::getTestcases)
            .toImmutableMap();
    }

    private static Path resolveResultFilePath(String programName) {
        return OUTPUT_DIR.resolve(programName + ".json");
    }

    @Value
    private static class TestcaseJam {
        List<TestcaseForProgram> inner;
    }

    @Value
    private static class TestcaseForProgram {
        Program program;
        List<Defects4jTestcase> testcases;
    }

    @Value
    private static class CacheItem {
        List<Defects4jTestcase> testcases;
    }
}
