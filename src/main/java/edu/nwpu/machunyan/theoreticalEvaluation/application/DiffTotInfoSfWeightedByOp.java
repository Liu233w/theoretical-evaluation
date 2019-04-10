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
 * 最简化版本的比较代码，只比较使用 Op 加权、并且使用 Op 计算结果的代码。
 * 能够输出比较好看的结果。
 */
public class DiffTotInfoSfWeightedByOp {

    private static final String outputFile = "./target/outputs/totInfoSfWeightDiffByOp.csv";

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

        FileUtils.saveString(outputFile, toCsvString(diff));
    }

    private static String toCsvString(DiffRankJam diff) {
        final ArrayList<CsvLine> csvLines = new ArrayList<>();
        csvLines.add(new CsvLine(new Object[]{"program title", "statement index", "rank change", "rank diff"}));

        StreamEx
            .of(diff.getDiffRankForPrograms())
            .sortedByInt(a -> Integer.parseInt(a.getProgramTitle().substring(1)))
            .forEach(item -> {
                final String programTitle = item.getProgramTitle();
                item.getDiffRankForStatements().forEach(statement -> {
                    if (statement.getLeft().getRank() == -1) {
                        // 跳过不存在的语句
                        return;
                    }
                    csvLines.add(new CsvLine(new Object[]{
                        programTitle,
                        statement.getStatementIndex(),
                        statement.getLeft().getRank() + " -> " + statement.getRight().getRank(),
                        statement.getRankDiff(),
                    }));
                });
            });

        return CsvExporter.toCsvString(csvLines);
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

        final double mm = 1.0;
        // 权重加成倍数：测试用例的个数 * mm
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
            .formula(SuspiciousnessFactorFormulas::op)
            .formulaTitle("op")
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
            .filter(a -> a.getFormulaTitle().equals("op"))
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
            .filter(a -> a.getFormula().equals("op"))
            .toImmutableList();

        return new SuspiciousnessFactorJam(list);
    }
}
