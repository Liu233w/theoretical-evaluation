package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.SingleRunResult;

/**
 * 用于生成 {@link VectorTableModelRecord}
 */
public class VectorTableModelRecordBuilder {

    /**
     * 语句的序号
     */
    private final int statementIndex;

    // 4 个数据

    private int anf;

    private int anp;

    private int aef;

    private int aep;

    public VectorTableModelRecordBuilder(int statementIndex) {
        this.statementIndex = statementIndex;
    }

    /**
     * 根据一次运行结果将 4 个数值中的一个递增
     *
     * @param singleRunResult
     */
    public void processSingleRunResult(SingleRunResult singleRunResult) {

        final boolean correct = singleRunResult.isCorrect();
        final boolean hit = singleRunResult.getCoverage()
                .getCoverageForStatement(statementIndex) > 0;

        if (correct) {

            if (hit) {
                ++aep;
            } else {
                ++anp;
            }
        } else {

            if (hit) {
                ++aef;
            } else {
                ++anf;
            }
        }
    }

    /**
     * 获取构建好的 VectorTableModelRecord
     *
     * @return
     */
    public VectorTableModelRecord build() {
        return new VectorTableModelRecord(statementIndex, anf, anp, aef, aep);
    }
}
