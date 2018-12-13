package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import lombok.Data;

/**
 * 表示 vector table model 中的一行
 */
@Data
public class VectorTableModelRecord {

    /**
     * 语句的序号
     */
    private final int statementIndex;

    // 4 个数据

    private final int anf;

    private final int anp;

    private final int aef;

    private final int aep;

    /**
     * 类型
     */
    public static enum Type {
        /**
         * 在每个测试用例中都执行过的语句
         */
        TypeOne,
        /**
         * 不是 {@link Type#TypeOne} 的语句
         */
        TypeTwo,
    }

    /**
     * 获取当前语句的类型
     *
     * @return
     */
    public Type getType() {
        return anf == 0 && anp == 0 ? Type.TypeOne : Type.TypeTwo;
    }

    // anf => m; anp => k

    /**
     * 使用 O 公式来计算当前语句的错误指数
     *
     * @return
     */
    public int calculateSuspiciousnessFactorAsO() {
        if (getType() == Type.TypeOne) {
            return 0;
        } else {
            if (anf == 0) { // 表示 anp != 0
                return anp;
            } else {
                return -1;
            }
        }
    }

    /**
     * 使用 Op 公式来计算当前语句的错误指数
     *
     * @param testcaseCount   总共的测试用例数量
     * @param passedTestCount 通过的测试用例数量
     * @return
     */
    public double calculateSuspiciousnessFactorAsOp(int testcaseCount, int passedTestCount) {
        final double t = testcaseCount;
        final double p = passedTestCount;


        if (getType() == Type.TypeOne) {
            return t - p - (p / (p + 1));
        } else {
            if (anf == 0) {
                return t - p - ((p - anp) / (p + 1));
            } else {
                return t - p - (anf * (p + 1) + p - anp) / (p + 1);
            }
        }
    }
}
