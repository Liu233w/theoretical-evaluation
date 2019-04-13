package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.*;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvLine;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import one.util.streamex.StreamEx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * 最简化版本的比较代码，只比较使用 特定公式 加权、并且使用 该公式 计算结果的代码。
 * 能够输出比较好看的结果。
 */
public class DiffTotInfoSfWeightedByCertainFormula {

    // 权重加成倍数：测试用例的个数 * mm
    private static final double mm = 1.0;

    private static final SuspiciousnessFactorFormula formula =
        SuspiciousnessFactorFormulas::op;
    private static final String formulaTitle = "op";

    private static final String outputFile = "./target/outputs/totInfoSfWeightDiffBy-" + formulaTitle + ".csv";


    public static void main(String[] args) throws IOException, URISyntaxException {

        final FaultLocationJam faultLocations = TotInfoFaultLocationLoader.getFaultLocations();
        final SuspiciousnessFactorJam sfUnweighted = ResolveTotInfoSuspiciousnessFactor.getResultFromFile();

        final SuspiciousnessFactorJam sfWeighted = resolveWeightedSf();

        final DiffRankJam diff = DiffRankResolver.resolve(
            filterSf(sfUnweighted),
            sfWeighted,
            "",
            "",
            faultLocations);

        FileUtils.saveString(outputFile, CsvExporter.toSimplifiedCsvString(diff));
    }

    /**
     * 获取使用 Op 公式加权之后的可疑因子
     *
     * @return
     * @throws FileNotFoundException
     */
    private static SuspiciousnessFactorJam resolveWeightedSf() throws FileNotFoundException {

        final RunResultJam jam = RunTotInfo.getRunResultsFromSavedFile();
        final TestcaseWeightJam testcaseWeightJam = ResolveTotInfoTestcaseWeight.loadFromFile();

        final double testcaseWeightMultiply = jam
            .getRunResultForPrograms()
            .get(0)
            .getRunResults()
            .size()
            * mm;

        final TestcaseWeightJam multipliedWeight = TestcaseWeightMultiplyingResolver.resolve(
            filterWeights(testcaseWeightJam),
            testcaseWeightMultiply);
        final VectorTableModelJam vtm = VectorTableModelResolver.resolveWithWeights(jam, multipliedWeight);

        return SuspiciousnessFactorResolver
            .builder()
            .formula(formula)
            .formulaTitle(formulaTitle)
            .build()
            .resolve(vtm);
    }

    /**
     * 找出使用 Op 公式加权的结果
     *
     * @param jam
     * @return
     */
    private static TestcaseWeightJam filterWeights(TestcaseWeightJam jam) {

        final List<TestcaseWeightForProgram> list = StreamEx
            .of(jam.getTestcaseWeightForPrograms())
            .filter(a -> a.getFormulaTitle().equals(formulaTitle))
            .toImmutableList();

        return new TestcaseWeightJam(list);
    }

    /**
     * 找出使用 Op 公式加权的可疑因子
     *
     * @param jam
     * @return
     */
    private static SuspiciousnessFactorJam filterSf(SuspiciousnessFactorJam jam) {

        final List<SuspiciousnessFactorForProgram> list = StreamEx
            .of(jam.getResultForPrograms())
            .filter(a -> a.getFormula().equals(formulaTitle))
            .toImmutableList();

        return new SuspiciousnessFactorJam(list);
    }
}
