package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 表示 vector table model 中的一行
 */
@EqualsAndHashCode
@ToString
public final class VectorTableModelForStatement {

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
    public VectorTableModelForStatement(int statementIndex, int anf, int anp, int aef, int aep, double weightedAnf, double weightedAnp, double weightedAef, double weightedAep) {
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
    public VectorTableModelForStatement(int statementIndex, int anf, int anp, int aef, int aep) {
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

    /**
     * 用于生成 {@link VectorTableModelForStatement}
     */
    public static class Builder {

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

        public Builder(int statementIndex) {
            this(statementIndex, false);
        }

        public Builder(int statementIndex, boolean useWeight) {
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
         * 获取构建好的 {@link VectorTableModelForStatement}
         *
         * @return
         */
        public VectorTableModelForStatement build() {
            if (useWeight) {
                return new VectorTableModelForStatement(statementIndex, anf, anp, aef, aep, weightedAnf, weightedAnp, weightedAef, weightedAep);
            }
            return new VectorTableModelForStatement(statementIndex, anf, anp, aef, aep);
        }
    }
}
