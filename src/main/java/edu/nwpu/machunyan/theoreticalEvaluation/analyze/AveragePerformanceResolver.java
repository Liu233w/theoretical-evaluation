package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import java.util.ArrayList;

/**
 * 从 {@link OrderedVectorTableModel} 中生成测试用例的平均代价
 * <p>
 * 是故障定位付出的代价测量，越小越好。
 */
public class AveragePerformanceResolver {

    /**
     * 从 vtm 中生成 II-set1 分区信息，包含每个分区的长度（只有长度大于1才被称为一个分区）
     *
     * @param vtm
     * @return
     */
    public static ArrayList<Integer> resolvePartition(OrderedVectorTableModel vtm) {
        final ArrayList<Integer> result = new ArrayList<>();

        double lastMatchedAnp = -1;
        int partitionDegree = 0;
        for (int i = vtm.getIIset1BeginPosition(); i < vtm.getIIset2BeginPosition(); i++) {
            final double thisAnp = vtm.getVectorTableModel().get(i).getAnp();
            if (thisAnp == lastMatchedAnp) {
                ++partitionDegree;
            } else {
                lastMatchedAnp = thisAnp;
                if (partitionDegree > 1) {
                    result.add(partitionDegree);
                }
                partitionDegree = 1;
            }
        }

        // 输出最后一个分区
        if (partitionDegree > 1) {
            result.add(partitionDegree);
        }

        return result;
    }

    public static double resolveAveragePerformance(OrderedVectorTableModel vtm) {

        final ArrayList<Integer> partitions = resolvePartition(vtm);
        final double x = vtm.getIIset1BeginPosition();
        final double z = vtm.getIIset2BeginPosition() - x;
        final double n = vtm.getVectorTableModel().size();

        int sumPartitions = partitions.stream().reduce(0, Integer::sum);

        final double result = (Math.pow(x + z, 2) - z + sumPartitions)
                /
                (2 * n * (x + z));

        return result;
    }
}
