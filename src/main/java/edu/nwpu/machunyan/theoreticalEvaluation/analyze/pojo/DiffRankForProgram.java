package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 一个程序中所有的加权比较
 */
@Data
@AllArgsConstructor
@Builder
public class DiffRankForProgram {

    String programTitle;

    String formulaTitle;

    List<DiffRankForStatement> diffRankForStatements;

    /**
     * 比较的左侧的 rank 标题
     */
    @Builder.Default
    String leftRankTitle = "";

    /**
     * 比较的右侧的 rank 标题
     */
    @Builder.Default
    String rightRankTitle = "";
}
