package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import one.util.streamex.StreamEx;

import java.util.Collection;
import java.util.List;

public class TestcaseWeightHelper {

    /**
     * 简化权重，将结果为 1.0 的权重删除
     *
     * @param input
     * @return
     */
    public static List<TestcaseWeightForTestcase> simplifyTestcaseWeights(List<TestcaseWeightForTestcase> input) {
        return StreamEx
            .of(input)
            .filter(a -> a.getTestcaseWeight() != 1.0)
            .toImmutableList();
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
            simplifyTestcaseWeights(input.getTestcaseWeights())
        );
    }

    /**
     * 简化权重，将结果为 1.0 的权重删除
     *
     * @param input
     * @return
     */
    public static TestcaseWeightJam simplifyTestcaseWeights(TestcaseWeightJam input) {
        final List<TestcaseWeightForProgram> collect = StreamEx
            .of(input.getTestcaseWeightForPrograms())
            .map(TestcaseWeightHelper::simplifyTestcaseWeights)
            .toImmutableList();
        return new TestcaseWeightJam(collect);
    }

    /**
     * 在 run result 上运行每个 resolver，将结果收集到一起
     */
    public static TestcaseWeightJam runOnAllResolvers(
        RunResultJam jam,
        List<TestcaseWeightResolver> resolvers) {

        final List<TestcaseWeightForProgram> collect = StreamEx
            .of(resolvers)
            .parallel()
            .map(resolver -> resolver.resolve(jam))
            .map(TestcaseWeightJam::getTestcaseWeightForPrograms)
            .flatMap(Collection::stream)
            .toImmutableList();
        return new TestcaseWeightJam(collect);
    }
}
