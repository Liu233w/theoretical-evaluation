package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.ProgramDefination;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.*;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.RunResultFromRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.*;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CacheHandler;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import lombok.Cleanup;
import lombok.Lombok;
import me.tongfei.progressbar.ProgressBar;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 批量执行测试程序，为每个程序单独返回结果
 * <p>
 * 应该在 linux 下被执行。因为有一些输入程序的参数可能是 *，在 windows 下会被当成路径通配符
 */
public class Run {

    private static final String resultDir = "./target/outputs/run-results";

    public static void main(String[] args) {

        StreamEx
            .of(ProgramDefination.GCC_RUN_LIST)
            // 跳过已经计算出的结果
            .filter(a -> !Files.exists(Paths.get(resolveResultFilePath(a.getProgramDir()))))
            .peek(a -> LogUtils.logInfo("Running program: " + a))
            .mapToEntry(ProgramDefination.ProgramDir::getProgramDir, Run::runAndGetResult)
            .filterValues(Optional::isPresent)
            .mapValues(Optional::get)
            .forKeyValue((name, result) -> {

                final String filePath = resolveResultFilePath(name);
                try {
                    FileUtils.saveObject(filePath, result);
                } catch (IOException e) {
                    LogUtils.logError(e);
                }
            });

        /*
        defects4j 在运行时有时会出现随机的错误，必须重启容器才能解决，所以在外面多加一个重试代码。
        对 runDefects4jAndGetResult 的调用会启动容器，并在调用结束时关闭容器。
         */
        final int defects4jRetry = 3;

        for (String programName : ProgramDefination.DEFECTS4J_RUN_LIST) {
            if (Files.exists(Paths.get(resolveResultFilePath(programName)))) {
                continue;
            }

            RunResultJam result = null;
            for (int i = 0; i <= defects4jRetry; i++) {
                if (result == null) {
                    LogUtils.logInfo("Working on " + programName);
                    result = runDefects4jAndGetResult(programName).orElse(null);
                } else {
                    try {
                        FileUtils.saveObject(resolveResultFilePath(programName), result);
                        break;
                    } catch (IOException e) {
                        LogUtils.logError(e);
                    }
                }
            }
        }
    }

    public static RunResultJam getResultFromFile(String programName) throws FileNotFoundException {
        final String path = resolveResultFilePath(programName);
        return FileUtils.loadObject(path, RunResultJam.class);
    }

    private static String resolveResultFilePath(String programName) {
        return resultDir + "/" + programName + ".json";
    }

    public static Optional<RunResultJam> runAndGetResult(ProgramDefination.ProgramDir defination) {

        // 存版本的文件夹
        try {
            final Path versionsDir = FileUtils.getFilePathFromResources(
                defination.getProgramDir() + "/versions");

            final List<IProgramInput> inputs = StreamEx
                .of(TestcaseResolver.resolve(defination.getProgramDir()))
                .map(a -> new GccReadFromStdIoInput(
                    a.getParams(),
                    a.getInput(),
                    a.getOutput()
                ))
                .map(a -> (IProgramInput) a)
                .toImmutableList();

            final List<String> subDirName = resolveSubDirName(versionsDir);
            final List<Program> programs = StreamEx
                .of(subDirName)
                .map(versionStr -> new Program(
                    versionStr,
                    versionsDir.resolve(versionStr).resolve(defination.getProgramName()).toString()
                ))
                .toImmutableList();

            final RunResultJam runResultJam = RunningResultResolver.runProgramForAllVersions(
                programs, inputs, GccReadFromStdIoRunner::new);

            return Optional.of(runResultJam);

        } catch (URISyntaxException | IOException e) {
            LogUtils.logError(e);
            return Optional.empty();
        }
    }

    public static Optional<RunResultJam> runDefects4jAndGetResult(String programName) {

        /*
        控制流：一经异常立刻退出，不返回任何结果。
        使用 cache 来缓存计算出的结果，所以立刻退出的代价不会很大。
         */

        final int retry = 3;

        try {

            @Cleanup final Defects4jContainerExecutor executor
                = Defects4jContainerExecutor.newInstance();
            Runtime.getRuntime().addShutdownHook(new Thread(executor::close));

            /*
            当大部分的结果都在 cache 中时， parallelStream 可能会以为所有的操作都是短期的操作，导致只使用单个的线程来运行
            剩下的结果，没法有效利用并发。
             */

            final Map<Program, List<Defects4jTestcase>> versionToTestcase = ResolveDefects4jTestcase.getResultFromFile(programName);

            final CacheHandler cache = new CacheHandler("run-defects4j-" + programName);

            final Map<Program, List<Defects4jTestcase>> unResolved = EntryStream.of(versionToTestcase)
                .parallel()
                .filterKeys(program -> !cache.isKeyCached(program.getTitle()))
                .toMap();

            LogUtils.logFine("Remain version count: " + unResolved.size());

            final int totalCount = StreamEx.of(unResolved.values()).mapToInt(List::size).sum();
            @Cleanup final ProgressBar progressBar = LogUtils.newProgressBarInstance("", totalCount);

            EntryStream
                .of(unResolved)
                .parallel()
                .forKeyValue((program, testcases) -> {

                    final List<IProgramInput> inputs = StreamEx.of(testcases)
                        .map(a -> (IProgramInput) a)
                        .toList();

                    try {
                        final ArrayList<RunResultFromRunner> results = RunningScheduler
                            .builder()
                            .progressBar(progressBar)
                            .cache(new CacheHandler("run-defects4j-" + programName + "-" + program.getTitle()))
                            .retry(retry)
                            .build()
                            .runAndGetResults(
                                () -> new Defects4jRunner(executor, programName),
                                program,
                                inputs);

                        final RunResultForProgram runResultForProgram = RunningResultResolver.mapFromRunResult(results);

                        cache.saveCache(program.getTitle(), runResultForProgram);

                    } catch (CoverageRunnerException e) {
                        // 被外层捕获的异常
                        throw Lombok.sneakyThrow(e);
                    }
                });

            final List<RunResultForProgram> result = StreamEx.of(versionToTestcase.keySet())
                .map(program -> cache.tryLoadCache(program.getTitle(), RunResultForProgram.class).get())
                .toImmutableList();

            cache.deleteAllCaches();

            return Optional.of(new RunResultJam(result));

        } catch (Throwable e) {
            LogUtils.logError(e);
            return Optional.empty();
        }
    }

    /**
     * 返回文件夹里的所有子文件夹名称
     *
     * @param versionsDir
     * @return
     * @throws IOException
     */
    private static List<String> resolveSubDirName(Path versionsDir) throws IOException {
        return StreamEx.of(Files.list(versionsDir))
            .filter(item -> item.toFile().isDirectory())
            .map(item -> item.getFileName().toString())
            .toImmutableList();
    }

}
