package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModelResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModelJam;
import edu.nwpu.machunyan.theoreticalEvaluation.interData.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.ProgramRunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.IOException;

public class ResolveTotInfoVtm {

    private static String outputPath = "./target/outputs/tot_info-vtm.csv";

    public static void main(String[] args) throws IOException {

        final ProgramRunResultJam results = RunTotInfo.getRunResultsFromSavedFile();
        final VectorTableModelJam vectorTableModelJam = VectorTableModelResolver.fromProgramResultJam(results);
        final String csvString = CsvExporter.toCsvString(vectorTableModelJam);
        FileUtils.saveString(outputPath, csvString);
    }
}
