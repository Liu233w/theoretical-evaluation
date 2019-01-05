package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import lombok.EqualsAndHashCode;
import lombok.Getter;
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

    @Getter
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

    // anf => m; anp => k

    /**
     * 使用 O 公式来计算当前语句的错误指数
     *
     * @return
     */
    public double calculateSuspiciousnessFactorAsO() {
        if (getAnf() > 0) {
            return -1;
        } else {
            return getAnp();
        }
    }

    /**
     * 使用 Op 公式来计算当前语句的错误指数
     *
     * @return
     */
    public double calculateSuspiciousnessFactorAsOp() {
        return getAef() - getAep() / (getAep() + getAnp() + 1);
    }
}
