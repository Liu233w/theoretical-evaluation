package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorResolver;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * {@link SuspiciousnessFactorResolver} 中的一条记录
 */
@Data
@AllArgsConstructor
public class SuspiciousnessFactorItem {

    /**
     * 语句序号
     */
    private final int statementIndex;

    /**
     * 计算出的错误率指数
     */
    private final double suspiciousnessFactor;
}
