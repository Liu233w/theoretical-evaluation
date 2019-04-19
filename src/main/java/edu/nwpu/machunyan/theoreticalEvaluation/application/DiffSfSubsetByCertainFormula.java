package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.*;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import lombok.Value;
import one.util.streamex.StreamEx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 最简化版本的比较代码，只比较使用 特定公式 划分测试用例子集、并且使用 该公式 计算结果的代码。
 * 能够输出比较好看的结果。
 */
public class DiffSfSubsetByCertainFormula {

    private static final String[] MAIN_LIST = new String[]{
        "my_sort",
        "schedule2",
        "tcas",
        "tot_info",
        "replace",
        "print_tokens",
    };

    // 权重加成倍数：测试用例的个数 * mm
    private static final double mm = 1.0;

    private static final SuspiciousnessFactorFormula formula =
        SuspiciousnessFactorFormulas::op;
    private static final String formulaTitle = "op";

    private static final String outputDir = "./target/outputs/sf-subset-diff-by-" + formulaTitle;


    public static void main(String[] args) throws IOException, URISyntaxException {

        for (String name : MAIN_LIST) {

            if (Files.exists(Paths.get(outputDir + "/" + name + ".csv"))) {
                continue;
            }

            LogUtils.logInfo("Working on " + name);
            resolveAndSave(name);
        }
    }

    public static void resolveAndSave(String name) throws IOException, URISyntaxException {

        final FaultLocationJam faultLocations = FaultLocationLoader.getFaultLocations(name);
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
