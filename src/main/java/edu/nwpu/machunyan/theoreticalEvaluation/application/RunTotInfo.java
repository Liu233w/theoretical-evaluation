package edu.nwpu.machunyan.theoreticalEvaluation.application;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.IProgramInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.RunningResultResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.GccReadFromStdIoInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.GccReadFromStdIoRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import lombok.Value;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RunTotInfo {

    // 结果的输出位置
    private static final String resultOutputPath = "./target/outputs/totInfoRunningResult.json";

    public static RunResultJam runAndGetResult() throws URISyntaxException, IOException {

        // 最后一个版本编号
        final int lastVersionNumber = 23;
        // 存版本的文件夹
        final Path versionsDir = FileUtils.getFilePathFromResources("tot_info/versions");

        final List<IProgramInput> inputs = StreamEx
            .of(resolveTestcases())
            .map(a -> new GccReadFromStdIoInput(
                new String[]{},
                a.getInput(),
                a.getOutput()
            ))
            .map(a -> (IProgramInput) a)
            .toImmutableList();

        final List<Program> programs = IntStreamEx.range(1, lastVersionNumber + 1)
            .mapToObj(i -> "v" + i)
            .map(versionStr -> new Program(
                versionStr,
                versionsDir.resolve(versionStr).resolve("tot_info.c").toString()
            ))
            .toImmutableList();

        return RunningResultResolver.runProgramForAllVersions(programs, inputs, GccReadFromStdIoRunner::newInstance);
    }

    public static void runAndSaveResultsAsJson() throws IOException, URISyntaxException {
        final RunResultJam result = runAndGetResult();

        FileUtils.saveObject(resultOutputPath, result);
    }

    public static RunResultJam getRunResultsFromSavedFile() throws FileNotFoundException {
        return FileUtils.loadObject(resultOutputPath, RunResultJam.class);
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        runAndSaveResultsAsJson();
    }

    private static List<TestcaseItem> resolveTestcases() throws URISyntaxException, IOException {

        final Path casePath = FileUtils.getFilePathFromResources("tot_info/testplans/cases.json");
        final InputStreamReader jsonReader = new InputStreamReader(Files.newInputStream(casePath));
        final Type testcaseType = new TypeToken<List<TestcaseItem>>() {
        }.getType();

        return new Gson().fromJson(jsonReader, testcaseType);
    }

    @Value
    private static class TestcaseItem {
        private String input;
        private String output;
        private String name;
    }
}
