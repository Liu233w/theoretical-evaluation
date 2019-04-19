package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.*;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import one.util.streamex.StreamEx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * 使用划分测试用例的方式计算出的结果，检查结果的效果
 */
public class DiffTotInfoSfSubsetByOp {

    private static final SuspiciousnessFactorFormula formula =
        SuspiciousnessFactorFormulas::op;
    private static final String formulaTitle = "op";

    private static final String outputFile = "./target/outputs/totInfoSfSubsetDiffBy-" + formulaTitle + ".csv";


    public static void main(String[] args) throws IOException, URISyntaxException {

        final FaultLocationJam faultLocations = TotInfoFaultLocationLoader.getFaultLocations();
        final SuspiciousnessFactorJam sfUnSubseted = ResolveTotInfoSuspiciousnessFactor.getResultFromFile();
        final SuspiciousnessFactorJam sfSubseted = resolveSubsetedSf();

        final DiffRankJam diff = DiffRankResolver.resolve(
            filterSf(sfUnSubseted),
            sfSubseted,
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
    private static SuspiciousnessFactorJam resolveSubsetedSf() throws FileNotFoundException {

        final TestSuitSubsetJam testSuitSubsetJam = ResolveTestSuitSubsetByOp.getResultFromFile("tot_info");
        final RunResultJam runResults = Run.getResultFromFile("tot_info");
        final RunResultJam runResultJam = testSuitSubsetJam.getRunResultJam(runResults);
        final VectorTableModelJam vectorTableModelJam = VectorTableModelResolver.resolve(runResultJam);

        return SuspiciousnessFactorResolver
            .builder()
            .formula(formula)
            .formulaTitle(formulaTitle)
            .build()
            .resolve(vectorTableModelJam);
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
