package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.TestcaseResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.TestcaseItem;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvLine;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import one.util.streamex.IntStreamEx;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取根据权重排名过的测试用例，便于观察结果
 */
public class ResolveSortedTestcaseWeight {

    private static final String[] MAIN_LIST = new String[]{
        "my_sort",
        "schedule2",
        "tcas",
        "tot_info",
        "replace",
        "print_tokens",
    };

    private static final Path OUTPUT_DIR = Paths.get("./target/outputs/sorted-testcase");

    public static void main(String[] args) throws IOException, URISyntaxException {

        for (String name : MAIN_LIST) {

            final Path programDir = OUTPUT_DIR.resolve(name);

            if (Files.exists(programDir)) {
                continue;
            }

            LogUtils.logInfo("Working on " + name);

            final TestcaseWeightJam weights = ResolveTestcaseWeight.getResultFromFile(name);
            final List<TestcaseItem> testcases = TestcaseResolver.resolve(name);

            for (TestcaseWeightForProgram weight : weights.getTestcaseWeightForPrograms()) {
                final OutputItem[] outputItems = resolveForProgram(weight.getTestcaseWeights(), testcases);

                final Path resFile = programDir.resolve(
                    weight.getFormulaTitle() + "-" + weight.getTitle() + ".csv");
                FileUtils.saveString(resFile, toCsvString(outputItems));
            }
        }
    }

    private static String toCsvString(OutputItem[] list) {

        final ArrayList<CsvLine> csvLines = new ArrayList<>();

        csvLines.add(new CsvLine(new Object[]{
            "rank", "weight", "name", "params", "input", "should output"
        }));

        for (OutputItem item : list) {
            final TestcaseItem testcaseItem = item.getTestcaseItem();
            csvLines.add(new CsvLine(new Object[]{
                item.getRank(), item.getWeight(),
                testcaseItem.getName(),
                testcaseItem.getInput(), toString(testcaseItem.getParams()),
                testcaseItem.getOutput(),
            }));
        }

        return CsvExporter.toCsvString(csvLines);
    }

    private static String toString(String[] inputs) {
        final StringBuilder sb = new StringBuilder();
        for (String in : inputs) {
            sb.append(in).append(", ");
        }
        return sb.toString();
    }

    private static OutputItem[] resolveForProgram(
        List<TestcaseWeightForTestcase> weights,
        List<TestcaseItem> testcases) {

        if (weights.size() != testcases.size()) {
            throw new IllegalArgumentException("数量不一致");
        }

        final OutputItem[] outputItems = IntStreamEx.range(0, weights.size())
            .mapToObj(i -> new OutputItem(
                testcases.get(i),
                weights.get(i).getTestcaseWeight(),
                0))
            // weight都大于0，相反数一下就变成从大到小排序了
            .sortedBy(a -> -a.getWeight())
            .toArray(OutputItem[]::new);

        int lastRank = 0;
        double lastWeight = Double.MAX_VALUE;
        for (OutputItem outputItem : outputItems) {
            if (outputItem.getWeight() < lastWeight) {
                lastWeight = outputItem.getWeight();
                ++lastRank;
            }
            outputItem.setRank(lastRank);
        }

        return outputItems;
    }

    @Data
    @AllArgsConstructor
    private static class OutputItem {
        TestcaseItem testcaseItem;
        double weight;
        int rank;
    }
}
