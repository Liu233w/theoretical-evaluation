package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.*;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.MultipleFormulaSuspiciousnessFactorJam;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorJam;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CustomCollectors;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.FileNotFoundException;
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

        final Map<String, SuspiciousnessFactorJam> resultMap = resolveAndGetResult();

        for (Map.Entry<String, SuspiciousnessFactorJam> entry : resultMap.entrySet()) {

            final String formula = entry.getKey();
            final MultipleFormulaSuspiciousnessFactorJam result =
                SuspiciousnessFactorHelper.collectAsMultipleFormula(entry.getValue());

            final Path savePath = csvOutputDir.resolve("weighted-by-" + formula + ".csv");
            FileUtils.saveString(savePath, CsvExporter.toCsvString(result));
        }
    }

    public static Map<String, SuspiciousnessFactorJam> resolveAndGetResult() throws FileNotFoundException {

        final RunResultJam jam = RunTotInfo.getRunResultsFromSavedFile();
        final TestcaseWeightJam testcaseWeightJam = ResolveTotInfoTestcaseWeight.loadFromFile();

        // 权重加成倍数：测试用例的个数
        final double testcaseWeightMultiply = jam
            .getRunResultForPrograms()
            .get(0)
            .getRunResults()
            .size();

        final List<SuspiciousnessFactorResolver> resolvers = SuspiciousnessFactorResolver.of(
            SuspiciousnessFactorFormulas.getAllFormulas()
        );

        final Set<String> formulas = testcaseWeightJam
            .getTestcaseWeightForPrograms()
            .stream()
            .map(TestcaseWeightForProgram::getFormulaTitle)
            .collect(Collectors.toSet());

        final HashMap<String, SuspiciousnessFactorJam> resultMap = new HashMap<>();
        formulas.forEach(formula -> testcaseWeightJam
            .getTestcaseWeightForPrograms()
            .stream()
            .filter(a -> a.getFormulaTitle().equals(formula))
            .collect(CustomCollectors.collectToOptional(Collectors.toList()))
            // List<TestcaseWeightForProgram>
            .map(TestcaseWeightJam::new)
            // TestcaseWeightJam
            .map(a -> TestcaseWeightMultiplier.resolve(a, testcaseWeightMultiply))
            // multiplied TestcaseWeightJam
            .map(a -> VectorTableModelResolver.resolveWithWeights(jam, a))
            // VectorTableModel
            .map(a -> SuspiciousnessFactorHelper.runOnAllResolvers(a, resolvers))
            .ifPresent(a -> resultMap.put(formula, a)));

        return resultMap;
    }
}
