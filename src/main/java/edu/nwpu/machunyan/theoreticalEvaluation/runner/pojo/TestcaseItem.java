package edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo;

import lombok.Value;

@Value
public class TestcaseItem {
    /**
     * 从 stdin 的输入
     */
    private String input;
    /**
     * 程序的 stdout
     */
    private String output;
    /**
     * 程序的参数
     */
    private String[] params;
    /**
     * 标识一个测试用例（没啥用处）
     */
    private String name;
}
