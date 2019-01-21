package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.MultipleFormulaSuspiciousnessFactorForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.MultipleFormulaSuspiciousnessFactorForStatement;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.MultipleFormulaSuspiciousnessFactorJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import one.util.streamex.StreamEx;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class DiffMultipleFormulaSf {
    public static void main(String[] args) throws FileNotFoundException {

        final MultipleFormulaSuspiciousnessFactorJam left = FileUtils.loadObject(
            "./target/outputs/tot_info-suspiciousness-factors.json",
            MultipleFormulaSuspiciousnessFactorJam.class);
        final MultipleFormulaSuspiciousnessFactorJam right = FileUtils.loadObject(
            "./target/outputs/tot_info-suspiciousness-factors - 副本.json",
            MultipleFormulaSuspiciousnessFactorJam.class);

        if (!left.getAllFormulaTitle().equals(right.getAllFormulaTitle())) {
            System.out.println("formula title is different");
            return;
        }

        System.out.println("left to right");
        compare(left, right);
        System.out.println("right to left");
        compare(right, left);
        System.out.println("done");
    }

    private static void compare(
        MultipleFormulaSuspiciousnessFactorJam left,
        MultipleFormulaSuspiciousnessFactorJam right) {

        final Map<String, Map<Integer, Map<String, Double>>> programToStatementToFormulaToResult = StreamEx
            .of(left.getResultForPrograms())
            .mapToEntry(
                MultipleFormulaSuspiciousnessFactorForProgram::getProgramTitle,
                MultipleFormulaSuspiciousnessFactorForProgram::getResultForStatements
            )
            .mapValues(a -> StreamEx
                .of(a)
                .toMap(
                    MultipleFormulaSuspiciousnessFactorForStatement::getStatementIndex,
                    MultipleFormulaSuspiciousnessFactorForStatement::getFormulaTitleToResult
                )
            )
            .toMap();

        right.getResultForPrograms().forEach(program -> {
            program.getResultForStatements().forEach(statement -> {
                statement.getFormulaTitleToResult().forEach((formula, result) -> {


                    final Double aDouble = programToStatementToFormulaToResult
                        .getOrDefault(program.getProgramTitle(), new HashMap<>())
                        .getOrDefault(statement.getStatementIndex(), new HashMap<>())
                        .get(formula);

                    if (!result.equals(aDouble)) {

                        System.out.printf("%s %s %s: wrong - %s to %s%n",
                            program.getProgramTitle(), statement.getStatementIndex(),
                            formula, result, aDouble);
                    }
                });
            });
        });
    }
}
