package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorHelper;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankJam;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.JsonUtils;

import java.io.FileNotFoundException;
import java.util.stream.Collectors;

public class DiffSuspiciousnessFactor {
    public static void main(String[] args) throws FileNotFoundException {

        final SuspiciousnessFactorJam left = FileUtils.loadObject(
            "./target/outputs/tot_info-suspiciousness-factors.json",
            SuspiciousnessFactorJam.class);
        final SuspiciousnessFactorJam right = FileUtils.loadObject(
            "./target/outputs/tot_info-suspiciousness-factors - 副本.json",
            SuspiciousnessFactorJam.class);

        final DiffRankJam diff = SuspiciousnessFactorHelper.diff(left, right, "orig", "dump");

        final DiffRankJam filteredDiff = new DiffRankJam(diff
            .getDiffRankForPrograms()
            .stream()
            .filter(a -> a.getDiffRankForStatements().size() != 0)
            .collect(Collectors.toList()));

        final String s = JsonUtils.toJson(filteredDiff).toString();
        System.out.println(s);
    }
}
