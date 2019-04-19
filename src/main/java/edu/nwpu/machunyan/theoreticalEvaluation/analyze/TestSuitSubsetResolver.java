package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestSuitSubsetForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestSuitSubsetJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * 从一个大的测试用例中去除掉无效的测试用例
 */
@EqualsAndHashCode
@ToString
public class TestSuitSubsetResolver {

    private final SuspiciousnessFactorResolver resolver;

    /**
     * 设置汇报器，在 resolve(jam) 时，每生成一个 {@link TestSuitSubsetForProgram} 都会用其
     * 调用汇报器。
     */
    private final Reporter reporter;

    /**
     * 设置提供器。每个 {@link TestSuitSubsetForProgram} 生成之前，都会将其参数传递给这个提供器。
     * 如果提供器返回了 {@link Optional#of(Object)}，则不再进行计算，直接使用提供器的结果。
     */
    private final Provider provider;

    /**
     * 是否使用并行计算
     */
    private final boolean useParallel;

    public TestSuitSubsetResolver(@NonNull SuspiciousnessFactorFormula sfFormula) {
        this(sfFormula, "", null, null, true);
    }

    @Builder
    public TestSuitSubsetResolver(
        @NonNull SuspiciousnessFactorFormula sfFormula,
        String formulaTitle, Reporter reporter, Provider provider, boolean useParallel) {

        this.reporter = reporter;
        this.provider = provider;
        this.useParallel = useParallel;

        this.resolver = SuspiciousnessFactorResolver.builder()
            .formula(sfFormula)
            .formulaTitle(formulaTitle)
            .sort(true)
            .build();
    }

    public SuspiciousnessFactorFormula getFormula() {
        return resolver.getFormula();
    }

    public String getFormulaTitle() {
        return resolver.getFormulaTitle();
    }

    public TestSuitSubsetForProgram resolve(
        RunResultForProgram runResultForProgram) {

        final int statementCount = runResultForProgram.getStatementMap().getStatementCount();

        final List<RunResultForTestcase> rawRunResult = runResultForProgram.getRunResults();

        final Boolean[] useItem = new Boolean[rawRunResult.size()];
        Arrays.fill(useItem, true);

        final double averagePerformanceBeforeDivide =
            resolveAveragePerformance(
                buildStreamSkipBy(rawRunResult, useItem),
                statementCount);

        double currentAveragePerformance = averagePerformanceBeforeDivide;

        // 使用贪心算法划分子集
        int lastRemovedIndex = -1;
        while (true) {

            boolean findOne = false;

            for (int i = lastRemovedIndex + 1; i < useItem.length; ++i) {

                useItem[i] = false;
                final double averagePerformance =
                    resolveAveragePerformance(
                        buildStreamSkipBy(rawRunResult, useItem),
                        statementCount);

                if (averagePerformance <= currentAveragePerformance) {
                    // 找到一个可以被移除的测试用例
                    currentAveragePerformance = averagePerformance;
                    lastRemovedIndex = i;
                    findOne = true;
                    break;
                } else {
                    // 这个测试用例不能被移除，检查下一个
                    useItem[i] = true;
                }
            }

            if (!findOne) {
                // 没有能被移除的测试用例了，贪心结束
                break;
            }
        }

        final int usedCount = (int) Arrays.stream(useItem)
            .filter(a -> a)
            .count();
        final int[] toOldSetMap = new int[usedCount];
        final RunResultForTestcase[] res = new RunResultForTestcase[usedCount];

        int tail = 0;
        for (int i = 0; i < useItem.length; ++i) {
            if (useItem[i]) {
                toOldSetMap[tail] = i;
                res[tail] = rawRunResult.get(i);
                ++tail;
            }
        }

        final List<RunResultForTestcase> list = Collections.unmodifiableList(Arrays.asList(res));
        return new TestSuitSubsetForProgram(
            runResultForProgram.getProgramTitle(),
            currentAveragePerformance,
            averagePerformanceBeforeDivide,
            toOldSetMap);
    }

    public TestSuitSubsetJam resolve(RunResultJam jam) {
        StreamEx<RunResultForProgram> stream = StreamEx
            .of(jam.getRunResultForPrograms());

        if (useParallel) {
            stream = stream
                .parallel();
        }

        StreamEx<TestSuitSubsetForProgram> resStream;
        if (provider != null) {
            resStream = stream.map(item -> provider
                .apply(this, item)
                .orElseGet(() -> this.resolve(item))
            );
        } else {
            resStream = stream.map(this::resolve);
        }

        if (reporter != null) {
            resStream = resStream.peek(reporter);
        }

        final List<TestSuitSubsetForProgram> collect = resStream
            .toImmutableList();
        return new TestSuitSubsetJam(collect);
    }

    private double resolveAveragePerformance(
        StreamEx<RunResultForTestcase> stream,
        int statementCount) {

        return AveragePerformanceResolver.resolve(
            VectorTableModelResolver.resolve(stream, statementCount),
            resolver);
    }

    /**
     * 使用一个布尔数组来筛选输入，只有对应的布尔值为 true 的测试用例才会出现在结果的流里
     *
     * @param runResults
     * @param useItem    是否使用下标对应的那个测试用例
     * @return
     */
    private static StreamEx<RunResultForTestcase> buildStreamSkipBy(
        List<RunResultForTestcase> runResults,
        Boolean[] useItem) {
        // 这边不存在 BooleanStream，所以只能使用装箱过的布尔数组了
        // 不过 java 会缓存 true 和 false 两个对象，所以这里应该不会影响 CPU 的 cache 优化

        if (useItem.length != runResults.size()) {
            throw new IllegalArgumentException("useItem 必须和 runResults 一一对应");
        }

        return EntryStream
            .of(useItem)
            .filterValues(a -> a)
            .keys()
            .map(runResults::get);
    }

    public interface Reporter extends Consumer<TestSuitSubsetForProgram> {
    }

    public interface Provider extends BiFunction<TestSuitSubsetResolver, RunResultForProgram, Optional<TestSuitSubsetForProgram>> {
    }
}
