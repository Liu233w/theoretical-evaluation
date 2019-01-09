package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;

import java.util.List;
import java.util.stream.Collectors;

public class TestcaseWeightHelper {

    /**
     * 简化权重，将结果为 1.0 的权重删除
     *
     * @param input
     * @return
     */
    public static List<TestcaseWeightForTestcase> simplifyTestcaseWeights(List<TestcaseWeightForTestcase> input) {
        return input.stream()
            .filter(a -> a.getTestCaseWeight() != 1.0)
            .collect(Collectors.toList());
    }

    /**
     * 简化权重，将结果为 1.0 的权重删除
     *
     * @param input
     * @return
     */
    public static TestcaseWeightForProgram simplifyTestcaseWeights(TestcaseWeightForProgram input) {
        return new TestcaseWeightForProgram(
            input.getTitle(),
            input.getFormulaTitle(),
            simplifyTestcaseWeights(input.getTestCaseWeights())
        );
    }

    /**
     * 简化权重，将结果为 1.0 的权重删除
     *
     * @param input
     * @return
     */
    public static TestcaseWeightJam simplifyTestcaseWeights(TestcaseWeightJam input) {
        final List<TestcaseWeightForProgram> collect = input.getTestCaseWeightForPrograms().stream()
            .map(TestcaseWeightHelper::simplifyTestcaseWeights)
            .collect(Collectors.toList());
        return new TestcaseWeightJam(collect);
    }
}
