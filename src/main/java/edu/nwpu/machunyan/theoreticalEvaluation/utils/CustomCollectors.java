package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import java.util.Optional;
import java.util.stream.Collector;

public class CustomCollectors {

    /**
     * 使用指定的 collector 收集结果，包装成一个 {@link Optional#of(Object)}，便于链式调用
     *
     * @param downstream
     * @param <T>
     * @param <A>
     * @param <R>
     * @return
     */
    public static <T, A, R> Collector<T, A, Optional<R>> collectToOptional(Collector<T, A, R> downstream) {
        return Collector.of(
            downstream.supplier(),
            downstream.accumulator(),
            downstream.combiner(),
            res -> Optional.of(downstream.finisher().apply(res))
        );
    }
}
