package edu.nwpu.machunyan.theoreticalEvaluation.application;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorRecord;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModelGenerator;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModelRecord;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.SingleRunResult;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 计算 tcas 的 suspiciousness factor，请保证上一步输出的结果文件存在
 */
public class ResolveTcasSuspiciousnessFactor {

    // 结果的输出路径
    private static final String resultOutputPath = "./target/outputs/suspiciousness-factors.json";

    public static void main(String[] args) throws IOException {

        final HashMap<String, ArrayList<SingleRunResult>> runResultsForVersion = RunTcas.getRunResultsFromSavedFile();

        final int versionCount = runResultsForVersion.size();

        final JsonArray result = new JsonArray(versionCount);
        for (int i = 1; i <= versionCount; i++) {

            final String versionNumStr = "v" + i;
            final ArrayList<SingleRunResult> runResults = runResultsForVersion.get(versionNumStr);

            final long passedCount = runResults.stream().filter(SingleRunResult::isCorrect).count();
            final ArrayList<VectorTableModelRecord> vectorTableModel = VectorTableModelGenerator.generateFromRunResult(runResults);

            // 某行代码的错误率（排序过的）
            final ArrayList<SuspiciousnessFactorRecord> factorOfO = SuspiciousnessFactorResolver.getSuspiciousnessFactorMatrixOrdered(
                    vectorTableModel,
                    record -> (double) record.calculateSuspiciousnessFactorAsO()
            );
            final ArrayList<SuspiciousnessFactorRecord> factorOfOp = SuspiciousnessFactorResolver.getSuspiciousnessFactorMatrixOrdered(
                    vectorTableModel,
                    record -> record.calculateSuspiciousnessFactorAsOp()
            );

            // 记录结果
            final JsonObject resultRecord = new JsonObject();
            resultRecord.add("version", new JsonPrimitive(versionNumStr));
            resultRecord.add("factors of O", new Gson().toJsonTree(factorOfO));
            resultRecord.add("factors of Op", new Gson().toJsonTree(factorOfOp));

            result.add(resultRecord);
        }

        FileUtils.printJsonToFile(Paths.get(resultOutputPath), result);
    }

}
