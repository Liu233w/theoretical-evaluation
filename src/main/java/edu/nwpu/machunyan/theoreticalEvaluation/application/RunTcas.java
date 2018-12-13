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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class RunTcas {

    public static void main(String[] args) throws URISyntaxException, IOException, CoverageRunnerException {

        final ArrayList<IProgramInput> testCases = buildTestCases();

        // 程序文件夹
        final Path versionsDir = FileUtils.getFilePathFromResources("tcas/versions");
        // 一共多少个版本
        final int lastVersionNum = 41;

        // 输出结果
        final JsonArray output = new JsonArray();

        for (int versionNum = 1; versionNum <= lastVersionNum; versionNum++) {

            // 准备当前版本的程序
            final String versionNumString = "v" + versionNum;
            final Path sourceFilePath = versionsDir.resolve(versionNumString).resolve("tcas.c");
            final RunningScheduler runningScheduler = new RunningScheduler(
                    new Program(versionNumString, sourceFilePath.toString()),
                    new GccReadFromStdIoRunner(),
                    testCases);

            // 使用测试用例运行程序
            LogUtils.logInfo("running version: " + versionNumString);
            final ArrayList<SingleRunResult> runResults = runningScheduler.runAndGetResults();
            final long passedCount = runResults.stream().filter(SingleRunResult::isCorrect).count();

            LogUtils.logInfo("passed test case count: " + passedCount);

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

            // 输出结果
            final JsonObject resultRecord = new JsonObject();
            resultRecord.add("version", new JsonPrimitive(versionNumString));

            resultRecord.add("factors of O", new Gson().toJsonTree(factorOfO));
            resultRecord.add("factors of Op", new Gson().toJsonTree(factorOfOp));

            output.add(resultRecord);
        }

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final byte[] outputBytes = gson.toJson(output).getBytes();
        Files.write(Paths.get("suspiciousness-factors.json"), outputBytes);
    }

    private static ArrayList<IProgramInput> buildTestCases() throws URISyntaxException, IOException {

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
