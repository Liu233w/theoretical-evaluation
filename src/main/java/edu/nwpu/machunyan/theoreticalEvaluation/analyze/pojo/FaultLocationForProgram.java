package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.Value;

import java.util.Set;

/**
 * 一个程序中所有可能的错误位置，便于分析计算结果用
 */
@Value
public class FaultLocationForProgram {

    String programTitle;

    /**
     * 每一个元素代表一个可能的错误位置，用语句序号(statementIndex)表示
     */
    Set<Integer> locations;

    /**
     * 此版本和原先版本的比较
     */
    String diff;

    /**
     * 对这一部分错误的注释（可空）
     */
    String comments;
}
