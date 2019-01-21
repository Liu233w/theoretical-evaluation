package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorHelper;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvExporter;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.CsvLine;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class DiffTotInfoSfWithWeightSimplifiedByFaultLocation {

    private static final String outputFile = "./target/outputs/totInfoSfWeightDiffSimplifiedByFaultLocation.csv";

    public static void main(String[] args) throws URISyntaxException, IOException {

        final FaultLocationJam faultLocations = getFaultLocations();
        final SuspiciousnessFactorJam sfUnweighted = ResolveTotInfoSuspiciousnessFactor.getResultFromFile();
        final Map<String, SuspiciousnessFactorJam> sfWeighted = ResolveTotInfoSuspiciousnessFactorWithWeight.resolveAndGetResult();

        final Map<String, DiffRankJam> collect = StreamEx
            .of(sfWeighted.entrySet())
            .mapToEntry(
                Map.Entry::getKey,
                a -> SuspiciousnessFactorHelper.diff(
                    sfUnweighted, a.getValue(),
                    "unweighted", "weighted",
                    faultLocations
                )
            )
            .toImmutableMap();

        FileUtils.saveString(outputFile, toCsvString(collect));
    }

    private static FaultLocationJam getFaultLocations() throws URISyntaxException, IOException {

        final String faultLocationFile = FileUtils
            .getFilePathFromResources("tot_info/fault_locations.csv")
            .toString();
        final CSVParser csvRecords = CSVFormat
            .EXCEL
            .withFirstRecordAsHeader()
            .parse(new FileReader(faultLocationFile));

        final List<FaultLocationForProgram> collect = StreamEx
            .of(csvRecords.getRecords())
            .map(a -> new FaultLocationForProgram(
                a.get(0),
                splitLines(a.get(1))
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

    private static String toCsvString(Map<String, DiffRankJam> map) {

        //noinspection OptionalGetWithoutIsPresent
        final DiffRankForProgram sampleDiff = map
            .values()
            .stream()
            .findFirst()
            .get()
            .getDiffRankForPrograms()
            .get(0);
        final String leftRankTitle = sampleDiff.getLeftRankTitle();
        final String rightRankTitle = sampleDiff.getRightRankTitle();

        final ArrayList<CsvLine> csvLines = new ArrayList<>();

        csvLines.add(new CsvLine(new Object[]{
            "weighting formula", "program title", "formula", "statement index",
            leftRankTitle + "-rank", leftRankTitle + "-suspiciousnessFactor",
            rightRankTitle + "-rank", rightRankTitle + "-suspiciousnessFactor",
            "rank diff"
        }));

        map.forEach((weightFormulaTitle, jam) -> {
            for (DiffRankForProgram program : jam.getDiffRankForPrograms()) {
                for (DiffRankForStatement statement : program.getDiffRankForStatements()) {

                    final DiffRankForSide left = statement.getLeft();
                    final DiffRankForSide right = statement.getRight();

                    csvLines.add(new CsvLine(new Object[]{
                        weightFormulaTitle,
                        program.getProgramTitle(), program.getFormulaTitle(),
                        statement.getStatementIndex(),
                        left.getRank(), left.getSuspiciousnessFactor(),
                        right.getRank(), right.getSuspiciousnessFactor(),
                        statement.getRankDiff(),
                    }));
                }
            }
        });

        return CsvExporter.toCsvString(csvLines);
    }
}
