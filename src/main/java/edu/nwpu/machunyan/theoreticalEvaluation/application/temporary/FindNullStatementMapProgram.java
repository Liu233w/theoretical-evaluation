package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.application.Run;
import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.ProgramDefination;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;

import java.io.FileNotFoundException;

public class FindNullStatementMapProgram {

    public static void main(String[] args) {

        for (String programName : ProgramDefination.PROGRAM_LIST) {

            final RunResultJam resultJam;
            try {
                resultJam = Run.getResultFromFile(programName);
            } catch (FileNotFoundException e) {
                continue;
            }

            LogUtils.logInfo("Working on " + programName);

            for (int i = 0; i < resultJam.getRunResultForPrograms().size(); i++) {
                final RunResultForProgram runResultForProgram = resultJam.getRunResultForPrograms().get(i);
                if (runResultForProgram.getStatementMap() == null) {
                    LogUtils.logInfo("index: " + i + ", name: " + runResultForProgram.getProgramTitle());
                }
            }
        }
    }
}
