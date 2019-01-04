package edu.nwpu.machunyan.theoreticalEvaluation.application;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorRecord;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModelGenerator;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModelRecord;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.RunResultFromRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.IntStream;

public class ResolveTotInfoSuspiciousnessFactor {

    // 输出文件
    private static final String outputFilePath = "./target/outputs/tot_info-suspiciousness-factors.json";

    public static void main(String[] args) throws IOException {
        final Map<String, ArrayList<RunResultFromRunner>> runResultsFromSavedFile = RunTotInfo.getRunResultsFromSavedFile();

        final JsonArray result = IntStream.range(1, runResultsFromSavedFile.size() + 1)
                .parallel()
                .mapToObj(i -> {
                    final String versionStr = "v" + i;
                    final JsonObject resultRecord = new JsonObject();
                    resultRecord.add("version", new JsonPrimitive(versionStr));

                    final ArrayList<RunResultFromRunner> runResults = runResultsFromSavedFile.get(versionStr);

                    final long passedCount = runResults.stream().filter(RunResultFromRunner::isCorrect).count();
                    final ArrayList<VectorTableModelRecord> vectorTableModel = VectorTableModelGenerator.generateFromRunResult(runResults);

                    // 某行代码的错误率（排序过的）
                    final ArrayList<SuspiciousnessFactorRecord> factorOfO = SuspiciousnessFactorResolver.getSuspiciousnessFactorMatrixOrdered(
                            vectorTableModel,
                            record -> record.calculateSuspiciousnessFactorAsO()
                    );
                    final ArrayList<SuspiciousnessFactorRecord> factorOfOp = SuspiciousnessFactorResolver.getSuspiciousnessFactorMatrixOrdered(
                            vectorTableModel,
                            record -> record.calculateSuspiciousnessFactorAsOp()
                    );

                    resultRecord.add("factors of O", new Gson().toJsonTree(factorOfO));
                    resultRecord.add("factors of Op", new Gson().toJsonTree(factorOfOp));
                    return resultRecord;
                })
                .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);

        FileUtils.printJsonToFile(Paths.get(outputFilePath), result);
    }
}
