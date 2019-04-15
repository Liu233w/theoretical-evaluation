package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import com.google.gson.GsonBuilder;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.DiffRankResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankJam;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import one.util.streamex.StreamEx;

import java.io.FileNotFoundException;

public class DiffSuspiciousnessFactor {
    public static void main(String[] args) throws FileNotFoundException {

        final SuspiciousnessFactorJam left = FileUtils.loadObject(
            "./target/outputs/tot_info-suspiciousness-factors.json",
            SuspiciousnessFactorJam.class);
        final SuspiciousnessFactorJam right = FileUtils.loadObject(
            "./target/outputs/tot_info-suspiciousness-factors - 副本.json",
            SuspiciousnessFactorJam.class);

        final DiffRankJam diff = DiffRankResolver.resolve(left, right, "orig", "dump");

        final DiffRankJam filteredDiff = new DiffRankJam(StreamEx
            .of(diff.getDiffRankForPrograms())
            .filter(a -> a.getDiffRankForStatements().size() != 0)
            .toImmutableList());

        final String s = new GsonBuilder()
            .serializeSpecialFloatingPointValues()
            .create()
            .toJsonTree(filteredDiff)
            .toString();
        System.out.println(s);
    }
}
