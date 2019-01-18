package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.FaultLocationForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.FaultLocationJam;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DiffTotInfoSfWithWeightSimplifiedByFaultLocation {

    public static void main(String[] args) throws URISyntaxException, IOException {

        final FaultLocationJam faultLocations = getFaultLocations();
        final SuspiciousnessFactorJam sfUnweighted = ResolveTotInfoSuspiciousnessFactor.getResultFromFile();
        final Map<String, SuspiciousnessFactorJam> sfWeighted = ResolveTotInfoSuspiciousnessFactorWithWeight.resolveAndGetResult();


    }

    private static FaultLocationJam getFaultLocations() throws URISyntaxException, IOException {

        final String faultLocationFile = FileUtils
            .getFilePathFromResources("tot_info/fault_locations.csv")
            .toString();
        final CSVParser csvRecords = CSVFormat
            .EXCEL
            .withFirstRecordAsHeader()
            .parse(new FileReader(faultLocationFile));

        final List<FaultLocationForProgram> collect = csvRecords
            .getRecords()
            .stream()
            .map(a -> new FaultLocationForProgram(
                a.get(0),
                splitLines(a.get(1))
            ))
            .collect(Collectors.toList());
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

            return IntStream.range(start, end + 1)
                .boxed()
                .collect(Collectors.toSet());

        } else {
            throw new IllegalArgumentException("无法解析此输入");
        }
    }
}
