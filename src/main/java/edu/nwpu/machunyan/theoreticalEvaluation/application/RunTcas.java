package edu.nwpu.machunyan.theoreticalEvaluation.application;

import com.google.gson.*;
import edu.nwpu.machunyan.theoreticalEvaluation.interData.RunResultsJsonProcessor;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.CoverageRunnerException;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.GccReadFromStdIoInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.GccReadFromStdIoRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.RunningScheduler;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.IProgramInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.SingleRunResult;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import me.tongfei.progressbar.ProgressBar;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RunTcas {

    // 准备好的测试用例
    private final ArrayList<IProgramInput> testCases = buildTestCasesObject();
    // 程序文件夹
    private final Path versionsDir = FileUtils.getFilePathFromResources("tcas/versions");
    // 一共多少个版本
    private final int lastVersionNum = 1;
    // 结果的输出位置
    private static final String resultOutputPath = "./target/outputs/tcasRunningResult.json";

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        new RunTcas().runAllProgramsAndOutputResultsToFile();
    }

    /**
     * 从之前的输出中获取中间的运行结果
     *
     * @return key 为程序的 title（版本），value 为该程序的所有运行结果和覆盖值
     */
    public static HashMap<String, ArrayList<SingleRunResult>> getRunResultsFromSavedFile() throws FileNotFoundException {

        final File file = Paths.get(resultOutputPath).toFile();
        final JsonObject jsonObject = new JsonParser().parse(new FileReader(file)).getAsJsonObject();

        final HashMap<String, ArrayList<SingleRunResult>> result = new HashMap<>();

        for (Map.Entry<String, JsonElement> stringJsonElementEntry : jsonObject.entrySet()) {

            final JsonObject runResultsJsonObject = stringJsonElementEntry.getValue().getAsJsonObject();
            final ArrayList<SingleRunResult> runResults = RunResultsJsonProcessor.loadFromJson(runResultsJsonObject, GccReadFromStdIoInput.class);

            result.put(stringJsonElementEntry.getKey(), runResults);
        }

        return result;
    }

    private ProgressBar progressBar;

    private void runAllProgramsAndOutputResultsToFile() throws InterruptedException, IOException {

        // 初始化线程池
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        LogUtils.logInfo("ready to start, thread pool size: " + availableProcessors);
        final ExecutorService threadPool = Executors.newFixedThreadPool(availableProcessors);

        //初始化进度条
        progressBar = new ProgressBar("", testCases.size() * lastVersionNum);

        // 填充任务
        final JsonObject result = new JsonObject();
        for (int versionNum = 1; versionNum <= lastVersionNum; versionNum++) {

            // 准备当前版本的程序
            final String versionNumString = "v" + versionNum;
            final Path sourceFilePath = versionsDir.resolve(versionNumString).resolve("tcas.c");

            threadPool.submit(() -> {
                try {
                    final JsonObject resultRecord = runAndGetJsonObject(versionNumString, sourceFilePath);
                    synchronized (result) {
                        result.add(versionNumString, resultRecord);
                    }
                } catch (CoverageRunnerException e) {
                    e.printStackTrace();
                }
            });
        }

        // 等待所有任务完成
        threadPool.shutdown();
        threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        progressBar.close();

        // 输出结果
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File outputFile = Paths.get(resultOutputPath).toFile();
        outputFile.getParentFile().mkdirs();
        gson.toJson(result, new FileWriter(outputFile));
    }

    /**
     * 运行某个版本程序的所有测试用例，获得结果 jsonObject
     *
     * @param versionNumString
     * @param sourceFilePath
     * @return
     * @throws CoverageRunnerException
     */
    private JsonObject runAndGetJsonObject(String versionNumString, Path sourceFilePath) throws CoverageRunnerException {

        final RunningScheduler runningScheduler = new RunningScheduler(
                new Program(versionNumString, sourceFilePath.toString()),
                new GccReadFromStdIoRunner(),
                testCases,
                progressBar);

        // 使用测试用例运行程序
        LogUtils.logInfo("start running version: " + versionNumString);
        final ArrayList<SingleRunResult> runResults = runningScheduler.runAndGetResults();
        final long passedCount = runResults.stream().filter(SingleRunResult::isCorrect).count();

        LogUtils.logInfo("passed test case count for version " + versionNumString + ": " + passedCount);

        return RunResultsJsonProcessor.bumpToJson(runResults, GccReadFromStdIoInput.class);
    }

    private ArrayList<IProgramInput> buildTestCasesObject() throws URISyntaxException, IOException {

        final Path casePath = FileUtils.getFilePathFromResources("tcas/testplans/cases.json");
        final InputStreamReader jsonReader = new InputStreamReader(Files.newInputStream(casePath));


        final JsonArray list = new JsonParser().parse(jsonReader).getAsJsonArray();

        final ArrayList<IProgramInput> result = new ArrayList<>(list.size());
        for (JsonElement item :
                list) {
            final JsonObject testCase = item.getAsJsonObject();
            final String[] input = new Gson().fromJson(testCase.get("input"), String[].class);
            final String output = testCase.get("output").getAsString();

            result.add(new GccReadFromStdIoInput(input, output));
        }

        return result;
    }

    private RunTcas() throws IOException, URISyntaxException {
    }
}
