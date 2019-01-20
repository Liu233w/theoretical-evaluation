package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModel;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.IOException;

public class ResolveTotInfoVtm {

    private static String outputPath = "./target/outputs/tot_info-vtm.csv";

    public static void main(String[] args) throws IOException {

        final RunResultJam results = RunTotInfo.getRunResultsFromSavedFile();
        final VectorTableModel.Pojo.VectorTableModelJam vectorTableModelJam = VectorTableModel.VectorTableModelResolver.resolve(results);
        final String csvString = CsvExporter.toCsvString(vectorTableModelJam);
        FileUtils.saveString(outputPath, csvString);
    }
}
