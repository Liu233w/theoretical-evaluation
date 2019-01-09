package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 一个程序的测试用例权重所需的所有数据
 */
@Data
@AllArgsConstructor
public class TestcaseWeightJam {

    List<TestcaseWeightForProgram> testCaseWeightForPrograms;
}
