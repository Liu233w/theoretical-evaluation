package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorBatchRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModelResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.MultipleFormulaSuspiciousnessFactorJam;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorJam;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModelJam;
import edu.nwpu.machunyan.theoreticalEvaluation.interData.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.ProgramRunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.IOException;
import java.util.List;

public class ResolveTotInfoSuspiciousnessFactor {

    // 输出文件
    private static final String jsonOutputPath = "./target/outputs/tot_info-suspiciousness-factors.json";
    private static final String csvOutputPath = "./target/outputs/tot_info-suspiciousness-factors.csv";

    public static void main(String[] args) throws IOException {

        final ProgramRunResultJam jam = RunTotInfo.getRunResultsFromSavedFile();

        final List<SuspiciousnessFactorResolver> resolvers = SuspiciousnessFactorResolver.of(
            SuspiciousnessFactorFormulas.getAllFormulas()
        );

        final VectorTableModelJam vtm = VectorTableModelResolver.resolve(jam);

        final SuspiciousnessFactorJam suspiciousnessFactorJam = SuspiciousnessFactorBatchRunner.runAll(vtm, resolvers);
        final MultipleFormulaSuspiciousnessFactorJam result = SuspiciousnessFactorBatchRunner.collectAsMultipleFormula(suspiciousnessFactorJam);

        FileUtils.saveObject(jsonOutputPath, result);
        FileUtils.saveString(csvOutputPath, CsvExporter.toCsvString(suspiciousnessFactorJam));
    }
}

