package edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo;

import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.Coverage;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.StatementMap;
import lombok.Value;

/**
 * 表示一次运行的结果，不包含多余的数据
 */
@Value
public class RunResultForTestcase {

    /**
     * 当前运行（测试用例）是否得到了正确的结果
     */
    boolean correct;

    /**
     * 本次运行的覆盖情况
     */
    Coverage coverage;

    /**
     * 本次运行中分析器所得出的语句的对应关系，正常情况下应该为 null，并使用 {@link RunResultForProgram#getStatementMap()} 的结果
     */
    StatementMap statementMap;
}
