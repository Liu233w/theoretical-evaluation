package edu.nwpu.machunyan.theoreticalEvaluation.runner;

import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.IProgramInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.RunResultFromRunner;

/**
 * 通过特定的分析器运行程序，得出覆盖信息的接口
 */
public interface ICoverageRunner {

    /**
     * 准备程序。在开始时运行一次。部分分析器可以利用这个方法提高运行效率。
     * 比如 c++ 这一类先编译后运行的语言，可以提前编译一次，防止之后重复编译。
     *
     * @param program 需要保证其中的 path 可用
     */
    void prepare(Program program) throws CoverageRunnerException;

    /**
     * 运行一次程序，获取运行结果。需要在 prepare 之后运行，使用 prepare 中传入的程序路径。
     *
     * @param programInput 本次运行的输入
     * @return 单次运行结果
     */
    RunResultFromRunner runWithInput(IProgramInput programInput) throws CoverageRunnerException;

    /**
     * 清理程序。与 prepare 对应，在最后运行。使用 prepare 中的输入。
     */
    void cleanUp() throws CoverageRunnerException;
}
