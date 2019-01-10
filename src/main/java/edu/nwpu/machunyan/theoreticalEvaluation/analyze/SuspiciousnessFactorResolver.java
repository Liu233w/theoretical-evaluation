package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 统计一次运行中所有语句的错误率指数，忽略从来没有运行过的语句 (aep+aef==0)
 */
@Data
@Builder
@AllArgsConstructor
public class SuspiciousnessFactorResolver {

    /**
     * 是否对运行结果排序
     */
    @Builder.Default
    private boolean sort = false;

    /**
     * 输出时在公式位置显示的内容
     */
    @Builder.Default
    private String formulaTitle = "";

    /**
     * 用来计算可疑指数的公式
     */
    @NonNull
    private Function<VectorTableModelRecord, Double> formula;

    public SuspiciousnessFactorResolver(@NonNull Function<VectorTableModelRecord, Double> formula) {
        this.formula = formula;
        this.formulaTitle = "";
        this.sort = false;
    }

    /**
     * 从参数中生成一系列指定公式的 resolver
     *
     * @param map key 为公式名， value 为公式
     * @return
     */
    public static List<SuspiciousnessFactorResolver> of(Map<String, Function<VectorTableModelRecord, Double>> map) {
        return of(map, SuspiciousnessFactorResolver.builder());
    }

    /**
     * 从参数中生成一系列指定公式的 resolver
     *
     * @param map     key 为公式名， value 为公式
     * @param builder 提供一些默认参数
     * @return
     */
    public static List<SuspiciousnessFactorResolver> of(Map<String, Function<VectorTableModelRecord, Double>> map, SuspiciousnessFactorResolverBuilder builder) {
        return map.entrySet().stream()
            .map(entry -> builder
                .formulaTitle(entry.getKey())
                .formula(entry.getValue())
                .build()
            )
            .collect(Collectors.toList());
    }

    /**
     * 返回 {@link VectorTableModelRecord} 代表的语句是否执行过
     *
     * @param record
     * @return
     */
    private static boolean isStatementEvaluated(VectorTableModelRecord record) {
        return record.getUnWeightedAep() + record.getUnWeightedAef() > 0;
    }

    public List<SuspiciousnessFactorForStatement> resolve(List<VectorTableModelRecord> records) {

        Stream<SuspiciousnessFactorForStatement> stream = records.stream()
            .filter(Objects::nonNull)
            .filter(SuspiciousnessFactorResolver::isStatementEvaluated)
            .map(item -> new SuspiciousnessFactorForStatement(
                item.getStatementIndex(),
                formula.apply(item)
            ));

        if (sort) {
            stream = SuspiciousnessFactorHelper.rankedStream(stream);
        }
        return stream.collect(Collectors.toList());
    }

    public SuspiciousnessFactorJam resolve(VectorTableModelJam jam) {
        final List<SuspiciousnessFactorForProgram> collect = jam.getVectorTableModels().stream()
            .map(this::resolve)
            .collect(Collectors.toList());
        return new SuspiciousnessFactorJam(collect);
    }

    public SuspiciousnessFactorForProgram resolve(VectorTableModel vtm) {
        return new SuspiciousnessFactorForProgram(vtm.getProgramTitle(), formulaTitle, resolve(vtm.getRecords()));
    }
}

