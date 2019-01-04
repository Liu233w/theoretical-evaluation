package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.Data;

import java.util.List;

/**
 * 表示一个程序的title和测试用例
 */
@Data
public class TestCaseWeightForProgramItem {

    String title;

    List<TestCaseWeightItem> testCaseWeights;

    public TestCaseWeightForProgramItem(String title, List<TestCaseWeightItem> testCaseWeights) {
        this.title = title;
        this.testCaseWeights = testCaseWeights;
    }
}
