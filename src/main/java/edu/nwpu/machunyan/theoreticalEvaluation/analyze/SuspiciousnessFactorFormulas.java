package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

/**
 * 保存错误率指数的计算公式
 */
@SuppressWarnings("Duplicates")
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

    // === o ?
    public static double naish1(VectorTableModelRecord record) {
        final double f = resolveF(record);
        final double aef = record.getAef();
        final double anp = record.getAnp();

        if (aef < f) {
            return -1;
        } else {
            return anp;
        }
    }

    public static double andergerg(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double f = resolveF(record);
        final double aep = record.getAep();

        return aef / (2 * f - aef + 2 * aep);
    }

    public static double goodman(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double f = resolveF(record);
        final double aep = record.getAep();

        return (3 * aef - f - aep) / (aef + f + aep);
    }

    public static double sorensenDice(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double f = resolveF(record);
        final double aep = record.getAep();
        final double anf = record.getAnf();

        return 2 * aef / (2 * f - anf + aep);
    }

    public static double cbiInc(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return aef / (aef + aep) - f / (f + p);
    }

    public static double tarantula(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);

        return (aef / f) / (aef / f + aep / f);
    }

    public static double hamann(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double anp = record.getAnp();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return (2 * aef + 2 * anp) / (f + p) - 1;
    }

    public static double rogersTanimoto(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double anp = record.getAnp();
        final double anf = record.getAnf();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return (aef + anp) / (anf + aep + f + p);
    }

    public static double simpleMatching(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double anp = record.getAnp();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return (aef + anp) / (f + p);
    }

    public static double sokal(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double anp = record.getAnp();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return (2 * aef + 2 * anp) / (aef + anp + f + p);
    }

    public static double binary(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double f = resolveF(record);

        if (aef < f) {
            return 0;
        } else {
            return 1;
        }
    }

    public static double wong1(VectorTableModelRecord record) {
        return record.getAef();
    }

    public static double rogot1(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double anp = record.getAnp();
        final double anf = record.getAnf();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return (aef / 2) / (aef + f + aep) + (anp / 2) / (anf + anp + p);
    }

    public static double scott(VectorTableModelRecord record) {
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

    public static double ample(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return Math.abs(aef / f - aep / p);
    }

    public static double arithmeticMean(VectorTableModelRecord record) {
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

    public static double cohen(VectorTableModelRecord record) {
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

    public static double fleiss(VectorTableModelRecord record) {
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

    public static double m1(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double anp = record.getAnp();
        final double f = resolveF(record);

        return (aef + anp) / (f - aef + aep);
    }

    public static double wong3(VectorTableModelRecord record) {
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

    public static double naish2(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double p = resolveP(record);

        return aef - aep / (p + 1);
    }

    public static double dice(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);

        return 2 * aef / (f + aep);
    }

    public static double jaccard(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);

        return aef / (f + aep);
    }

    public static double qe(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double aep = record.getAep();

        return aef / (aef + aep);
    }

    public static double euclid(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double anp = record.getAnp();
        final double anf = record.getAnf();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return Math.sqrt(aef + anp);
    }

    public static double hammingEtc(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double anp = record.getAnp();

        return aef + anp;
    }

    public static double wong2(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double aep = record.getAep();

        return aef - aep;
    }

    public static double russelRao(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return aef / (f + p);
    }

    public static double ample2(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return aef / f - aep / p;
    }

    public static double kulcznski1(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);

        return aef / (f - aef + aep);
    }

    public static double kulcznski2(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);

        return aef / (2 * f) + aef / (2 * aef + 2 * aep);
    }

    public static double ochiai(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);

        return aef / Math.sqrt(f * (aef + aep));
    }

    public static double m2(VectorTableModelRecord record) {
        final double aef = record.getAef();
        final double aep = record.getAep();
        final double f = resolveF(record);
        final double p = resolveP(record);

        return aef / (2 * f - aef + p + aep);
    }
}
