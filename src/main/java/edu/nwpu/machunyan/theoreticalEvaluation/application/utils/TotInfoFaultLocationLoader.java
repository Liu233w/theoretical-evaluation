package edu.nwpu.machunyan.theoreticalEvaluation.application.utils;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.FaultLocationForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.FaultLocationJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 从预先的文件中导入 tot info 的错误代码位置
 */
public class TotInfoFaultLocationLoader {

    public static FaultLocationJam getFaultLocations() throws URISyntaxException, IOException {

        final String faultLocationFile = FileUtils
            .getFilePathFromResources("tot_info/fault_locations.csv")
            .toString();
        final CSVParser csvRecords = CSVFormat
            .EXCEL
            .withFirstRecordAsHeader()
            .parse(new InputStreamReader(new FileInputStream(faultLocationFile), Charset.forName("utf-8")));

        final List<FaultLocationForProgram> collect = StreamEx
            .of(csvRecords.getRecords())
            .map(a -> new FaultLocationForProgram(
                a.get(0),
                splitLines(a.get(1)),
                "",
                ""
            ))
            .toImmutableList();
        return new FaultLocationJam(collect);
    }

    /**
     * 将形如 20-22 这样的字符串分割成 {20,21,22} 这样的 set
     *
     * @param input
     * @return
     */
    private static Set<Integer> splitLines(String input) {

        final String[] split = input.split("-");
        if (split.length == 1) {

            return Collections.singleton(Integer.parseInt(split[0]));

        } else if (split.length == 2) {

            final int start = Integer.parseInt(split[0]);
            final int end = Integer.parseInt(split[1]);
            if (start > end) {
                throw new IllegalArgumentException("for " + input + ": 起点必须比终点低");
            }

            return IntStreamEx.range(start, end + 1)
                .boxed()
                .toImmutableSet();

        } else {
            throw new IllegalArgumentException("无法解析此输入");
        }
    }

}
