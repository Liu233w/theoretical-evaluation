package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.SingleRunResult;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.StatementMap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class VectorTableModelGenerator {

    /**
     * 从多个运行结果中得出 vector table model。每个运行结果的 statementMap 必须一致。
     *
     * @param runResults
     * @return vector table model，对应一个矩阵。第 0 个元素为 null，从第一个元素开始每个元素表示一行。
     */
    public static ArrayList<VectorTableModelRecord> generateFromRunResult(List<SingleRunResult> runResults) {
        return generateFromStream(runResults.stream(), runResults.get(0).getStatementMap().getStatementCount());
    }

    /**
     * 从一个表示运行结果的流中得出 vector table model。每个运行结果的 statementMap 必须一致。
     *
     * @param stream
     * @param statementCount 总体的语句数量
     * @return vector table model，对应一个矩阵。第 0 个元素为 null，从第一个元素开始每个元素表示一行。
     */
    public static ArrayList<VectorTableModelRecord> generateFromStream(Stream<SingleRunResult> stream, int statementCount) {

        final ArrayList<VectorTableModelRecordBuilder> builders = new ArrayList<>(statementCount);

        for (int i = 0; i < statementCount; i++) {
            builders.add(new VectorTableModelRecordBuilder(i + 1));
        }

        stream.forEach(singleRunResult -> {
            for (int i = 0; i < statementCount; i++) {
                builders.get(i).processSingleRunResult(singleRunResult);
            }
        });

        final ArrayList<VectorTableModelRecord> result = new ArrayList<>(statementCount + 1);
        result.add(null);
        for (VectorTableModelRecordBuilder builder :
                builders) {
            result.add(builder.build());
        }

        return result;
    }
}
