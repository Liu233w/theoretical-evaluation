package edu.nwpu.machunyan.theoreticalEvaluation.runner.data;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示一个程序源代码的语句信息，包含了文件中每条语句的位置和语句编号。编号从 1 开始。因此 map 的第 0 个元素是 null
 */
@Value
public class StatementMap {

    /**
     * 语句编号同语句信息的对应，编号从 1 开始
     */
    private final List<StatementInfo> mapList;
    private final StatementMapType type;

    public StatementMap(StatementMapType type, ArrayList<StatementInfo> mapList) {
        this.mapList = mapList;
        this.type = type;
    }

    /**
     * 生成一个基于行的 StatementMap，由于基于行，因此不需要读取源文件，只需要知道行数即可
     *
     * @param lineCount
     * @param filePath
     * @return
     */
    public static StatementMap ofLineBasedStatementMap(int lineCount, String filePath) {

        final ArrayList<StatementInfo> mapList = new ArrayList<>(lineCount + 1);
        mapList.add(null);
        for (int i = 1; i <= lineCount; i++) {
            mapList.add(new StatementInfo(i, filePath, i, i));
        }

        return new StatementMap(StatementMapType.LINE_BASED, mapList);
    }

    /**
     * 获取总共的语句数量
     *
     * @return
     */
    public int getStatementCount() {
        // 除去第一个位置的 null
        return mapList.size() - 1;
    }
}
