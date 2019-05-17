package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.*;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.FaultLocationLoader;
import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.ProgramDefination;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvLine;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import one.util.streamex.StreamEx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 最简化版本的比较代码，只比较使用 特定公式 划分测试用例子集、并且使用 该公式 计算结果的代码。
 * 能够输出比较好看的结果。
 */
public class DiffSfSubsetByCertainFormula {

    private static final SuspiciousnessFactorFormula formula =
        SuspiciousnessFactorFormulas::op;
    private static final String formulaTitle = "op";

    private static final String outputDir = "./target/outputs/sf-subset-diff-by-" + formulaTitle;


    public static void main(String[] args) throws IOException, URISyntaxException {

        for (String name : ProgramDefination.PROGRAM_LIST) {

            if (Files.exists(Paths.get(outputDir + "/" + name + ".csv"))) {
                continue;
            }

            LogUtils.logInfo("Working on " + name);
            resolveAndSave(name);
        }
    }

    private static void resolveAndSave(String name) throws IOException, URISyntaxException {

        final Optional<FaultLocationJam> faultLocationOptional = FaultLocationLoader.getFaultLocations(name);
        if (!faultLocationOptional.isPresent()) {
            LogUtils.logError("Fault location not exist for " + name);
            return;
        }

        final FaultLocationJam faultLocations = faultLocationOptional.get();
        final SuspiciousnessFactorJam sfOrigin = ResolveSuspiciousnessFactor.getResultFromFile(name);

        final SuspiciousnessFactorJam sfSubset = resolveSubsetSf(name);
        final Set<String> emptySfProgram = findEmptySfProgram(sfSubset);

        final DiffRankJam diff = DiffRankResolver.resolve(
            filterSf(sfOrigin, emptySfProgram),
            filterSf(sfSubset, emptySfProgram),
            "",
            "",
            faultLocations);
        FileUtils.saveString(outputDir + "/" + name + ".csv",
            CsvExporter.toSimplifiedCsvString(diff, faultLocations));

        final DiffRankJam diffDetailed = DiffRankResolver.resolve(
            filterSf(sfOrigin, emptySfProgram),
            filterSf(sfSubset, emptySfProgram),
            "unSubset",
            "subset");
        FileUtils.saveString(outputDir + "/" + name + "-detail.csv",
            CsvExporter.toCsvString(diffDetailed));

        final List<CsvLine> csvLines = StreamEx
            .of(diff.getDiffRankForPrograms())
            .map(diffItem -> {
                final String programTitle = diffItem.getProgramTitle();
                final double effectSize = RankDiffAnalyzer.resolveEffectSize(diffItem);
                return new CsvLine(new Object[]{programTitle, effectSize});
            })
            .prepend(new CsvLine(new Object[]{"programTitle", "effectSize"}))
            .toImmutableList();
        FileUtils.saveString(outputDir + "/" + name + "-effect-size.csv",
            CsvExporter.toCsvString(csvLines));
    }

    /**
     * 获取使用 Op 公式加权之后的可疑因子
     *
     * @return
     * @throws FileNotFoundException
     */
    private static SuspiciousnessFactorJam resolveSubsetSf(String name) throws FileNotFoundException {

        final RunResultJam jam = Run.getResultFromFile(name);
        // TODO: 如果改成多个公式的话，需要把这里换成 ResolveTestSuitSubset
        final TestSuitSubsetJam subsetJam = ResolveTestSuitSubsetByOp.getResultFromFile(name);
        final RunResultJam subsetResult = subsetJam.getRunResultJam(jam);

        final VectorTableModelJam vtm = VectorTableModelResolver.resolve(subsetResult);

        return SuspiciousnessFactorResolver
            .builder()
            .formula(formula)
            .formulaTitle(formulaTitle)
            .build()
            .resolve(vtm);
    }

    private static Set<String> findEmptySfProgram(SuspiciousnessFactorJam jam) {

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
        Set<String> filterSet) {

        final List<SuspiciousnessFactorForProgram> list = StreamEx
            .of(jam.getResultForPrograms())
            .filter(a -> a.getFormula().equals(formulaTitle))
            .filter(a -> !filterSet.contains(a.getProgramTitle()))
            .toImmutableList();

        return new SuspiciousnessFactorJam(list);
    }
}
