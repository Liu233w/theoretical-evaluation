package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.StatementMap;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import lombok.Value;

/**
 * 由 {@link edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestSuitSubsetResolver} 处理过的结果。
 */
@Value
public class TestSuitSubsetForProgram {

    /**
     * 程序的标题
     */
    String programTitle;

    /**
     * 本次运行中分析器所得出的语句的对应关系，正常情况下同一个程序的 statement map 应该是一样的
     */
    StatementMap statementMap;

    /**
     * 当前测试用例的 average performance
     */
    double averagePerformance;

    /**
     * 在划分之前，测试用例的 average performance
     */
    double averagePerformanceBefore;

    /**
     * 当前的 {@link TestSuitSubsetForProgram#runResults} 的长度会小于等于
     * 划分之前的运行结果长度。
     * <p>
     * 因此这里的索引和 {@link TestSuitSubsetForProgram#runResults} 的索引
     * 一一对应，表示这一个运行结果在划分之前的下标。
     */
    int[] toOldSetMap;

    RunResultForTestcase[] runResults;

    /**
     * 生成 {@link RunResultForProgram}，便于参与其他的计算
     *
     * @return
     */
    public RunResultForProgram getRunResultForProgram() {
        return new RunResultForProgram(
            programTitle,
            statementMap,
            runResults
        );
    }
}
