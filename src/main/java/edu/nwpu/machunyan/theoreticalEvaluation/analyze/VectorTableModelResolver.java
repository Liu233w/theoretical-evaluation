package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModel;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModelJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.ProgramRunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;

import java.util.ArrayList;
import java.util.List;
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
    public static List<VectorTableModelRecord> resolve(Stream<RunResultForTestcase> runResult, int statementCount) {

        final List<VectorTableModelRecordBuilder> builders = IntStream.range(0, statementCount)
            .mapToObj(i -> new VectorTableModelRecordBuilder(i + 1))
            .collect(Collectors.toList());

        runResult.forEach(runResultItem ->
            builders.stream().forEach(builder ->
                builder.processRunResultForTestcase(runResultItem)));

        return buildVtm(builders);
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
     * @param programRunResultJam
     * @return
     */
    public static VectorTableModelJam resolve(ProgramRunResultJam programRunResultJam) {
        final List<VectorTableModel> vectorTableModels = programRunResultJam.getRunResultForPrograms().stream()
            .map(VectorTableModelResolver::resolve)
            .collect(Collectors.toList());
        return new VectorTableModelJam(vectorTableModels);
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
