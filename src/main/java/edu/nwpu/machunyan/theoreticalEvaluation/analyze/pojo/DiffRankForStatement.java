package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 表示一条语句在两种计算方式下排名的区别
 */
@Data
@AllArgsConstructor
public class DiffRankForStatement {

    int statementIndex;

    DiffRankForSide left;

    DiffRankForSide right;
}
