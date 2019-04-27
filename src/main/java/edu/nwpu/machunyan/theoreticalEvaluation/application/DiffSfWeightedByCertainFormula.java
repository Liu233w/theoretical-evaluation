package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.*;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.FaultLocationLoader;
import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.ProgramDefination;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import one.util.streamex.StreamEx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * 最简化版本的比较代码，只比较使用 特定公式 加权、并且使用 该公式 计算结果的代码。
 * 能够输出比较好看的结果。
 */
public class DiffSfWeightedByCertainFormula {

    // 权重加成倍数：测试用例的个数 * mm
    private static final double mm = 1.0;

    private static final SuspiciousnessFactorFormula formula =
        SuspiciousnessFactorFormulas::op;
    private static final String formulaTitle = "op";

    private static final String outputDir = "./target/outputs/sf-weight-diff-by-" + formulaTitle;


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
        final SuspiciousnessFactorJam sfUnweighted = ResolveSuspiciousnessFactor.getResultFromFile(name);

        final SuspiciousnessFactorJam sfWeighted = resolveWeightedSf(name);

        final DiffRankJam diff = DiffRankResolver.resolve(
            filterSf(sfUnweighted),
            sfWeighted,
            "",
            "",
            faultLocations);
        FileUtils.saveString(outputDir + "/" + name + ".csv",
            CsvExporter.toSimplifiedCsvString(diff, faultLocations));

        final DiffRankJam diffDetailed = DiffRankResolver.resolve(
            filterSf(sfUnweighted),
            sfWeighted,
            "unweighted",
            "weighted");
        FileUtils.saveString(outputDir + "/" + name + "-detail.csv",
            CsvExporter.toCsvString(diffDetailed));
    }

    /**
     * 获取使用 Op 公式加权之后的可疑因子
     *
     * @return
     * @throws FileNotFoundException
     */
    private static SuspiciousnessFactorJam resolveWeightedSf(String name) throws FileNotFoundException {

        final RunResultJam jam = Run.getResultFromFile(name);
        final TestcaseWeightJam testcaseWeightJam = ResolveTestcaseWeight.getResultFromFile(name, formulaTitle);

        final double testcaseWeightMultiply = jam
            .getRunResultForPrograms()
            .get(0)
            .getRunResults()
            .size()
            * mm;

        final TestcaseWeightJam multipliedWeight = TestcaseWeightMultiplyingResolver.resolve(
            testcaseWeightJam, testcaseWeightMultiply);
        final VectorTableModelJam vtm = VectorTableModelResolver.resolveWithWeights(jam, multipliedWeight);

        return SuspiciousnessFactorResolver
            .builder()
            .formula(formula)
            .formulaTitle(formulaTitle)
            .build()
            .resolve(vtm);
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
