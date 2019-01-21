package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import lombok.NonNull;
import lombok.Value;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class SuspiciousnessFactorHelper {

    /**
     * 在vtm上运行每个 resolver，将结果收集到一起
     *
     * @param jam
     * @param resolvers
     * @return
     */
    public static SuspiciousnessFactorJam runOnAllResolvers(
        VectorTableModelJam jam,
        List<SuspiciousnessFactorResolver> resolvers) {

        final List<SuspiciousnessFactorForProgram> collect = StreamEx
            .of(resolvers)
            .map(resolver -> resolver.resolve(jam))
            .map(SuspiciousnessFactorJam::getResultForPrograms)
            .flatMap(Collection::stream)
            .toImmutableList();
        return new SuspiciousnessFactorJam(collect);
    }

    /**
     * 比较两个 suspiciousness factor 的区别。两个必须有同样的语句数量和编号。
     *
     * @param left
     * @param right
     * @return
     */
    public static List<DiffRankForStatement> diff(
        List<SuspiciousnessFactorForStatement> left,
        List<SuspiciousnessFactorForStatement> right) {
        return diff(left, right, null);
    }

    /**
     * 比较两个 suspiciousness factor 的区别。两个必须有同样的语句数量和编号。
     * 使用 {@link FaultLocationForProgram} 来筛选。
     * 如果语句不存在，返回的 {@link DiffRankForSide} 为 (-1, NaN)
     *
     * @param left
     * @param right
     * @param faultLocationForProgram 用来筛选语句。为 null 时回退到 {@link SuspiciousnessFactorHelper#diff(List, List)}
     * @return
     */
    public static List<DiffRankForStatement> diff(
        List<SuspiciousnessFactorForStatement> left,
        List<SuspiciousnessFactorForStatement> right,
        @Nullable FaultLocationForProgram faultLocationForProgram) {

        if (left.size() != right.size()) {
            throw new IllegalArgumentException("用于比较的两侧的语句数必须相同");
        }

        final Map<Integer, DiffRankForSide> leftMap = resolveIndexToRank(left);
        final Map<Integer, DiffRankForSide> rightMap = resolveIndexToRank(right);

        if (!leftMap.keySet().equals(rightMap.keySet())) {
            throw new IllegalArgumentException("左右两侧的语句编号不同");
        }

        // 为 null 时回退到普通情况
        if (faultLocationForProgram == null) {

            return StreamEx
                .of(leftMap.entrySet())
                .filter(a -> a.getValue().getRank() != rightMap.get(a.getKey()).getRank())
                .map(a -> new DiffRankForStatement(
                    a.getKey(),
                    a.getValue(),
                    rightMap.get(a.getKey())
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
     * 比较两侧的可疑因子排名，必须有同样的编号
     *
     * @param left
     * @param right
     * @param leftRankTitle  用来标记左侧的标签（比如 weighted）
     * @param rightRankTitle 用来标记右侧的标签（比如 weighted）
     * @return
     */
    public static DiffRankForProgram diff(
        SuspiciousnessFactorForProgram left,
        SuspiciousnessFactorForProgram right,
        String leftRankTitle,
        String rightRankTitle) {

        return new DiffRankForProgram(
            left.getProgramTitle(),
            left.getFormula(),
            diff(left.getResultForStatements(), right.getResultForStatements()),
            leftRankTitle,
            rightRankTitle);
    }

    /**
     * 比较两侧的可疑因子排名，必须有同样的编号
     *
     * @param left
     * @param right
     * @param leftRankTitle           用来标记左侧的标签（比如 weighted）
     * @param rightRankTitle          用来标记右侧的标签（比如 weighted）
     * @param faultLocationForProgram 用来表示要输出哪些语句
     * @return
     */
    public static DiffRankForProgram diff(
        SuspiciousnessFactorForProgram left,
        SuspiciousnessFactorForProgram right,
        String leftRankTitle,
        String rightRankTitle,
        FaultLocationForProgram faultLocationForProgram) {

        return new DiffRankForProgram(
            left.getProgramTitle(),
            left.getFormula(),
            diff(
                left.getResultForStatements(),
                right.getResultForStatements(),
                faultLocationForProgram
            ),
            leftRankTitle,
            rightRankTitle);
    }

    /**
     * 比较两侧的可疑因子排名，必须一一对应
     *
     * @param left
     * @param right
     * @param leftRankTitle  用来标记左侧的标签（比如 weighted）
     * @param rightRankTitle 用来标记右侧的标签（比如 weighted）
     * @return
     */
    public static DiffRankJam diff(
        SuspiciousnessFactorJam left,
        SuspiciousnessFactorJam right,
        String leftRankTitle,
        String rightRankTitle) {

        return diff(
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
    public static DiffRankJam diff(
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
            .map(a -> diff(
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
    public static Map<Integer, DiffRankForSide> resolveIndexToRank(
        List<SuspiciousnessFactorForStatement> side) {

        return rankedStream(StreamEx.of(side))
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

    /**
     * 返回根据可疑因子排序过的流
     *
     * @param stream
     * @return
     */
    public static StreamEx<SuspiciousnessFactorForStatement> rankedStream(StreamEx<SuspiciousnessFactorForStatement> stream) {
        return stream.sorted(
            Comparator.comparingDouble(
                SuspiciousnessFactorForStatement::getSuspiciousnessFactor)
                .reversed());
    }

    /**
     * 从 jam 中将不同的公式运行的结果提取出来
     *
     * @param jam
     * @return
     */
    public static MultipleFormulaSuspiciousnessFactorJam collectAsMultipleFormula(
        SuspiciousnessFactorJam jam) {

        final List<SuspiciousnessFactorForProgram> sfForPrograms = jam.getResultForPrograms();

        final List<MultipleFormulaSuspiciousnessFactorForProgram> collect = StreamEx
            .of(sfForPrograms)
            .groupRuns((l, r) -> l.getProgramTitle().equals(r.getProgramTitle()))
            .map(resultsForSameProgram -> new MultipleFormulaSuspiciousnessFactorForProgram(
                resultsForSameProgram.get(0).getProgramTitle(),
                resolveMultipleFormulaSf(resultsForSameProgram)
            ))
            .toImmutableList();

        final Set<String> formulaTitles = StreamEx.of(sfForPrograms)
            .map(SuspiciousnessFactorForProgram::getFormula)
            .toImmutableSet();

        return new MultipleFormulaSuspiciousnessFactorJam(collect, formulaTitles);
    }

    /**
     * 从一个 program 列表获取到本 program 的所有语句和公式的可疑因子。
     * 列表中的 program 必须有相同的 programTitle
     *
     * @param lstForProgram
     * @return
     */
    private static List<MultipleFormulaSuspiciousnessFactorForStatement> resolveMultipleFormulaSf(
        List<SuspiciousnessFactorForProgram> lstForProgram) {

        // resolve builder
        // 同一个程序得到的 vtm 是一样的。因此执行的语句也是一样的。
        final Map<Integer, Map<String, Double>> statementIdxToFormulaTitleToResult = StreamEx
            .of(lstForProgram.get(0).getResultForStatements())
            .map(SuspiciousnessFactorForStatement::getStatementIndex)
            .toMap(HashMap::new);

        // process builder
        lstForProgram.forEach(program ->
            program.getResultForStatements().forEach(statement -> {
                statementIdxToFormulaTitleToResult
                    .get(statement.getStatementIndex())
                    .put(program.getFormula(), statement.getSuspiciousnessFactor());
            }));

        // build
        return EntryStream
            .of(statementIdxToFormulaTitleToResult)
            .mapValues(map -> Collections.unmodifiableMap(new HashMap<>(map)))
            .sorted(Comparator.comparingInt(Map.Entry::getKey))
            .map(entry -> new MultipleFormulaSuspiciousnessFactorForStatement(
                entry.getKey(),
                entry.getValue()))
            .toImmutableList();
    }

    @Value
    private static class Key {
        String programTitle;
        String formulaTitle;
    }
}
