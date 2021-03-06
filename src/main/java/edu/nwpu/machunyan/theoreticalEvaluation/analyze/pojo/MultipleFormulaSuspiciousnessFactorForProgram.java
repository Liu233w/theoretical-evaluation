package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.Value;

import java.util.List;

@Value
public class MultipleFormulaSuspiciousnessFactorForProgram {

    String programTitle;

    /**
     * 所有执行的语句的结果。只有在所有用例中执行过的语句（aef+aep>0)才会出现在这里。
     */
    List<MultipleFormulaSuspiciousnessFactorForStatement> resultForStatements;
}
