package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.application.Run;
import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.ProgramDefination;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;

import java.io.FileNotFoundException;

/**
 * 找到 Defects4j 里错误的测试用例的数量
 */
public class FindNotCorrectTestcases {

    public static void main(String[] args) {

        for (String name : ProgramDefination.DEFECTS4J_RUN_LIST) {

            final RunResultJam resultFromFile;
            try {
                resultFromFile = Run.getResultFromFile(name);
            } catch (FileNotFoundException e) {
                return;
            }

            LogUtils.logInfo("program: " + name);
            for (RunResultForProgram runResultForProgram : resultFromFile.getRunResultForPrograms()) {

                final long count = runResultForProgram.getRunResults()
                    .stream()
                    .filter(a -> !a.isCorrect())
                    .count();

                LogUtils.logInfo(runResultForProgram.getProgramTitle() + ": " + count);
            }
        }
    }
}
