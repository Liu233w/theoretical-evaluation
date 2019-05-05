package edu.nwpu.machunyan.theoreticalEvaluation.runner.impl;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.IProgramInput;
import lombok.Value;

@Value
public class Defects4jTestcase implements IProgramInput {

    /**
     * 测试用例属于的类
     */
    private final String testcaseClass;

    /**
     * 测试用例的方法名
     */
    private final String testcaseMethod;

    @Override
    public String getInputDescription() {
        // 跟使用 defects4j test 时生成的 all_tests 文件格式一致
        return testcaseMethod + "(" + testcaseClass + ")";
    }
}