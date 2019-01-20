package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.Value;

import java.util.Collections;
import java.util.HashMap;
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

    public static class Builder {

        private final int statementIndex;

        private final HashMap<String, Double> formulaTitleToResult;

        public Builder(int statementIndex) {
            this.statementIndex = statementIndex;
            this.formulaTitleToResult = new HashMap<>();
        }

        public void process(SuspiciousnessFactorForStatement input, String formulaTitle) {
            if (input.getStatementIndex() == statementIndex) {
                formulaTitleToResult.put(formulaTitle, input.getSuspiciousnessFactor());
            }
        }

        public MultipleFormulaSuspiciousnessFactorForStatement build() {
            return new MultipleFormulaSuspiciousnessFactorForStatement(
                statementIndex,
                Collections.unmodifiableMap(formulaTitleToResult));
        }
    }
}
