package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

/**
 * 保存错误率指数的计算公式
 */
public class SuspiciousnessFactorFormulas {

    private static double resolveP(VectorTableModelRecord record) {
        // 通过的测试用例
        return record.getAep() + record.getAnp();
    }

    private static double resolveF(VectorTableModelRecord record) {
        // 失败的测试用例
        return record.getAef() + record.getAnf();
    }

    /**
     * O
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
        return record.getAef() - record.getAep() / (resolveP(record) + 1);
    }
}
