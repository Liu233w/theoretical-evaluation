package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import lombok.NonNull;
import lombok.Value;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * 比较两个 Suspiciousness Factor，输出结果
 */
public class DiffRankResolver {

    /**
     * 比较两个 suspiciousness factor 的区别。
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
     * 如果语句不存在，返回的 {@link DiffRankForSide} 为 (-1, NaN)
     *
     * @param left
     * @param right
     * @param resultFilterForProgram 用来筛选语句。为 null 时返回所有的语句。
     *                               参数表示计算结果，返回值表示最终是否包含此结果
     * @return
     */
    public static List<DiffRankForStatement> resolve(
        List<SuspiciousnessFactorForStatement> left,
        List<SuspiciousnessFactorForStatement> right,
        @Nullable Predicate<DiffRankForStatement> resultFilterForProgram) {

        final Map<Integer, DiffRankForSide> leftMap = resolveIndexToRank(left);
        final Map<Integer, DiffRankForSide> rightMap = resolveIndexToRank(right);

        final DiffRankForSide absentRank = new DiffRankForSide(-1, Double.NaN);

        final HashSet<Integer> allIndex = new HashSet<>();
        allIndex.addAll(leftMap.keySet());
        allIndex.addAll(rightMap.keySet());

        StreamEx<DiffRankForStatement> stream = StreamEx
            .of(allIndex)
            .map(a -> new DiffRankForStatement(
                a,
                leftMap.getOrDefault(a, absentRank),
                rightMap.getOrDefault(a, absentRank)
            ));

        if (resultFilterForProgram != null) {
            stream = stream.filter(resultFilterForProgram);
        }

        return stream.toImmutableList();
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
     * @param leftRankTitle          用来标记左侧的标签（比如 weighted）
     * @param rightRankTitle         用来标记右侧的标签（比如 weighted）
     * @param resultFilterForProgram 用来表示要输出哪些结果
     * @return
     */
    public static DiffRankForProgram resolve(
        SuspiciousnessFactorForProgram left,
        SuspiciousnessFactorForProgram right,
        String leftRankTitle,
        String rightRankTitle,
        Predicate<DiffRankForStatement> resultFilterForProgram) {

        return new DiffRankForProgram(
            left.getProgramTitle(),
            left.getFormula(),
            resolve(
                left.getResultForStatements(),
                right.getResultForStatements(),
                resultFilterForProgram
            ),
            leftRankTitle,
            rightRankTitle);
    }

    /**
     * 比较两侧的可疑因子排名，返回所有的比较结果（不进行筛选）
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
            // 保留所有结果，不进行筛选
            (a, b) -> true);
    }

    /**
     * 比较两侧的可疑因子排名，必须一一对应
     *
     * @param left
     * @param right
     * @param leftRankTitle      用来标记左侧的标签（比如 weighted）
     * @param rightRankTitle     用来标记右侧的标签（比如 weighted）
     * @param resultFilterForJam 用来筛选得到的结果。
     *                           第一个参数是版本名称，第二个参数是生成的 diff 结果。
     *                           如果返回值是 true，表示保留这个结果。
     * @return
     */
    public static DiffRankJam resolve(
        SuspiciousnessFactorJam left,
        SuspiciousnessFactorJam right,
        String leftRankTitle,
        String rightRankTitle,
        @NonNull BiPredicate<String, DiffRankForStatement> resultFilterForJam) {

        if (left.getResultForPrograms().size() != right.getResultForPrograms().size()) {
            throw new IllegalArgumentException("左右两侧必须一一对应");
        }

        final Map<Key, SuspiciousnessFactorForProgram> rightMapper = resolveMapper(right);
        final Map<Key, SuspiciousnessFactorForProgram> leftMapper = resolveMapper(left);

        if (!leftMapper.keySet().equals(rightMapper.keySet())) {
            throw new IllegalArgumentException("左右两侧必须一一对应");
        }

        final List<DiffRankForProgram> collect = StreamEx
            .of(leftMapper.entrySet())
            .map(a -> resolve(
                a.getValue(),
                rightMapper.get(a.getKey()),
                leftRankTitle,
                rightRankTitle,
                diffRankForStatement -> resultFilterForJam.test(a.getKey().getProgramTitle(), diffRankForStatement)
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

        if (side.size() == 0) {
            return Collections.emptyMap();
        }

        final SuspiciousnessFactorForStatement[] sfs
            = SuspiciousnessFactorUtils.rankedStream(StreamEx.of(side))
            .toArray(SuspiciousnessFactorForStatement[]::new);

        final HashMap<Integer, DiffRankForSide> res = new HashMap<>();

        int lastRank = 1;
        double lastSf = sfs[0].getSuspiciousnessFactor();
        res.put(sfs[0].getStatementIndex(), new DiffRankForSide(lastRank, lastSf));

        for (int i = 0; i < sfs.length; i++) {

            if (sfs[i].getSuspiciousnessFactor() != lastSf) {
                lastRank = i + 1;
                lastSf = sfs[i].getSuspiciousnessFactor();
            }
            res.put(sfs[i].getStatementIndex(), new DiffRankForSide(lastRank, lastSf));
        }

        return Collections.unmodifiableMap(res);
    }

    @Value
    private static class Key {
        String programTitle;
        String formulaTitle;
    }
}
