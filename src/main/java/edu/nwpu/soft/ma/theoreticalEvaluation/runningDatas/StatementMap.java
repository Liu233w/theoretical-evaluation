package edu.nwpu.soft.ma.theoreticalEvaluation.runningDatas;

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

    /**
     * 获取总共的语句数量
     *
     * @return
     */
    public int getStatementCount() {
        return mapList.size();
    }

    /**
     * 生成一个基于行的 StatementMap，由于基于行，因此不需要读取源文件，只需要知道行数即可
     *
     * @param lineCount
     * @param filePath
     * @return
     */
    public static StatementMap ofLineBasedStatementMap(int lineCount, String filePath) {

        final StatementMap statementMap = new StatementMap(StatementMapType.LINE_BASED);

        final ArrayList<StatementInfo> mapList = statementMap.getMapList();
        mapList.ensureCapacity(lineCount + 1);
        mapList.add(null);
        for (int i = 1; i <= lineCount; i++) {
            mapList.add(new StatementInfo(i, filePath, i, i));
        }

        return statementMap;
    }
}
