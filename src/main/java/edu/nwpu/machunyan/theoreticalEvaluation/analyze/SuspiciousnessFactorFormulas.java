package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

/**
 * 保存错误率指数的计算公式
 */
public class SuspiciousnessFactorFormulas {

    /**
     * 公式 O
     *
     * @param record
     * @return
     */
    public static double o(VectorTableModelRecord record) {

        if (record.getAnf() > 0) {
            return -1;
        } else {
            return record.getAnp();
        }
    }

    /**
     * Op
     *
     * @param record
     * @return
     */
    public static double op(VectorTableModelRecord record) {
        return record.getAef() - record.getAep() / (record.getAep() + record.getAnp() + 1);
    }
}
