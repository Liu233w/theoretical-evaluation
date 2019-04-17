package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import one.util.streamex.StreamEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 将数据输出成 csv 格式
 */
public class CsvExporter {

    public static String toCsvString(List<CsvLine> lines) {
        final StringBuilder sb = new StringBuilder();
        for (CsvLine line : lines) {
            for (Object item : line.getLineItems()) {
                sb.append(item).append(",");
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    public static String toCsvString(VectorTableModelJam jam) {
        final ArrayList<CsvLine> csvLines = new ArrayList<>();

        csvLines.add(new CsvLine(new Object[]{
            "program title", "use weight", "Anf", "Anp", "Aef", "Aep",
            "Unweighted Anf", "Unweighted Anp", "Unweighted Aef", "Unweighted Aep"
        }));

        for (VectorTableModelForProgram vtm : jam.getVectorTableModelForPrograms()) {
            vtm.getRecords().stream()
                .skip(1)
                .map(record -> new CsvLine(new Object[]{
                    vtm.getProgramTitle(), record.isUseWeight(),
                    record.getAnf(), record.getAnp(), record.getAef(), record.getAep(),
                    record.getUnWeightedAnf(), record.getUnWeightedAnp(), record.getUnWeightedAef(), record.getUnWeightedAep(),
                }))
                .forEach(csvLines::add);
        }

        return toCsvString(csvLines);
    }

    public static String toCsvString(SuspiciousnessFactorJam jam) {

        final ArrayList<CsvLine> csvLines = new ArrayList<>();

        csvLines.add(new CsvLine(new Object[]{
            "program title", "formula", "statement index", "suspiciousness factor"
        }));

        for (SuspiciousnessFactorForProgram program : jam.getResultForPrograms()) {
            for (SuspiciousnessFactorForStatement item : program.getResultForStatements()) {
                csvLines.add(new CsvLine(new Object[]{
                    program.getProgramTitle(), program.getFormula(),
                    item.getStatementIndex(), item.getSuspiciousnessFactor(),
                }));
            }
        }

        return toCsvString(csvLines);
    }

    public static String toCsvString(MultipleFormulaSuspiciousnessFactorJam jam) {

        final Object[] formulaTitles = jam.getAllFormulaTitle().toArray();

        final ArrayList<CsvLine> csvLines = new ArrayList<>();

        csvLines.add(new CsvLine(ArrayUtils.concat(new Object[]{
            "program title", "statement index"
        }, formulaTitles)));

        final int formulaOffset = 2;

        for (MultipleFormulaSuspiciousnessFactorForProgram resultForProgram : jam.getResultForPrograms()) {
            for (MultipleFormulaSuspiciousnessFactorForStatement resultForStatement : resultForProgram.getResultForStatements()) {
                final Object[] line = new Object[formulaOffset + formulaTitles.length];
                line[0] = resultForProgram.getProgramTitle();
                line[1] = resultForStatement.getStatementIndex();
                for (int i = 0; i < formulaTitles.length; i++) {
                    final Object formulaTitle = formulaTitles[i];
                    line[formulaOffset + i] = resultForStatement.getFormulaTitleToResult().get(formulaTitle);
                }
                csvLines.add(new CsvLine(line));
            }
        }

        return toCsvString(csvLines);
    }

    public static String toCsvString(DiffRankJam jam) {

        final DiffRankForProgram sampleDiff = jam.getDiffRankForPrograms().get(0);
        final String leftRankTitle = sampleDiff.getLeftRankTitle();
        final String rightRankTitle = sampleDiff.getRightRankTitle();

        final ArrayList<CsvLine> csvLines = new ArrayList<>();

        csvLines.add(new CsvLine(new Object[]{
            "program title", "formula", "statement index",
            leftRankTitle + "-rank", leftRankTitle + "-suspiciousnessFactor",
            rightRankTitle + "-rank", rightRankTitle + "-suspiciousnessFactor",
        }));

        for (DiffRankForProgram program : jam.getDiffRankForPrograms()) {
            for (DiffRankForStatement statement : program.getDiffRankForStatements()) {

                final DiffRankForSide left = statement.getLeft();
                final DiffRankForSide right = statement.getRight();

                csvLines.add(new CsvLine(new Object[]{
                    program.getProgramTitle(), program.getFormulaTitle(),
                    statement.getStatementIndex(),
                    left.getRank(), left.getSuspiciousnessFactor(),
                    right.getRank(), right.getSuspiciousnessFactor(),
                }));
            }
        }

        return toCsvString(csvLines);
    }

    /**
     * 输出一个简化后的 csv 语句，将 left 和 right 合成一个，并且移除 suspiciousness factor
     *
     * @param diff
     * @return
     */
    public static String toSimplifiedCsvString(DiffRankJam diff) {
        final ArrayList<CsvLine> csvLines = new ArrayList<>();
        csvLines.add(new CsvLine(new Object[]{"program title", "formula", "statement index", "rank change", "rank diff"}));

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
                        item.getFormulaTitle(),
                        statement.getStatementIndex(),
                        statement.getLeft().getRank() + " -> " + statement.getRight().getRank(),
                        statement.getRankDiff(),
                    }));
                });
            });

        return CsvExporter.toCsvString(csvLines);
    }

    /**
     * 输出一个简化后的 csv 语句，将 left 和 right 合成一个，并且移除 suspiciousness factor
     * <p>
     * 加上diff和comments
     *
     * @param diff
     * @return
     */
    public static String toSimplifiedCsvString(DiffRankJam diff, FaultLocationJam faultLocations) {
        final ArrayList<CsvLine> csvLines = new ArrayList<>();
        csvLines.add(new CsvLine(new Object[]{"program title", "diff", "diff comments", "formula", "statement index", "rank change", "rank diff"}));

        Map<String, FaultLocationForProgram> titleToLocations
            = StreamEx.of(faultLocations.getFaultLocationForPrograms())
            .toMap(
                FaultLocationForProgram::getProgramTitle,
                a -> a
            );

        StreamEx
            .of(diff.getDiffRankForPrograms())
            .sortedByInt(a -> {
                try {
                    return Integer.parseInt(a.getProgramTitle().substring(1));
                } catch (NumberFormatException e) {
                    return Integer.MAX_VALUE;
                }
            })
            .forEach(item -> {
                final String programTitle = item.getProgramTitle();

                FaultLocationForProgram location = titleToLocations.remove(programTitle);

                item.getDiffRankForStatements().forEach(statement -> {
                    if (statement.getLeft().getRank() == -1) {
                        // 跳过不存在的语句
                        return;
                    }
                    csvLines.add(new CsvLine(new Object[]{
                        programTitle,
                        location.getDiff(),
                        location.getComments(),
                        item.getFormulaTitle(),
                        statement.getStatementIndex(),
                        statement.getLeft().getRank() + " -> " + statement.getRight().getRank(),
                        statement.getRankDiff(),
                    }));
                });
            });

        return CsvExporter.toCsvString(csvLines);
    }
}

