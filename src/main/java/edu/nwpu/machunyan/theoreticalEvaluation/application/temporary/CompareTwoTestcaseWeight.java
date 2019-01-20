package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestcaseWeight;
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

        final TestcaseWeight.Pojo.Jam left = FileUtils.loadObject(
            "./target/outputs/tot_info-testcase-weight.json",
            TestcaseWeight.Pojo.Jam.class);
        final TestcaseWeight.Pojo.Jam right = FileUtils.loadObject(
            "./target/outputs/tot_info-testcase-weight - 副本.json",
            TestcaseWeight.Pojo.Jam.class);

        if (left.equals(right)) {

            System.out.println("equal");
            return;
        }

        final List<TestcaseWeight.Pojo.ForProgram> collect = Stream
            .concat(diff(left, right), diff(right, left))
            .collect(Collectors.toList());

        final TestcaseWeight.Pojo.Jam jam = new TestcaseWeight.Pojo.Jam(collect);

        FileUtils.saveObject("./target/outputs/tot_info-testcase-weight-diff.json", jam);
    }

    private static Stream<TestcaseWeight.Pojo.ForProgram> diff(TestcaseWeight.Pojo.Jam left, TestcaseWeight.Pojo.Jam right) {

        final Map<String, List<TestcaseWeight.Pojo.ForTestcase>> leftMap = left
            .getForPrograms().stream()
            .collect(Collectors.toMap(
                a -> a.getTitle() + "-" + a.getFormulaTitle(),
                TestcaseWeight.Pojo.ForProgram::getTestcaseWeights
            ));
        return right.getForPrograms().stream()
            .map(a -> {
                val leftTests = leftMap.get(a.getTitle() + "-" + a.getFormulaTitle());
                final List<TestcaseWeight.Pojo.ForTestcase> collect = StreamUtils.zipWithIndex(a.getTestcaseWeights().stream())
                    .filter(b -> !b.getValue().equals(leftTests.get(b.getKey())))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
                return new TestcaseWeight.Pojo.ForProgram(a.getTitle(), a.getFormulaTitle(), collect);
            })
            .filter(a -> a.getTestcaseWeights().size() != 0);
    }
}
