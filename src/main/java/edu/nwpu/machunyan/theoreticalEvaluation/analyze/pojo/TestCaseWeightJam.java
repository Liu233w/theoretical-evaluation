package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.Data;

import java.util.List;

/**
 * 一个程序的测试用例权重所需的所有数据
 */
@Data
public class TestCaseWeightJam {

    List<TestCaseWeightForProgramItem> testCaseWeightForPrograms;

    public TestCaseWeightJam(List<TestCaseWeightForProgramItem> testCaseWeightForPrograms) {
        this.testCaseWeightForPrograms = testCaseWeightForPrograms;
    }
}
