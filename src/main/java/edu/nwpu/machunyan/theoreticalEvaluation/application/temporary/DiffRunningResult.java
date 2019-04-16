package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.StatementInfo;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 比较两个运行结果
 */
public class DiffRunningResult {

    private static final String left = "d:\\Temp\\theoretical-evaluation\\results\\run-results\\tot_info.json";
    private static final String right = "d:\\Sources\\project\\theoretical-evaluation\\target\\outputs\\totInfoRunningResult.json";

    public static void main(String[] args) throws FileNotFoundException {

        final RunResultForProgram[] leftList = FileUtils.loadObject(Paths.get(left), RunResultJam.class).getRunResultForPrograms();
        final RunResultForProgram[] rightList = FileUtils.loadObject(Paths.get(right), RunResultJam.class).getRunResultForPrograms();

        if (leftList.length != rightList.length) {
            throw new RuntimeException("diff size");
        }

        final Comparator<RunResultForProgram> titleCmp = Comparator.comparingInt(a -> Integer.parseInt(a.getProgramTitle().substring(1)));
        Arrays.sort(leftList, titleCmp);
        Arrays.sort(rightList, titleCmp);

        for (int i = 0; i < leftList.length; i++) {
            final RunResultForProgram l = leftList[i];
            final RunResultForProgram r = rightList[i];

            if (!l.getProgramTitle().equals(r.getProgramTitle())) {
                throw new RuntimeException("title not equal " + l.getProgramTitle() + ' ' + r.getProgramTitle());
            }

            final List<Integer> ls = l.getStatementMap().getMapList().stream().filter(Objects::nonNull).map(StatementInfo::getStartRow).collect(Collectors.toList());
            final List<Integer> rs = r.getStatementMap().getMapList().stream().filter(Objects::nonNull).map(StatementInfo::getStartRow).collect(Collectors.toList());
            if (!ls.equals(rs)) {
                throw new RuntimeException("statement map not equal: " + l.getProgramTitle());
            }

            if (l.getRunResults().length != r.getRunResults().length) {
                throw new RuntimeException("run result not equal: " + l.getProgramTitle());
            }

            for (int j = 0; j < l.getRunResults().length; j++) {
                final RunResultForTestcase ll = l.getRunResults()[j];
                final RunResultForTestcase rr = r.getRunResults()[j];

                if (ll.isCorrect() != rr.isCorrect()) {
                    System.out.println(ll);
                    System.out.println(rr);
                    throw new RuntimeException("run result not equal: " + l.getProgramTitle());
                }
            }
        }
    }
}
