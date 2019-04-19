package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import lombok.Value;
import one.util.streamex.StreamEx;

import java.util.List;

@Value
public class TestSuitSubsetJam {

    List<TestSuitSubsetForProgram> testSuitSubsetForPrograms;

    /**
     * 从原始的运行结果生成划分过子集的运行结果。参数必须和当前的数据一一对应（拥有相同的 programTitle）
     *
     * @param origin
     * @return
     */
    public RunResultJam getRunResultJam(RunResultJam origin) {

        final List<RunResultForProgram> origins = origin.getRunResultForPrograms();
        if (origins.size() != testSuitSubsetForPrograms.size()) {
            throw new IllegalArgumentException("数据必须拥有相同的元素数量");
        }

        final List<RunResultForProgram> list = StreamEx
            .of(testSuitSubsetForPrograms)
            .sortedBy(TestSuitSubsetForProgram::getProgramTitle)
            .zipWith(
                StreamEx.of(origins).sortedBy(RunResultForProgram::getProgramTitle),
                TestSuitSubsetForProgram::getRunResultForProgram)
            .toImmutableList();
        return new RunResultJam(list);
    }
}
