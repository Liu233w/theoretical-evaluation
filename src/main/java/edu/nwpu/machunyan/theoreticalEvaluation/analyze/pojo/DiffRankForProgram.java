package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.Value;

import java.util.List;

/**
 * 一个程序中所有的加权比较
 */
@Value
public class DiffRankForProgram {

    String programTitle;

    String formulaTitle;

    List<DiffRankForStatement> diffRankForStatements;

    /**
     * 比较的左侧的 rank 标题
     */
    String leftRankTitle;

    /**
     * 比较的右侧的 rank 标题
     */
    String rightRankTitle;
}
