package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestCaseWeightForProgramItem;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestCaseWeightItem;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestCaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.RunResultFromRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 计算出测试用例的权重
 */
public class TestCaseWeightResolver {

    /**
     * 移除所有权重为1的结果
     *
     * @param input
     * @return
     */
    public static TestCaseWeightJam simplifyResult(TestCaseWeightJam input) {
        final List<TestCaseWeightForProgramItem> result = input.getTestCaseWeightForPrograms()
                .stream()
                .map(TestCaseWeightResolver::simplifyResult)
                .collect(Collectors.toList());
        return new TestCaseWeightJam(result);
    }

    public static TestCaseWeightForProgramItem simplifyResult(TestCaseWeightForProgramItem input) {
        final List<TestCaseWeightItem> result = input.getTestCaseWeights()
                .stream()
                .filter(item -> item.getTestCaseWeight() != 1.0)
                .collect(Collectors.toList());
        return new TestCaseWeightForProgramItem(input.getTitle(), result);
    }

    /**
     * 从程序的所有版本计算出测试用例的权重
     *
     * @param imports
     * @return
     */
    public static TestCaseWeightJam resolveFromRunResults(Map<String, ArrayList<RunResultFromRunner>> imports) {
        return resolveFromRunResults(imports, false);
    }

    /**
     * 从程序的所有版本计算出测试用例的权重
     *
     * @param imports
     * @param sort    是否对结果排序
     * @return
     */
    public static TestCaseWeightJam resolveFromRunResults(Map<String, ArrayList<RunResultFromRunner>> imports, boolean sort) {

        final int versionCount = imports.size();
        final Function<List<RunResultFromRunner>, List<TestCaseWeightItem>> resolveFunction;

        final List<TestCaseWeightForProgramItem> collect = IntStream.range(1, versionCount + 1)
                .mapToObj(i -> imports.get("v" + i))
                .parallel()
                .map(runResults -> new TestCaseWeightForProgramItem(
                        runResults.get(0).getProgram().getTitle(),
                        TestCaseWeightResolver.resolveTestCaseWeight(runResults, sort))
                )
                .collect(Collectors.toList());

        return new TestCaseWeightJam(collect);
    }

    /**
     * 从运行结果中得出测试用例的权重
     *
     * @param runResults
     * @return
     */
    public static List<TestCaseWeightItem> resolveTestCaseWeight(List<RunResultFromRunner> runResults) {
        return resolveTestCaseWeight(runResults, false);
    }

    /**
     * 从运行结果中得出测试用例的权重
     *
     * @param runResults
     * @param sort       是否按照从高到低排序
     * @return
     */
    public static List<TestCaseWeightItem> resolveTestCaseWeight(List<RunResultFromRunner> runResults, boolean sort) {

        final ArrayList<Double> testCaseWeight = doResolveTestCaseWeight(runResults);

        Stream<TestCaseWeightItem> stream = IntStream.range(0, testCaseWeight.size())
                .mapToObj(i -> new TestCaseWeightItem(i, testCaseWeight.get(i)));

        if (sort) {
            stream = stream.sorted();
        }

        return stream.collect(Collectors.toList());
    }

    /**
     * 从一系列运行结果（测试用例）中计算出测试用例的权重
     *
     * @param runResults
     * @return
     */
    private static ArrayList<Double> doResolveTestCaseWeight(List<RunResultFromRunner> runResults) {

        final int statementCount = runResults.get(0).getStatementMap().getStatementCount();
        final double overall = resolveAveragePerformance(runResults.stream(), statementCount);

        final ArrayList<Double> result = new ArrayList<>(runResults.size());

        for (int i = 0; i < runResults.size(); i++) {
            final Stream<RunResultFromRunner> skippedStream = ofStreamSkipAtIndex(runResults, i);
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
    private static Stream<RunResultFromRunner> ofStreamSkipAtIndex(List<RunResultFromRunner> runResults, int index) {
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
    private static double resolveAveragePerformance(Stream<RunResultFromRunner> testCases, int statementCount) {
//        final ArrayList<VectorTableModelRecord> vtm = VectorTableModelGenerator.generateFromStream(testCases, statementCount);
//        final OrderedVectorTableModel orderedVtm = OrderedVectorTableModel.fromVectorTableModel(vtm);
//        return AveragePerformanceResolver.resolveAveragePerformance(orderedVtm);
        throw new RuntimeException("TODO");
    }
}
