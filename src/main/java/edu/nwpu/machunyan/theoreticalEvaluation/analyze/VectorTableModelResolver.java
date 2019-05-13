package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 生成 {@link VectorTableModelForProgram}
 */
public class VectorTableModelResolver {

    /**
     * 从 runResults 的流中构建 vtm
     *
     * @param runResult
     * @param statementCount
     * @return
     */
    public static List<VectorTableModelForStatement> resolve(
        Stream<RunResultForTestcase> runResult,
        int statementCount) {

        final List<VectorTableModelForStatement.Builder> builders = IntStreamEx
            .range(0, statementCount)
            .mapToObj(i -> new VectorTableModelForStatement.Builder(i + 1))
            .toImmutableList();

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
    public static VectorTableModelForProgram resolveWithWeights(
        RunResultForProgram runResultForProgram,
        List<TestcaseWeightForTestcase> testcaseWeights) {

        if (runResultForProgram.getRunResults().size() != testcaseWeights.size()) {
            throw new IllegalArgumentException("运行结果和权重必须一一对应（一个 RunResultForTestcase 对应一个 testcaseWeight）");
        }

        final int statementCount = runResultForProgram.getStatementMap().getStatementCount();
        final List<VectorTableModelForStatement.Builder> builders = IntStreamEx
            .range(0, statementCount)
            .mapToObj(i -> new VectorTableModelForStatement.Builder(i + 1, true))
            .toImmutableList();

        final List<RunResultForTestcase> runResults = runResultForProgram.getRunResults();
        for (int i = 0; i < runResults.size(); i++) {
            for (VectorTableModelForStatement.Builder builder : builders) {
                builder.processRunResultForTestcase(runResults.get(i), testcaseWeights.get(i).getTestcaseWeight());
            }
        }

        return new VectorTableModelForProgram(runResultForProgram.getProgramTitle(), buildVtm(builders));
    }

    /**
     * 从 {@link RunResultForProgram} 批量生成
     *
     * @param runResultForProgram
     * @return
     */
    public static VectorTableModelForProgram resolve(RunResultForProgram runResultForProgram) {
        final int statementCount = runResultForProgram.getStatementMap().getStatementCount();
        final Stream<RunResultForTestcase> stream = runResultForProgram.getRunResults().stream();
        final List<VectorTableModelForStatement> vectorTableModelForStatements = resolve(stream, statementCount);
        return new VectorTableModelForProgram(runResultForProgram.getProgramTitle(), vectorTableModelForStatements);
    }

    /**
     * 从 programResultJam 批量生成
     *
     * @param runResultJam
     * @return
     */
    public static VectorTableModelJam resolve(RunResultJam runResultJam) {
        return new VectorTableModelJam(resolve(runResultJam.getRunResultForPrograms()));
    }

    public static VectorTableModelJam resolveWithWeights(
        RunResultJam runResultJam,
        TestcaseWeightJam testcaseWeightJam) {

        final Map<String, List<TestcaseWeightForTestcase>> titleToWeights = StreamEx
            .of(testcaseWeightJam.getTestcaseWeightForPrograms())
            .toMap(
                TestcaseWeightForProgram::getTitle,
                TestcaseWeightForProgram::getTestcaseWeights
            );

        final List<VectorTableModelForProgram> collect = StreamEx
            .of(runResultJam.getRunResultForPrograms())
            .map(a -> resolveWithWeights(a, titleToWeights.get(a.getProgramTitle())))
            .toImmutableList();

        return new VectorTableModelJam(collect);
    }

    public static List<VectorTableModelForProgram> resolve(
        Iterable<RunResultForProgram> runResultForPrograms) {

        return StreamEx
            .of(runResultForPrograms.iterator())
            .map(VectorTableModelResolver::resolve)
            .toImmutableList();
    }

    private static List<VectorTableModelForStatement> buildVtm(List<VectorTableModelForStatement.Builder> builders) {
        final ArrayList<VectorTableModelForStatement> result = new ArrayList<>(builders.size() + 1);
        result.add(null);
        for (VectorTableModelForStatement.Builder builder :
            builders) {
            result.add(builder.build());
        }
        return result;
    }
}
