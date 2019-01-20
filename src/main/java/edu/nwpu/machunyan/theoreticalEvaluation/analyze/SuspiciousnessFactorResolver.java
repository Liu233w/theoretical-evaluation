package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 统计一次运行中所有语句的可疑因子，忽略从来没有运行过的语句 (aep+aef==0)
 */
@Value
public class SuspiciousnessFactorResolver {

    /**
     * 是否对运行结果排序
     */
    private final boolean sort;

    /**
     * 输出时在公式位置显示的内容
     */
    private final String formulaTitle;

    /**
     * 用来计算可疑因子的公式
     */
    @NonNull
    private final Function<VectorTableModel.Pojo.ForStatement, Double> formula;

    public SuspiciousnessFactorResolver(@NonNull Function<VectorTableModel.Pojo.ForStatement, Double> formula) {
        this(false, null, formula);
    }

    @Builder
    public SuspiciousnessFactorResolver(boolean sort, String formulaTitle, @NonNull Function<VectorTableModel.Pojo.ForStatement, Double> formula) {

        // default is false
        this.sort = sort;
        if (formulaTitle == null) {
            this.formulaTitle = "";
        } else {
            this.formulaTitle = formulaTitle;
        }
        this.formula = formula;
    }

    /**
     * 从参数中生成一系列指定公式的 resolver
     *
     * @param map key 为公式名， value 为公式
     * @return
     */
    public static List<SuspiciousnessFactorResolver> of(Map<String, Function<VectorTableModel.Pojo.ForStatement, Double>> map) {
        return of(map, SuspiciousnessFactorResolver.builder());
    }

    /**
     * 从参数中生成一系列指定公式的 resolver
     *
     * @param map     key 为公式名， value 为公式
     * @param builder 提供一些默认参数
     * @return
     */
    public static List<SuspiciousnessFactorResolver> of(Map<String, Function<VectorTableModel.Pojo.ForStatement, Double>> map, SuspiciousnessFactorResolverBuilder builder) {
        return map.entrySet().stream()
            .map(entry -> builder
                .formulaTitle(entry.getKey())
                .formula(entry.getValue())
                .build()
            )
            .collect(Collectors.toList());
    }

    /**
     * 返回 {@link VectorTableModel.Pojo.ForStatement} 代表的语句是否执行过
     *
     * @param record
     * @return
     */
    private static boolean isStatementEvaluated(VectorTableModel.Pojo.ForStatement record) {
        return record.getUnWeightedAep() + record.getUnWeightedAef() > 0;
    }

    public List<SuspiciousnessFactorForStatement> resolve(List<VectorTableModel.Pojo.ForStatement> records) {

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

    public SuspiciousnessFactorJam resolve(VectorTableModel.Pojo.VectorTableModelJam jam) {
        final List<SuspiciousnessFactorForProgram> collect = jam.getVectorTableModelForPrograms().stream()
            .map(this::resolve)
            .collect(Collectors.toList());
        return new SuspiciousnessFactorJam(collect);
    }

    public SuspiciousnessFactorForProgram resolve(VectorTableModel.Pojo.VectorTableModelForProgram vtm) {
        return new SuspiciousnessFactorForProgram(vtm.getProgramTitle(), formulaTitle, resolve(vtm.getRecords()));
    }
}

