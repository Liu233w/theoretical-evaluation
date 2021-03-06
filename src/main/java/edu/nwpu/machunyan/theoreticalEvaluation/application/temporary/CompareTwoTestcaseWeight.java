package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import lombok.val;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CompareTwoTestcaseWeight {
    public static void main(String[] args) throws IOException {

        final TestcaseWeightJam left = FileUtils.loadObject(
            "./target/outputs/tot_info-testcase-weight.json",
            TestcaseWeightJam.class);
        final TestcaseWeightJam right = FileUtils.loadObject(
            "./target/outputs/tot_info-testcase-weight - 副本.json",
            TestcaseWeightJam.class);

        if (left.equals(right)) {

            System.out.println("equal");
            return;
        }

        final List<TestcaseWeightForProgram> collect = diff(left, right)
            .append(diff(right, left))
            .toImmutableList();

        final TestcaseWeightJam jam = new TestcaseWeightJam(collect);

        FileUtils.saveObject("./target/outputs/tot_info-testcase-weight-diff.json", jam);
    }

    private static StreamEx<TestcaseWeightForProgram> diff(TestcaseWeightJam left, TestcaseWeightJam right) {

        final Map<String, List<TestcaseWeightForTestcase>> leftMap = StreamEx
            .of(left.getTestcaseWeightForPrograms())
            .toMap(
                a -> a.getTitle() + "-" + a.getFormulaTitle(),
                TestcaseWeightForProgram::getTestcaseWeights
            );
        return StreamEx
            .of(right.getTestcaseWeightForPrograms())
            .map(a -> {
                val leftTests = leftMap.get(a.getTitle() + "-" + a.getFormulaTitle());
                final List<TestcaseWeightForTestcase> collect = EntryStream
                    .of(a.getTestcaseWeights())
                    .filter(b -> !b.getValue().equals(leftTests.get(b.getKey())))
                    .map(Map.Entry::getValue)
                    .toImmutableList();
                return new TestcaseWeightForProgram(a.getTitle(), a.getFormulaTitle(), collect);
            })
            .filter(a -> a.getTestcaseWeights().size() != 0);
    }
}
