package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestSuitSubsetForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestSuitSubsetJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import lombok.val;
import one.util.streamex.StreamEx;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CompareTwoTestSuitSubset {
    public static void main(String[] args) throws IOException {

        final TestSuitSubsetJam left = FileUtils.loadObject(
            "./target/outputs/test-suit-subset-op/replace.json",
            TestSuitSubsetJam.class);
        final TestSuitSubsetJam right = FileUtils.loadObject(
            "./target/outputs/test-suit-subset-op/replace - 副本.json",
            TestSuitSubsetJam.class);

        if (left.equals(right)) {

            System.out.println("equal");
            return;
        }

        final List<TestSuitSubsetForProgram> collect = diff(left, right)
            .append(diff(right, left))
            .toImmutableList();

        final TestSuitSubsetJam jam = new TestSuitSubsetJam(collect);

        FileUtils.saveObject("./target/outputs/replace-test-suit-subset-diff.json", jam);
    }

    private static StreamEx<TestSuitSubsetForProgram> diff(
        TestSuitSubsetJam left,
        TestSuitSubsetJam right) {

        final Map<String, TestSuitSubsetForProgram> leftMap = StreamEx
            .of(left.getTestSuitSubsetForPrograms())
            .mapToEntry(
                TestSuitSubsetForProgram::getProgramTitle,
                a -> a)
            .toMap();

        return StreamEx
            .of(right.getTestSuitSubsetForPrograms())
            .filter(a -> {
                val leftItem = leftMap.get(a.getProgramTitle());
                return Arrays.equals(leftItem.getToOldSetMap(), a.getToOldSetMap());
            });
    }
}
