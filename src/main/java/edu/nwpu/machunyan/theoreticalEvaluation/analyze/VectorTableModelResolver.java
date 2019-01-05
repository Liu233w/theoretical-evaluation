package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultItem;

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
    public static List<VectorTableModelRecord> fromRunResults(Stream<RunResultItem> runResult, int statementCount) {

        final List<VectorTableModelRecordBuilder> builders = IntStream.range(0, statementCount)
                .mapToObj(i -> new VectorTableModelRecordBuilder(i + 1))
                .collect(Collectors.toList());

        runResult.forEach(runResultItem ->
                builders.stream().forEach(builder ->
                        builder.processSingleRunResult(runResultItem)));

        return buildVtm(builders);
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
