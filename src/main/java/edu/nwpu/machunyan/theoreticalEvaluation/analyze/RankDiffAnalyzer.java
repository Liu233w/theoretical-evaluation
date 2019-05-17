package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankForSide;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankForStatement;
import one.util.streamex.StreamEx;

import java.util.List;

/**
 * 分析两个排名的相关程度，用来分析结果好不好
 */
public class RankDiffAnalyzer {

    /**
     * 计算左右两边结果的排名方差
     *
     * @param diffRankForProgram
     * @return
     */
    public static double resolveVarianceDeviation(DiffRankForProgram diffRankForProgram) {

        // TODO: 如果有一边的语句没有出现（没有排名）怎么办？

        return StreamEx
            .of(diffRankForProgram.getDiffRankForStatements())
            // 现在暂时将这些语句排除
            .filter(a -> a.getLeft().getRank() != -1 && a.getRight().getRank() != -1)
            .mapToDouble(DiffRankForStatement::getRankDiff)
            .map(a -> a * a)
            .average()
            .orElseThrow(() -> new RuntimeException("Can't calculate the variance deviation."));
    }

    /**
     * 计算左右两边结果的排名标准差
     *
     * @param diffRankForProgram
     * @return
     */
    public static double resolveStandardDeviation(DiffRankForProgram diffRankForProgram) {
        return Math.sqrt(resolveVarianceDeviation(diffRankForProgram));
    }

    /**
     * 计算左右两边结果排名的效应量（效应值）。使用 左-右 的方法，也就是说，如果左边结果的平均排名低于右边，结果将是负数。
     * <p>
     * 如果使用左边表示加权（或者划分子集）之前的结果，右边表示之后的结果，则本方法的计算结果越大越好（正数也好于负数）
     *
     * @param diffRankForProgram
     * @return
     */
    public static double resolveEffectSize(DiffRankForProgram diffRankForProgram) {

        final List<DiffRankForStatement> list = StreamEx
            .of(diffRankForProgram.getDiffRankForStatements())
            // TODO: 同上，记得改这里
            .filter(a -> a.getLeft().getRank() != -1 && a.getRight().getRank() != -1)
            .toList();

        final double leftMean = StreamEx
            .of(list)
            .map(DiffRankForStatement::getLeft)
            .mapToDouble(DiffRankForSide::getRank)
            .average()
            .orElseThrow(() -> new RuntimeException("Can't calculate left side mean."));
        final double rightMean = StreamEx
            .of(list)
            .map(DiffRankForStatement::getRight)
            .mapToDouble(DiffRankForSide::getRank)
            .average()
            .orElseThrow(() -> new RuntimeException("Can't calculate right side mean."));

        return (leftMean - rightMean) / resolveStandardDeviation(diffRankForProgram);
    }
}
