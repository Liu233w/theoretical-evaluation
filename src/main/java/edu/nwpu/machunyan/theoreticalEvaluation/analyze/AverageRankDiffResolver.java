package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分析结果用。
 * <p>
 * 一个程序可能有多个错误位置。它们在加权前和加权之后的可疑因子排名可能是不同的。
 * 这里接收多条语句的可疑因子排名的变动值，算出这个程序整体的可疑因子变动值。
 */
public class AverageRankDiffResolver {

    // 这里直接求出错误位置的排名变动的平均值
    public static AverageDiffRankForProgram resolve(
        DiffRankForProgram diff,
        FaultLocationForProgram location) {

        final double average = diff.getDiffRankForStatements()
            .stream()
            .filter(item -> location.getLocations().contains(item.getStatementIndex()))
            .collect(Collectors.averagingDouble(DiffRankForStatement::getRankDiff));

        return new AverageDiffRankForProgram(
            diff.getProgramTitle(),
            diff.getFormulaTitle(),
            average,
            diff.getLeftRankTitle(),
            diff.getRightRankTitle()
        );
    }

    public static AverageDiffRankJam resolve(
        DiffRankJam jam,
        FaultLocationJam locations) {

        final List<AverageDiffRankForProgram> list = StreamEx.of(jam.getDiffRankForPrograms())
            .map(item -> {
                final FaultLocationForProgram location = locations.getFaultLocationForPrograms()
                    .stream()
                    .filter(a -> a.getProgramTitle().equals(item.getProgramTitle()))
                    .findAny()
                    .orElseThrow(() ->
                        new IllegalArgumentException("locations 中应当包含与 jam 的 programTitle 相同的条目。" + item.getProgramTitle()));

                return resolve(item, location);
            })
            .toImmutableList();

        return new AverageDiffRankJam(list);
    }
}
