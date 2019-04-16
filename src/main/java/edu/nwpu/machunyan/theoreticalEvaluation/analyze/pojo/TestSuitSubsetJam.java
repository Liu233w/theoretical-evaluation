package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import lombok.Value;
import one.util.streamex.StreamEx;

import java.util.List;

@Value
public class TestSuitSubsetJam {

    List<TestSuitSubsetForProgram> testSuitSubsetForPrograms;

    public RunResultJam getRunResultJam() {

        final RunResultForProgram[] list = StreamEx
            .of(testSuitSubsetForPrograms)
            .map(TestSuitSubsetForProgram::getRunResultForProgram)
            .toArray(RunResultForProgram[]::new);
        return new RunResultJam(list);
    }
}
