package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 生成测试用例的权重
 */
@EqualsAndHashCode
@ToString
public class TestcaseWeightResolver {

    private final SuspiciousnessFactorResolver resolver;

    /**
     * 设置汇报器，在 resolve(jam) 时，每生成一个 {@link TestcaseWeightForProgram} 都会用其
     * 调用汇报器。
     */
    private final Reporter reporter;

    /**
     * 设置提供器。每个 {@link TestcaseWeightForProgram} 生成之前，都会将其参数传递给这个提供器。
     * 如果提供器返回了 {@link Optional#of(Object)}，则不再进行计算，直接使用提供器的结果。
     */
    private final Provider provider;

    /**
     * 是否使用并行计算
     */
    private final boolean useParallel;

    public TestcaseWeightResolver(@NonNull SuspiciousnessFactorFormula sfFormula) {
        this(sfFormula, "", null, null, true, 0.0);
    }

    private TestcaseWeightResolver(
        @NonNull SuspiciousnessFactorFormula sfFormula,
        String formulaTitle,
        Reporter reporter,
        Provider provider,
        boolean useParallel,
        double preLimitSfRate) {

        this.reporter = reporter;
        this.provider = provider;
        this.useParallel = useParallel;

        this.resolver = SuspiciousnessFactorResolver.builder()
            .formula(sfFormula)
            .formulaTitle(formulaTitle)
            .sort(true)
            .preLimitSfRate(preLimitSfRate)
            .build();
    }

    /**
     * 从参数中生成一系列指定公式的 resolver，剩余的参数使用构造器中的参数。
     * <p>
     * 会改变此构造器的参数。
     *
     * @param map     key 为公式名， value 为公式
     * @param builder 用于提供其他参数的builder
     * @return
     */
    public static List<TestcaseWeightResolver> of(
        Map<String, SuspiciousnessFactorFormula> map,
        Builder builder) {

        return StreamEx
            .of(map.entrySet())
            .map(entry -> {
                builder.formulaTitle(entry.getKey());
                builder.sfFormula(entry.getValue());
                return builder.build();
            })
            .toImmutableList();
    }

    private static Stream<RunResultForTestcase> buildStreamSkipAt(List<RunResultForTestcase> runResults, int index) {
        return IntStream.range(0, runResults.size())
            .filter(i -> i != index)
            .mapToObj(runResults::get);
    }

    public static Builder builder() {
        return new Builder();
    }

    public SuspiciousnessFactorFormula getFormula() {
        return resolver.getFormula();
    }

    public String getFormulaTitle() {
        return resolver.getFormulaTitle();
    }

    public List<TestcaseWeightForProgram> resolve(Iterable<RunResultForProgram> runResults) {

        StreamEx<RunResultForProgram> stream = StreamEx
            .of(runResults.iterator());

        if (useParallel) {
            stream = stream.parallel();
        }

        StreamEx<TestcaseWeightForProgram> resStream;
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

        return resStream.toImmutableList();
    }

    public TestcaseWeightJam resolve(RunResultJam jam) {

        return new TestcaseWeightJam(resolve(jam.getRunResultForPrograms()));
    }

    public TestcaseWeightForProgram resolve(RunResultForProgram runResultForProgram) {

        // 1. prepare
        final List<RunResultForTestcase> runResults = runResultForProgram.getRunResults();
        final int statementCount = runResultForProgram.getStatementMap().getStatementCount();

        // 2. resolve average performance
        final double overall = AveragePerformanceResolver.resolve(
            VectorTableModelResolver.resolve(runResults.stream(), statementCount),
            resolver);

        final double[] result = new double[runResults.size()];
        for (int i = 0; i < result.length; i++) {

            final double ap = AveragePerformanceResolver.resolve(
                VectorTableModelResolver.resolve(buildStreamSkipAt(runResults, i), statementCount),
                resolver);
            result[i] = ap - overall;
        }

        // 3. normalize average performance
        final double improvedAverage = Arrays.stream(result)
            .filter(a -> a < 0)
            .reduce(0.0, Double::sum);
        final double reducedAverage = Arrays.stream(result)
            .filter(a -> a > 0)
            .reduce(0.0, Double::sum);

        for (int i = 0; i < result.length; i++) {
            final double a = result[i];
            if (a < 0) {
                result[i] = a / improvedAverage;
            } else if (a > 0) {
                result[i] = a / reducedAverage + 1;
            } else {
                result[i] = 1.0;
            }
        }

        // 4. output
        final List<TestcaseWeightForTestcase> testcaseWeights = IntStreamEx
            .range(0, result.length)
            .mapToObj(i -> new TestcaseWeightForTestcase(i, result[i]))
            .toImmutableList();
        return new TestcaseWeightForProgram(
            runResultForProgram.getProgramTitle(),
            getFormulaTitle(),
            testcaseWeights);
    }

    public interface Reporter extends Consumer<TestcaseWeightForProgram> {
    }

    public interface Provider extends BiFunction<TestcaseWeightResolver, RunResultForProgram, Optional<TestcaseWeightForProgram>> {
    }

    @ToString
    public static class Builder {
        private @NonNull SuspiciousnessFactorFormula sfFormula;
        private String formulaTitle;
        private Reporter reporter;
        private Provider provider;
        private boolean useParallel = true;
        // 取前 20% 的可疑因子
        private double preLimitSfRate = 0.2;

        Builder() {
        }

        public Builder sfFormula(@NonNull SuspiciousnessFactorFormula sfFormula) {
            this.sfFormula = sfFormula;
            return this;
        }

        public Builder formulaTitle(String formulaTitle) {
            this.formulaTitle = formulaTitle;
            return this;
        }

        public Builder reporter(Reporter reporter) {
            this.reporter = reporter;
            return this;
        }

        public Builder provider(Provider provider) {
            this.provider = provider;
            return this;
        }

        /**
         * 是否使用并行计算，默认为 true
         *
         * @param useParallel
         * @return
         */
        public Builder useParallel(boolean useParallel) {
            this.useParallel = useParallel;
            return this;
        }

        /**
         * 范围： (0,1.0]
         * 生成的可疑因子列表中，取前百分之多少
         * <p>
         * 默认为 0.2，即取前 20%
         *
         * @param preLimitSfRate
         * @return
         */
        public Builder preLimitSfRate(double preLimitSfRate) {
            this.preLimitSfRate = preLimitSfRate;
            return this;
        }

        public TestcaseWeightResolver build() {
            return new TestcaseWeightResolver(sfFormula, formulaTitle, reporter, provider, useParallel, preLimitSfRate);
        }
    }
}
