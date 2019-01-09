package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgramItem;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.ProgramRunResult;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.ProgramRunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 生成测试用例的权重
 */
@Data
@AllArgsConstructor
@Builder
public class TestcaseWeightResolver {

    @NonNull
    private Function<VectorTableModelRecord, Double> sfFormula;

    @Builder.Default
    private String formulaTitle = "";

    public TestcaseWeightResolver(@NonNull Function<VectorTableModelRecord, Double> sfFormula) {
        this.sfFormula = sfFormula;
    }

    public TestcaseWeightJam resolve(ProgramRunResultJam jam) {
        final List<TestcaseWeightForProgramItem> collect = jam.getProgramRunResults().stream()
            .map(this::resolve)
            .collect(Collectors.toList());
        return new TestcaseWeightJam(collect);
    }

    public TestcaseWeightForProgramItem resolve(ProgramRunResult programRunResult) {

        // 1. prepare
        final List<RunResultForTestcase> runResults = programRunResult.getRunResults();
        final int statementCount = programRunResult.getStatementMap().getStatementCount();

        // 2. resolve average performance
        final double overall = AveragePerformanceResolver.resolve(
            VectorTableModelResolver.resolve(runResults.stream(), statementCount),
            sfFormula);

        final double[] result = IntStream.range(0, runResults.size())
            .parallel()
            .mapToObj(i -> buildStreamSkipAt(runResults, i))
            .map(stream -> VectorTableModelResolver.resolve(stream, statementCount))
            .map(vtm -> AveragePerformanceResolver.resolve(vtm, sfFormula))
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
        return new TestcaseWeightForProgramItem(
            programRunResult.getProgramTitle(),
            formulaTitle,
            testcaseWeights);
    }

    private static Stream<RunResultForTestcase> buildStreamSkipAt(List<RunResultForTestcase> runResults, int index) {
        return IntStream.range(0, runResults.size())
            .filter(i -> i != index)
            .mapToObj(runResults::get);
    }
}
