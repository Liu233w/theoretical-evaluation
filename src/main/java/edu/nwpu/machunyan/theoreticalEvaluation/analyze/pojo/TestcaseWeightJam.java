package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.Value;

import java.util.List;

/**
 * 一个程序的测试用例权重所需的所有数据
 */
@Value
public class TestcaseWeightJam {

    List<TestcaseWeightForProgram> testcaseWeightForPrograms;
}
