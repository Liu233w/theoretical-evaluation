package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestSuitSubsetForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestSuitSubsetJam;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModelForStatement;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import lombok.*;
import me.tongfei.progressbar.ProgressBar;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 从一个大的测试用例中去除掉无效的测试用例
 */
@EqualsAndHashCode
@ToString
public class TestSuitSubsetResolver {

    private final SuspiciousnessFactorResolver resolver;

    public TestSuitSubsetResolver(@NonNull SuspiciousnessFactorFormula sfFormula) {
        this(sfFormula, "");
    }

    public TestSuitSubsetResolver(
        @NonNull SuspiciousnessFactorFormula sfFormula,
        String formulaTitle) {

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

        // 使用深度优先搜索划分子集
        final RecursiveResolver result = RecursiveResolver.resolve(resolver, runResultForProgram);

        final List<RunResultForTestcase> rawRunResult = runResultForProgram.getRunResults();
        final int usedCount = (int) result.getBestCount();
        final Boolean[] useItem = result.getBestSubset();

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
            runResultForProgram.getStatementMap(),
            result.getBestAveragePerformance(),
            result.getAveragePerformanceBeforeDivide(),
            toOldSetMap,
            list);
    }

    public TestSuitSubsetJam resolve(RunResultJam jam) {

        final List<RunResultForProgram> runResultForPrograms = jam.getRunResultForPrograms();

        @Cleanup final ProgressBar progressBar
            = new ProgressBar("", runResultForPrograms.size());

        final List<TestSuitSubsetForProgram> list = StreamEx
            .of(runResultForPrograms)
            .parallel()
            .map(this::resolve)
            .peek(a -> progressBar.step())
            .toImmutableList();
        return new TestSuitSubsetJam(list);
    }

    /**
     * 用来进行递归的程序，使用类来共享变量，简化代码
     */
    private static class RecursiveResolver {

        private final SuspiciousnessFactorResolver resolver;
        private final List<RunResultForTestcase> rawRunResults;
        private final int statementCount;

        @Getter
        private Boolean[] bestSubset;
        @Getter
        private double bestAveragePerformance;
        /**
         * 最好的子集中的测试用例数量
         */
        @Getter
        private long bestCount;

        private Boolean[] currentSubset;
        private double currentAveragePerformance;

        @Getter
        private final double averagePerformanceBeforeDivide;

        public static RecursiveResolver resolve(SuspiciousnessFactorResolver resolver, RunResultForProgram runResultForProgram) {
            final RecursiveResolver res = new RecursiveResolver(resolver, runResultForProgram);
            res.resolve(-1);
            return res;
        }

        // init
        private RecursiveResolver(SuspiciousnessFactorResolver resolver, RunResultForProgram runResultForProgram) {

            this.resolver = resolver;

            rawRunResults = runResultForProgram.getRunResults();
            statementCount = runResultForProgram.getStatementMap().getStatementCount();

            bestSubset = null;
            bestAveragePerformance = Double.MAX_VALUE;
            bestCount = Long.MAX_VALUE;

            currentSubset = new Boolean[rawRunResults.size()];
            Arrays.fill(currentSubset, true);

            // 必须在 currentSubset 之后初始化
            averagePerformanceBeforeDivide = resolveAveragePerformance();

            currentAveragePerformance = averagePerformanceBeforeDivide;
        }

        // recursive resolve
        private void resolve(int startIdx) {

            // 使用深度优先搜索

            for (int i = startIdx + 1; i < currentSubset.length; ++i) {

                currentSubset[i] = false;
                final double averagePerformance = resolveAveragePerformance();

                if (averagePerformance <= currentAveragePerformance) {
                    // 找到一个可以被移除的测试用例

                    final double lastAveragePerformance = currentAveragePerformance;
                    currentAveragePerformance = averagePerformance;

                    // 递归：在移除了此元素之后的计算结果
                    resolve(i);

                    // 在不移除此元素的情况下的计算结果（下一个循环）
                    currentSubset[i] = true;
                    currentAveragePerformance = lastAveragePerformance;

                } else {
                    // 不需要计算移除此元素的计算结果，直接检查下一个

                    currentSubset[i] = true;
                }
            }

            // 所有分支已经枚举完毕， currentAveragePerformance 就是当前最好的结果
            // 检查此结果是否比记录的最好结果还要好。

            final long currentCount = StreamEx.of(currentSubset).filter(a -> a).count();

            if (currentAveragePerformance < bestAveragePerformance
                || currentAveragePerformance == bestAveragePerformance && currentCount < bestCount) {
                // average performance 更小或者在相同的情况下选取的测试用例更少。

                bestAveragePerformance = currentAveragePerformance;
                bestSubset = Arrays.copyOf(currentSubset, currentSubset.length);
                bestCount = currentCount;
            }
        }

        // helper functions

        /**
         * 使用 {@link this#currentSubset} 来筛选输入，只有对应的布尔值为 true 的测试用例才会用来计算 average performance
         *
         * @return
         */
        private double resolveAveragePerformance() {
            // 这边不存在 BooleanStream，所以只能使用装箱过的布尔数组了
            // 不过 java 会缓存 true 和 false 两个对象，所以这里应该不会影响 CPU 的 cache 优化

            final StreamEx<RunResultForTestcase> stream = EntryStream
                .of(currentSubset)
                .filterValues(a -> a)
                .keys()
                .map(rawRunResults::get);
            final List<VectorTableModelForStatement> vtm =
                VectorTableModelResolver.resolve(stream, statementCount);
            return AveragePerformanceResolver.resolve(vtm, resolver);
        }
    }
}
