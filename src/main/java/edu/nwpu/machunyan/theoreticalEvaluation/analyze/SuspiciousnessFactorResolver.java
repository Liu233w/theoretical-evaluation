package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 统计一次运行中所有语句的可疑因子，忽略从来没有运行过的语句 (aep+aef==0)
 */
@Data
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
    private final SuspiciousnessFactorFormula formula;

    public SuspiciousnessFactorResolver(@NonNull SuspiciousnessFactorFormula formula) {
        this(false, null, formula);
    }

    @Builder
    public SuspiciousnessFactorResolver(boolean sort, String formulaTitle, @NonNull SuspiciousnessFactorFormula formula) {

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
    public static List<SuspiciousnessFactorResolver> of(Map<String, SuspiciousnessFactorFormula> map) {
        return of(map, SuspiciousnessFactorResolver.builder());
    }

    /**
     * 从参数中生成一系列指定公式的 resolver
     *
     * @param map     key 为公式名， value 为公式
     * @param builder 提供一些默认参数
     * @return
     */
    public static List<SuspiciousnessFactorResolver> of(Map<String, SuspiciousnessFactorFormula> map, SuspiciousnessFactorResolverBuilder builder) {
        return StreamEx
            .of(map.entrySet())
            .map(entry -> builder
                .formulaTitle(entry.getKey())
                .formula(entry.getValue())
                .build()
            )
            .toImmutableList();
    }

    /**
     * 返回 {@link VectorTableModelForStatement} 代表的语句是否执行过
     *
     * @param record
     * @return
     */
    private static boolean isStatementEvaluated(VectorTableModelForStatement record) {
        return record.getUnWeightedAep() + record.getUnWeightedAef() > 0;
    }

    public List<SuspiciousnessFactorForStatement> resolve(List<VectorTableModelForStatement> records) {

        StreamEx<SuspiciousnessFactorForStatement> stream = StreamEx
            .of(records)
            .filter(Objects::nonNull)
            .filter(SuspiciousnessFactorResolver::isStatementEvaluated)
            .map(item -> new SuspiciousnessFactorForStatement(
                item.getStatementIndex(),
                formula.apply(item)
            ));

        if (sort) {
            stream = SuspiciousnessFactorHelper.rankedStream(stream);
        }
        return stream.toImmutableList();
    }

    public SuspiciousnessFactorJam resolve(VectorTableModelJam jam) {
        final List<SuspiciousnessFactorForProgram> collect = StreamEx
            .of(jam.getVectorTableModelForPrograms())
            .map(this::resolve)
            .toImmutableList();
        return new SuspiciousnessFactorJam(collect);
    }

    public SuspiciousnessFactorForProgram resolve(VectorTableModelForProgram vtm) {
        return new SuspiciousnessFactorForProgram(vtm.getProgramTitle(), formulaTitle, resolve(vtm.getRecords()));
    }
}

