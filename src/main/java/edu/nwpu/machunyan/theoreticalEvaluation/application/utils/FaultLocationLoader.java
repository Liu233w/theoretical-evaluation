package edu.nwpu.machunyan.theoreticalEvaluation.application.utils;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.FaultLocationForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.FaultLocationJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import lombok.NonNull;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 从预先的文件中导入错误代码位置
 */
public class FaultLocationLoader {

    public static Optional<FaultLocationJam> getFaultLocations(String name) throws URISyntaxException, IOException {

        final String faultLocationFile = FileUtils
            .getFilePathFromResources(name + "/fault_locations.csv")
            .toString();
        final CSVParser csvRecords;
        try {
            csvRecords = CSVFormat
                .EXCEL
                .withFirstRecordAsHeader()
                .parse(new InputStreamReader(new FileInputStream(faultLocationFile), Charset.forName("utf-8")));
        } catch (IOException e) {
            return Optional.empty();
        }

        final List<FaultLocationForProgram> collect = StreamEx
            .of(csvRecords.getRecords())
            .map(a -> new FaultLocationForProgram(
                a.get("version"),
                splitLines(a.get("line_number")),
                a.get("diff"),
                a.get("comments"),
                Boolean.parseBoolean(a.get("in_effect_size"))
            ))
            .toImmutableList();
        return Optional.of(new FaultLocationJam(collect));
    }

    /**
     * 将形如 20-22 这样的字符串分割成 {20,21,22} 这样的 set
     * <p>
     * 可以分割 20,22-24,30 这样的格式的字符串
     *
     * @param input
     * @return
     */
    private static Set<Integer> splitLines(@NonNull String input) {

        final HashSet<Integer> result = new HashSet<>();

        String[] fields = input.split("\\,");
        for (String field : fields) {

            if (field.equals("")) {
                continue;
            }

            final String[] split = field.split("-");

            if (split.length == 1) {
                result.add(Integer.parseInt(split[0]));

            } else if (split.length == 2) {

                final int start = Integer.parseInt(split[0]);
                final int end = Integer.parseInt(split[1]);
                if (start > end) {
                    throw new IllegalArgumentException("for " + input + ": 起点必须比终点低");
                }

                IntStreamEx.range(start, end + 1)
                    .forEach(result::add);

            } else {
                throw new IllegalArgumentException("无法解析此输入");
            }
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("必须至少有一个值");
        }

        return result;
    }

}
