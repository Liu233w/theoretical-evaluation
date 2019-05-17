package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankForSide;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankForStatement;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankJam;
import lombok.Value;
import one.util.streamex.DoubleStreamEx;
import one.util.streamex.StreamEx;

import java.util.Set;
import java.util.function.Function;

/**
 * 分析两个排名的相关程度，用来分析结果好不好
 */
public class RankDiffAnalyzer {

    /**
     * 计算左右两边结果排名的效应量（效应值）。使用 左-右 的方法，也就是说，如果左边结果的平均排名低于右边，结果将是负数。
     * <p>
     * 如果使用左边表示加权（或者划分子集）之前的结果，右边表示之后的结果，则本方法的计算结果越大越好（正数也好于负数）
     *
     * @param jam 一个程序的所有版本在同一个公式下计算出来的排名比较（应该只保留和错误相关的行）
     * @return
     */
    public static double resolveEffectSizeCohensD(DiffRankJam jam) {

        final int formulaCount = StreamEx.of(jam.getDiffRankForPrograms())
            .map(DiffRankForProgram::getFormulaTitle)
            .toSetAndThen(Set::size);
        if (formulaCount > 1) {
            throw new IllegalArgumentException("There should be only one formula in jam");
        }

        /*
        https://www.atyun.com/23635.html

        计算两个样本的平均值之间的差如下：

        d = (u1 - u2) / s

        其中d是Cohen的d，u1是第一个样本的平均值，u2是第二个样本的平均值，s是两个样本的并合标准差。

        两个独立样本的并合标准差可以计算如下：
        s = sqrt(((n1 – 1) . s1^2 + (n2 – 1) . s2^2) / (n1 + n2 – 2))

        其中s是并合标准差，n1和n2是第一个样本和第二个样本的大小，s1 ^ 2和s2 ^ 2是第一个和第二个样本的方差。减法是对自由度数量的调整。
         */

        final double[] left = ranksForSide(jam, DiffRankForStatement::getLeft);
        final double[] right = ranksForSide(jam, DiffRankForStatement::getRight);

        final MeanVarianceDeviation leftRes = resolveMeanAndVarianceDeviation(left);
        final MeanVarianceDeviation rightRes = resolveMeanAndVarianceDeviation(right);

        final double s = Math.sqrt(
            ((left.length - 1) * leftRes.getVarianceDeviation() + (right.length - 1) * rightRes.getVarianceDeviation())
                /
                (left.length + right.length - 2)
        );

        return (leftRes.getMean() - rightRes.getMean()) / s;
    }

    /**
     * 获取一侧的所有排名
     *
     * @param jam
     * @param sideExtractor 用于表示是哪一侧
     * @return
     */
    private static double[] ranksForSide(
        DiffRankJam jam,
        Function<DiffRankForStatement, DiffRankForSide> sideExtractor) {

        return StreamEx
            .of(jam.getDiffRankForPrograms())
            .flatMap(a -> a.getDiffRankForStatements().stream())
            .map(sideExtractor)
            .mapToDouble(DiffRankForSide::getRank)
            .filter(a -> a != -1)
            .toArray();
    }

    /**
     * 从所有排名获取平均值和方差
     *
     * @param array
     * @return
     */
    private static MeanVarianceDeviation resolveMeanAndVarianceDeviation(double[] array) {

        assert array.length > 0 : "array not empty";

        final double mean = DoubleStreamEx
            .of(array)
            .average()
            .orElse(Double.NaN);

        final double vd = DoubleStreamEx
            .of(array)
            .map(a -> a - mean)
            .map(a -> a * a)
            .average()
            .orElse(Double.NaN);

        return new MeanVarianceDeviation(mean, vd);
    }

    @Value
    private static class MeanVarianceDeviation {
        double mean;
        double varianceDeviation;
    }
}
