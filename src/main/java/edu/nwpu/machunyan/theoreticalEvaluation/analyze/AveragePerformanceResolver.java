package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorForStatement;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModelForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModelForStatement;

import java.util.*;

/**
 * 从 {@link VectorTableModelForProgram} 中生成测试用例的平均代价
 * <p>
 * 是故障定位付出的代价测量，越小越好。
 */
public class AveragePerformanceResolver {

    /**
     * 获取 vtm 的 average performance。如果vtm中没有满足要求的语句（即所有执行过的语句都是 anf != 0的），返回 0
     *
     * @param vtm
     * @param sfFormula 用来计算 SuspiciousnessFactor 的公式
     * @return
     */
    public static double resolve(VectorTableModelForProgram vtm, SuspiciousnessFactorFormula sfFormula) {
        return resolve(vtm.getRecords(), sfFormula);
    }

    /**
     * 获取 vtm 的 average performance。如果vtm中没有满足要求的语句（即所有执行过的语句都是 anf != 0的），返回 0
     *
     * @param vtmRecords
     * @param sfFormula  用来计算 SuspiciousnessFactor 的公式
     * @return
     */
    public static double resolve(
        List<VectorTableModelForStatement> vtmRecords,
        SuspiciousnessFactorFormula sfFormula) {

        return resolve(
            vtmRecords,
            SuspiciousnessFactorResolver
                .builder()
                .formula(sfFormula)
                .sort(true)
                .build());
    }

    public static double resolve(
        List<VectorTableModelForStatement> vtmRecords,
        SuspiciousnessFactorResolver sfResolver) {

        final List<SuspiciousnessFactorForStatement> sfs = sfResolver.resolve(vtmRecords);
        final Map<Integer, Double> examScore = resolveExamScore(sfs, sfResolver.isSort());

        return vtmRecords.stream()
            .filter(Objects::nonNull)
            .filter(AveragePerformanceResolver::isStatementNeeded)
            .map(VectorTableModelForStatement::getStatementIndex)
            .map(examScore::get)
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0);
    }

    private static boolean isStatementNeeded(VectorTableModelForStatement record) {
        return record.getUnWeightedAep() + record.getUnWeightedAef() > 0
            && record.getUnWeightedAnf() == 0;
    }

    /**
     * 使用 EXAM 公式计算语句的代价
     *
     * @param sfs
     * @return key 是语句编号， value 是结果
     */
    private static Map<Integer, Double> resolveExamScore(List<SuspiciousnessFactorForStatement> sfs, boolean inputSorted) {

        final int n = sfs.size();

        final List<SuspiciousnessFactorForStatement> sortedInput;
        if (inputSorted) {
            sortedInput = sfs;
        } else {
            sortedInput = new ArrayList<>(sfs);
            sortedInput.sort(Comparator.comparingDouble(
                SuspiciousnessFactorForStatement::getSuspiciousnessFactor)
                .reversed());
        }

        final HashMap<Integer, Double> result = new HashMap<>();

        // 当前元素的开始位置（sf一样的元素）
        int rankBeginIdx = 0;
        // 当前元素的截止位置的下一个元素的位置
        int rankEndNextIdx = 0;

        while (rankEndNextIdx < sortedInput.size()) {

            if (sortedInput.get(rankBeginIdx).getSuspiciousnessFactor() ==
                sortedInput.get(rankEndNextIdx).getSuspiciousnessFactor()) {

                ++rankEndNextIdx;
            } else {
                resolveAndFillResult(sortedInput, rankBeginIdx, rankEndNextIdx, result, n);
                rankBeginIdx = rankEndNextIdx;
                ++rankEndNextIdx;
            }
        }

        resolveAndFillResult(sortedInput, rankBeginIdx, rankEndNextIdx, result, n);
        return result;
    }

    /**
     * 用来减少代码重复的函数，和 {@link AveragePerformanceResolver#resolveExamScore(List, boolean)} )} 一起看
     *
     * @param rankBeginIdx
     * @param rankEndNextIdx
     * @param result
     */
    private static void resolveAndFillResult(
        List<SuspiciousnessFactorForStatement> sortedInput,
        int rankBeginIdx,
        int rankEndNextIdx,
        Map<Integer, Double> result,
        int n) {

        //noinspection UnnecessaryLocalVariable
        final double g = rankBeginIdx;
        final int thisRankCount = rankEndNextIdx - rankBeginIdx;
        // 如果没有和它相同的， e=0，否则 e=thisRankCount
        final double e = thisRankCount > 1 ? thisRankCount : 0;
        final double ap = (g + e / 2) / n;

        for (int i = rankBeginIdx; i < rankEndNextIdx; i++) {
            result.put(sortedInput.get(i).getStatementIndex(), ap);
        }
    }
}
