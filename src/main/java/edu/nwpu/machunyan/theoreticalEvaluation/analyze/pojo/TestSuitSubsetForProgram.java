package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.StatementMap;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import lombok.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
     * 当前测试用例的 average performance
     */
    double averagePerformance;

    /**
     * 在划分之前，测试用例的 average performance
     */
    double averagePerformanceBefore;

    /**
     * 当前的 {@link TestSuitSubsetForProgram#getRunResults(List)} 的长度会小于等于
     * 划分之前的运行结果长度。
     * <p>
     * 因此这里的索引和 {@link TestSuitSubsetForProgram#getRunResults(List)} 的索引
     * 一一对应，表示这一个运行结果在划分之前的下标。
     */
    int[] toOldSetMap;

    /**
     * 从整体的运行结果将当前的子集划分出来
     *
     * @param origin
     * @return
     */
    public List<RunResultForTestcase> getRunResults(List<RunResultForTestcase> origin) {
        final RunResultForTestcase[] result = new RunResultForTestcase[toOldSetMap.length];
        for (int i = 0; i < toOldSetMap.length; i++) {
            result[i] = origin.get(toOldSetMap[i]);
        }
        return Collections.unmodifiableList(Arrays.asList(result));
    }

    /**
     * 生成 {@link RunResultForProgram}，便于参与其他的计算
     *
     * @param origin 原始（包含全部测试用例的）的运行结果，必须和当前的 programTitle 相同
     * @return
     */
    public RunResultForProgram getRunResultForProgram(RunResultForProgram origin) {

        if (!origin.getProgramTitle().equals(this.getProgramTitle())) {
            throw new IllegalArgumentException("ProgramTitle 必须相同，本对象：" + this.getProgramTitle()
                + "参数：" + origin.getProgramTitle());
        }

        return new RunResultForProgram(
            programTitle,
            origin.getStatementMap(),
            this.getRunResults(origin.getRunResults()));
    }
}
