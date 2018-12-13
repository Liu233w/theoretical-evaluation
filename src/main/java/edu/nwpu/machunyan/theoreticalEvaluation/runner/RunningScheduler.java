package edu.nwpu.machunyan.theoreticalEvaluation.runner;

import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.IProgramInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.SingleRunResult;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;

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

            for (int i = 0; i < inputs.size(); i++) {
                if (i >= inputs.size() * (3.0 / 4.0)) {
                    progressReport("reach 3/4");
                } else if (i >= inputs.size() / 2) {
                    progressReport("reach 1/2");
                } else if (i >= inputs.size() * (1.0 / 4.0)) {
                    progressReport("reach 1/4");
                }

                final SingleRunResult result = runner.runWithInput(inputs.get(i));
                runResults.add(result);
            }
        } finally {
            runner.cleanUp();
        }

        progressReport("all testcase finished running");

        return runResults;
    }

    private void progressReport(String info) {
        LogUtils.logFine("Progress report for " + program.getTitle() + ": " + info);
    }
}
