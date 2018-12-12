package edu.nwpu.machunyan.theoreticalEvaluation.runningDatas;

/**
 * 表示语句对应表的类型。
 */
public enum StatementMapType {

    /**
     * 语句可以精确到单个字符
     */
    CHAR_BASED,
    /**
     * 语句只能精确到行
     */
    LINE_BASED,
}
