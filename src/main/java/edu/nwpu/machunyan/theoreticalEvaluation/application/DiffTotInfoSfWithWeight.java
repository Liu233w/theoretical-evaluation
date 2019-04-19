package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.DiffRankResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankJam;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 比较加权之前和之后的每条语句的区别
 */
public class DiffTotInfoSfWithWeight {

    // 输出文件
    private static final Path csvOutputDir = Paths.get("./target/outputs/totInfoSfWeightDiff");

    public static void main(String[] args) throws IOException {

        final Map<String, SuspiciousnessFactorJam> formulaToResult =
            ResolveTotInfoSuspiciousnessFactorWithWeight.resolveAndGetResult();
        final SuspiciousnessFactorJam unWeightedResult = ResolveSuspiciousnessFactor.getResultFromFile("tot_info");

        for (Map.Entry<String, SuspiciousnessFactorJam> entry : formulaToResult.entrySet()) {
            final String formula = entry.getKey();
            final SuspiciousnessFactorJam weightedResult = entry.getValue();

            final DiffRankJam diff = DiffRankResolver.resolve(
                unWeightedResult, weightedResult,
                "unweighted", "weighted");

            final Path savePath = csvOutputDir.resolve("weighted-by-" + formula + ".csv");
            FileUtils.saveString(savePath, CsvExporter.toCsvString(diff));
        }
    }
}
