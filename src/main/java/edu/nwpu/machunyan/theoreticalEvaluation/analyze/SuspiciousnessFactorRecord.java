package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import lombok.Data;

/**
 * {@link SuspiciousnessFactorResolver} 中的一条记录
 */
@Data
public class SuspiciousnessFactorRecord {

    /**
     * 语句序号
     */
    private final int statementIndex;

    /**
     * 计算出的错误率指数
     */
    private final double suspiciousnessFactor;
}
