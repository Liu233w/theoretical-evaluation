package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModelRecord;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModelResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorJam;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModelJam;
import edu.nwpu.machunyan.theoreticalEvaluation.interData.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.ProgramRunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class ResolveTotInfoSuspiciousnessFactor {

    // 输出文件
    private static final String outputFilePath = "./target/outputs/tot_info-suspiciousness-factors.csv";

    public static void main(String[] args) throws IOException {

        final ProgramRunResultJam jam = RunTotInfo.getRunResultsFromSavedFile();

        final List<SuspiciousnessFactorResolver> resolvers = SuspiciousnessFactorResolver.of(new HashMap<String, Function<VectorTableModelRecord, Double>>() {{
            put("O", SuspiciousnessFactorFormulas::o);
            put("Op", SuspiciousnessFactorFormulas::op);
        }});

        final VectorTableModelJam vtm = VectorTableModelResolver.fromProgramResultJam(jam);

        final SuspiciousnessFactorJam suspiciousnessFactorJam = SuspiciousnessFactorResolver.runAll(vtm, resolvers);

        FileUtils.saveString(outputFilePath, CsvExporter.toCsvString(suspiciousnessFactorJam));
    }
}

