package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.*;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import one.util.streamex.StreamEx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                SuspiciousnessFactorUtils.collectAsMultipleFormula(entry.getValue());

            final Path savePath = csvOutputDir.resolve("weighted-by-" + formula + ".csv");
            FileUtils.saveString(savePath, CsvExporter.toCsvString(result));
        }
    }

    /**
     * @return Key 是用来加权的公式， Value 是加权之后计算的结果（分别用每个公式来加权）
     * @throws FileNotFoundException
     */
    public static Map<String, SuspiciousnessFactorJam> resolveAndGetResult() throws IOException {

        final RunResultJam jam = Run.getResultFromFile("tot_info");
        final TestcaseWeightJam testcaseWeightJam = ResolveTestcaseWeight.getResultFromFile("tot_info");

        // 权重加成倍数：测试用例的个数
        final double testcaseWeightMultiply = jam
            .getRunResultForPrograms()
            .get(0)
            .getRunResults()
            .size();

        final List<SuspiciousnessFactorResolver> resolvers = SuspiciousnessFactorResolver.of(
            SuspiciousnessFactorFormulas.getAllFormulas()
        );

        final Set<String> formulaTitles = StreamEx
            .of(testcaseWeightJam.getTestcaseWeightForPrograms())
            .map(TestcaseWeightForProgram::getFormulaTitle)
            .toImmutableSet();

        final HashMap<String, SuspiciousnessFactorJam> resultMap = new HashMap<>();
        formulaTitles.forEach(formula -> {
            final List<TestcaseWeightForProgram> weights = StreamEx
                .of(testcaseWeightJam.getTestcaseWeightForPrograms())
                .filter(a -> a.getFormulaTitle().equals(formula))
                .toImmutableList();

            final TestcaseWeightJam weightJam = TestcaseWeightMultiplyingResolver.resolve(
                new TestcaseWeightJam(weights),
                testcaseWeightMultiply);

            final VectorTableModelJam vtm = VectorTableModelResolver.resolveWithWeights(jam, weightJam);

            final SuspiciousnessFactorJam result = SuspiciousnessFactorUtils.runOnAllResolvers(vtm, resolvers);

            resultMap.put(formula, result);
        });

        return resultMap;
    }
}
