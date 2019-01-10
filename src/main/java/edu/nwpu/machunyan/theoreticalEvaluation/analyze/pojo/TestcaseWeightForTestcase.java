package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.Value;

/**
 * 一个测试用例的权重
 */
@Value
public class TestcaseWeightForTestcase {

    /**
     * 测试用例序号
     */
    int testcaseIndex;

    /**
     * 测试用例权重
     */
    double testcaseWeight;
}
