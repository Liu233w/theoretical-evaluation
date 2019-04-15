package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModelResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.MultipleFormulaSuspiciousnessFactorJam;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorJam;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModelJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * 获取
 */
public class ResolveSuspiciousnessFactor {

    // 输出文件
    private static final String jsonOutputDir = "./target/outputs/suspiciousness-factors-json";
    private static final String csvOutputDir = "./target/outputs/suspiciousness-factors-csv";

    private static final String[] MAIN_LIST = new String[]{
        "my_sort",
        "schedule2",
        "tcas",
        "tot_info",
        "replace",
        "print_tokens",
    };

    public static void main(String[] args) throws IOException {

        for (String name : MAIN_LIST) {

            // 跳过已经计算出的结果
            if (Files.exists(Paths.get(resolveResultFilePath(name)))) {
                continue;
            }

            final SuspiciousnessFactorJam json = runAndGetResult(name);
            final MultipleFormulaSuspiciousnessFactorJam csv = SuspiciousnessFactorUtils.collectAsMultipleFormula(json);

            FileUtils.saveObject(resolveResultFilePath(name), json);
            FileUtils.saveString(resolveCsvFilePath(name), CsvExporter.toCsvString(csv));
        }
    }

    public static SuspiciousnessFactorJam getResultFromFile(String programName) throws FileNotFoundException {
        final String path = resolveResultFilePath(programName);
        return FileUtils.loadObject(path, SuspiciousnessFactorJam.class);
    }

    public static SuspiciousnessFactorJam runAndGetResult(String programName) throws FileNotFoundException {

        final RunResultJam jam = Run.getResultFromFile(programName);

        final List<SuspiciousnessFactorResolver> resolvers = SuspiciousnessFactorResolver.of(
            SuspiciousnessFactorFormulas.getAllFormulas()
        );

        final VectorTableModelJam vtm = VectorTableModelResolver.resolve(jam);

        return SuspiciousnessFactorUtils.runOnAllResolvers(vtm, resolvers);
    }


    private static String resolveResultFilePath(String programName) {
        return jsonOutputDir + "/" + programName + ".json";
    }

    private static String resolveCsvFilePath(String programName) {
        return csvOutputDir + "/" + programName + ".csv";
    }
}

