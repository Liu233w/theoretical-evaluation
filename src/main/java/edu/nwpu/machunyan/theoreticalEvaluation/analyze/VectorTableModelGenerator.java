package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.SingleRunResult;

import java.util.ArrayList;
import java.util.List;

public class VectorTableModelGenerator {

    /**
     * 从多个运行结果中得出 vector table model。每个运行结果的 statementMap 必须一致。
     *
     * @param runResults
     * @return vector table model，对应一个矩阵。第 0 个元素为 null，从第一个元素开始每个元素表示一行。
     */
    public static ArrayList<VectorTableModelRecord> generateFromRunResult(List<SingleRunResult> runResults) {

        final int statementCount = runResults.get(0).getStatementMap().getStatementCount();

        final ArrayList<VectorTableModelRecordBuilder> builders = new ArrayList<>(statementCount);

        for (int i = 0; i < statementCount; i++) {
            builders.add(new VectorTableModelRecordBuilder(i + 1));
        }

        for (SingleRunResult singleRunResult :
                runResults) {
            for (int i = 0; i < statementCount; i++) {
                builders.get(i).processSingleRunResult(singleRunResult);
            }
        }

        final ArrayList<VectorTableModelRecord> result = new ArrayList<>(statementCount + 1);
        result.add(null);
        for (VectorTableModelRecordBuilder builder :
                builders) {
            result.add(builder.build());
        }

        return result;
    }

}
