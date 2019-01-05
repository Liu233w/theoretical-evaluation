package edu.nwpu.machunyan.theoreticalEvaluation.interData;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModelJam;

import java.util.ArrayList;
import java.util.List;

/**
 * 将数据输出成 csv 格式
 */
public class CsvExporter {

    public static String toCsvString(List<CsvLine> lines) {
        final StringBuilder sb = new StringBuilder();
        for (CsvLine line : lines) {
            for (Object item : line.lineItems) {
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

        jam.getVectorTableModels().forEach(vtm ->
            vtm.getRecords().stream()
                .skip(1)
                .map(record -> new CsvLine(new Object[]{
                    vtm.getProgramTitle(), record.isUseWeight(),
                    record.getAnf(), record.getAnp(), record.getAef(), record.getAep(),
                    record.getUnWeightedAnf(), record.getUnWeightedAnp(), record.getUnWeightedAef(), record.getUnWeightedAep(),
                })).forEach(csvLines::add));

        return toCsvString(csvLines);
    }
}

