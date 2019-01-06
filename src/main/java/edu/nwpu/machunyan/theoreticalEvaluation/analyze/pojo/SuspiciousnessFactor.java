package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 程序中的所有可疑指数
 */
@Data
@AllArgsConstructor
public class SuspiciousnessFactor {

    String programTitle;

    /**
     * 使用的可疑指数公式
     */
    String formula;

    List<SuspiciousnessFactorItem> resultForStatements;
}
