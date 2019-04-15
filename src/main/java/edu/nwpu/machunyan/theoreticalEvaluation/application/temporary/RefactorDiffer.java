package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.AveragePerformanceResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModelResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModelForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.application.Run;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;

import java.io.FileNotFoundException;

/**
 * 比较重构之前和之后的运行结果，查看有什么区别
 */
public class RefactorDiffer {

    public static void main(String[] args) throws FileNotFoundException {

        final RunResultJam runResult = Run.getResultFromFile("tot_info");

        for (RunResultForProgram item :
            runResult.getRunResultForPrograms()) {

            final VectorTableModelForProgram vtm = VectorTableModelResolver.resolve(item);

            final double newAp = AveragePerformanceResolver.resolve(vtm, SuspiciousnessFactorFormulas::ample);
            final double oldAp = OldAveragePerformanceResolver.resolve(vtm, SuspiciousnessFactorFormulas::ample);

            if (Math.abs(newAp - oldAp) > 0.0000001) {
                throw new RuntimeException("title: " + item.getProgramTitle() + " new: " + newAp + " old: " + oldAp);
            }
        }
    }
}

