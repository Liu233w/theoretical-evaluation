package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import edu.nwpu.machunyan.theoreticalEvaluation.chart.pojo.EffectSizeItem;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

/**
 * 将数据输出成 csv 格式
 */
public class CsvExporter {

    @SneakyThrows(IOException.class) // 不太可能会发生的异常
    public static String toCsvString(List<CsvLine> lines) {
        StringWriter out = new StringWriter();
        CSVPrinter printer = CSVFormat.DEFAULT.print(out);
        for (CsvLine line : lines) {
            printer.printRecord(line.getLineItems());
        }
        return out.toString();
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

        csvLines.add(new CsvLine(ArrayUtils.addAll(new Object[]{
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
                    final OptionalInt rankDiff = statement.getRankDiff();
                    csvLines.add(new CsvLine(new Object[]{
                        programTitle,
                        item.getFormulaTitle(),
                        statement.getStatementIndex(),
                        statement.getLeft().getRank() + " -> " + statement.getRight().getRank(),
                        rankDiff.isPresent() ? rankDiff.getAsInt() : "NaN",
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

                final FaultLocationForProgram location = titleToLocations.remove(programTitle);
                final String diffStr, commentsStr;
                if (location == null) {
                    diffStr = "";
                    commentsStr = "";
                } else {
                    diffStr = location.getDiff();
                    commentsStr = location.getComments();
                }

                // 确保每个程序的 diff 打印了并且只打印了一次
                boolean printed = false;

                for (DiffRankForStatement statement : item.getDiffRankForStatements()) {
                    if (statement.getLeft().getRank() == -1) {
                        // 跳过不存在的语句
                        continue;
                    }

                    final OptionalInt rankDiff = statement.getRankDiff();
                    csvLines.add(new CsvLine(new Object[]{
                        programTitle,
                        printed ? "" : diffStr,
                        printed ? "" : commentsStr,
                        item.getFormulaTitle(),
                        statement.getStatementIndex(),
                        statement.getLeft().getRank() + " -> " + statement.getRight().getRank(),
                        rankDiff.isPresent() ? rankDiff.getAsInt() : "NaN",
                    }));

                    printed = true;
                }

                if (!printed) {
                    // 这个程序的每一条语句都跳过去了

                    csvLines.add(new CsvLine(new Object[]{
                        programTitle,
                        diffStr,
                        commentsStr,
                        item.getFormulaTitle(),
                        "None",
                        "",
                        "",
                    }));
                }
            });

        return CsvExporter.toCsvString(csvLines);
    }

    // java 的泛型是类型擦除，这里不能用 List<EffectSizeItem>
    public static String toCsvString(EffectSizeItem[] effectSizeItems) {

        final ArrayList<CsvLine> csvLines = new ArrayList<>(effectSizeItems.length);
        csvLines.add(new CsvLine(new Object[]{"program title", "formula", "effect size"}));

        for (EffectSizeItem item : effectSizeItems) {
            csvLines.add(new CsvLine(new Object[]{item.getProgramName(), item.getFormulaTitle(), item.getEffectSize()}));
        }

        return CsvExporter.toCsvString(csvLines);
    }
}

