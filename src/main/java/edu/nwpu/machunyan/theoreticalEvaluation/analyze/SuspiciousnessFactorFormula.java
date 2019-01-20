package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModelForStatement;

import java.util.function.Function;

/**
 * 一个计算可疑因子的公式
 */
public interface SuspiciousnessFactorFormula extends Function<VectorTableModelForStatement, Double> {
}
