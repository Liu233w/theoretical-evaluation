package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.*;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.FaultLocationLoader;
import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.ProgramDefination;
import edu.nwpu.machunyan.theoreticalEvaluation.chart.EffectSizeChart;
import edu.nwpu.machunyan.theoreticalEvaluation.chart.pojo.EffectSizeItem;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import one.util.streamex.StreamEx;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * 计算所有程序，所有公式的 effect size
 */
public class ResolveAllEffectSize {

    private static final List<String> formulas = Arrays.asList(
        "tarantula",
        "ochiai",
        "ochiai2",
        "op",
        "sbi",
        "jaccard",
        "kulcznski1",
        "dStar2",
        "o");

    private static final String outputDir = "./target/outputs/effect-size/";

    public static void main(String[] args) throws IOException {

        final ArrayList<EffectSizeItem> results = new ArrayList<>(formulas.size() * ProgramDefination.PROGRAM_LIST.length);

        for (String programName : ProgramDefination.PROGRAM_LIST) {
            for (String formula : formulas) {

                final Optional<FaultLocationJam> faultLocationOptional = FaultLocationLoader.getFaultLocations(programName);
                if (!faultLocationOptional.isPresent()) {
                    throw new FileNotFoundException("fault location not found, " + programName);
                }

                final FaultLocationJam faultLocations = faultLocationOptional.get();
                final SuspiciousnessFactorJam sfOrigin = ResolveSuspiciousnessFactor.getResultFromFile(programName);

                final SuspiciousnessFactorJam sfSubset = resolveSubsetSf(programName, formula);
                final Set<String> emptySfProgram = findEmptySfProgram(sfSubset, formula);

                final DiffRankJam diffForEffectSize = DiffRankResolver.resolve(
                    filterSf(sfOrigin, emptySfProgram, formula),
                    filterSf(sfSubset, emptySfProgram, formula),
                    "before",
                    "after",
                    DiffRankFilters.onlyInList(faultLocations, true));
                final double effectSize = RankDiffAnalyzer.resolveEffectSizeCohensD(diffForEffectSize);

                results.add(new EffectSizeItem(programName, formula, effectSize));
            }
        }

        FileUtils.saveString(outputDir + "result.csv", CsvExporter.toCsvString(results.toArray(new EffectSizeItem[0])));
        final JFreeChart chart = EffectSizeChart.resolveGroupByProgram(results, "All Effect Sizes");

        ChartUtils.saveChartAsJPEG(
            Paths.get(outputDir).resolve("result.jpg").toFile(),
            chart,
            1200,
            800);
    }

    /**
     * 获取使用 Op 公式加权之后的可疑因子
     *
     * @return
     * @throws FileNotFoundException
     */
    private static SuspiciousnessFactorJam resolveSubsetSf(String programName, String formula) throws FileNotFoundException {

        final RunResultJam jam = Run.getResultFromFile(programName);
        final TestSuitSubsetJam subsetJam = ResolveTestSuitSubset.getResultFromFile(programName, formula);
        final RunResultJam subsetResult = subsetJam.getRunResultJam(jam);

        final VectorTableModelJam vtm = VectorTableModelResolver.resolve(subsetResult);

        return SuspiciousnessFactorResolver
            .builder()
            .formula(SuspiciousnessFactorFormulas.getAllFormulas().get(formula))
            .formulaTitle(formula)
            .build()
            .resolve(vtm);
    }

    private static Set<String> findEmptySfProgram(SuspiciousnessFactorJam jam, String formulaTitle) {

        // 像是 schedule2 - v4 这样的情况， average performance 全是 0
        // 得到的语句只有一条，还没有执行，这种要单独拿出来

        return StreamEx.of(jam.getResultForPrograms())
            .filter(a -> a.getFormula().equals(formulaTitle))
            .filter(a -> a.getResultForStatements().size() == 0)
            .map(SuspiciousnessFactorForProgram::getProgramTitle)
            .toImmutableSet();
    }

    /**
     * 找出使用 Op 公式加权的可疑因子，将 programTitle 包含在 set 中的结果删除
     *
     * @param jam
     * @return
     */
    private static SuspiciousnessFactorJam filterSf(
        SuspiciousnessFactorJam jam,
        Set<String> filterSet,
        String formulaTitle) {

        final List<SuspiciousnessFactorForProgram> list = StreamEx
            .of(jam.getResultForPrograms())
            .filter(a -> a.getFormula().equals(formulaTitle))
            .filter(a -> !filterSet.contains(a.getProgramTitle()))
            .toImmutableList();

        return new SuspiciousnessFactorJam(list);
    }
}
