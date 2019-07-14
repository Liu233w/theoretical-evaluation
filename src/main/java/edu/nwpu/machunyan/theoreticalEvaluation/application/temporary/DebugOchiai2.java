package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.application.Run;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import one.util.streamex.EntryStream;
import org.apache.commons.lang.SystemUtils;

import java.io.FileNotFoundException;

public class DebugOchiai2 {
    public static void main(String[] args) throws FileNotFoundException {

        final RunResultJam chartRes = Run.getResultFromFile("Chart");
        final RunResultForProgram res9B = chartRes.getRunResultForPrograms().stream()
            .filter(a -> a.getProgramTitle().equals("9b"))
            .findAny()
            .get();
        final int[] idxArray = EntryStream.of(res9B.getRunResults())
            .filterValues(a -> a.getCoverage().getCoverageForStatement(129) > 0)
            .keys()
            .mapToInt(a -> a)
            .toArray();
        System.out.println("count: " + idxArray.length);
        System.out.println("values: ");
        for (int i = 0; i < idxArray.length; i++) {
            System.out.println(idxArray[i]);
        }
    }
}
