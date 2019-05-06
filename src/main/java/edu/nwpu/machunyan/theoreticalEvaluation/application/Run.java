package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.ProgramDefination;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.*;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.*;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import lombok.Cleanup;
import me.tongfei.progressbar.ProgressBar;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
                    e.printStackTrace();
                }
            });
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
                programs, inputs, GccReadFromStdIoRunner::newInstance);

            return Optional.of(runResultJam);

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static RunResultJam runDefects4jAndGetResult(String programName)
        throws FileNotFoundException, CoverageRunnerException {

        final Defects4jContainerExecutor executor = Defects4jContainerExecutor.getInstance();

        final Map<Program, List<Defects4jTestcase>> versionToTestcase = ResolveDefects4jTestcase.getResultFromFile(programName);

        final int totalCount = StreamEx
            .of(versionToTestcase.values())
            .mapToInt(List::size)
            .sum();
        @Cleanup final ProgressBar progressBar = new ProgressBar("", totalCount);

        final List<RunResultForProgram> result = EntryStream
            .of(versionToTestcase)
            .parallel()
            .mapValues(StreamEx::of)
            .mapValues(s -> s.map(a -> (IProgramInput) a))
            .mapValues(StreamEx::toList)
            .map(entry -> new RunningScheduler(
                entry.getKey(),
                () -> new Defects4jRunner(executor, programName),
                entry.getValue(),
                progressBar))
            .map(scheduler -> {
                try {
                    return scheduler.runAndGetResults();
                } catch (CoverageRunnerException e) {
                    LogUtils.logError("CoverageRunnerException for " + scheduler.getProgram().getPath());
                    LogUtils.logError(e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .map(RunningResultResolver::mapFromRunResult)
            .toImmutableList();

        return new RunResultJam(result);
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
