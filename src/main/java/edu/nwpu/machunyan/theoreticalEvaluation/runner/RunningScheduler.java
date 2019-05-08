package edu.nwpu.machunyan.theoreticalEvaluation.runner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.RunResultFromRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CacheHandler;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import lombok.Builder;
import lombok.Getter;
import me.tongfei.progressbar.ProgressBar;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Builder
public class RunningScheduler {

    /**
     * 进度条信息。如果为 null （省略），将不会汇报进度条信息。
     */
    @Getter
    private final ProgressBar progressBar;

    /**
     * 缓存对象，如果为 null，则不使用缓存
     */
    @Getter
    private final CacheHandler cache;

    /**
     * 运行所有的测试用例。输出结果。
     *
     * @return
     * @throws CoverageRunnerException 运行中出现的所有错误
     */
    public ArrayList<RunResultFromRunner> runAndGetResults(
        ICoverageRunner runner,
        Program program,
        List<IProgramInput> inputs)
        throws CoverageRunnerException {

        ArrayList<RunResultFromRunner> runResults = new ArrayList<>(inputs.size());

        try {
            progressReport(program, "start preparing");
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
                if (progressBar != null) {
                    progressBar.step();
                }
            }
        } finally {
            progressReport(program, "start cleaning up");
            runner.cleanUp();
        }

        if (cache != null) {
            cache.deleteAllCaches();
        }
        progressReport(program, "all testcase finished running");

        return runResults;
    }

    private static void progressReport(Program program, String info) {

        final String message = "Progress report for " + program.getTitle() + ": " + info;
        LogUtils.logFine(message);
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
