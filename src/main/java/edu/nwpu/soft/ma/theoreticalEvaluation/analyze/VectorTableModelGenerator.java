package edu.nwpu.soft.ma.theoreticalEvaluation.analyze;

import edu.nwpu.soft.ma.theoreticalEvaluation.runningDatas.SingleRunResult;

import java.util.ArrayList;
import java.util.List;

public class VectorTableModelGenerator {

    /**
     * 从多个运行结果中得出 vector table model。每个运行结果的 statementMap 必须一致。
     *
     * @param runResults
     * @return vector table model，对应一个矩阵。第 0 个元素为 null，从第一个元素开始每个元素表示一行。
     */
    public static ArrayList<VectorTableModelRecord> generateVectorTableModelFromRunResult(List<SingleRunResult> runResults) {

        final int statementCount = runResults.get(0).getStatementMap().getStatementCount();
        final ArrayList<VectorTableModelRecord> result = new ArrayList<>(statementCount);

        result.add(null);
        for (int i = 1; i <= statementCount; i++) {
            result.add(new VectorTableModelRecord(i));
        }

        for (SingleRunResult singleRunResult :
                runResults) {
            for (int i = 1; i <= statementCount; i++) {
                result.get(i).processSingleRunResult(singleRunResult);
            }
        }

        return result;
    }

}
