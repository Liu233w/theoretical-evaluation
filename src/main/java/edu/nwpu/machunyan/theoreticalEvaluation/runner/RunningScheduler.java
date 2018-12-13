package edu.nwpu.machunyan.theoreticalEvaluation.runner;

import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.IProgramInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.SingleRunResult;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import me.tongfei.progressbar.ProgressBar;

import java.util.ArrayList;
import java.util.List;

public class RunningScheduler {

    private final ICoverageRunner runner;
    private final List<IProgramInput> inputs;
    private final Program program;

    private ArrayList<SingleRunResult> runResults;

    private final ProgressBar progressBar;

    public RunningScheduler(Program program, ICoverageRunner runner, List<IProgramInput> inputs) {
        this(program, runner, inputs, null);
    }

    /**
     * 构造函数。
     *
     * @param program
     * @param runner
     * @param inputs
     * @param progressBar 进度条信息。如果为 null （省略），将不会汇报进度条信息。
     */
    public RunningScheduler(Program program, ICoverageRunner runner, List<IProgramInput> inputs, ProgressBar progressBar) {
        this.runner = runner;
        this.inputs = inputs;
        this.program = program;
        this.progressBar = progressBar;
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
            progressReport("start preparing");
            runner.prepare(program);

            for (IProgramInput input :
                    inputs) {
                final SingleRunResult result = runner.runWithInput(input);
                runResults.add(result);
                stepProgressBar();
            }
        } finally {
            progressReport("start cleaning up");
            runner.cleanUp();
        }

        progressReport("all testcase finished running");

        return runResults;
    }

    private void progressReport(String info) {

        final String message = "Progress report for " + program.getTitle() + ": " + info;
        LogUtils.logFine(message);
    }

    private void stepProgressBar() {
        if (progressBar != null) {
            progressBar.step();
        }
    }
}
