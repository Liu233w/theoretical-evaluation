package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.AverageRankDiffResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.DiffRankResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.ArrayUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvLine;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 比较加权之前和之后的每条语句的区别，使用实现定义好的错误位置进行筛选。
 * 简化版，将计算sf的公式和用来加权的公式合成一个。
 */
public class DiffTotInfoSfWithWeightSimplified {

    private static final String outputFile = "./target/outputs/totInfoSfWeightDiffSimplified.csv";
    private static final String sfOutputFile = "./target/outputs/totInfoSfWeightedLimited.csv";

    public static void main(String[] args) throws URISyntaxException, IOException {

        final FaultLocationJam faultLocations = TotInfoFaultLocationLoader.getFaultLocations();
        final SuspiciousnessFactorJam sfUnweighted = ResolveTotInfoSuspiciousnessFactor.getResultFromFile();
        final Map<String, SuspiciousnessFactorJam> sfWeighted = ResolveTotInfoSuspiciousnessFactorWithWeight.resolveAndGetResult();

        final SuspiciousnessFactorJam simplifyedWeightedSf = getSimplifyedWeightedSf(sfWeighted);

        final DiffRankJam diff = DiffRankResolver.resolve(sfUnweighted, simplifyedWeightedSf, "unweighted", "weighted", faultLocations);

        final AverageDiffRankJam averageDiffRankJam = AverageRankDiffResolver.resolve(diff, faultLocations);

        FileUtils.saveString(outputFile, toCsvString(averageDiffRankJam));
        FileUtils.saveString(sfOutputFile, toCsvString(diff));
    }

    /**
     * 原先的加权结果太过复杂，可能是先用公式 A 加权，然后把结果使用公式 B 来计算可疑因子。
     * 这里把它简化成如果使用公式 A 加权，结果一定再使用公式 A 来计算可疑因子。
     *
     * @param sfWeighted
     * @return
     */
    private static SuspiciousnessFactorJam getSimplifyedWeightedSf(Map<String, SuspiciousnessFactorJam> sfWeighted) {

        final List<SuspiciousnessFactorForProgram> list = EntryStream
            .of(sfWeighted)
            .flatMap(entry -> {
                final String formula = entry.getKey();
                final SuspiciousnessFactorJam jam = entry.getValue();

                return StreamEx
                    .of(jam.getResultForPrograms())
                    .filter(item -> item.getFormula().equals(formula));
            })
            .toImmutableList();

        return new SuspiciousnessFactorJam(list);
    }

    private static String toCsvString(AverageDiffRankJam jam) {

        final Object[] formulaTitles = StreamEx.of(jam.getAverageDiffRankForPrograms())
            .map(AverageDiffRankForProgram::getFormulaTitle)
            .toSet()
            .toArray();

        final ArrayList<CsvLine> csvLines = new ArrayList<>();
        csvLines.add(new CsvLine(ArrayUtils.concat(new Object[]{"program title"}, formulaTitles)));

        // program title -> formula -> average diff
        final HashMap<String, HashMap<String, Double>> tmp = new HashMap<>();
        jam.getAverageDiffRankForPrograms().forEach(item -> {
            tmp.computeIfAbsent(item.getProgramTitle(), k -> new HashMap<>())
                .put(item.getFormulaTitle(), item.getAverageRankDiff());
        });

        final List<String> sortedProgramTitle = tmp.keySet().stream()
            .map(a -> a.substring(1))
            .map(Integer::parseInt)
            .sorted()
            .map(a -> "v" + a)
            .collect(Collectors.toList());

        for (String programTitle : sortedProgramTitle) {
            final HashMap<String, Double> formulaToDiff = tmp.get(programTitle);

            final Object[] diffs = new Object[formulaTitles.length];
            for (int i = 0; i < formulaTitles.length; i++) {
                diffs[i] = formulaToDiff.get(formulaTitles[i]);
            }

            csvLines.add(new CsvLine(ArrayUtils.concat(new Object[]{programTitle}, diffs)));
        }

        return CsvExporter.toCsvString(csvLines);
    }

    private static String toCsvString(DiffRankJam jam) {

        final Object[] formulaTitles = StreamEx.of(jam.getDiffRankForPrograms())
            .map(DiffRankForProgram::getFormulaTitle)
            .toSet()
            .toArray();

        final ArrayList<CsvLine> csvLines = new ArrayList<>();
        csvLines.add(new CsvLine(ArrayUtils.concat(new Object[]{"program title", "statement index"}, formulaTitles)));

        // program title -> statement index -> formula -> rank
        final HashMap<String, HashMap<Integer, HashMap<String, String>>> tmp = new HashMap<>();
        jam.getDiffRankForPrograms().forEach(program -> {
            program.getDiffRankForStatements().forEach(statement -> {
                tmp
                    .computeIfAbsent(program.getProgramTitle(), k -> new HashMap<>())
                    .computeIfAbsent(statement.getStatementIndex(), k -> new HashMap<>())
                    .put(program.getFormulaTitle(),
                        statement.getLeft().getRank() + " -> "
                            + statement.getRight().getRank());
            });
        });

        final List<String> sortedProgramTitle = tmp.keySet().stream()
            .map(a -> a.substring(1))
            .map(Integer::parseInt)
            .sorted()
            .map(a -> "v" + a)
            .collect(Collectors.toList());

        for (String programTitle : sortedProgramTitle) {
            final HashMap<Integer, HashMap<String, String>> map = tmp.get(programTitle);

            map.forEach((statementIdx, formulaToRank) -> {
                final Object[] ranks = new Object[formulaTitles.length];
                for (int i = 0; i < formulaTitles.length; i++) {
                    ranks[i] = formulaToRank.get(formulaTitles[i]);
                }

                csvLines.add(new CsvLine(ArrayUtils.concat(new Object[]{programTitle, statementIdx}, ranks)));
            });
        }

        return CsvExporter.toCsvString(csvLines);
    }
}
