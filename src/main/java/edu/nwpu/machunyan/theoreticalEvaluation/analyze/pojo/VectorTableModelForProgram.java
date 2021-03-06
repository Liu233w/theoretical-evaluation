package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.Value;

import java.util.List;

/**
 * 一个 vtm
 */
@Value
public class VectorTableModelForProgram {

    String programTitle;

    /**
     * 第一个元素为 null，剩下的元素每一个和 vtm 中的一行一一对应
     */
    List<VectorTableModelForStatement> records;
}
