package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
import one.util.streamex.StreamEx;

import java.util.List;

/**
 * 将测试用例的权重扩大，使结果更加明显。
 * 不会改变现有的数值，而是产生新的数值。
 */
public class TestcaseWeightMultiplyingResolver {

    /**
     * @param input
     * @param multiply 扩大的倍数
     * @return
     */
    public static TestcaseWeightForTestcase resolve(TestcaseWeightForTestcase input, double multiply) {
        final double testcaseWeight = input.getTestcaseWeight();
        if (testcaseWeight > 1.0) {
            return new TestcaseWeightForTestcase(
                input.getTestcaseIndex(),
                testcaseWeight * multiply
            );
        } else if (testcaseWeight < 1.0) {
            return new TestcaseWeightForTestcase(
                input.getTestcaseIndex(),
                testcaseWeight / multiply
            );
        } else {
            return input;
        }
    }

    public static TestcaseWeightForProgram resolve(TestcaseWeightForProgram input, double multiply) {
        final List<TestcaseWeightForTestcase> collect = StreamEx
            .of(input.getTestcaseWeights())
            .map(a -> resolve(a, multiply))
            .toImmutableList();
        return new TestcaseWeightForProgram(
            input.getTitle(),
            input.getFormulaTitle(),
            collect
        );
    }

    public static TestcaseWeightJam resolve(TestcaseWeightJam input, double multiply) {
        final List<TestcaseWeightForProgram> collect = StreamEx
            .of(input.getTestcaseWeightForPrograms())
            .map(a -> resolve(a, multiply))
            .toImmutableList();
        return new TestcaseWeightJam(collect);
    }
}
