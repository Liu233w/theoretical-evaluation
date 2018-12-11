package edu.nwpu.soft.ma.theoreticalEvaluation.analyze;

import edu.nwpu.soft.ma.theoreticalEvaluation.runningDatas.SingleRunResult;
import lombok.Data;

/**
 * 表示 vector table model 中的一行
 */
@Data
public class VectorTableModelRecord {

    /**
     * 语句的序号
     */
    private final int statementIndex;

    // 4 个数据

    private int anf;

    private int anp;

    private int aef;

    private int aep;

    public VectorTableModelRecord(int statementIndex) {

        this.statementIndex = statementIndex;

        this.aef = 0;
        this.anf = 0;
        this.aep = 0;
        this.anp = 0;
    }

    public VectorTableModelRecord(int statementIndex, int anf, int anp, int aef, int aep) {
        this.statementIndex = statementIndex;
        this.anf = anf;
        this.anp = anp;
        this.aef = aef;
        this.aep = aep;
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
}
