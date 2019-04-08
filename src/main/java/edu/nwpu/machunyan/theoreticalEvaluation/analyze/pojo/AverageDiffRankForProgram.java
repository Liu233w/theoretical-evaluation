package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.Value;

/**
 * 参见 {@link edu.nwpu.machunyan.theoreticalEvaluation.analyze.AverageRankDiffResolver}
 */
@Value
public class AverageDiffRankForProgram {

    String programTitle;

    String formulaTitle;

    double averageRankDiff;

    /**
     * 比较的左侧的 rank 标题
     */
    String leftRankTitle;

    /**
     * 比较的右侧的 rank 标题
     */
    String rightRankTitle;
}
