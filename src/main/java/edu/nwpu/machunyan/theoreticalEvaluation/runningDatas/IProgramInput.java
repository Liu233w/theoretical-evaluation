package edu.nwpu.machunyan.theoreticalEvaluation.runningDatas;

/**
 * 一次运行的输入。具体类型由分析器决定
 */
public interface IProgramInput {

    /**
     * 获取表示这次输入的描述信息。便于显示给用户
     *
     * @return
     */
    String getInputDescription();
}
