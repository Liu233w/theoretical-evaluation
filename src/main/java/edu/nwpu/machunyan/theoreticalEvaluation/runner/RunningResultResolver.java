package edu.nwpu.machunyan.theoreticalEvaluation.runner;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.ProgramRunResult;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.ProgramRunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultItem;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.IProgramInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.RunResultFromRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.StatementMap;
import me.tongfei.progressbar.ProgressBar;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
    public static ProgramRunResultJam runProgramForAllVersions(List<Program> programs, List<IProgramInput> inputs, Supplier<ICoverageRunner> runnerFactory) {

        final ProgressBar progressBar = new ProgressBar("", inputs.size() * programs.size());

        final List<ProgramRunResult> result = programs.stream()
                .parallel()
                .map(program -> new RunningScheduler(program, runnerFactory, inputs, progressBar))
                .map(scheduler -> {
                    try {
                        return scheduler.runAndGetResults();
                    } catch (CoverageRunnerException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(RunningResultResolver::mapFromRunResult)
                .collect(Collectors.toList());

        progressBar.close();

        return new ProgramRunResultJam(result);
    }

    /**
     * 从 runner 的输出结果映射成算法程序方便读取的形式
     *
     * @param runResultFromRunner
     * @return
     */
    public static RunResultItem mapFromRunResult(RunResultFromRunner runResultFromRunner) {
        return new RunResultItem(runResultFromRunner.isCorrect(), runResultFromRunner.getCoverage(), runResultFromRunner.getStatementMap());
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
    public static RunResultItem mapFromRunResult(RunResultFromRunner runResultFromRunner, StatementMap defaultStatememtMap) {
        final StatementMap thatStatementMap = runResultFromRunner.getStatementMap();
        final StatementMap statementMap = thatStatementMap.equals(defaultStatememtMap) ? null : thatStatementMap;
        return new RunResultItem(runResultFromRunner.isCorrect(), runResultFromRunner.getCoverage(), statementMap);
    }

    /**
     * 把一个程序的所有运行结果映射成算法方便读取的形式，复用第一次运行结果中的 statement map。
     *
     * @param runResults
     * @return
     */
    public static ProgramRunResult mapFromRunResult(List<RunResultFromRunner> runResults) {
        final String title = runResults.get(0).getProgram().getTitle();
        final StatementMap defaultStatememtMap = runResults.get(0).getStatementMap();
        final List<RunResultItem> runResultItems = runResults.stream()
                .map(item -> mapFromRunResult(item, defaultStatememtMap))
                .collect(Collectors.toList());
        return new ProgramRunResult(title, defaultStatememtMap, runResultItems);
    }
}
