package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;

import java.util.ArrayList;
import java.util.List;

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

        for (VectorTableModel vtm : jam.getVectorTableModels()) {
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
            "program title", "formula",
            leftRankTitle + "-rank", leftRankTitle + "-suspiciousnessFactor",
            rightRankTitle + "-rank", rightRankTitle + "-suspiciousnessFactor",
        }));

        for (DiffRankForProgram program : jam.getDiffRankForPrograms()) {
            for (DiffRankForStatement statement : program.getDiffRankForStatements()) {

                final DiffRankForSide left = statement.getLeft();
                final DiffRankForSide right = statement.getRight();

                csvLines.add(new CsvLine(new Object[]{
                    program.getProgramTitle(), program.getFormulaTitle(),
                    left.getRank(), left.getSuspiciousnessFactor(),
                    right.getRank(), right.getSuspiciousnessFactor(),
                }));
            }
        }

        return toCsvString(csvLines);
    }
}

