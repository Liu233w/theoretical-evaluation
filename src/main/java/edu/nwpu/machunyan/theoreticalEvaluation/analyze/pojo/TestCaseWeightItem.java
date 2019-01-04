package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * 一个测试用例的权重
 */
@Data
public class TestCaseWeightItem implements Comparable<TestCaseWeightItem> {

    /**
     * 测试用例序号
     */
    int testCaseIndex;

    /**
     * 测试用例权重
     */
    double testCaseWeight;

    public TestCaseWeightItem(int testCaseIndex, double testCaseWeight) {
        this.testCaseIndex = testCaseIndex;
        this.testCaseWeight = testCaseWeight;
    }

    @Override
    public int compareTo(@NotNull TestCaseWeightItem o) {
        return -Double.compare(this.testCaseWeight, o.testCaseIndex);
    }
}
