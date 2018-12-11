package edu.nwpu.soft.ma.theoreticalEvaluation.utils;

public class ArrayUtils {

    /**
     * 使用 from 数组填充 dist 数组。从 dist[start] 开始按照顺序填充。
     * 不会检查数组大小是否合适。
     *
     * @param dist
     * @param from
     * @param start
     */
    public static void fillBy(Object[] dist, Object[] from, int start) {
        for (int i = 0; i < from.length; i++) {
            dist[i + start] = from[i];
        }
    }
}
