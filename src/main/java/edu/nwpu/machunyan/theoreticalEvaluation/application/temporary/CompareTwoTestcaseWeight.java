package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.StreamUtils;
import lombok.val;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        final List<TestcaseWeightForProgram> collect = Stream
            .concat(diff(left, right), diff(right, left))
            .collect(Collectors.toList());

        final TestcaseWeightJam jam = new TestcaseWeightJam(collect);

        FileUtils.saveObject("./target/outputs/tot_info-testcase-weight-diff.json", jam);
    }

    private static Stream<TestcaseWeightForProgram> diff(TestcaseWeightJam left, TestcaseWeightJam right) {

        final Map<String, List<TestcaseWeightForTestcase>> leftMap = left
            .getTestcaseWeightForPrograms().stream()
            .collect(Collectors.toMap(
                a -> a.getTitle() + "-" + a.getFormulaTitle(),
                TestcaseWeightForProgram::getTestcaseWeights
            ));
        return right.getTestcaseWeightForPrograms().stream()
            .map(a -> {
                val leftTests = leftMap.get(a.getTitle() + "-" + a.getFormulaTitle());
                final List<TestcaseWeightForTestcase> collect = StreamUtils.zipWithIndex(a.getTestcaseWeights().stream())
                    .filter(b -> !b.getValue().equals(leftTests.get(b.getKey())))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
                return new TestcaseWeightForProgram(a.getTitle(), a.getFormulaTitle(), collect);
            })
            .filter(a -> a.getTestcaseWeights().size() != 0);
    }
}
