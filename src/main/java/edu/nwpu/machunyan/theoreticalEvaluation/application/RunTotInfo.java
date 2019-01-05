package edu.nwpu.machunyan.theoreticalEvaluation.application;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.GccReadFromStdIoInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.GccReadFromStdIoRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.RunningResultResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.ProgramRunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.IProgramInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import lombok.Data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RunTotInfo {

    // 结果的输出位置
    private static final String resultOutputPath = "./target/outputs/totInfoRunningResult.json";

    public static ProgramRunResultJam runAndGetResult() throws URISyntaxException, IOException {

        // 最后一个版本编号
        final int lastVersionNumber = 23;
        // 存版本的文件夹
        final Path versionsDir = FileUtils.getFilePathFromResources("tot_info/versions");

        final List<IProgramInput> inputs = resolveTestCases().stream()
            .map(a -> new GccReadFromStdIoInput(
                new String[]{},
                a.getInput(),
                a.getOutput()
            ))
            .map(a -> (IProgramInput) a)
            .collect(Collectors.toList());

        final List<Program> programs = IntStream.range(1, lastVersionNumber + 1)
            .mapToObj(i -> "v" + i)
            .map(versionStr -> new Program(
                versionStr,
                versionsDir.resolve(versionStr).resolve("tot_info.c").toString()))
            .collect(Collectors.toList());

        return RunningResultResolver.runProgramForAllVersions(programs, inputs, GccReadFromStdIoRunner::newInstance);
    }

    public static void runAndSaveResultsAsJson() throws IOException, URISyntaxException {
        final ProgramRunResultJam result = runAndGetResult();

        FileUtils.saveObject(resultOutputPath, result);
    }

    public static ProgramRunResultJam getRunResultsFromSavedFile() throws FileNotFoundException {
        return FileUtils.loadObject(resultOutputPath, ProgramRunResultJam.class);
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        runAndSaveResultsAsJson();
    }

    private static List<TestCaseItem> resolveTestCases() throws URISyntaxException, IOException {

        final Path casePath = FileUtils.getFilePathFromResources("tot_info/testplans/cases.json");
        final InputStreamReader jsonReader = new InputStreamReader(Files.newInputStream(casePath));
        final Type testcaseType = new TypeToken<List<TestCaseItem>>() {
        }.getType();

        return new Gson().fromJson(jsonReader, testcaseType);
    }
}

@Data
class TestCaseItem {
    private String input;
    private String output;
    private String name;
}
