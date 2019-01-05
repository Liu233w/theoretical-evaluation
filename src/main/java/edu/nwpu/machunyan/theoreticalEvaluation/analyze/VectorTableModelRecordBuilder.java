package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultItem;

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
     * @param runResultItem
     */
    public void processSingleRunResult(RunResultItem runResultItem) {
        processSingleRunResult(runResultItem, 1.0);
    }

    /**
     * 根据一次运行结果将 4 个数值中的一个递增
     *
     * @param runResultItem
     * @param testCaseWeight
     */
    public void processSingleRunResult(RunResultItem runResultItem, Double testCaseWeight) {

        final boolean correct = runResultItem.isCorrect();
        final boolean hit = runResultItem.getCoverage()
            .getCoverageForStatement(statementIndex) > 0;

        if (correct) {

            if (hit) {
                ++aep;
                weightedAep += testCaseWeight;
            } else {
                ++anp;
                weightedAnp += testCaseWeight;
            }
        } else {

            if (hit) {
                ++aef;
                weightedAef += testCaseWeight;
            } else {
                ++anf;
                weightedAnf += testCaseWeight;
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
