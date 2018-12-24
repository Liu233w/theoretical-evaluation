package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 根据要求排序过的 vector table model，可以从中获得 I-set, II-set1, II-set2
 */
@Data
public class OrderedVectorTableModel {

    /**
     * 排序之后的 vector table model
     */
    private final ArrayList<VectorTableModelRecord> vectorTableModel;

    /**
     * II-set1 的开始位置，如果和 {@see OrderedVectorTableModel#getIIset2BeginPosition} 重合，代表不存在
     */
    private final int IIset1BeginPosition;

    /**
     * II-set2 的开始位置，如果为 {@see OrderedVectorTableModel#getVectorTableModel#size} ，表示不存在。
     */
    private final int IIset2BeginPosition;

    private OrderedVectorTableModel(ArrayList<VectorTableModelRecord> vectorTableModel, int iIset1Position, int iIset2Position) {
        this.vectorTableModel = vectorTableModel;
        IIset1BeginPosition = iIset1Position;
        IIset2BeginPosition = iIset2Position;
    }

    /**
     * 从 vtm 生成对象。不会改变参数中的 vtm。
     *
     * @param vectorTableModel
     * @return
     */
    public static OrderedVectorTableModel fromVectorTableModel(List<VectorTableModelRecord> vectorTableModel) {
        final ArrayList<VectorTableModelRecord> result = new ArrayList<>(vectorTableModel);
        Collections.sort(result);

        int i = 0;
        for (; i < result.size(); ++i) {
            if (result.get(i).getAnp() != 0) {
                break;
            }
        }
        final int IIset1Begin = i;

        for (; i < result.size(); ++i) {
            if (result.get(i).getAnf() != 0) {
                break;
            }
        }
        final int IIset2Begin = i;

        return new OrderedVectorTableModel(result, IIset1Begin, IIset2Begin);
    }
}
