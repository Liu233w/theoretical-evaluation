package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import lombok.NonNull;
import lombok.Value;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 比较两个 Suspiciousness Factor，输出结果
 */
public class DiffRankResolver {
    /**
     * 比较两个 suspiciousness factor 的区别。两个必须有同样的语句数量和编号。
     *
     * @param left
     * @param right
     * @return
     */
    public static List<DiffRankForStatement> resolve(
        List<SuspiciousnessFactorForStatement> left,
        List<SuspiciousnessFactorForStatement> right) {
        return resolve(left, right, null);
    }

    /**
     * 比较两个 suspiciousness factor 的区别。
     * 使用 {@link FaultLocationForProgram} 来筛选。
     * 如果语句不存在，返回的 {@link DiffRankForSide} 为 (-1, NaN)
     *
     * @param left
     * @param right
     * @param faultLocationForProgram 用来筛选语句。为 null 时回退到比较所有的语句。
     * @return
     */
    public static List<DiffRankForStatement> resolve(
        List<SuspiciousnessFactorForStatement> left,
        List<SuspiciousnessFactorForStatement> right,
        @Nullable FaultLocationForProgram faultLocationForProgram) {

        final Map<Integer, DiffRankForSide> leftMap = resolveIndexToRank(left);
        final Map<Integer, DiffRankForSide> rightMap = resolveIndexToRank(right);

        // 为 null 时回退到普通情况
        if (faultLocationForProgram == null) {

            final HashSet<Integer> allIndex = new HashSet<>();
            allIndex.addAll(leftMap.keySet());
            allIndex.addAll(rightMap.keySet());

            final DiffRankForSide absentRank = new DiffRankForSide(-1, Double.NaN);

            return StreamEx
                .of(allIndex)
                .filter(a -> leftMap.getOrDefault(a, absentRank).getRank()
                    != rightMap.getOrDefault(a, absentRank).getRank())
                .map(a -> new DiffRankForStatement(
                    a,
                    leftMap.getOrDefault(a, absentRank),
                    rightMap.getOrDefault(a, absentRank)
                ))
                .toImmutableList();
        } else {

            return StreamEx
                .of(faultLocationForProgram.getLocations())
                .map(a -> new DiffRankForStatement(
                    a,
                    leftMap.getOrDefault(a, new DiffRankForSide(-1, Double.NaN)),
                    rightMap.getOrDefault(a, new DiffRankForSide(-1, Double.NaN))
                ))
                .toImmutableList();
        }
    }

    /**
     * 比较两侧的可疑因子排名
     *
     * @param left
     * @param right
     * @param leftRankTitle  用来标记左侧的标签（比如 weighted）
     * @param rightRankTitle 用来标记右侧的标签（比如 weighted）
     * @return
     */
    public static DiffRankForProgram resolve(
        SuspiciousnessFactorForProgram left,
        SuspiciousnessFactorForProgram right,
        String leftRankTitle,
        String rightRankTitle) {

        return new DiffRankForProgram(
            left.getProgramTitle(),
            left.getFormula(),
            resolve(left.getResultForStatements(), right.getResultForStatements()),
            leftRankTitle,
            rightRankTitle);
    }

    /**
     * 比较两侧的可疑因子排名
     *
     * @param left
     * @param right
     * @param leftRankTitle           用来标记左侧的标签（比如 weighted）
     * @param rightRankTitle          用来标记右侧的标签（比如 weighted）
     * @param faultLocationForProgram 用来表示要输出哪些语句
     * @return
     */
    public static DiffRankForProgram resolve(
        SuspiciousnessFactorForProgram left,
        SuspiciousnessFactorForProgram right,
        String leftRankTitle,
        String rightRankTitle,
        FaultLocationForProgram faultLocationForProgram) {

        return new DiffRankForProgram(
            left.getProgramTitle(),
            left.getFormula(),
            resolve(
                left.getResultForStatements(),
                right.getResultForStatements(),
                faultLocationForProgram
            ),
            leftRankTitle,
            rightRankTitle);
    }

