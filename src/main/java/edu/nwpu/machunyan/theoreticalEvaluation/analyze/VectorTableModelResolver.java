package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import one.util.streamex.StreamEx;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 生成 {@link VectorTableModelForProgram}
 */
public class VectorTableModelResolver {

    public static VectorTableModelForProgram resolve(
        RunResultForProgram runResultForProgram) {

        final int statementCount = runResultForProgram.getStatementMap().getStatementCount();
        final VectorTableModelForStatement.Builder[] builders = resolveBuilders(statementCount, false);

        for (RunResultForTestcase runResult : runResultForProgram.getRunResults()) {
            for (VectorTableModelForStatement.Builder builder : builders) {
                builder.processRunResultForTestcase(runResult);
            }
        }

        return new VectorTableModelForProgram(
            runResultForProgram.getProgramTitle(),
            buildVtm(builders));
    }

    /**
     * 从运行结果获取 VTM，跳过 skipAt 位置的结果
     *
     * @param runResultForProgram
     * @param skipAt
     * @return
     */
    public static VectorTableModelForProgram resolveSkipBy(
        RunResultForProgram runResultForProgram,
        int skipAt) {

        final int statementCount = runResultForProgram.getStatementMap().getStatementCount();
        final List<RunResultForTestcase> runResults = runResultForProgram.getRunResults();
        final VectorTableModelForStatement.Builder[] builders = resolveBuilders(statementCount, false);

        for (int i = 0; i < skipAt; i++) {
            for (VectorTableModelForStatement.Builder builder : builders) {
                builder.processRunResultForTestcase(runResults.get(i));
            }
        }
        for (int i = skipAt + 1; i < runResults.size(); i++) {
            for (VectorTableModelForStatement.Builder builder : builders) {
                builder.processRunResultForTestcase(runResults.get(i));
            }
        }

        return new VectorTableModelForProgram(
            runResultForProgram.getProgramTitle(),
            buildVtm(builders));
    }

    /**
     * 从运行结果获取 VTM，只有对应的 useItem 为 true 的运行结果会被使用
     *
     * @param runResultForProgram
     * @param useItem             下标和 runResults 一一对应，为 true 的那个 runResult 才会被使用。
     * @return
     */
    public static VectorTableModelForProgram resolveSkipBy(
        RunResultForProgram runResultForProgram,
        boolean[] useItem) {

        final List<RunResultForTestcase> runResults = runResultForProgram.getRunResults();
        if (useItem.length != runResults.size()) {
            throw new IllegalArgumentException("useItem 必须和 runResults 一一对应");
        }

        final int statementCount = runResultForProgram.getStatementMap().getStatementCount();
        final VectorTableModelForStatement.Builder[] builders = resolveBuilders(statementCount, false);

        for (int i = 0; i < runResults.size(); i++) {
            if (useItem[i]) {
                for (VectorTableModelForStatement.Builder builder : builders) {
                    builder.processRunResultForTestcase(runResults.get(i));
                }
            }
        }

        return new VectorTableModelForProgram(
            runResultForProgram.getProgramTitle(),
            buildVtm(builders));
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
        final VectorTableModelForStatement.Builder[] builders = resolveBuilders(statementCount, true);

        final List<RunResultForTestcase> runResults = runResultForProgram.getRunResults();
        for (int i = 0; i < runResults.size(); i++) {
            for (VectorTableModelForStatement.Builder builder : builders) {
                builder.processRunResultForTestcase(runResults.get(i), testcaseWeights.get(i).getTestcaseWeight());
            }
        }

        return new VectorTableModelForProgram(
            runResultForProgram.getProgramTitle(),
            buildVtm(builders));
    }

    /**
     * 从 programResultJam 批量生成
     *
     * @param runResultJam
     * @return
     */
    public static VectorTableModelJam resolve(RunResultJam runResultJam) {
        final List<VectorTableModelForProgram> vectorTableModelForPrograms = StreamEx
            .of(runResultJam.getRunResultForPrograms())
            .map(VectorTableModelResolver::resolve)
            .toImmutableList();
        return new VectorTableModelJam(vectorTableModelForPrograms);
    }

    public static VectorTableModelJam resolveWithWeights(
        RunResultJam runResultJam,
        TestcaseWeightJam testcaseWeightJam) {

        final Map<String, List<TestcaseWeightForTestcase>> programTitleToWeights = StreamEx
            .of(testcaseWeightJam.getTestcaseWeightForPrograms())
            .toMap(
                TestcaseWeightForProgram::getTitle,
                TestcaseWeightForProgram::getTestcaseWeights
            );

        final List<VectorTableModelForProgram> collect = StreamEx
            .of(runResultJam.getRunResultForPrograms())
            .map(a -> resolveWithWeights(a, programTitleToWeights.get(a.getProgramTitle())))
            .toImmutableList();

        return new VectorTableModelJam(collect);
    }

    private static List<VectorTableModelForStatement> buildVtm(
        VectorTableModelForStatement.Builder[] builders) {

        final VectorTableModelForStatement[] result = new VectorTableModelForStatement[builders.length + 1];
        result[0] = null;
        for (int i = 0; i < builders.length; i++) {
            result[i + 1] = builders[i].build();
        }

        return Collections.unmodifiableList(Arrays.asList(result));
    }

    private static VectorTableModelForStatement.Builder[] resolveBuilders(
        int statementCount,
        boolean useWeight) {

        final VectorTableModelForStatement.Builder[] builders = new VectorTableModelForStatement.Builder[statementCount];
        for (int i = 0; i < statementCount; i++) {
            builders[i] = new VectorTableModelForStatement.Builder(i + 1, useWeight);
        }
        return builders;
    }
}
