package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestSuitSubsetForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestSuitSubsetJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
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

        final RunResultForTestcase[] rawRunResult = runResultForProgram.getRunResults();

        final boolean[] useItem = new boolean[rawRunResult.length];
        Arrays.fill(useItem, true);

        final double averagePerformanceBeforeDivide =
            resolveAveragePerformance(runResultForProgram, useItem);

        double currentAveragePerformance = averagePerformanceBeforeDivide;

        // 使用贪心算法划分子集
        int lastRemovedIndex = -1;
        while (true) {

            boolean findOne = false;

            for (int i = lastRemovedIndex + 1; i < useItem.length; ++i) {

                useItem[i] = false;
                final double averagePerformance =
                    resolveAveragePerformance(runResultForProgram, useItem);

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

        int usedCount = 0;
        for (boolean b : useItem) {
            if (b) {
                ++usedCount;
            }
        }

        final int[] toOldSetMap = new int[usedCount];
        final RunResultForTestcase[] res = new RunResultForTestcase[usedCount];

        int tail = 0;
        for (int i = 0; i < useItem.length; ++i) {
            if (useItem[i]) {
                toOldSetMap[tail] = i;
                res[tail] = rawRunResult[i];
                ++tail;
            }
        }

        return new TestSuitSubsetForProgram(
            runResultForProgram.getProgramTitle(),
            runResultForProgram.getStatementMap(),
            currentAveragePerformance,
            averagePerformanceBeforeDivide,
            toOldSetMap,
            res);
    }

    public TestSuitSubsetJam resolve(RunResultJam jam) {
        final List<TestSuitSubsetForProgram> list = StreamEx
            .of(jam.getRunResultForPrograms())
            .parallel()
            .map(this::resolve)
            .toImmutableList();
        return new TestSuitSubsetJam(list);
    }

    private double resolveAveragePerformance(
        RunResultForProgram runResults,
        boolean[] useItem) {

        return AveragePerformanceResolver.resolve(
            VectorTableModelResolver.resolveSkipBy(runResults, useItem).getRecords(),
            resolver);
    }
}
