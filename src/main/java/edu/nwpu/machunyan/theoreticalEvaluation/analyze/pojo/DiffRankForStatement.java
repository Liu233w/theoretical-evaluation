package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.Value;

/**
 * 表示一条语句在两种计算方式下排名的区别
 */
@Value
public class DiffRankForStatement {

    int statementIndex;

    DiffRankForSide left;

    DiffRankForSide right;
}
