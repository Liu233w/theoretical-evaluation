package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 表示一个程序的title和测试用例
 */
@Data
@AllArgsConstructor
public class TestcaseWeightForProgramItem {

    String title;

    String formulaTitle;

    List<TestcaseWeightItem> testCaseWeights;
}
