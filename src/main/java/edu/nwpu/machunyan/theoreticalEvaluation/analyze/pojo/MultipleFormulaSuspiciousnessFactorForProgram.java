package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MultipleFormulaSuspiciousnessFactorForProgram {

    String programTitle;

    /**
     * 所有执行的语句的结果。只有在所有用例中执行过的语句（aef+aep>0)才会出现在这里。
     */
    List<MultipleFormulaSuspiciousnessFactorItem> resultForStatements;
}
