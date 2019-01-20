package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModelForStatement;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 生成测试用例的权重
 */
@EqualsAndHashCode
@ToString
public class TestcaseWeightResolver {

    private final SuspiciousnessFactorResolver resolver;

    public TestcaseWeightResolver(@NonNull Function<VectorTableModelForStatement, Double> sfFormula) {
        this(sfFormula, "");
    }

    public TestcaseWeightResolver(
        @NonNull Function<VectorTableModelForStatement, Double> sfFormula,
        String formulaTitle) {

        this.resolver = SuspiciousnessFactorResolver.builder()
            .formula(sfFormula)
            .formulaTitle(formulaTitle)
            .sort(true)
            .build();
    }

    /**
     * 从参数中生成一系列指定公式的 resolver
     *
     * @param map key 为公式名， value 为公式
     * @return
     */
    public static List<TestcaseWeightResolver> of(
        Map<String, Function<VectorTableModelForStatement, Double>> map) {

        return map.entrySet().stream()
            .map(entry -> new TestcaseWeightResolver(entry.getValue(), entry.getKey()))
            .collect(Collectors.toList());
    }

    private static Stream<RunResultForTestcase> buildStreamSkipAt(List<RunResultForTestcase> runResults, int index) {
        return IntStream.range(0, runResults.size())
            .filter(i -> i != index)
            .mapToObj(runResults::get);
    }

    public Function<VectorTableModelForStatement, Double> getFormula() {
        return resolver.getFormula();
    }

    public String getFormulaTitle() {
        return resolver.getFormulaTitle();
    }

    public TestcaseWeightJam resolve(RunResultJam jam) {
        final List<TestcaseWeightForProgram> collect = jam.getRunResultForPrograms().stream()
            .parallel()
            .map(this::resolve)
            .collect(Collectors.toList());
        return new TestcaseWeightJam(collect);
    }

    public TestcaseWeightForProgram resolve(RunResultForProgram runResultForProgram) {

        // 1. prepare
        final List<RunResultForTestcase> runResults = runResultForProgram.getRunResults();
        final int statementCount = runResultForProgram.getStatementMap().getStatementCount();

        // 2. resolve average performance
        final double overall = AveragePerformanceResolver.resolve(
            VectorTableModelResolver.resolve(runResults.stream(), statementCount),
            resolver);

        final double[] result = IntStream.range(0, runResults.size())
            .mapToObj(i -> buildStreamSkipAt(runResults, i))
            .map(stream -> VectorTableModelResolver.resolve(stream, statementCount))
            .map(vtm -> AveragePerformanceResolver.resolve(vtm, resolver))
            .mapToDouble(averagePerformance -> averagePerformance - overall)
            .toArray();

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
        final List<TestcaseWeightForTestcase> testcaseWeights = IntStream
            .range(0, result.length)
            .mapToObj(i -> new TestcaseWeightForTestcase(i, result[i]))
            .collect(Collectors.toList());
        return new TestcaseWeightForProgram(
            runResultForProgram.getProgramTitle(),
            getFormulaTitle(),
            testcaseWeights);
    }
}