    /**
     * 比较两侧的可疑因子排名
     *
     * @param left
     * @param right
     * @param leftRankTitle  用来标记左侧的标签（比如 weighted）
     * @param rightRankTitle 用来标记右侧的标签（比如 weighted）
     * @return
     */
    public static DiffRankJam resolve(
        SuspiciousnessFactorJam left,
        SuspiciousnessFactorJam right,
        String leftRankTitle,
        String rightRankTitle) {

        return resolve(
            left, right,
            leftRankTitle, rightRankTitle,
            new FaultLocationJam(Collections.emptyList()));
    }

    /**
     * 比较两侧的可疑因子排名，必须一一对应
     *
     * @param left
     * @param right
     * @param leftRankTitle    用来标记左侧的标签（比如 weighted）
     * @param rightRankTitle   用来标记右侧的标签（比如 weighted）
     * @param faultLocationJam 用来表示要在结果中包含的语句编号，
     *                         如果某个 programTitle 的对应 {@link FaultLocationForProgram} 不存在，回退到不使用它的情况
     * @return
     */
    public static DiffRankJam resolve(
        SuspiciousnessFactorJam left,
        SuspiciousnessFactorJam right,
        String leftRankTitle,
        String rightRankTitle,
        @NonNull FaultLocationJam faultLocationJam) {

        if (left.getResultForPrograms().size() != right.getResultForPrograms().size()) {
            throw new IllegalArgumentException("左右两侧必须一一对应");
        }

        final Map<Key, SuspiciousnessFactorForProgram> rightMapper = resolveMapper(right);
        final Map<Key, SuspiciousnessFactorForProgram> leftMapper = resolveMapper(left);

        if (!leftMapper.keySet().equals(rightMapper.keySet())) {
            throw new IllegalArgumentException("左右两侧必须一一对应");
        }

        final Map<String, FaultLocationForProgram> titleToLocationMap = StreamEx
            .of(faultLocationJam.getFaultLocationForPrograms())
            .toMap(
                FaultLocationForProgram::getProgramTitle,
                a -> a
            );

        final List<DiffRankForProgram> collect = StreamEx
            .of(leftMapper.entrySet())
            .map(a -> resolve(
                a.getValue(),
                rightMapper.get(a.getKey()),
                leftRankTitle,
                rightRankTitle,
                titleToLocationMap.get(a.getKey().getProgramTitle())
            ))
            .toImmutableList();
        return new DiffRankJam(collect);
    }

    private static Map<Key, SuspiciousnessFactorForProgram> resolveMapper(SuspiciousnessFactorJam jam) {
        return StreamEx
            .of(jam.getResultForPrograms())
            .toMap(
                a -> new Key(a.getProgramTitle(), a.getFormula()),
                a -> a
            );
    }

    /**
     * 获取一个可疑因子的语句和对应的排名
     *
     * @param side
     * @return key为语句编号，value为排名和可疑因子
     */
    private static Map<Integer, DiffRankForSide> resolveIndexToRank(
        List<SuspiciousnessFactorForStatement> side) {

        return SuspiciousnessFactorUtils.rankedStream(StreamEx.of(side))
            .mapToEntry(
                SuspiciousnessFactorForStatement::getStatementIndex,
                a -> a
            )
            .mapValues(
                new Function<SuspiciousnessFactorForStatement, DiffRankForSide>() {

                    int lastRank = 0;
                    double lastSf = Double.NaN;

                    @Override
                    public DiffRankForSide apply(SuspiciousnessFactorForStatement item) {

                        if (item.getSuspiciousnessFactor() != lastSf) {
                            ++lastRank;
                            lastSf = item.getSuspiciousnessFactor();
                        }
                        return new DiffRankForSide(lastRank, lastSf);
                    }
                }
            )
            .toImmutableMap();
    }

    @Value
    private static class Key {
        String programTitle;
        String formulaTitle;
    }
}
