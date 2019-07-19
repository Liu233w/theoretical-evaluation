package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

import java.util.*;

public class SuspiciousnessFactorUtils {

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
     * 返回根据可疑因子排序过的流
     *
     * @param stream
     * @return
     */
    public static StreamEx<SuspiciousnessFactorForStatement> rankedStream(StreamEx<SuspiciousnessFactorForStatement> stream) {
        return stream.sorted(SuspiciousnessFactorUtils.getSuspiciousnessFactorComparator());
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
            .sortedBy(SuspiciousnessFactorForProgram::getProgramTitle)
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

    public static Comparator<SuspiciousnessFactorForStatement> getSuspiciousnessFactorComparator() {
        return (left, right) -> {
            double lf = left.getSuspiciousnessFactor();
            double rf = right.getSuspiciousnessFactor();
            if (Double.isNaN(lf)) {
                lf = Double.NEGATIVE_INFINITY;
            }
            if (Double.isNaN(rf)) {
                rf = Double.NEGATIVE_INFINITY;
            }
            return -Double.compare(lf, rf);
        };
    }

    public static List<SuspiciousnessFactorForStatement> removeNanSf(List<SuspiciousnessFactorForStatement> input) {
        return StreamEx.of(input)
            .filter(a -> !Double.isNaN(a.getSuspiciousnessFactor()))
            .toImmutableList();
    }
}
