package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;

/**
 * 用于生成 {@link VectorTableModelRecord}
 */
public class VectorTableModelRecordBuilder {

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

    public VectorTableModelRecordBuilder(int statementIndex) {
        this(statementIndex, false);
    }

    public VectorTableModelRecordBuilder(int statementIndex, boolean useWeight) {
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
    public void processRunResultForTestcase(RunResultForTestcase runResultForTestcase, Double testcaseWeight) {

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
     * 获取构建好的 VectorTableModelRecord
     *
     * @return
     */
    public VectorTableModelRecord build() {
        if (useWeight) {
            return new VectorTableModelRecord(statementIndex, anf, anp, aef, aep, weightedAnf, weightedAnp, weightedAef, weightedAep);
        }
        return new VectorTableModelRecord(statementIndex, anf, anp, aef, aep);
    }
}
