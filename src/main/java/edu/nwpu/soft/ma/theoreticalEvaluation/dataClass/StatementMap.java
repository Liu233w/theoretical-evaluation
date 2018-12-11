package edu.nwpu.soft.ma.theoreticalEvaluation.dataClass;

import lombok.Data;

import java.util.ArrayList;

/**
 * 表示一个程序源代码的语句信息，包含了文件中每条语句的位置和语句编号
 */
@Data
public class StatementMap {

    public StatementMap(StatementMapType type) {

        this.type = type;
        this.mapList = new ArrayList<>();
    }

    /**
     * 语句编号同语句信息的对应，编号从 1 开始
     */
    private final ArrayList<StatementInfo> mapList;

    private final StatementMapType type;
}
