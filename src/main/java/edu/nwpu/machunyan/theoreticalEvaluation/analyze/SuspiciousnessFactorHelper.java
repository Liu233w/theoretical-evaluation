package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import lombok.Value;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        final List<SuspiciousnessFactorForProgram> collect = resolvers.stream()
            .map(resolver -> resolver.resolve(jam))
            .map(SuspiciousnessFactorJam::getResultForPrograms)
            .map(Collection::stream)
            .reduce(Stream::concat)
            .orElseGet(Stream::empty)
            .collect(Collectors.toList());
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

        final LeftRightRankMap diffMap = checkParametersAndRank(left, right);
        final Map<Integer, DiffRankForSide> leftMap = diffMap.getLeft();
        final Map<Integer, DiffRankForSide> rightMap = diffMap.getRight();

        return leftMap.entrySet().stream()
            .filter(a -> a.getValue().getRank() != rightMap.get(a.getKey()).getRank())
            .map(a -> new DiffRankForStatement(
                a.getKey(),
                a.getValue(),
                rightMap.get(a.getKey())))
            .collect(Collectors.toList());
    }

    private static LeftRightRankMap checkParametersAndRank(
        List<SuspiciousnessFactorForStatement> left,
        List<SuspiciousnessFactorForStatement> right) {

        if (left.size() != right.size()) {
            throw new IllegalArgumentException("用于比较的两侧的语句数必须相同");
        }

        final Map<Integer, DiffRankForSide> leftMap = resolveIndexToRank(left);
        final Map<Integer, DiffRankForSide> rightMap = resolveIndexToRank(right);

        if (!leftMap.keySet().equals(rightMap.keySet())) {
            throw new IllegalArgumentException("左右两侧的语句编号不同");
        }

        return new LeftRightRankMap(leftMap, rightMap);
    }

    /**
     * 比较两个 suspiciousness factor 的区别。两个必须有同样的语句数量和编号。
     * 使用 {@link FaultLocationForProgram} 来筛选。
     * 如果语句不存在，返回的 {@link DiffRankForSide} 为 (-1, NaN)
     *
     * @param left
     * @param right
     * @return
     */
    public static List<DiffRankForStatement> diff(
        List<SuspiciousnessFactorForStatement> left,
        List<SuspiciousnessFactorForStatement> right,
        FaultLocationForProgram faultLocationForProgram
    ) {

        final LeftRightRankMap diffMap = checkParametersAndRank(left, right);
        final Map<Integer, DiffRankForSide> leftMap = diffMap.getLeft();
        final Map<Integer, DiffRankForSide> rightMap = diffMap.getRight();

        return faultLocationForProgram
            .getLocations()
            .stream()
            .map(a -> new DiffRankForStatement(
                a,
                leftMap.getOrDefault(a, new DiffRankForSide(-1, Double.NaN)),
                rightMap.getOrDefault(a, new DiffRankForSide(-1, Double.NaN))
            ))
            .collect(Collectors.toList());
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
                faultLocationForProgram),
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

        if (left.getResultForPrograms().size() != right.getResultForPrograms().size()) {
            throw new IllegalArgumentException("左右两侧必须一一对应");
        }

        final Map<Key, SuspiciousnessFactorForProgram> rightMapper = resolveMapper(right);
        final Map<Key, SuspiciousnessFactorForProgram> leftMapper = resolveMapper(left);

        if (!leftMapper.keySet().equals(rightMapper.keySet())) {
            throw new IllegalArgumentException("左右两侧必须一一对应");
        }

        final List<DiffRankForProgram> collect = leftMapper.entrySet().stream()
            .map(a -> diff(a.getValue(), rightMapper.get(a.getKey()), leftRankTitle, rightRankTitle))
            .collect(Collectors.toList());
        return new DiffRankJam(collect);
    }

    private static Map<Key, SuspiciousnessFactorForProgram> resolveMapper(SuspiciousnessFactorJam jam) {
        return jam.getResultForPrograms().stream()
            .collect(Collectors.toMap(
                a -> new Key(a.getProgramTitle(), a.getFormula()),
                a -> a
            ));
    }

    /**
     * 获取一个可疑因子的语句和对应的排名
     *
     * @param side
     * @return key为语句编号，value为排名和可疑因子
     */
    public static Map<Integer, DiffRankForSide> resolveIndexToRank(
        List<SuspiciousnessFactorForStatement> side) {

        final List<SuspiciousnessFactorForStatement> orderedSf = rankedStream(side.stream())
            .collect(Collectors.toList());

        final HashMap<Integer, DiffRankForSide> result = new HashMap<>();
        int lastRank = 0;
        double lastSf = Double.NaN;
        for (SuspiciousnessFactorForStatement item :
            orderedSf) {

            if (item.getSuspiciousnessFactor() != lastSf) {
                ++lastRank;
                lastSf = item.getSuspiciousnessFactor();
            }
            result.put(
                item.getStatementIndex(),
                new DiffRankForSide(lastRank, lastSf)
            );
        }
        return result;
    }

    /**
     * 返回根据可疑因子排序过的流
     *
     * @param stream
     * @return
     */
    public static Stream<SuspiciousnessFactorForStatement> rankedStream(Stream<SuspiciousnessFactorForStatement> stream) {
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

        final List<SuspiciousnessFactorForProgram> prevResult = jam.getResultForPrograms();

        final Set<String> programTitles = prevResult.stream()
            .map(SuspiciousnessFactorForProgram::getProgramTitle)
            .collect(Collectors.toSet());
        final Set<String> formulaTitles = prevResult.stream()
            .map(SuspiciousnessFactorForProgram::getFormula)
            .collect(Collectors.toSet());

        final List<MultipleFormulaSuspiciousnessFactorForProgram> collect = programTitles.stream()
            .map(programTitle -> {

                // 同一个程序得到的 vtm 是一样的。因此执行的语句也是一样的。
                @SuppressWarnings("OptionalGetWithoutIsPresent") final Map<Integer, MultipleFormulaSuspiciousnessFactorForStatement>
                    idxToItemMap = prevResult.stream()
                    .filter(a -> a.getProgramTitle().equals(programTitle))
                    .findAny()
                    .get()
                    .getResultForStatements()
                    .stream()
                    .map(SuspiciousnessFactorForStatement::getStatementIndex)
                    .collect(Collectors.toMap(
                        i -> i,
                        i -> new MultipleFormulaSuspiciousnessFactorForStatement(i, new HashMap<>())
                    ));

                prevResult.stream()
                    .filter(a -> a.getProgramTitle().equals(programTitle))
                    .forEach(sfForProgram -> {

                        sfForProgram
                            .getResultForStatements()
                            .forEach(statement -> {
                                idxToItemMap.get(statement.getStatementIndex())
                                    .getFormulaTitleToResult()
                                    .put(sfForProgram.getFormula(), statement.getSuspiciousnessFactor());
                            });
                    });

                final List<MultipleFormulaSuspiciousnessFactorForStatement> result = idxToItemMap.values().stream()
                    .sorted(Comparator.comparingInt(MultipleFormulaSuspiciousnessFactorForStatement::getStatementIndex))
                    .collect(Collectors.toList());
                return new MultipleFormulaSuspiciousnessFactorForProgram(programTitle, result);
            })
            .collect(Collectors.toList());

        return new MultipleFormulaSuspiciousnessFactorJam(collect, formulaTitles);
    }

    @Value
    private static class Key {
        String programTitle;
        String formulaTitle;
    }

    @Value
    private static class LeftRightRankMap {
        Map<Integer, DiffRankForSide> left;
        Map<Integer, DiffRankForSide> right;
    }
}
