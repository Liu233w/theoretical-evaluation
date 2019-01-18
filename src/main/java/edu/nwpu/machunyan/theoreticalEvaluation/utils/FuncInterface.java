package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import java.util.function.*;

public class FuncInterface {

    /**
     * helper 函数，方便将一个 lambda 转换成指定的函数
     *
     * @param func
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> Function<T, R> of(Function<T, R> func) {
        return func;
    }

    public static <T, U, R> BiFunction<T, U, R> of(BiFunction<T, U, R> func) {
        return func;
    }

    public static <T> Consumer<T> toConsumer(Consumer<T> func) {
        return func;
    }

    public static <T, U> BiConsumer<T, U> toConsumer(BiConsumer<T, U> func) {
        return func;
    }

    public static <T> Supplier<T> of(Supplier<T> func) {
        return func;
    }

    public static <T, R> BiPredicate<T, R> toPredicate(BiPredicate<T, R> func) {
        return func;
    }

    public static <T> Predicate<T> toPredicate(Predicate<T> func) {
        return func;
    }
}
