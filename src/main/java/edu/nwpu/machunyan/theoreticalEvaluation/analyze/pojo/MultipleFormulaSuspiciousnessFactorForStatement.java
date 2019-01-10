package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.Value;

import java.util.Map;

/**
 * 一条语句中有多个公式的结果
 */
@Value
public class MultipleFormulaSuspiciousnessFactorForStatement {

    int statementIndex;

    /**
     * 公式的名称和计算出的结果
     */
    Map<String, Double> formulaTitleToResult;
}
