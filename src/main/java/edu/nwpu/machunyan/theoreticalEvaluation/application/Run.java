package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.IProgramInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.RunningResultResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.TestcaseResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.GccReadFromStdIoInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.GccReadFromStdIoRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import lombok.Value;
import one.util.streamex.StreamEx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * 批量执行测试程序，为每个程序单独返回结果
 */
public class Run {

    /**
     * 要执行的所有程序
     */
    private static final ProgramDefination[] MAIN_LIST = new ProgramDefination[]{
        new ProgramDefination("tcas", "tcas.c"),
        new ProgramDefination("schedule2", "schedule2.c"),
        new ProgramDefination("my_sort", "my_sort.c"),
        new ProgramDefination("tot_info", "tot_info.c"),
    };

    private static final String resultDir = "./target/outputs/run-results";

    public static void main(String[] args) {

        StreamEx
            .of(MAIN_LIST)
            // 跳过已经计算出的结果
            .filter(a -> !Files.exists(Paths.get(resolveResultFilePath(a.programDir))))
            .peek(a -> LogUtils.logInfo("Running program: " + a))
            .mapToEntry(ProgramDefination::getProgramDir, Run::runAndGetResult)
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

    public static Optional<RunResultJam> runAndGetResult(ProgramDefination defination) {

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

    @Value
    private static class ProgramDefination {
        /**
         * resources 文件夹中存放程序文件的文件夹名
         */
        String programDir;
        /**
         * 程序的源代码名称
         */
        String programName;
    }

}
