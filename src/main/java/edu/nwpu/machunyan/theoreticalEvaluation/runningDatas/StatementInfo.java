package edu.nwpu.machunyan.theoreticalEvaluation.runningDatas;

import lombok.Value;

/**
 * 一条语句的信息
 */
@Value
public class StatementInfo {

    /**
     * 语句编号（索引）
     */
    int statementIndex;

    /**
     * 包含语句的源代码的路径
     */
    String filePath;

    /**
     * 语句在源代码中的起始行
     */
    int startRow;

    /**
     * 语句在源代码中的起始列。如果分析器不支持显示列，则此字段为 -1。
     */
    int startColomn;

    /**
     * 语句在源代码中的终止行
     */
    int endRow;

    /**
     * 语句在源代码中的终止列。如果分析器不支持显示列，则此字段为 -1。
     */
    int endColomn;

    /**
     * @param statementIndex
     * @param filePath       包含语句的源代码的路径
     * @param startRow
     * @param endRow
     */
    public StatementInfo(int statementIndex, String filePath, int startRow, int endRow) {
        this.statementIndex = statementIndex;

        this.filePath = filePath;
        this.startRow = startRow;
        this.endRow = endRow;

        this.startColomn = 0;
        this.endColomn = 0;
    }
}
