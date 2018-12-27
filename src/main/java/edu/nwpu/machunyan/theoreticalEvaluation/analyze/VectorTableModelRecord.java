package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

/**
 * 表示 vector table model 中的一行
 */
@EqualsAndHashCode
public class VectorTableModelRecord implements Comparable {

    /**
     * 语句的序号
     */
    private final int statementIndex;

    // 4 个数据

    private final int anf;

    private final int anp;

    private final int aef;

    private final int aep;

    private final double weightedAnf;

    private final double weightedAnp;

    private final double weightedAef;

    private final double weightedAep;

    private final boolean useWeight;

    /**
     * 使用四个数值和加权之后的四个数值初始化
     *
     * @param statementIndex
     * @param anf
     * @param anp
     * @param aef
     * @param aep
     * @param weightedAnf
     * @param weightedAnp
     * @param weightedAef
     * @param weightedAep
     */
    public VectorTableModelRecord(int statementIndex, int anf, int anp, int aef, int aep, double weightedAnf, double weightedAnp, double weightedAef, double weightedAep) {
        this.useWeight = true;

        this.statementIndex = statementIndex;
        this.anf = anf;
        this.anp = anp;
        this.aef = aef;
        this.aep = aep;
        this.weightedAnf = weightedAnf;
        this.weightedAnp = weightedAnp;
        this.weightedAef = weightedAef;
        this.weightedAep = weightedAep;
    }

    /**
     * 使用四个数值初始化。不采用加权。
     *
     * @param statementIndex
     * @param anf
     * @param anp
     * @param aef
     * @param aep
     */
    public VectorTableModelRecord(int statementIndex, int anf, int anp, int aef, int aep) {
        this.useWeight = false;

        this.statementIndex = statementIndex;
        this.anf = anf;
        this.anp = anp;
        this.aef = aef;
        this.aep = aep;

        this.weightedAnf = 0.0;
        this.weightedAnp = 0.0;
        this.weightedAef = 0.0;
        this.weightedAep = 0.0;
    }

    public double getAnf() {
        if (useWeight) {
            return weightedAnf;
        } else {
            return anf;
        }
    }

    public double getAnp() {
        if (useWeight) {
            return weightedAnp;
        } else {
            return anp;
        }
    }

    public double getAef() {
        if (useWeight) {
            return weightedAef;
        } else {
            return aef;
        }
    }

    public double getAep() {
        if (useWeight) {
            return weightedAep;
        } else {
            return aep;
        }
    }

    /**
     * 获取未加权的对应数值
     *
     * @return
     */
    public int getUnWeightedAnf() {
        return anf;
    }

    /**
     * 获取未加权的对应数值
     *
     * @return
     */
    public int getUnWeightedAnp() {
        return anp;
    }

    /**
     * 获取未加权的对应数值
     *
     * @return
     */
    public int getUnWeightedAef() {
        return aef;
    }

    /**
     * 获取未加权的对应数值
     *
     * @return
     */
    public int getUnWeightedAep() {
        return aep;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (!(o instanceof VectorTableModelRecord)) {
            throw new IllegalArgumentException("VectorTableModelRecord can only be compared to VectorTableModelRecord");
        }
        VectorTableModelRecord that = (VectorTableModelRecord) o;

        if (this.useWeight != that.useWeight) {
            throw new IllegalArgumentException("Cannot compare a weighted record with an un-weighted record.");
        }

        if (anf != that.getAnf()) {
            return Double.compare(anf, that.getAnf());
        }
        if (anp != that.getAnp()) {
            return Double.compare(anp, that.getAnp());
        }
        if (aef != that.getAef()) {
            return Double.compare(aef, that.getAef());
        }
        return Double.compare(aep, that.getAep());
    }

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
