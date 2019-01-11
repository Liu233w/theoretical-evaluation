package edu.nwpu.machunyan.theoreticalEvaluation.runner.data;

import lombok.Value;

/**
 * 程序的单次运行结果，采用了适合 runner 的抽象
 */
@Value
public class RunResultFromRunner {

    /**
     * 程序信息
     */
    Program program;

    /**
     * 程序的输入，对于不同的分析器类型不同
     */
    IProgramInput input;

    /**
     * 当前运行（测试用例）是否得到了正确的结果
     */
    boolean correct;

    /**
     * 本次运行的覆盖情况
     */
    Coverage coverage;

    /**
     * 本次运行中分析器所得出的语句的对应关系
     */
    StatementMap statementMap;
}
