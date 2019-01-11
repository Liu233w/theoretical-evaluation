package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorHelper;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModelResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用权重来解决 suspiciousness factor
 */
public class ResolveTotInfoSuspiciousnessFactorWithWeight {

    // 输出文件
    private static final Path csvOutputDir = Paths.get("./target/outputs/totInfoSuspiciousnessFactorsWithWeight");

    public static void main(String[] args) throws IOException {

        final RunResultJam jam = RunTotInfo.getRunResultsFromSavedFile();
        final TestcaseWeightJam testcaseWeightJam = ResolveTotInfoTestcaseWeight.loadFromFile();

        final List<SuspiciousnessFactorResolver> resolvers = SuspiciousnessFactorResolver.of(
            SuspiciousnessFactorFormulas.getAllFormulas()
        );

        final Set<String> formulas = testcaseWeightJam
            .getTestcaseWeightForPrograms()
            .stream()
            .map(TestcaseWeightForProgram::getFormulaTitle)
            .collect(Collectors.toSet());

        final HashMap<String, MultipleFormulaSuspiciousnessFactorJam> resultMap = new HashMap<>();
        formulas.forEach(formula -> {
            final List<TestcaseWeightForProgram> weights = testcaseWeightJam
                .getTestcaseWeightForPrograms()
                .stream()
                .filter(a -> a.getFormulaTitle().equals(formula))
                .collect(Collectors.toList());

            final VectorTableModelJam vtm = VectorTableModelResolver.resolveWithWeights(jam, weights);

            final SuspiciousnessFactorJam suspiciousnessFactorJam = SuspiciousnessFactorHelper.runOnAllResolvers(vtm, resolvers);
            final MultipleFormulaSuspiciousnessFactorJam result = SuspiciousnessFactorHelper.collectAsMultipleFormula(suspiciousnessFactorJam);

            resultMap.put(formula, result);
        });

        for (Map.Entry<String, MultipleFormulaSuspiciousnessFactorJam> entry : resultMap.entrySet()) {

            final String formula = entry.getKey();
            final MultipleFormulaSuspiciousnessFactorJam result = entry.getValue();

            final Path savePath = csvOutputDir.resolve("weighted-by-" + formula + ".csv");
            FileUtils.saveString(savePath, CsvExporter.toCsvString(result));
        }
    }
}
