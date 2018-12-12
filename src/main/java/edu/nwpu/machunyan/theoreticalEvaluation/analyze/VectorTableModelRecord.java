package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import lombok.Data;

/**
 * 表示 vector table model 中的一行
 */
@Data
public class VectorTableModelRecord {

    /**
     * 语句的序号
     */
    private final int statementIndex;

    // 4 个数据

    private final int anf;

    private final int anp;

    private final int aef;

    private final int aep;
}
