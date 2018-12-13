package edu.nwpu.machunyan.theoreticalEvaluation.application;

import com.google.gson.*;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorGenerator;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorRecord;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModelGenerator;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModelRecord;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.CoverageRunnerException;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.GccReadFromStdIoInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.GccReadFromStdIoRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.RunningScheduler;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.IProgramInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.SingleRunResult;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.*;

public class RunTcas {

    // 准备好的测试用例
    private static ArrayList<IProgramInput> testCases;
    // 程序文件夹
    private static Path versionsDir;
    // 一共多少个版本
    private static final int lastVersionNum = 41;

    static {
        try {
            testCases = buildTestCasesObject();
            versionsDir = FileUtils.getFilePathFromResources("tcas/versions");
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        LogUtils.logInfo("ready to start, thread pool size: " + availableProcessors);
        final ExecutorService threadPool = Executors.newFixedThreadPool(availableProcessors);

        final JsonArray output = new JsonArray();

        for (int versionNum = 1; versionNum <= lastVersionNum; versionNum++) {

            // 准备当前版本的程序
            final String versionNumString = "v" + versionNum;
            final Path sourceFilePath = versionsDir.resolve(versionNumString).resolve("tcas.c");

            // 多线程运行程序
            final JsonObject resultRecord = new JsonObject();
            output.add(resultRecord);

            threadPool.submit(() -> {
                try {
                    runAndFillJsonObject(versionNumString, sourceFilePath, resultRecord);
                } catch (CoverageRunnerException e) {
                    e.printStackTrace();
                }
            });
        }

        threadPool.shutdown();
        threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final byte[] outputBytes = gson.toJson(output).getBytes();
        Files.write(Paths.get("suspiciousness-factors.json"), outputBytes);
    }

    /**
     * 运行某个版本程序的所有测试用例，用结果填充 json string
     *
     * @param versionNumString
     * @param sourceFilePath
     * @param resultRecord
     * @throws CoverageRunnerException
     */
    private static void runAndFillJsonObject(String versionNumString, Path sourceFilePath, JsonObject resultRecord) throws CoverageRunnerException {

        final RunningScheduler runningScheduler = new RunningScheduler(
                new Program(versionNumString, sourceFilePath.toString()),
                new GccReadFromStdIoRunner(),
                testCases);

        // 使用测试用例运行程序
        LogUtils.logInfo("start running version: " + versionNumString);
        final ArrayList<SingleRunResult> runResults = runningScheduler.runAndGetResults();
        final long passedCount = runResults.stream().filter(SingleRunResult::isCorrect).count();

        LogUtils.logInfo("passed test case count for version " + versionNumString + ": " + passedCount);

        final ArrayList<VectorTableModelRecord> vectorTableModel = VectorTableModelGenerator.generateVectorTableModelFromRunResult(runResults);

        // 某行代码的错误率（排序过的）
        final ArrayList<SuspiciousnessFactorRecord> factorOfO = SuspiciousnessFactorGenerator.getSuspiciousnessFactorMatrixOrdered(
                vectorTableModel,
                record -> (double) record.calculateSuspiciousnessFactorAsO()
        );
        final ArrayList<SuspiciousnessFactorRecord> factorOfOp = SuspiciousnessFactorGenerator.getSuspiciousnessFactorMatrixOrdered(
                vectorTableModel,
                record -> record.calculateSuspiciousnessFactorAsOp(testCases.size(), (int) passedCount)
        );

        //输出结果
        resultRecord.add("version", new JsonPrimitive(versionNumString));
        resultRecord.add("factors of O", new Gson().toJsonTree(factorOfO));
        resultRecord.add("factors of Op", new Gson().toJsonTree(factorOfOp));
    }

    private static ArrayList<IProgramInput> buildTestCasesObject() throws URISyntaxException, IOException {

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
}
