package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 程序中的所有可疑指数
 */
@Data
@AllArgsConstructor
public class SuspiciousnessFactorForProgram {

    String programTitle;

    /**
     * 使用的可疑指数公式
     */
    String formula;

    /**
     * 所有执行的语句的结果。只有在所有用例中执行过的语句（aef+aep>0)才会出现在这里。
     */
    List<SuspiciousnessFactorItem> resultForStatements;
}
