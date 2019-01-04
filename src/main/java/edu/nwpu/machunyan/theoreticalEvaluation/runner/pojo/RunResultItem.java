package edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo;

import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.Coverage;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.StatementMap;
import lombok.Data;

/**
 * 表示一次运行的结果，不包含多余的数据
 */
@Data
public class RunResultItem {

    /**
     * 当前运行（测试用例）是否得到了正确的结果
     */
    private final boolean correct;

    /**
     * 本次运行的覆盖情况
     */
    private final Coverage coverage;

    /**
     * 本次运行中分析器所得出的语句的对应关系，正常情况下应该为 null，并使用 {@link ProgramRunResult#getStatementMap()} 的结果
     */
    private final StatementMap statementMap;

    public RunResultItem(boolean correct, Coverage coverage, StatementMap statementMap) {
        this.correct = correct;
        this.coverage = coverage;
        this.statementMap = statementMap;
    }
}
