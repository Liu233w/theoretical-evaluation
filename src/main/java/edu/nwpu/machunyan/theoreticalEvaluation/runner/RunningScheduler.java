package edu.nwpu.machunyan.theoreticalEvaluation.runner;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.RunResultFromRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;

import java.util.function.Supplier;

public class RunningScheduler {

    @Getter
    private final ICoverageRunner runner;
    @Getter
    private final IProgramInput[] inputs;
    @Getter
    private final Program program;
    @Getter
    private final ProgressBar progressBar;
    private RunResultFromRunner[] runResults;

    public RunningScheduler(Program program, Supplier<ICoverageRunner> runnerFactory, IProgramInput[] inputs) {
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
    public RunningScheduler(Program program, Supplier<ICoverageRunner> runnerFactory, IProgramInput[] inputs, ProgressBar progressBar) {
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
    public RunResultFromRunner[] runAndGetResults() throws CoverageRunnerException {

        runResults = new RunResultFromRunner[inputs.length];

        try {
            progressReport("start preparing");
            runner.prepare(program);

            for (int i = 0; i < inputs.length; i++) {
                final RunResultFromRunner result = runner.runWithInput(inputs[i]);
                runResults[i] = result;
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
