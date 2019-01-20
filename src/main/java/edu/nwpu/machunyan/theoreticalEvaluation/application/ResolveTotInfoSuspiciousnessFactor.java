package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.*;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.MultipleFormulaSuspiciousnessFactorJam;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class ResolveTotInfoSuspiciousnessFactor {

    // 输出文件
    private static final String jsonOutputPath = "./target/outputs/tot_info-suspiciousness-factors.json";
    private static final String csvOutputPath = "./target/outputs/tot_info-suspiciousness-factors.csv";

    public static void main(String[] args) throws IOException {

        final RunResultJam jam = RunTotInfo.getRunResultsFromSavedFile();

        final List<SuspiciousnessFactorResolver> resolvers = SuspiciousnessFactorResolver.of(
            SuspiciousnessFactorFormulas.getAllFormulas()
        );

        final VectorTableModel.Pojo.VectorTableModelJam vtm = VectorTableModel.VectorTableModelResolver.resolve(jam);

        final SuspiciousnessFactorJam suspiciousnessFactorJam = SuspiciousnessFactorHelper.runOnAllResolvers(vtm, resolvers);
        final MultipleFormulaSuspiciousnessFactorJam result = SuspiciousnessFactorHelper.collectAsMultipleFormula(suspiciousnessFactorJam);

        FileUtils.saveObject(jsonOutputPath, suspiciousnessFactorJam);
        FileUtils.saveString(csvOutputPath, CsvExporter.toCsvString(result));
    }

    public static SuspiciousnessFactorJam getResultFromFile() throws FileNotFoundException {

        return FileUtils.loadObject(jsonOutputPath, SuspiciousnessFactorJam.class);
    }
}

