package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.*;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
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
        final TestcaseWeight.Pojo.Jam testcaseWeightJam = ResolveTotInfoTestcaseWeight.loadFromFile();

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
            .getForPrograms()
            .stream()
            .map(TestcaseWeight.Pojo.ForProgram::getFormulaTitle)
            .collect(Collectors.toSet());

        final HashMap<String, SuspiciousnessFactorJam> resultMap = new HashMap<>();
        formulas.forEach(formula -> {
            final List<TestcaseWeight.Pojo.ForProgram> weights = testcaseWeightJam
                .getForPrograms()
                .stream()
                .filter(a -> a.getFormulaTitle().equals(formula))
                .collect(Collectors.toList());

            final TestcaseWeight.Pojo.Jam weightJam = TestcaseWeight.Multiplier.resolve(
                new TestcaseWeight.Pojo.Jam(weights),
                testcaseWeightMultiply);

            final VectorTableModel.Pojo.VectorTableModelJam vtm = VectorTableModel.VectorTableModelResolver.resolveWithWeights(jam, weightJam);

            final SuspiciousnessFactorJam result = SuspiciousnessFactorHelper.runOnAllResolvers(vtm, resolvers);

            resultMap.put(formula, result);
        });

        return resultMap;
    }
}
