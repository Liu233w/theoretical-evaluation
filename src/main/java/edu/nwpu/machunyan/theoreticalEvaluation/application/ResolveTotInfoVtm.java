package edu.nwpu.machunyan.theoreticalEvaluation.application;

import java.io.IOException;

public class ResolveTotInfoVtm {

    private static String outputPath = "./target/outputs/tot_info-vtm.json";

    public static void main(String[] args) throws IOException {
//
//        final Map<String, ArrayList<RunResultFromRunner>> imports = RunTotInfo.getRunResultsFromSavedFile();
//        final TestCaseWeightJam testCaseWeightJam = ResolveTotInfoTestCaseWeight.loadFromFile();
//
//        final HashMap<String, ArrayList<VectorTableModelRecord>> result = new HashMap<>();
//        for (int i = 0; i < imports.size(); i++) {
//            final List<TestCaseWeightItem> testCaseWeights = testCaseWeightJam.getTestCaseWeightForPrograms().get(i).getTestCaseWeights();
//
//            final List<Double> weights = testCaseWeights.stream()
//                    .sorted(Comparator.comparingInt(TestCaseWeightItem::getTestCaseIndex))
//                    .map(TestCaseWeightItem::getTestCaseWeight)
//                    .collect(Collectors.toList());
//            final String version = "v" + (i + 1);
//            final ArrayList<VectorTableModelRecord> vtm = VectorTableModelGenerator
//                    .generateFromRunResultWithWeight(imports.get(version), weights);
//
//            result.put(version, vtm);
//        }
//        final JsonElement jsonElement = JsonUtils.toJson(result);
//        FileUtils.printJsonToFile(Paths.get(outputPath), jsonElement);
    }
}
