package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModelForStatement;
import lombok.Lombok;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 保存可疑因子的计算公式
 */
@SuppressWarnings("Duplicates")
public class SuspiciousnessFactorFormulas {

    /**
     * 获取所有的公式
     *
     * @return
     */
    public static Map<String, Function<VectorTableModelForStatement, Double>> getAllFormulas() {

        return Arrays.stream(SuspiciousnessFactorFormulas.class.getMethods())
            .filter(item -> item.getAnnotation(Formula.class) != null)
            .collect(Collectors.toMap(
                Method::getName,
                SuspiciousnessFactorFormulas::toFunctionInterface
            ));
    }

    // ============= 公式 ===========================

    private static double resolveP(VectorTableModelForStatement record) {
        // 通过的测试用例
        return record.getAep() + record.getAnp();
    }

    private static double resolveF(VectorTableModelForStatement record) {
        // 失败的测试用例
        return record.getAef() + record.getAnf();
    }

    /**
     * O
     *
     * @param record
     * @return
     */
    @Formula
    public static double o(VectorTableModelForStatement record) {

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
    @Formula
    public static double op(VectorTableModelForStatement record) {
        return record.getAef() - record.getAep() / (resolveP(record) + 1);
    }

    // === o ?
    @Formula
    public static double naish1(VectorTableModelForStatement record) {
        final double f = resolveF(record);
        final double aef = record.getAef();
        final double anp = record.getAnp();

        if (aef < f) {
            return -1;
        } else {
            return anp;
        }
    }

    @Formula
    public static double andergerg(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double f = resolveF(record);
        final double aep = record.getAep();

        return aef / (2 * f - aef + 2 * aep);
    }

    @Formula
    public static double goodman(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double f = resolveF(record);
        final double aep = record.getAep();

        return (3 * aef - f - aep) / (aef + f + aep);
    }

    @Formula
    public static double sorensenDice(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double f = resolveF(record);
        final double aep = record.getAep();
        final double anf = record.getAnf();

        return 2 * aef / (2 * f - anf + aep);
    }

    @Formula
    public static double cbiInc(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return aef / (aef + aep) - f / (f + p);
    }

    @Formula
    public static double tarantula(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);

        return (aef / f) / (aef / f + aep / f);
    }

    @Formula
    public static double hamann(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double anp = record.getAnp();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return (2 * aef + 2 * anp) / (f + p) - 1;
    }

    @Formula
    public static double rogersTanimoto(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double anp = record.getAnp();
        final double anf = record.getAnf();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return (aef + anp) / (anf + aep + f + p);
    }

    @Formula
    public static double simpleMatching(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double anp = record.getAnp();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return (aef + anp) / (f + p);
    }

    @Formula
    public static double sokal(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double anp = record.getAnp();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return (2 * aef + 2 * anp) / (aef + anp + f + p);
    }

    @Formula
    public static double binary(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double f = resolveF(record);

        if (aef < f) {
            return 0;
        } else {
            return 1;
        }
    }

    @Formula
    public static double wong1(VectorTableModelForStatement record) {
        return record.getAef();
    }

    @Formula
    public static double rogot1(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double anp = record.getAnp();
        final double anf = record.getAnf();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return (aef / 2) / (aef + f + aep) + (anp / 2) / (anf + anp + p);
    }

    @Formula
    public static double scott(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double anp = record.getAnp();
        final double anf = record.getAnf();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return (4 * aef * anp - 2 * anf * aep - anf * anf - aep * aep)
            /
            ((aef + f + aep) * (anp + anf + p));
    }

    @Formula
    public static double ample(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return Math.abs(aef / f - aep / p);
    }

    @Formula
    public static double arithmeticMean(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double anp = record.getAnp();
        final double anf = record.getAnf();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return (2 * aef * anp - 2 * anf * aep)
            /
            ((aef + aep) * (anf + anp) + f * p);
    }

    @Formula
    public static double cohen(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double anp = record.getAnp();
        final double anf = record.getAnf();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return (2 * aef * anp - 2 * anf * aep)
            /
            ((aef + aep) * p + (anf + anp) * f);
    }

    @Formula
    public static double fleiss(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double anp = record.getAnp();
        final double anf = record.getAnf();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return (4 * aef * anp - 2 * anf * aep - anf * anf - aep * aep)
            /
            (2 * f + 2 * p);
    }

    @Formula
    public static double m1(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double anp = record.getAnp();
        final double f = resolveF(record);

        return (aef + anp) / (f - aef + aep);
    }

    @Formula
    public static double wong3(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();

        if (aep <= 2) {
            return aef - aep;
        } else if (aep <= 10) {
            return aef - (2 + 0.1 * (aep - 2));
        } else {
            return aef - (2.8 + 0.001 * (aep - 10));
        }
    }

    @Formula
    public static double naish2(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double p = resolveP(record);

        return aef - aep / (p + 1);
    }

    @Formula
    public static double dice(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);

        return 2 * aef / (f + aep);
    }

    @Formula
    public static double jaccard(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);

        return aef / (f + aep);
    }

    @Formula
    public static double qe(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();

        return aef / (aef + aep);
    }

    @Formula
    public static double euclid(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double anp = record.getAnp();
        final double anf = record.getAnf();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return Math.sqrt(aef + anp);
    }

    @Formula
    public static double hammingEtc(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double anp = record.getAnp();

        return aef + anp;
    }

    @Formula
    public static double wong2(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();

        return aef - aep;
    }

    @Formula
    public static double russelRao(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return aef / (f + p);
    }

    @Formula
    public static double ample2(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return aef / f - aep / p;
    }

    @Formula
    public static double kulcznski1(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);

        return aef / (f - aef + aep);
    }

    @Formula
    public static double kulcznski2(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);

        return aef / (2 * f) + aef / (2 * aef + 2 * aep);
    }

    @Formula
    public static double ochiai(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);

        return aef / Math.sqrt(f * (aef + aep));
    }

    @Formula
    public static double m2(VectorTableModelForStatement record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return aef / (2 * f - aef + p + aep);
    }

    // ===== meta programming utils ======================================================

    /**
     * 从表示公式的 {@link Method} 得到调用它的函数接口
     *
     * @param formula
     * @return
     */
    private static Function<VectorTableModelForStatement, Double> toFunctionInterface(Method formula) {
        final Function<VectorTableModelForStatement, Double> function = (VectorTableModelForStatement input) -> {
            try {
                return (double) formula.invoke(null, input);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw Lombok.sneakyThrow(e);
            }
        };
        return function;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Formula {
    }
}
