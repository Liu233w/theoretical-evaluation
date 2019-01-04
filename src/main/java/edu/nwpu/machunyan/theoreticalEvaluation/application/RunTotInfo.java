package edu.nwpu.machunyan.theoreticalEvaluation.application;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import edu.nwpu.machunyan.theoreticalEvaluation.interData.RunResultsJsonProcessor;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.CoverageRunnerException;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.GccReadFromStdIoInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.GccReadFromStdIoRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.RunningScheduler;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.IProgramInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.RunResultFromRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.StreamUtils;
import me.tongfei.progressbar.ProgressBar;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RunTotInfo {

    private static Path versionsDir;

    private static final int lastVersionNumber = 23;
    // 结果的输出位置
    private static final String resultOutputPath = "./target/outputs/totInfoRunningResult.json";

    static {
        try {
            versionsDir = FileUtils.getFilePathFromResources("tot_info/versions");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static List<ArrayList<RunResultFromRunner>> runAndGetResult() throws URISyntaxException, IOException {

        final Path casePath = FileUtils.getFilePathFromResources("tot_info/testplans/cases.json");
        final InputStreamReader jsonReader = new InputStreamReader(Files.newInputStream(casePath));

        final JsonArray list = new JsonParser().parse(jsonReader).getAsJsonArray();
        final List<IProgramInput> testCases = StreamUtils.asStream(list.iterator())
                .map(JsonElement::getAsJsonObject)
                .map(a -> new GccReadFromStdIoInput(
                        new String[]{},
                        a.getAsJsonPrimitive("input").getAsString(),
                        a.getAsJsonPrimitive("output").getAsString()
                ))
                .map(a -> (IProgramInput) a)
                .collect(Collectors.toList());

        final ProgressBar progressBar = new ProgressBar("", testCases.size() * lastVersionNumber);

        final List<ArrayList<RunResultFromRunner>> result = IntStream.range(1, lastVersionNumber + 1)
                .parallel()
                .mapToObj(i -> "v" + i)
                .map(versionStr -> new Program(versionStr, versionsDir.resolve(versionStr).resolve("tot_info.c").toString()))
                .map(program -> new RunningScheduler(program, new GccReadFromStdIoRunner(), testCases, progressBar))
                .map(runningScheduler -> {
                    try {
                        return runningScheduler.runAndGetResults();
                    } catch (CoverageRunnerException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toList());

        progressBar.close();

        return result;
    }

    public static void runAndSaveResultsAsJson() throws IOException, URISyntaxException {
        final JsonArray jsonArray = runAndGetResult().stream()
                .map(results -> RunResultsJsonProcessor.bumpToJson(results, GccReadFromStdIoInput.class))
                .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);

        FileUtils.printJsonToFile(Paths.get(resultOutputPath), jsonArray);
    }

    public static Map<String, ArrayList<RunResultFromRunner>> getRunResultsFromSavedFile() throws FileNotFoundException {
        final JsonArray jsonArray = FileUtils.getJsonFromFile(resultOutputPath).getAsJsonArray();

        return StreamUtils.asStream(jsonArray.iterator())
                .map(JsonElement::getAsJsonObject)
                .map(a -> RunResultsJsonProcessor.loadFromJson(a, GccReadFromStdIoInput.class))
                .collect(Collectors.toMap(a -> a.get(0).getProgram().getTitle(), a -> a));
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        runAndSaveResultsAsJson();
    }
}
