package edu.nwpu.soft.ma.theoreticalEvaluation.dataClass;

import lombok.Data;

/**
 * 程序的单次运行结果
 */
@Data
public class SingleRunResult {

    /**
     * 程序信息
     */
    private final Program program;

    /**
     * 程序的输入，对于不同的分析器类型不同
     */
    private final ProgramInput input;

    /**
     * 当前运行（测试用例）是否得到了正确的结果
     */
    private final boolean correct;

    /**
     * 本次运行的覆盖情况
     */
    private final Coverage coverage;

    /**
     * 本次运行中分析器所得出的语句的对应关系
     */
    private final StatementMap statementMap;
}
