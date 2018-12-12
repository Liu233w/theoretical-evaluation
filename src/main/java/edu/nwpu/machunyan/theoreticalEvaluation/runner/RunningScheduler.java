package edu.nwpu.machunyan.theoreticalEvaluation.runner;

import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.IProgramInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.SingleRunResult;

import java.util.ArrayList;
import java.util.List;

public class RunningScheduler {

    private final ICoverageRunner runner;
    private final List<IProgramInput> inputs;
    private final Program program;

    private ArrayList<SingleRunResult> runResults;

    public RunningScheduler(Program program, ICoverageRunner runner, List<IProgramInput> inputs) {
        this.runner = runner;
        this.inputs = inputs;
        this.program = program;
    }

    /**
     * 运行所有的测试用例。输出结果。
     *
     * @return
     * @throws CoverageRunnerException 运行中出现的所有错误
     */
    public ArrayList<SingleRunResult> runAndGetResults() throws CoverageRunnerException {

        runResults = new ArrayList<>(inputs.size());

        try {
            runner.prepare(program);

            for (IProgramInput input :
                    inputs) {
                final SingleRunResult result = runner.runWithInput(input);
                runResults.add(result);
            }
        } finally {
            runner.cleanUp();
        }

        return runResults;
    }
}
