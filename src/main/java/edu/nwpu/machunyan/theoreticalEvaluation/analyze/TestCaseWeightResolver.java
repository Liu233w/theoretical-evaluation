package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.SingleRunResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 计算出测试用例的权重
 */
public class TestCaseWeightResolver {

    /**
     * 从一系列运行结果（测试用例）中计算出测试用例的权重
     *
     * @param runResults
     * @return
     */
    public static ArrayList<Double> resolveTestCaseWeight(List<SingleRunResult> runResults) {

        final int statementCount = runResults.get(0).getStatementMap().getStatementCount();
        final double overall = resolveAveragePerformance(runResults.stream(), statementCount);

        final ArrayList<Double> result = new ArrayList<>(runResults.size());

        for (int i = 0; i < runResults.size(); i++) {
            final Stream<SingleRunResult> skippedStream = ofStreamSkipAtIndex(runResults, i);
            final double averagePerformance = resolveAveragePerformance(skippedStream, statementCount);
            result.add(averagePerformance - overall);
        }

        final Double improvedAverage = result.stream().filter(a -> a < 0).reduce(0.0, Double::sum);
        final Double reducedAverage = result.stream().filter(a -> a > 0).reduce(0.0, Double::sum);

        for (int i = 0; i < result.size(); i++) {
            final double a = result.get(i);
            if (a < 0) {
                result.set(i, a / improvedAverage);
            } else if (a > 0) {
                result.set(i, a / reducedAverage + 1);
            } else {
                result.set(i, 1.0);
            }
        }

        return result;
    }

    /**
     * 从测试用例中生成一个流，跳过特定某个位置的测试用例。
     *
     * @param runResults
     * @param index
     * @return
     */
    private static Stream<SingleRunResult> ofStreamSkipAtIndex(List<SingleRunResult> runResults, int index) {
        return IntStream.range(0, runResults.size())
                .filter(i -> i != index)
                .mapToObj(runResults::get);
    }

    /**
     * 从测试用例运行结果计算平均性能
     *
     * @param testCases
     * @param statementCount 语句数量
     * @return
     */
    private static double resolveAveragePerformance(Stream<SingleRunResult> testCases, int statementCount) {
        final ArrayList<VectorTableModelRecord> vtm = VectorTableModelGenerator.generateFromStream(testCases, statementCount);
        final OrderedVectorTableModel orderedVtm = OrderedVectorTableModel.fromVectorTableModel(vtm);
        return AveragePerformanceResolver.resolveAveragePerformance(orderedVtm);
    }
}
