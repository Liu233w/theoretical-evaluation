package edu.nwpu.machunyan.theoreticalEvaluation.application;

/**
 * 用权重来解决 suspiciousness factor
 */
public class ResolveTotInfoSuspiciousnessFactorWithWeight {

    // 输出文件
    private static final String outputFilePath = "./target/outputs/tot_info-suspiciousness-factors-with-weight.json";
//
//    public static void main(String[] args) throws IOException {
//        final Map<String, ArrayList<RunResultFromRunner>> runResultsFromSavedFile = RunTotInfo.getRunResultsFromSavedFile();
//        final Map<String, List<Double>> testCaseWeights = ResolveTotInfoTestcaseWeight.loadFromFile();
//
//        final JsonArray result = IntStream.range(1, runResultsFromSavedFile.size() + 1)
//                .parallel()
//                .mapToObj(i -> {
//                    final String versionStr = "v" + i;
//                    final JsonObject resultRecord = new JsonObject();
//                    resultRecord.add("version", new JsonPrimitive(versionStr));
//
//                    final ArrayList<RunResultFromRunner> runResults = runResultsFromSavedFile.get(versionStr);
//
//                    final long passedCount = runResults.stream().filter(RunResultFromRunner::isCorrect).count();
//                    final ArrayList<VectorTableModelRecord> vectorTableModel = VectorTableModelGenerator
//                            .generateFromRunResultWithWeight(runResults, testCaseWeights.get(versionStr));
//
//                    // 某行代码的错误率（排序过的）
//                    final ArrayList<SuspiciousnessFactorRecord> factorOfO = SuspiciousnessFactorResolver.getSuspiciousnessFactorMatrixOrdered(
//                            vectorTableModel,
//                            record -> record.calculateSuspiciousnessFactorAsO()
//                    );
//                    final ArrayList<SuspiciousnessFactorRecord> factorOfOp = SuspiciousnessFactorResolver.getSuspiciousnessFactorMatrixOrdered(
//                            vectorTableModel,
//                            record -> record.calculateSuspiciousnessFactorAsOp()
//                    );
//
//                    resultRecord.add("factors of O", new Gson().toJsonTree(factorOfO));
//                    resultRecord.add("factors of Op", new Gson().toJsonTree(factorOfOp));
//                    return resultRecord;
//                })
//                .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
//
//        FileUtils.printJsonToFile(Paths.get(outputFilePath), result);
//    }
}
