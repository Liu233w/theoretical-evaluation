package edu.nwpu.machunyan.theoreticalEvaluation.runner;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.RunResultFromRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.StatementMap;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import me.tongfei.progressbar.ProgressBar;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 运行特定组织结构的程序的工具
 */
public class RunningResultResolver {

    /**
     * 为每个程序运行 runnerFactory，将结果存储到一起，会显示一个进度条
     *
     * @param programs
     * @param inputs
     * @param runnerFactory
     * @return
     */
    public static RunResultJam runProgramForAllVersions(List<Program> programs, List<IProgramInput> inputs, Supplier<ICoverageRunner> runnerFactory) {

        final ProgressBar progressBar = new ProgressBar("", inputs.size() * programs.size());
        final RunningScheduler scheduler = RunningScheduler.builder().progressBar(progressBar).build();

        final List<RunResultForProgram> result = StreamEx
            .of(programs)
            .parallel()
            .map(program -> {
                try {
                    return scheduler.runAndGetResults(runnerFactory.get(), program, inputs);
                } catch (CoverageRunnerException e) {
                    LogUtils.logError("CoverageRunnerException for " + program.getPath());
                    LogUtils.logError(e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .map(RunningResultResolver::mapFromRunResult)
            .toImmutableList();

        progressBar.close();

        return new RunResultJam(result);
    }

    /**
     * 从 runner 的输出结果映射成算法程序方便读取的形式
     *
     * @param runResultFromRunner
     * @return
     */
    public static RunResultForTestcase mapFromRunResult(RunResultFromRunner runResultFromRunner) {
        return new RunResultForTestcase(runResultFromRunner.isCorrect(), runResultFromRunner.getCoverage(), runResultFromRunner.getStatementMap());
    }

    /**
     * 从 runner 的输出结果映射成算法程序方便读取的形式。
     * <p>
     * 将比较 runResultFromRunner 中的 statementMap 和 defaultStatememtMap，如果一致，将这个位置输出为 null。
     *
     * @param runResultFromRunner
     * @param defaultStatememtMap
     * @return
     */
    public static RunResultForTestcase mapFromRunResult(RunResultFromRunner runResultFromRunner, StatementMap defaultStatememtMap) {
        final StatementMap thatStatementMap = runResultFromRunner.getStatementMap();
        final StatementMap statementMap = thatStatementMap.equals(defaultStatememtMap) ? null : thatStatementMap;
        return new RunResultForTestcase(runResultFromRunner.isCorrect(), runResultFromRunner.getCoverage(), statementMap);
    }

    /**
     * 把一个程序的所有运行结果映射成算法方便读取的形式，复用第一次运行结果中的 statement map。
     *
     * @param runResults
     * @return
     */
    public static RunResultForProgram mapFromRunResult(List<RunResultFromRunner> runResults) {
        final String title = runResults.get(0).getProgram().getTitle();
        final StatementMap defaultStatememtMap = runResults.get(0).getStatementMap();
        final List<RunResultForTestcase> runResultForTestcases = StreamEx
            .of(runResults)
            .map(item -> mapFromRunResult(item, defaultStatememtMap))
            .toImmutableList();
        return new RunResultForProgram(title, defaultStatememtMap, runResultForTestcases);
    }
}
