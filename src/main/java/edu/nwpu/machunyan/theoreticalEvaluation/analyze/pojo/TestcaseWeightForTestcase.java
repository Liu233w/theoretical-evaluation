package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 一个测试用例的权重
 */
@Data
@AllArgsConstructor
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
