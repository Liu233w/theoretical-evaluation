package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 将测试用例的权重扩大，使结果更加明显。
 * 不会改变现有的数值，而是产生新的数值。
 */
public class TestcaseWeightMultiplier {

    /**
     * @param input
     * @param multiply 扩大的倍数
     * @return
     */
    public static TestcaseWeightForTestcase resolve(TestcaseWeightForTestcase input, double multiply) {
        if (input.getTestcaseWeight() == 1.0) {
            return input;
        } else {
            return new TestcaseWeightForTestcase(
                input.getTestcaseIndex(),
                input.getTestcaseWeight() * multiply
            );
        }
    }

    public static TestcaseWeightForProgram resolve(TestcaseWeightForProgram input, double multiply) {
        final List<TestcaseWeightForTestcase> collect = input
            .getTestcaseWeights()
            .stream()
            .map(a -> resolve(a, multiply))
            .collect(Collectors.toList());
        return new TestcaseWeightForProgram(
            input.getTitle(),
            input.getFormulaTitle(),
            collect
        );
    }

    public static TestcaseWeightJam resolve(TestcaseWeightJam input, double multiply) {
        final List<TestcaseWeightForProgram> collect = input
            .getTestcaseWeightForPrograms()
            .stream()
            .map(a -> resolve(a, multiply))
            .collect(Collectors.toList());
        return new TestcaseWeightJam(collect);
    }
}
