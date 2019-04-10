package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.DiffRankResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import edu.nwpu.machunyan.theoreticalEvaluation.application.ResolveTotInfoSuspiciousnessFactor;
import edu.nwpu.machunyan.theoreticalEvaluation.application.ResolveTotInfoSuspiciousnessFactorWithWeight;
import edu.nwpu.machunyan.theoreticalEvaluation.application.TotInfoFaultLocationLoader;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvLine;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import one.util.streamex.StreamEx;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

/**
 * 比较加权之前和之后的每条语句的区别，使用实现定义好的错误位置进行筛选
 */
public class DiffTotInfoSfWithWeightSimplifiedByFaultLocation {

    private static final String outputFile = "./target/outputs/totInfoSfWeightDiffSimplifiedByFaultLocation.csv";

    public static void main(String[] args) throws URISyntaxException, IOException {

        final FaultLocationJam faultLocations = TotInfoFaultLocationLoader.getFaultLocations();
        final SuspiciousnessFactorJam sfUnweighted = ResolveTotInfoSuspiciousnessFactor.getResultFromFile();
        final Map<String, SuspiciousnessFactorJam> sfWeighted = ResolveTotInfoSuspiciousnessFactorWithWeight.resolveAndGetResult();

        final Map<String, DiffRankJam> collect = StreamEx
            .of(sfWeighted.entrySet())
            .mapToEntry(
                Map.Entry::getKey,
                a -> DiffRankResolver.resolve(
                    sfUnweighted, a.getValue(),
                    "unweighted", "weighted",
                    faultLocations
                )
            )
            .toImmutableMap();

        FileUtils.saveString(outputFile, toCsvString(collect));
    }

    private static String toCsvString(Map<String, DiffRankJam> map) {

        //noinspection OptionalGetWithoutIsPresent
        final DiffRankForProgram sampleDiff = map
            .values()
            .stream()
            .findFirst()
            .get()
            .getDiffRankForPrograms()
            .get(0);
        final String leftRankTitle = sampleDiff.getLeftRankTitle();
        final String rightRankTitle = sampleDiff.getRightRankTitle();

        final ArrayList<CsvLine> csvLines = new ArrayList<>();

        csvLines.add(new CsvLine(new Object[]{
            "weighting formula", "program title", "formula", "statement index",
            leftRankTitle + "-rank", leftRankTitle + "-suspiciousnessFactor",
            rightRankTitle + "-rank", rightRankTitle + "-suspiciousnessFactor",
            "rank diff"
        }));

        map.forEach((weightFormulaTitle, jam) -> {
            for (DiffRankForProgram program : jam.getDiffRankForPrograms()) {
                for (DiffRankForStatement statement : program.getDiffRankForStatements()) {

                    final DiffRankForSide left = statement.getLeft();
                    final DiffRankForSide right = statement.getRight();

                    csvLines.add(new CsvLine(new Object[]{
                        weightFormulaTitle,
                        program.getProgramTitle(), program.getFormulaTitle(),
                        statement.getStatementIndex(),
                        left.getRank(), left.getSuspiciousnessFactor(),
                        right.getRank(), right.getSuspiciousnessFactor(),
                        statement.getRankDiff(),
                    }));
                }
            }
        });

        return CsvExporter.toCsvString(csvLines);
    }
}
