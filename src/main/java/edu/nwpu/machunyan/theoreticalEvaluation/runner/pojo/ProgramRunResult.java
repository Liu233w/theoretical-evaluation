package edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo;

import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.StatementMap;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 一个程序的运行结果
 */
@Data
@AllArgsConstructor
public class ProgramRunResult {

    /**
     * 程序的标题
     */
    String programTitle;

    /**
     * 本次运行中分析器所得出的语句的对应关系，正常情况下同一个程序的 statement map 应该是一样的
     */
    StatementMap statementMap;

    List<RunResultItem> runResults;
}
