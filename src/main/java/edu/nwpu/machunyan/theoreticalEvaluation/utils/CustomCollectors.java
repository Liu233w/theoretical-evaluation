package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import java.util.stream.Collector;
import java.util.stream.Stream;

public class CustomCollectors {

    public static <T, A, R> Collector<T, A, Stream<R>> collectToStream(Collector<T, A, R> downstream) {
        return Collector.of(
            downstream.supplier(),
            downstream.accumulator(),
            downstream.combiner(),
            res -> Stream.of(downstream.finisher().apply(res))
        );
    }
}
