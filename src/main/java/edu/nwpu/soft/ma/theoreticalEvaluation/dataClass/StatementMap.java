package edu.nwpu.soft.ma.theoreticalEvaluation.dataClass;

import java.util.ArrayList;

/**
 * 表示一个程序源代码的语句信息，包含了文件中每条语句的位置和语句编号
 */
public class StatementMap {

    /**
     * 语句编号同语句信息的对应，编号从 1 开始
     */
    private ArrayList<StatementInfo> mapList;

    private StatementMapType type;
}
