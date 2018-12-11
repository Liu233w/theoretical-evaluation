package edu.nwpu.soft.ma.theoreticalEvaluation.dataClass;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 一条语句的信息
 */
@Data
@AllArgsConstructor
public class StatementInfo {

    /**
     * 语句编号（索引）
     */
    private final int statementIndex;

    /**
     * 包含语句的源代码的路径
     */
    private final String path;

    /**
     * 语句在源代码中的起始行
     */
    private final int startRow;

    /**
     * 语句在源代码中的起始列。如果分析器不支持显示列，则此字段为 -1。
     */
    private final int startColomn;

    /**
     * 语句在源代码中的终止行
     */
    private final int endRow;

    /**
     * 语句在源代码中的终止列。如果分析器不支持显示列，则此字段为 -1。
     */
    private final int endColomn;

    public StatementInfo(int statementIndex, String path, int startRow, int endRow) {
        this.statementIndex = statementIndex;

        this.path = path;
        this.startRow = startRow;
        this.endRow = endRow;

        this.startColomn = 0;
        this.endColomn = 0;
    }
}
