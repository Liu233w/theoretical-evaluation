package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.ProgramDefination;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.TestcaseResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.TestcaseItem;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.*;
import lombok.Value;
import one.util.streamex.EntryStream;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 获取根据权重排名过的测试用例，便于观察结果
 */
public class ResolveSortedTestcaseWeight {

    private static final Path OUTPUT_DIR = Paths.get("./target/outputs/sorted-testcase");

    public static void main(String[] args) throws IOException, URISyntaxException {

        final String[] formulas = SuspiciousnessFactorFormulas.getAllFormulas().keySet().toArray(new String[0]);

        for (String name : ProgramDefination.PROGRAM_LIST) {

            final Path programDir = OUTPUT_DIR.resolve(name);

            if (Files.exists(programDir)) {
                continue;
            }

            LogUtils.logInfo("Working on " + name);

            final TestcaseWeightJam jam = ResolveTestcaseWeight.getResultFromFile(name);
            final List<TestcaseItem> testcases = TestcaseResolver.resolve(name);
            final List<RunResultForProgram> runResultForProgram = Run.getResultFromFile(name).getRunResultForPrograms();

            for (Map.Entry<String, TestcaseWeightForProgram[]> entry : getProgramToFormulas(jam.getTestcaseWeightForPrograms()).entrySet()) {
                String programTitle = entry.getKey();
                TestcaseWeightForProgram[] weights = entry.getValue();

                LogUtils.logFine("for " + programTitle);

                final List<RunResultForTestcase> runResultForTestcases = StreamEx.of(runResultForProgram)
                    .filter(a -> a.getProgramTitle().equals(programTitle))
                    .findAny()
                    .get()
                    .getRunResults();

                final OutputItem[] outputItems = resolveForProgram(weights, testcases, runResultForTestcases);

                final Path resFile = programDir.resolve(programTitle + ".csv");
                FileUtils.saveString(resFile, toCsvString(outputItems, formulas));
            }
        }
    }

    /**
     * 获取标题对应各个公式的map
     *
     * @param list
     * @return
     */
    private static Map<String, TestcaseWeightForProgram[]> getProgramToFormulas(
        List<TestcaseWeightForProgram> list) {

        return StreamEx
            .of(list)
            .map(TestcaseWeightForProgram::getTitle)
            .toSetAndThen(StreamEx::of)
            .mapToEntry(title -> StreamEx.of(list)
                .filterBy(TestcaseWeightForProgram::getTitle, title))
            .mapValues(a -> a.toArray(TestcaseWeightForProgram[]::new))
            .toMap();
    }

    private static String toCsvString(OutputItem[] list, String[] formulas) {

        final ArrayList<CsvLine> csvLines = new ArrayList<>();

        csvLines.add(new CsvLine(ArrayUtils.concat(formulas,
            new Object[]{"name", "params", "input", "should output", "correct"}
        )));

        for (OutputItem item : list) {
            final TestcaseItem testcaseItem = item.getTestcaseItem();
            final Map<String, Double> formulaToWeight = item.getFormulaToWeight();

            final Object[] weights = new Object[formulas.length];
            for (int i = 0; i < formulas.length; i++) {
                weights[i] = formulaToWeight.get(formulas[i]);
            }

            csvLines.add(new CsvLine(ArrayUtils.concat(weights, new Object[]{
                testcaseItem.getName(),
                testcaseItem.getInput(), toString(testcaseItem.getParams()),
                testcaseItem.getOutput(),
                item.isCorrect(),
            })));
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

    /**
     * @param weights   一个版本的所有公式得到的权重结果
     * @param testcases
     * @return
     */
    private static OutputItem[] resolveForProgram(
        TestcaseWeightForProgram[] weights,
        List<TestcaseItem> testcases,
        List<RunResultForTestcase> runResults) {

        if (weights[0].getTestcaseWeights().size() != testcases.size()) {
            throw new IllegalArgumentException("数量不一致");
        }

        final Map<String, TestcaseWeightForProgram> formulaToResult = StreamEx.of(weights)
            .toMap(TestcaseWeightForProgram::getFormulaTitle, a -> a);

        return IntStreamEx
            .range(0, testcases.size())
            .mapToObj(i -> {
                final TestcaseItem testcaseItem = testcases.get(i);
                final boolean correct = runResults.get(i).isCorrect();
                final Map<String, Double> formulaToWeight = EntryStream
                    .of(formulaToResult)
                    .mapValues(a ->
                        a.getTestcaseWeights().get(i).getTestcaseWeight())
                    .toMap();
                return new OutputItem(testcaseItem, formulaToWeight, correct);
            })
            .toArray(OutputItem[]::new);
    }

    @Value
    private static class OutputItem {
        TestcaseItem testcaseItem;
        Map<String, Double> formulaToWeight;
        boolean correct;
    }
}
