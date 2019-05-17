package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.NonNull;
import lombok.Value;

import java.util.OptionalInt;

/**
 * 表示一条语句在两种计算方式下排名的区别
 */
@Value
public class DiffRankForStatement {

    int statementIndex;

    @NonNull
    DiffRankForSide left;

    @NonNull
    DiffRankForSide right;

    /**
     * right 的 rank 比 left 高多少（可以是负数）
     *
     * @return
     */
    public OptionalInt getRankDiff() {
        if (left.getRank() == -1 || right.getRank() == -1) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(left.getRank() - right.getRank());
    }
}
