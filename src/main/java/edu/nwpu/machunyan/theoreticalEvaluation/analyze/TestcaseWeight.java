package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import lombok.*;
import me.tongfei.progressbar.ProgressBar;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TestcaseWeight {


    public static class Helper {

        /**
         * 简化权重，将结果为 1.0 的权重删除
         *
         * @param input
         * @return
         */
        public static List<Pojo.ForTestcase> simplifyTestcaseWeights(List<Pojo.ForTestcase> input) {
            return input.stream()
                .filter(a -> a.getTestcaseWeight() != 1.0)
                .collect(Collectors.toList());
        }

        /**
         * 简化权重，将结果为 1.0 的权重删除
         *
         * @param input
         * @return
         */
        public static Pojo.ForProgram simplifyTestcaseWeights(Pojo.ForProgram input) {
            return new Pojo.ForProgram(
                input.getTitle(),
                input.getFormulaTitle(),
                simplifyTestcaseWeights(input.getTestcaseWeights())
            );
        }

        /**
         * 简化权重，将结果为 1.0 的权重删除
         *
         * @param input
         * @return
         */
        public static Pojo.Jam simplifyTestcaseWeights(Pojo.Jam input) {
            final List<Pojo.ForProgram> collect = input.getForPrograms().stream()
                .map(Helper::simplifyTestcaseWeights)
                .collect(Collectors.toList());
            return new Pojo.Jam(collect);
        }

        /**
         * 在 run result 上运行每个 resolver，将结果收集到一起
         */
        public static Pojo.Jam runOnAllResolvers(
            RunResultJam jam,
            List<Resolver> resolvers) {

            @Cleanup final ProgressBar progressBar = new ProgressBar("", resolvers.size());

            final List<Pojo.ForProgram> collect = resolvers.stream()
                .parallel()
                .map(resolver -> resolver.resolve(jam))
                .map(Pojo.Jam::getForPrograms)
                .peek(a -> progressBar.step())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
            return new Pojo.Jam(collect);
        }
    }

    /**
     * 生成测试用例的权重
     */
    @EqualsAndHashCode
    @ToString
    public static class Resolver {

        private final SuspiciousnessFactorResolver resolver;

        public Resolver(@NonNull Function<VectorTableModel.Pojo.ForStatement, Double> sfFormula) {
            this(sfFormula, "");
        }

        public Resolver(
            @NonNull Function<VectorTableModel.Pojo.ForStatement, Double> sfFormula,
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
        public static List<Resolver> of(
            Map<String, Function<VectorTableModel.Pojo.ForStatement, Double>> map) {

            return map.entrySet().stream()
                .map(entry -> new Resolver(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());
        }

        private static Stream<RunResultForTestcase> buildStreamSkipAt(List<RunResultForTestcase> runResults, int index) {
            return IntStream.range(0, runResults.size())
                .filter(i -> i != index)
                .mapToObj(runResults::get);
        }

        public Function<VectorTableModel.Pojo.ForStatement, Double> getFormula() {
            return resolver.getFormula();
        }

        public String getFormulaTitle() {
            return resolver.getFormulaTitle();
        }

        public Pojo.Jam resolve(RunResultJam jam) {
            final List<Pojo.ForProgram> collect = jam.getRunResultForPrograms().stream()
                .parallel()
                .map(this::resolve)
                .collect(Collectors.toList());
            return new Pojo.Jam(collect);
        }

        public Pojo.ForProgram resolve(RunResultForProgram runResultForProgram) {

            // 1. prepare
            final List<RunResultForTestcase> runResults = runResultForProgram.getRunResults();
            final int statementCount = runResultForProgram.getStatementMap().getStatementCount();

            // 2. resolve average performance
            final double overall = AveragePerformanceResolver.resolve(
                VectorTableModel.VectorTableModelResolver.resolve(runResults.stream(), statementCount),
                resolver);

            final double[] result = IntStream.range(0, runResults.size())
                .mapToObj(i -> buildStreamSkipAt(runResults, i))
                .map(stream -> VectorTableModel.VectorTableModelResolver.resolve(stream, statementCount))
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
            final List<Pojo.ForTestcase> testcaseWeights = IntStream
                .range(0, result.length)
                .mapToObj(i -> new Pojo.ForTestcase(i, result[i]))
                .collect(Collectors.toList());
            return new Pojo.ForProgram(
                runResultForProgram.getProgramTitle(),
                getFormulaTitle(),
                testcaseWeights);
        }
    }

    /**
     * 将测试用例的权重扩大，使结果更加明显。
     * 不会改变现有的数值，而是产生新的数值。
     */
    public static class Multiplier {

        /**
         * @param input
         * @param multiply 扩大的倍数
         * @return
         */
        public static Pojo.ForTestcase resolve(Pojo.ForTestcase input, double multiply) {
            final double testcaseWeight = input.getTestcaseWeight();
            if (testcaseWeight > 1.0) {
                return new Pojo.ForTestcase(
                    input.getTestcaseIndex(),
                    testcaseWeight * multiply
                );
            } else if (testcaseWeight < 1.0) {
                return new Pojo.ForTestcase(
                    input.getTestcaseIndex(),
                    testcaseWeight * multiply
                );
            } else {
                return input;
            }
        }

        public static Pojo.ForProgram resolve(Pojo.ForProgram input, double multiply) {
            final List<Pojo.ForTestcase> collect = input
                .getTestcaseWeights()
                .stream()
                .map(a -> resolve(a, multiply))
                .collect(Collectors.toList());
            return new Pojo.ForProgram(
                input.getTitle(),
                input.getFormulaTitle(),
                collect
            );
        }

        public static Pojo.Jam resolve(Pojo.Jam input, double multiply) {
            final List<Pojo.ForProgram> collect = input
                .getForPrograms()
                .stream()
                .map(a -> resolve(a, multiply))
                .collect(Collectors.toList());
            return new Pojo.Jam(collect);
        }
    }

    public static class Pojo {

        /**
         * 表示一个程序的title和测试用例
         */
        @Value
        public static class ForProgram {

            String title;

            String formulaTitle;

            List<ForTestcase> testcaseWeights;
        }

        /**
         * 一个测试用例的权重
         */
        @Value
        public static class ForTestcase {

            /**
             * 测试用例序号
             */
            int testcaseIndex;

            /**
             * 测试用例权重
             */
            double testcaseWeight;
        }

        /**
         * 一个程序的测试用例权重所需的所有数据
         */
        @Value
        public static class Jam {

            List<ForProgram> forPrograms;
        }
    }

}
