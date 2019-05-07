package edu.nwpu.machunyan.theoreticalEvaluation.runner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.RunResultFromRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CacheHandler;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    @Getter
    private final boolean debug;
    @Getter
    private final CacheHandler cache;

    private ArrayList<RunResultFromRunner> runResults;

    public RunningScheduler(
        Program program,
        Supplier<ICoverageRunner> runnerFactory,
        List<IProgramInput> inputs) {

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
    public RunningScheduler(
        Program program,
        Supplier<ICoverageRunner> runnerFactory,
        List<IProgramInput> inputs,
        ProgressBar progressBar) {

        this(program, runnerFactory, inputs, progressBar, false);
    }

    /**
     * 构造函数。
     *
     * @param program
     * @param runnerFactory runner 的工厂函数
     * @param inputs
     * @param progressBar   进度条信息。如果为 null （省略），将不会汇报进度条信息。
     * @param debug
     */
    public RunningScheduler(
        Program program,
        Supplier<ICoverageRunner> runnerFactory,
        List<IProgramInput> inputs,
        ProgressBar progressBar,
        boolean debug) {
        this(program, runnerFactory, inputs, progressBar, null, debug);
    }

    /**
     * 构造函数。
     *
     * @param program
     * @param runnerFactory runner 的工厂函数
     * @param inputs
     * @param progressBar   进度条信息。如果为 null （省略），将不会汇报进度条信息。
     * @param cache
     * @param debug
     */
    public RunningScheduler(
        Program program,
        Supplier<ICoverageRunner> runnerFactory,
        List<IProgramInput> inputs,
        ProgressBar progressBar,
        CacheHandler cache,
        boolean debug) {

        this.runner = runnerFactory.get();
        this.inputs = inputs;
        this.program = program;
        this.progressBar = progressBar;
        this.cache = cache;
        this.debug = debug;

        if (debug) {
            this.runner.setDebug(true);
        }
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

            for (IProgramInput input : inputs) {

                if (cache != null) {

                    // 确保 IProgramInput 是正确的结果
                    final Gson gson = new GsonBuilder()
                        .registerTypeAdapter(IProgramInput.class, new CachingInputAdapter(input))
                        .create();

                    final Optional<RunResultFromRunner> resultOptional
                        = cache.tryLoadCache(input.getInputKey(), RunResultFromRunner.class, gson);

                    if (resultOptional.isPresent()) {
                        runResults.add(resultOptional.get());
                        if (progressBar != null) {
                            progressBar.maxHint(progressBar.getMax() - 1);
                        }
                        continue;
                    }
                }

                final RunResultFromRunner result = runner.runWithInput(input);

                if (cache != null) {
                    cache.saveCache(input.getInputKey(), result);
                }

                runResults.add(result);
                stepProgressBar();
            }
        } finally {
            progressReport("start cleaning up");
            runner.cleanUp();
        }

        if (cache != null) {
            cache.deleteAllCaches();
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

    private static class CachingInputAdapter implements InstanceCreator<IProgramInput> {

        private final IProgramInput instance;

        private CachingInputAdapter(IProgramInput instance) {
            this.instance = instance;
        }

        @Override
        public IProgramInput createInstance(Type type) {
            return instance;
        }
    }
}
