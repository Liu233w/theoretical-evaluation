package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class VectorTableModel {
    /**
     * 用于生成 {@link Pojo.ForStatement}
     */
    public static class VectorTableModelForStatementBuilder {

        /**
         * 语句的序号
         */
        private final int statementIndex;

        // 4 个数据
        /**
         * 是否使用权重
         */
        private final boolean useWeight;
        private int anf;
        private int anp;
        private int aef;
        private int aep;
        private double weightedAnf;
        private double weightedAnp;
        private double weightedAef;
        private double weightedAep;

        public VectorTableModelForStatementBuilder(int statementIndex) {
            this(statementIndex, false);
        }

        public VectorTableModelForStatementBuilder(int statementIndex, boolean useWeight) {
            this.statementIndex = statementIndex;
            this.useWeight = useWeight;
        }

        /**
         * 根据一次运行结果将 4 个数值中的一个递增
         *
         * @param runResultForTestcase
         */
        public void processRunResultForTestcase(RunResultForTestcase runResultForTestcase) {
            processRunResultForTestcase(runResultForTestcase, 1.0);
        }

        /**
         * 根据一次运行结果将 4 个数值中的一个递增
         *
         * @param runResultForTestcase
         * @param testcaseWeight
         */
        public void processRunResultForTestcase(RunResultForTestcase runResultForTestcase, double testcaseWeight) {

            final boolean correct = runResultForTestcase.isCorrect();
            final boolean hit = runResultForTestcase.getCoverage()
                .getCoverageForStatement(statementIndex) > 0;

            if (correct) {

                if (hit) {
                    ++aep;
                    weightedAep += testcaseWeight;
                } else {
                    ++anp;
                    weightedAnp += testcaseWeight;
                }
            } else {

                if (hit) {
                    ++aef;
                    weightedAef += testcaseWeight;
                } else {
                    ++anf;
                    weightedAnf += testcaseWeight;
                }
            }
        }

        /**
         * 获取构建好的 {@link VectorTableModel.Pojo.ForStatement}
         *
         * @return
         */
        public Pojo.ForStatement build() {
            if (useWeight) {
                return new Pojo.ForStatement(statementIndex, anf, anp, aef, aep, weightedAnf, weightedAnp, weightedAef, weightedAep);
            }
            return new Pojo.ForStatement(statementIndex, anf, anp, aef, aep);
        }
    }

    /**
     * 生成 {@link Pojo.VectorTableModelForProgram}
     */
    public static class VectorTableModelResolver {

        /**
         * 从 runResults 的流中构建 vtm
         *
         * @param runResult
         * @param statementCount
         * @return
         */
        public static List<Pojo.ForStatement> resolve(
            Stream<RunResultForTestcase> runResult,
            int statementCount) {

            final List<VectorTableModelForStatementBuilder> builders = IntStream.range(0, statementCount)
                .mapToObj(i -> new VectorTableModelForStatementBuilder(i + 1))
                .collect(Collectors.toList());

            runResult.forEach(runResultItem ->
                builders.forEach(builder ->
                    builder.processRunResultForTestcase(runResultItem)));

            return buildVtm(builders);
        }

        /**
         * 从一个程序的运行结果中生成 vtm，使用权重
         *
         * @param runResultForProgram
         * @param testcaseWeights     测试用例的权重，和运行结果一一对应
         * @return
         */
        public static Pojo.VectorTableModelForProgram resolveWithWeights(
            RunResultForProgram runResultForProgram,
            List<TestcaseWeight.Pojo.ForTestcase> testcaseWeights) {

            if (runResultForProgram.getRunResults().size() != testcaseWeights.size()) {
                throw new IllegalArgumentException("运行结果和权重必须一一对应（一个 RunResultForTestcase 对应一个 testcaseWeight）");
            }

            final int statementCount = runResultForProgram.getStatementMap().getStatementCount();
            final List<VectorTableModelForStatementBuilder> builders = IntStream.range(0, statementCount)
                .mapToObj(i -> new VectorTableModelForStatementBuilder(i + 1, true))
                .collect(Collectors.toList());

            final List<RunResultForTestcase> runResults = runResultForProgram.getRunResults();
            for (int i = 0; i < runResults.size(); i++) {
                for (VectorTableModelForStatementBuilder builder : builders) {
                    builder.processRunResultForTestcase(runResults.get(i), testcaseWeights.get(i).getTestcaseWeight());
                }
            }

            return new Pojo.VectorTableModelForProgram(runResultForProgram.getProgramTitle(), buildVtm(builders));
        }

        /**
         * 从 {@link RunResultForProgram} 批量生成
         *
         * @param runResultForProgram
         * @return
         */
        public static Pojo.VectorTableModelForProgram resolve(RunResultForProgram runResultForProgram) {
            final int statementCount = runResultForProgram.getStatementMap().getStatementCount();
            final Stream<RunResultForTestcase> stream = runResultForProgram.getRunResults().stream();
            final List<Pojo.ForStatement> vectorTableModelForStatements = resolve(stream, statementCount);
            return new Pojo.VectorTableModelForProgram(runResultForProgram.getProgramTitle(), vectorTableModelForStatements);
        }

        /**
         * 从 programResultJam 批量生成
         *
         * @param runResultJam
         * @return
         */
        public static Pojo.VectorTableModelJam resolve(RunResultJam runResultJam) {
            final List<Pojo.VectorTableModelForProgram> vectorTableModelForPrograms = runResultJam
                .getRunResultForPrograms().stream()
                .map(VectorTableModelResolver::resolve)
                .collect(Collectors.toList());
            return new Pojo.VectorTableModelJam(vectorTableModelForPrograms);
        }

        public static Pojo.VectorTableModelJam resolveWithWeights(
            RunResultJam runResultJam,
            TestcaseWeight.Pojo.Jam testcaseWeightJam) {

            final Map<String, List<TestcaseWeight.Pojo.ForTestcase>> titleToWeights = testcaseWeightJam
                .getForPrograms()
                .stream()
                .collect(Collectors.toMap(
                    TestcaseWeight.Pojo.ForProgram::getTitle,
                    TestcaseWeight.Pojo.ForProgram::getTestcaseWeights
                ));

            final List<Pojo.VectorTableModelForProgram> collect = runResultJam
                .getRunResultForPrograms()
                .stream()
                .map(a -> resolveWithWeights(a, titleToWeights.get(a.getProgramTitle())))
                .collect(Collectors.toList());

            return new Pojo.VectorTableModelJam(collect);
        }

        private static List<Pojo.ForStatement> buildVtm(List<VectorTableModelForStatementBuilder> builders) {
            final ArrayList<Pojo.ForStatement> result = new ArrayList<>(builders.size() + 1);
            result.add(null);
            for (VectorTableModelForStatementBuilder builder :
                builders) {
                result.add(builder.build());
            }
            return result;
        }
    }

    public static class Pojo {

        /**
         * 表示 vector table model 中的一行
         */
        @EqualsAndHashCode
        @ToString
        public static class ForStatement implements Comparable {

            /**
             * 语句的序号
             */
            @Getter
            private final int statementIndex;

            // 4 个数据

            private final int anf;

            private final int anp;

            private final int aef;

            private final int aep;

            private final double weightedAnf;

            private final double weightedAnp;

            private final double weightedAef;

            private final double weightedAep;

            @Getter
            private final boolean useWeight;

            /**
             * 使用四个数值和加权之后的四个数值初始化
             *
             * @param statementIndex
             * @param anf
             * @param anp
             * @param aef
             * @param aep
             * @param weightedAnf
             * @param weightedAnp
             * @param weightedAef
             * @param weightedAep
             */
            public ForStatement(int statementIndex, int anf, int anp, int aef, int aep, double weightedAnf, double weightedAnp, double weightedAef, double weightedAep) {
                this.useWeight = true;

                this.statementIndex = statementIndex;
                this.anf = anf;
                this.anp = anp;
                this.aef = aef;
                this.aep = aep;
                this.weightedAnf = weightedAnf;
                this.weightedAnp = weightedAnp;
                this.weightedAef = weightedAef;
                this.weightedAep = weightedAep;
            }

            /**
             * 使用四个数值初始化。不采用加权。
             *
             * @param statementIndex
             * @param anf
             * @param anp
             * @param aef
             * @param aep
             */
            public ForStatement(int statementIndex, int anf, int anp, int aef, int aep) {
                this.useWeight = false;

                this.statementIndex = statementIndex;
                this.anf = anf;
                this.anp = anp;
                this.aef = aef;
                this.aep = aep;

                this.weightedAnf = 0.0;
                this.weightedAnp = 0.0;
                this.weightedAef = 0.0;
                this.weightedAep = 0.0;
            }

            public double getAnf() {
                if (useWeight) {
                    return weightedAnf;
                } else {
                    return anf;
                }
            }

            public double getAnp() {
                if (useWeight) {
                    return weightedAnp;
                } else {
                    return anp;
                }
            }

            public double getAef() {
                if (useWeight) {
                    return weightedAef;
                } else {
                    return aef;
                }
            }

            public double getAep() {
                if (useWeight) {
                    return weightedAep;
                } else {
                    return aep;
                }
            }

            /**
             * 获取未加权的对应数值
             *
             * @return
             */
            public int getUnWeightedAnf() {
                return anf;
            }

            /**
             * 获取未加权的对应数值
             *
             * @return
             */
            public int getUnWeightedAnp() {
                return anp;
            }

            /**
             * 获取未加权的对应数值
             *
             * @return
             */
            public int getUnWeightedAef() {
                return aef;
            }

            /**
             * 获取未加权的对应数值
             *
             * @return
             */
            public int getUnWeightedAep() {
                return aep;
            }

            @Override
            public int compareTo(@NotNull Object o) {
                if (!(o instanceof ForStatement)) {
                    throw new IllegalArgumentException("VectorTableModel.Pojo.ForStatement can only be compared to VectorTableModel.Pojo.ForStatement");
                }
                ForStatement that = (ForStatement) o;

                if (this.useWeight != that.useWeight) {
                    throw new IllegalArgumentException("Cannot compare a weighted record with an un-weighted record.");
                }

                if (anf != that.getAnf()) {
                    return Double.compare(anf, that.getAnf());
                }
                if (anp != that.getAnp()) {
                    return Double.compare(anp, that.getAnp());
                }
                if (aef != that.getAef()) {
                    return Double.compare(aef, that.getAef());
                }
                return Double.compare(aep, that.getAep());
            }
        }

        /**
         * 一个 vtm
         */
        @Value
        public static class VectorTableModelForProgram {

            String programTitle;

            /**
             * 第一个元素为 null，剩下的元素每一个和 vtm 中的一行一一对应
             */
            List<ForStatement> records;
        }

        @Value
        public static class VectorTableModelJam {

            List<VectorTableModelForProgram> vectorTableModelForPrograms;
        }
    }
}
