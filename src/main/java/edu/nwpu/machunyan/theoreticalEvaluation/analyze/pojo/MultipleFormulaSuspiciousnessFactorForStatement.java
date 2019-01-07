package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * 一条语句中有多个公式的结果
 */
@Data
@AllArgsConstructor
public class MultipleFormulaSuspiciousnessFactorForStatement {

    int statementIndex;

    /**
     * 公式的名称和计算出的结果
     */
    Map<String, Double> formulaTitleToResult;
}
