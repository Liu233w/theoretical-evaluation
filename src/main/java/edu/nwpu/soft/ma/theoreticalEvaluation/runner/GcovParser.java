package edu.nwpu.soft.ma.theoreticalEvaluation.runner;

import edu.nwpu.soft.ma.theoreticalEvaluation.dataClass.Coverage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static edu.nwpu.soft.ma.theoreticalEvaluation.utils.ParameterUtils.checkNull;

/**
 * 从 gcov 文件生成覆盖信息，基于行
 */
public class GcovParser {

    /**
     * 基于行的覆盖信息生成器，返回每一行的执行次数
     *
     * @param path 覆盖率信息文件
     * @return
     * @throws IOException
     */
    public static Coverage generateCoverageFromFile(Path path) throws IOException {
        checkNull(path);

        List<String> lines = Files.readAllLines(path);
        GcovParser gcovParser = new GcovParser();
        gcovParser.doParse(lines);

        return gcovParser.coverage;
    }

    // ----------------------------------------------------

    private Coverage coverage = new Coverage();

    private GcovParser() {
    }

    private void doParse(List<String> lines) {

        for (String line :
                lines) {

            String[] parts = line.trim().split("[\\s:]+");

            // 至少需要两个参数来分析
            if (parts.length < 2) {
                continue;
            }

            // skip un-hit lines
            /*
            example:
                    -:     17:    } else {
                    #####: 18:        cout << "else" << endl;
            */
            if (parts[0].matches("-|#####|\\$\\$\\$\\$\\$|%%%%%")) {
                continue;
            }

            /*
            将多个 block 中的代码算作一行
            1:   30-block  0
            ^    ^      ^  ^
            |    |      |  |
            |    --------  |
            1    2         3
             */
            Integer hitOrNull = Integer.getInteger(parts[0], 10);
            if (hitOrNull != null && parts[1].endsWith("-block")) {
                int lineNumber = Integer.getInteger(parts[1].replace("-block", ""), 10);
                addHitToLineCoverage(lineNumber, hitOrNull);
                continue;
            }

            /*
            一行普通的代码
            1:   14:    int a = stoi(argv[1]);
             */
            Integer lineNumberOrNull = Integer.getInteger(parts[1], 10);
            if (hitOrNull != null && lineNumberOrNull != null) {
                addHitToLineCoverage(lineNumberOrNull, hitOrNull);
                continue;
            }

            // 跳过其他情况
            /*
            以 branch、unconditional 或者 function 开头的
             */
        }
    }

    // 如果某一行出现多次，叠加其执行次数
    /*
    example: https://gcc.gnu.org/onlinedocs/gcc/Invoking-Gcov.html#Invoking-Gcov
           1*:    7:  Foo(): b (1000) {}
    ------------------
    Foo<char>::Foo():
        #####:    7:  Foo(): b (1000) {}
    ------------------
    Foo<int>::Foo():
            1:    7:  Foo(): b (1000) {}

    其中第一行 `1*` 会被跳过
     */
    private void addHitToLineCoverage(int lineNumber, int hit) {
        int prevHit = coverage.getCoverageForStatement(lineNumber);
        coverage.setCoverageForStatement(lineNumber, prevHit + hit);
    }
}
