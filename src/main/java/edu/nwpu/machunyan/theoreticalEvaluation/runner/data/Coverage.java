package edu.nwpu.machunyan.theoreticalEvaluation.runner.data;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 表示一个测试用例的语句覆盖信息
 */
@EqualsAndHashCode
@ToString
public final class Coverage {

    /**
     * 1 语句编号
     * 2 执行次数；如果分析器不支持使用执行次数，将只为 0 或 1
     */
    private HashMap<Integer, Integer> map;

    /**
     * 默认构造函数，将所有语句初始化为 “没有执行”
     */
    public Coverage() {
        map = new HashMap<>();
    }

    public Coverage(Map<Integer, Integer> map) {
        this.map = new HashMap<>(map);
        zipData();
    }

    /**
     * 设置某条语句的覆盖信息
     *
     * @param statementIndex 语句编号
     * @param executionTimes 执行次数。如果分析器不支持使用执行次数，可以传入 1
     */
    public void setCoverageForStatement(int statementIndex, int executionTimes) {
        if (executionTimes != 0) {
            map.put(statementIndex, executionTimes);
        }
    }

    /**
     * 获取某一条语句的覆盖信息
     *
     * @param statementIndex 语句编号
     * @return 是否被执行过，执行过几次
     */
    public int getCoverageForStatement(int statementIndex) {
        return map.getOrDefault(statementIndex, 0);
    }

    /**
     * 移除所有值为 0 的 entry，用来压缩结果，生成体积更小的文件
     */
    public void zipData() {
        map.values().removeAll(Collections.singleton(0));
    }

    /**
     * 获取当前运行的所有覆盖信息，使用一个 ArrayList 表示。
     * 由于语句编号从 1 开始，因此 ArrayList 的 1 号位置表示 1 号语句的信息。
     * 0 号位置不含有意义的信息。
     *
     * @param statementNum 总的语句数量
     * @return 覆盖信息。长度为 statementNum+1
     */
    public ArrayList<Integer> getAllCoverageInformation(int statementNum) {

        final ArrayList<Integer> coverage = new ArrayList<>(statementNum + 1);

        coverage.add(0);
        for (int i = 1; i <= statementNum; i++) {
            coverage.add(getCoverageForStatement(i));
        }

        return coverage;
    }
}
