package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.application.ResolveDefects4jTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.ProgramDefination;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.Defects4jTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 检查测试用例有没有重复的
 */
public class CheckRedundantDefects4jTestcases {

    public static void main(String[] args) throws FileNotFoundException {

        for (String programName : ProgramDefination.DEFECTS4J_RUN_LIST) {

            final Map<Program, List<Defects4jTestcase>> resultFromFile = ResolveDefects4jTestcase.getResultFromFile(programName);

            for (Map.Entry<Program, List<Defects4jTestcase>> entry : resultFromFile.entrySet()) {
                final List<Defects4jTestcase> list = entry.getValue();
                final HashSet<Defects4jTestcase> set = new HashSet<>(list);

                final int diff = list.size() - set.size();
                if (diff != 0) {
                    LogUtils.logInfo(programName + " " + entry.getKey().getTitle() + " " + diff);
                }
            }
        }
    }
}
