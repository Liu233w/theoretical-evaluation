package edu.nwpu.soft.ma.theoreticalEvaluation.dataClass;

/**
 * 一条语句的信息
 */
public class StatementInfo {

    /**
     * 语句编号（索引）
     */
    private int statmentIndex;

    /**
     * 包含语句的源代码的路径
     */
    private String path;

    /**
     * 语句在源代码中的起始行
     */
    private int startRow;

    /**
     * 语句在源代码中的起始列。如果分析器不支持显示列，则此字段为 -1。
     */
    private int startColomn;

    /**
     * 语句在源代码中的终止行
     */
    private int endRow;

    /**
     * 语句在源代码中的终止列。如果分析器不支持显示列，则此字段为 -1。
     */
    private int endColomn;
}
