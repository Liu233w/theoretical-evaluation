package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.application.ResolveDefects4jTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.ProgramDefination;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.Defects4jTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 检查测试用例有没有重复的
 */
public class CheckRedundantDefects4jTestcases {

    public static void main(String[] args) throws FileNotFoundException {

        /*
         * Chart 有几个程序会有固定的重复的测试用例，这个先不管了
         */

        for (String programName : ProgramDefination.DEFECTS4J_RUN_LIST) {

            final Map<Program, List<Defects4jTestcase>> resultFromFile = ResolveDefects4jTestcase.getResultFromFile(programName);

            for (Map.Entry<Program, List<Defects4jTestcase>> entry : resultFromFile.entrySet()) {
                final List<Defects4jTestcase> list = entry.getValue();

                final HashMap<Defects4jTestcase, Integer> map = new HashMap<>();
                for (Defects4jTestcase testcase : list) {
                    final Integer old = map.computeIfAbsent(testcase, t -> 0);
                    map.put(testcase, old + 1);
                }

                final int diff = list.size() - map.size();
                if (diff != 0) {
                    LogUtils.logInfo(programName + " " + entry.getKey().getTitle() + " " + diff);
                    map.forEach((t, i) -> {
                        if (i > 1) {
                            LogUtils.logInfo(t.toString());
                        }
                    });
                }
            }
        }
    }
}
