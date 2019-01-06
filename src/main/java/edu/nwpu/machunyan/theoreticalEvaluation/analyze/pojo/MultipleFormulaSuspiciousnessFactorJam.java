package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class MultipleFormulaSuspiciousnessFactorJam {

    List<MultipleFormulaSuspiciousnessFactorForProgram> resultForPrograms;

    /**
     * 出现的所有公式的标题，便于生成结果
     */
    Set<String> allFormulaTitle;
}
