package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 统计一次运行中所有语句的错误率指数
 */
public class SuspiciousnessFactorGenerator {

    /**
     * 使用指定的公式从 vector table model 生成错误率指数。指数按从大到小的顺序排列。
     *
     * @param vectorTableModel
     * @param suspiciousnessFactorFunc
     * @return
     */
    public static ArrayList<SuspiciousnessFactorRecord> getSuspiciousnessFactorMatrixOrdered(
            List<VectorTableModelRecord> vectorTableModel,
            Function<VectorTableModelRecord, Double> suspiciousnessFactorFunc) {

        final ArrayList<SuspiciousnessFactorRecord> results = new ArrayList<>(vectorTableModel.size() - 1);
        for (int i = 1; i < vectorTableModel.size(); ++i) {
            results.add(new SuspiciousnessFactorRecord(i, suspiciousnessFactorFunc.apply(vectorTableModel.get(i))));
        }

        results.sort((l, r) -> -Double.compare(l.getSuspiciousnessFactor(), r.getSuspiciousnessFactor()));

        return results;
    }
}
