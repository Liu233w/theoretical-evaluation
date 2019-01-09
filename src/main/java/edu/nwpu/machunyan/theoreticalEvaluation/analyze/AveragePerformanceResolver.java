package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorForStatement;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModel;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 从 {@link OrderedVectorTableModel} 中生成测试用例的平均代价
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
    public static double resolve(VectorTableModel vtm, Function<VectorTableModelRecord, Double> sfFormula) {
        return resolve(vtm.getRecords(), sfFormula);
    }

    /**
     * 获取 vtm 的 average performance。如果vtm中没有满足要求的语句（即所有执行过的语句都是 anf != 0的），返回 0
     *
     * @param vtmRecords
     * @param sfFormula  用来计算 SuspiciousnessFactor 的公式
     * @return
     */
    public static double resolve(List<VectorTableModelRecord> vtmRecords, Function<VectorTableModelRecord, Double> sfFormula) {

        final List<SuspiciousnessFactorForStatement> sfs = new SuspiciousnessFactorResolver(sfFormula)
            .resolve(vtmRecords);
        final Map<Integer, Double> examScore = resolveExamScore(sfs);

        return vtmRecords.stream()
            .filter(Objects::nonNull)
            .filter(AveragePerformanceResolver::isStatementNeeded)
//            .peek(System.out::println)
            .map(VectorTableModelRecord::getStatementIndex)
            .map(examScore::get)
            .mapToDouble(Double::doubleValue)
            .peek(System.out::println)
            .average()
            .orElse(0);
    }

    private static boolean isStatementNeeded(VectorTableModelRecord record) {
        return record.getUnWeightedAep() + record.getUnWeightedAef() > 0
            && record.getUnWeightedAnf() == 0;
    }

    /**
     * 使用 EXAM 公式计算语句的代价
     *
     * @param sfs
     * @return key 是语句编号， value 是结果
     */
    private static Map<Integer, Double> resolveExamScore(List<SuspiciousnessFactorForStatement> sfs) {

        final int n = sfs.size();

        // 目前的时间复杂度是 O(n^2)
        // TODO: 通过一趟排序将时间复杂度优化到 O(nlogn)
        return sfs.stream()
            .collect(Collectors.toMap(
                SuspiciousnessFactorForStatement::getStatementIndex,
                item -> {
                    final double g = (int) sfs.stream()
                        .filter(a -> a.getSuspiciousnessFactor() > item.getSuspiciousnessFactor())
                        .count();
                    final double e = (int) sfs.stream()
                        .filter(a -> a.getSuspiciousnessFactor() == item.getSuspiciousnessFactor())
                        .count() - 1; // 去掉本身
                    return (g + e / 2) / n;
                }
            ));
    }
}
