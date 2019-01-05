package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

public class VectorTableModelGenerator {

//    /**
//     * 从多个运行结果中得出 vector table model。每个运行结果的 statementMap 必须一致。
//     *
//     * @param runResults
//     * @return vector table model，对应一个矩阵。第 0 个元素为 null，从第一个元素开始每个元素表示一行。
//     */
//    public static ArrayList<VectorTableModelRecord> generateFromRunResult(List<RunResultFromRunner> runResults) {
//        return generateFromStream(runResults.stream(), runResults.get(0).getStatementMap().getStatementCount());
//    }
//
//    /**
//     * 从运行结果和权重中得出加权之后的 vector table model。运行结果和权重必须一一对应。每个运行结果的 statementMap 必须一致。
//     *
//     * @param runResults
//     * @param weights
//     * @return
//     */
//    public static ArrayList<VectorTableModelRecord> generateFromRunResultWithWeight(List<RunResultFromRunner> runResults, List<Double> weights) {
//
//        if (runResults.size() != weights.size()) {
//            throw new IllegalArgumentException("runResults 必须和 weights 一一对应。");
//        }
//
//        final int statementCount = runResults.get(0).getStatementMap().getStatementCount();
//        final ArrayList<VectorTableModelRecordBuilder> builders = new ArrayList<>(statementCount);
//        for (int i = 0; i < statementCount; i++) {
//            builders.add(new VectorTableModelRecordBuilder(i + 1, true));
//        }
//
//        for (int i = 0; i < runResults.size(); i++) {
//            final RunResultFromRunner runResult = runResults.get(i);
//            final double weight = weights.get(i);
//            for (int j = 0; j < statementCount; j++) {
//                builders.get(j).processSingleRunResult(runResult, weight);
//            }
//        }
//
//        return collectVectorTableModelFromBuilder(statementCount, builders);
//    }
//
//    /**
//     * 从一个表示运行结果的流中得出 vector table model。每个运行结果的 statementMap 必须一致。
//     *
//     * @param stream
//     * @param statementCount 总体的语句数量
//     * @return vector table model，对应一个矩阵。第 0 个元素为 null，从第一个元素开始每个元素表示一行。
//     */
//    public static ArrayList<VectorTableModelRecord> generateFromStream(Stream<RunResultFromRunner> stream, int statementCount) {
//
//        final ArrayList<VectorTableModelRecordBuilder> builders = new ArrayList<>(statementCount);
//
//        for (int i = 0; i < statementCount; i++) {
//            builders.add(new VectorTableModelRecordBuilder(i + 1));
//        }
//
//        stream.forEach(singleRunResult -> {
//            for (int i = 0; i < statementCount; i++) {
//                builders.get(i).processSingleRunResult(singleRunResult);
//            }
//        });
//
//        return collectVectorTableModelFromBuilder(statementCount, builders);
//    }
//
//    @NotNull
//    private static ArrayList<VectorTableModelRecord> collectVectorTableModelFromBuilder(int statementCount, ArrayList<VectorTableModelRecordBuilder> builders) {
//
//        final ArrayList<VectorTableModelRecord> result = new ArrayList<>(statementCount + 1);
//        result.add(null);
//        for (VectorTableModelRecordBuilder builder :
//                builders) {
//            result.add(builder.build());
//        }
//        return result;
//    }
}
