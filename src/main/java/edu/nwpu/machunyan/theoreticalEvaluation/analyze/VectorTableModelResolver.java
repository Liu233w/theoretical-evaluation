package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 生成 {@link edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModel}
 */
public class VectorTableModelResolver {

    /**
     * 从 runResults 的流中构建 vtm
     *
     * @param runResult
     * @param statementCount
     * @return
     */
    public static List<VectorTableModelRecord> resolve(
        Stream<RunResultForTestcase> runResult,
        int statementCount) {

        final List<VectorTableModelRecordBuilder> builders = IntStream.range(0, statementCount)
            .mapToObj(i -> new VectorTableModelRecordBuilder(i + 1))
            .collect(Collectors.toList());

        runResult.forEach(runResultItem ->
            builders.forEach(builder ->
                builder.processRunResultForTestcase(runResultItem)));

        return buildVtm(builders);
    }

    /**
     * 从一个程序的运行结果中生成 vtm，使用权重
     *
     * @param runResultForProgram
     * @param testcaseWeights     测试用例的权重，和运行结果一一对应
     * @return
     */
    public static VectorTableModel resolveWithWeights(
        RunResultForProgram runResultForProgram,
        List<TestcaseWeightForTestcase> testcaseWeights) {

        if (runResultForProgram.getRunResults().size() != testcaseWeights.size()) {
            throw new IllegalArgumentException("运行结果和权重必须一一对应（一个 RunResultForTestcase 对应一个 testcaseWeight）");
        }

        final int statementCount = runResultForProgram.getStatementMap().getStatementCount();
        final List<VectorTableModelRecordBuilder> builders = IntStream.range(0, statementCount)
            .mapToObj(i -> new VectorTableModelRecordBuilder(i + 1, true))
            .collect(Collectors.toList());

        final List<RunResultForTestcase> runResults = runResultForProgram.getRunResults();
        for (int i = 0; i < runResults.size(); i++) {
            for (VectorTableModelRecordBuilder builder : builders) {
                builder.processRunResultForTestcase(runResults.get(i), testcaseWeights.get(i).getTestcaseWeight());
            }
        }

        return new VectorTableModel(runResultForProgram.getProgramTitle(), buildVtm(builders));
    }

    /**
     * 从 {@link RunResultForProgram} 批量生成
     *
     * @param runResultForProgram
     * @return
     */
    public static VectorTableModel resolve(RunResultForProgram runResultForProgram) {
        final int statementCount = runResultForProgram.getStatementMap().getStatementCount();
        final Stream<RunResultForTestcase> stream = runResultForProgram.getRunResults().stream();
        final List<VectorTableModelRecord> vectorTableModelRecords = resolve(stream, statementCount);
        return new VectorTableModel(runResultForProgram.getProgramTitle(), vectorTableModelRecords);
    }

    /**
     * 从 programResultJam 批量生成
     *
     * @param runResultJam
     * @return
     */
    public static VectorTableModelJam resolve(RunResultJam runResultJam) {
        final List<VectorTableModel> vectorTableModels = runResultJam
            .getRunResultForPrograms().stream()
            .map(VectorTableModelResolver::resolve)
            .collect(Collectors.toList());
        return new VectorTableModelJam(vectorTableModels);
    }

    public static VectorTableModelJam resolveWithWeights(
        RunResultJam runResultJam,
        TestcaseWeightJam testcaseWeightJam) {

        return resolveWithWeights(
            runResultJam,
            testcaseWeightJam.getTestcaseWeightForPrograms());
    }

    public static VectorTableModelJam resolveWithWeights(
        RunResultJam runResultJam,
        List<TestcaseWeightForProgram> weights) {

        final Map<String, List<TestcaseWeightForTestcase>> titleToWeights = weights
            .stream()
            .collect(Collectors.toMap(
                TestcaseWeightForProgram::getTitle,
                TestcaseWeightForProgram::getTestcaseWeights
            ));

        final List<VectorTableModel> collect = runResultJam
            .getRunResultForPrograms()
            .stream()
            .map(a -> resolveWithWeights(a, titleToWeights.get(a.getProgramTitle())))
            .collect(Collectors.toList());

        return new VectorTableModelJam(collect);
    }

    private static List<VectorTableModelRecord> buildVtm(List<VectorTableModelRecordBuilder> builders) {
        final ArrayList<VectorTableModelRecord> result = new ArrayList<>(builders.size() + 1);
        result.add(null);
        for (VectorTableModelRecordBuilder builder :
            builders) {
            result.add(builder.build());
        }
        return result;
    }
}
