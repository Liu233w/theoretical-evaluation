package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankForStatement;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.FaultLocationForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.FaultLocationJam;
import one.util.streamex.StreamEx;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * DiffRank 的筛选器，用来筛选 {@link DiffRankResolver} 获得的结果
 */
public class DiffRankFilters {

    /**
     * 筛选排名不相等的结果
     *
     * @return
     */
    public static BiPredicate<String, DiffRankForStatement> rankNotEqual() {
        return DiffRankFilters::RANK_NOT_EQUAL_FOR_PROGRAMS;
    }

    public static Predicate<DiffRankForStatement> rankNotEqualForStatements() {
        return DiffRankFilters::RANK_NOT_EQUAL_FOR_STATEMENTS;
    }

    /**
     * 只返回使用 faultLocation 中的语句编号定义的结果
     *
     * @param faultLocationForProgram
     * @return
     */
    public static Predicate<DiffRankForStatement> onlyInList(
        FaultLocationForProgram faultLocationForProgram) {

        final Set<Integer> locations = faultLocationForProgram.getLocations();
        return statement -> locations.contains(statement.getStatementIndex());
    }

    /**
     * 只返回使用 faultLocation 中的语句编号定义的结果
     *
     * @param jam
     * @return
     */
    public static BiPredicate<String, DiffRankForStatement> onlyInList(
        FaultLocationJam jam) {

        return onlyInList(jam, false);
    }

    /**
     * 只返回使用 faultLocation 中的语句编号定义的结果
     *
     * @param jam
     * @param excludeNotInEffectSize 默认为 false，如果是 true，
     *                               结果中将排除 usedInEffectSize 为 false 的版本。
     * @return
     */
    public static BiPredicate<String, DiffRankForStatement> onlyInList(
        FaultLocationJam jam,
        boolean excludeNotInEffectSize) {

        StreamEx<FaultLocationForProgram> steam = StreamEx
            .of(jam.getFaultLocationForPrograms());

        if (excludeNotInEffectSize) {
            steam = steam.filter(FaultLocationForProgram::isUsedInEffectSize);
        }

        final Map<String, Set<Integer>> programToLocationMap = steam
            .toMap(
                FaultLocationForProgram::getProgramTitle,
                FaultLocationForProgram::getLocations
            );

        return (programTitle, diffRankForStatement) -> programToLocationMap
            .getOrDefault(programTitle, Collections.emptySet())
            .contains(diffRankForStatement.getStatementIndex());
    }

    /**
     * 筛选排名不相等的结果
     *
     * @param programTitle
     * @param diffRankForStatement
     * @return
     */
    private static boolean RANK_NOT_EQUAL_FOR_PROGRAMS(
        String programTitle,
        DiffRankForStatement diffRankForStatement) {

        return diffRankForStatement.getRight().getRank()
            != diffRankForStatement.getLeft().getRank();
    }

    private static boolean RANK_NOT_EQUAL_FOR_STATEMENTS(
        DiffRankForStatement diffRankForStatement) {

        return diffRankForStatement.getRight().getRank()
            != diffRankForStatement.getLeft().getRank();
    }
}
