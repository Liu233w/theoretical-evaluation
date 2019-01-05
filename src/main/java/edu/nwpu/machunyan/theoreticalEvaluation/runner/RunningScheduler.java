package edu.nwpu.machunyan.theoreticalEvaluation.runner;

import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.IProgramInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.RunResultFromRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RunningScheduler {

    @Getter
    private final ICoverageRunner runner;
    @Getter
    private final List<IProgramInput> inputs;
    @Getter
    private final Program program;
    @Getter
    private final ProgressBar progressBar;
    private ArrayList<RunResultFromRunner> runResults;

    public RunningScheduler(Program program, Supplier<ICoverageRunner> runnerFactory, List<IProgramInput> inputs) {
        this(program, runnerFactory, inputs, null);
    }

    /**
     * 构造函数。
     *
     * @param program
     * @param runnerFactory runner 的工厂函数
     * @param inputs
     * @param progressBar   进度条信息。如果为 null （省略），将不会汇报进度条信息。
     */
    public RunningScheduler(Program program, Supplier<ICoverageRunner> runnerFactory, List<IProgramInput> inputs, ProgressBar progressBar) {
        this.runner = runnerFactory.get();
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
    public ArrayList<RunResultFromRunner> runAndGetResults() throws CoverageRunnerException {

        runResults = new ArrayList<>(inputs.size());

        try {
            progressReport("start preparing");
            runner.prepare(program);

            for (IProgramInput input :
                inputs) {
                final RunResultFromRunner result = runner.runWithInput(input);
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
