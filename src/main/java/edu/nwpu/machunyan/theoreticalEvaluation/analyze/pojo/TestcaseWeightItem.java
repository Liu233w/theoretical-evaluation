package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 一个测试用例的权重
 */
@Data
@AllArgsConstructor
public class TestcaseWeightItem {

    /**
     * 测试用例序号
     */
    int testCaseIndex;

    /**
     * 测试用例权重
     */
    double testCaseWeight;
}
