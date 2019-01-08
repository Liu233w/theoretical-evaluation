package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.ExamScoreForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.ExamScoreForStatement;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorForStatement;

import java.util.Comparator;
import java.util.List;

/**
 * 使用 EXAM 公式计算语句的代价
 */
public class ExamScoreResolver {

    public static List<ExamScoreForStatement> resolve(List<SuspiciousnessFactorForStatement> sfs) {

        final int n = sfs.size();
        sfs.sort(Comparator
            .comparingDouble(SuspiciousnessFactorForStatement::getSuspiciousnessFactor)
            .reversed());
    }
}
