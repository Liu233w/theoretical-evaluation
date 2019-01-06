package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 统计一次运行中所有语句的错误率指数
 */
@Data
@Builder
@AllArgsConstructor
public class SuspiciousnessFactorResolver {

    /**
     * 是否对运行结果排序
     */
    private boolean sort = false;

    /**
     * 输出时在公式位置显示的内容
     */
    private String formulaTitle = "";

    /**
     * 用来计算可疑指数的公式
     */
    @NotNull
    private Function<VectorTableModelRecord, Double> formula = SuspiciousnessFactorFormulas::o;

    public SuspiciousnessFactorResolver(@NotNull Function<VectorTableModelRecord, Double> formula) {
        this.formula = formula;
    }

    public List<SuspiciousnessFactorItem> resolve(List<VectorTableModelRecord> records) {

        final List<SuspiciousnessFactorItem> result = records.stream()
            .filter(Objects::nonNull)
            .map(item -> new SuspiciousnessFactorItem(
                item.getStatementIndex(),
                formula.apply(item)
            ))
            .collect(Collectors.toList());

        if (sort) {
            result.sort((l, r) -> -Double.compare(l.getSuspiciousnessFactor(), r.getSuspiciousnessFactor()));
        }
        return result;
    }

    public SuspiciousnessFactorJam resolve(VectorTableModelJam jam) {
        final List<SuspiciousnessFactor> collect = jam.getVectorTableModels().stream()
            .map(this::resolve)
            .collect(Collectors.toList());
        return new SuspiciousnessFactorJam(collect);
    }

    public SuspiciousnessFactor resolve(VectorTableModel vtm) {
        return new SuspiciousnessFactor(vtm.getProgramTitle(), formulaTitle, resolve(vtm.getRecords()));
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
}

