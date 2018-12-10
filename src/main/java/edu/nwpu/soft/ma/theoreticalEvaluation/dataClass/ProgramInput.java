package edu.nwpu.soft.ma.theoreticalEvaluation.dataClass;

/**
 * 一次运行的输入。具体类型由分析器决定
 */
public interface ProgramInput {

    /**
     * 获取表示这次输入的描述信息。便于显示给用户
     *
     * @return
     */
    String getInputDescription();
}
