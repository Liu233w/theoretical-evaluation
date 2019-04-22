package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 在测试用例权重列表中筛选出来权重不是 1.0 的权重列表
 */
public class FindNotOneTestcaseWeight {
    public static void main(String[] args) throws IOException {

        final TestcaseWeightJam tcas = ResolveTestcaseWeight.getResultFromFile("tcas");

        final List<TestcaseWeightForProgram> list = tcas.getTestcaseWeightForPrograms()
            .stream()
            .map(item -> {
                final List<TestcaseWeightForTestcase> collect = item.getTestcaseWeights()
                    .stream()
                    .filter(a -> a.getTestcaseWeight() != 1.0)
                    .collect(Collectors.toList());
                return new TestcaseWeightForProgram(
                    item.getTitle(),
                    item.getFormulaTitle(),
                    collect);
            })
            .collect(Collectors.toList());
        final TestcaseWeightJam res = new TestcaseWeightJam(list);

        FileUtils.saveObject("./target/outputs/not-one-testcase-weight.json",
            res);
    }
}
