package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModelRecord;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 一个 vtm
 */
@Data
@AllArgsConstructor
public class VectorTableModel {

    String programTitle;

    /**
     * 第一个元素为 null，剩下的元素每一个和 vtm 中的一行一一对应
     */
    List<VectorTableModelRecord> records;
}
