package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.ProgramDefination;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.CoverageRunnerException;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.Defects4jContainerExecutor;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.Defects4jTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import lombok.Cleanup;
import lombok.Value;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * 获取 defects4j 中的所有测试用例
 */
public class ResolveDefects4jTestcase {

    private static final Path OUTPUT_DIR = Paths.get("./target/outputs/defects4j-testcases");

    public static void main(String[] args) throws CoverageRunnerException, IOException {

        @Cleanup final Defects4jContainerExecutor executor = Defects4jContainerExecutor.getInstance();

        for (String programName : ProgramDefination.DEFECTS4J_RUN_LIST) {

            if (Files.exists(resolveResultFilePath(programName))) {
                continue;
            }
            LogUtils.logInfo("working on " + programName);

            final Map<Program, List<Defects4jTestcase>> result = executor.resolveTestcases(programName);

            final List<TestcaseForProgram> list = EntryStream
                .of(result)
                .map(a -> new TestcaseForProgram(a.getKey(), a.getValue()))
                .toImmutableList();
            final TestcaseJam jam = new TestcaseJam(list);
            FileUtils.saveObject(resolveResultFilePath(programName), jam);
        }
    }

    /**
     * 从运行结果读取数据，必须已经存在
     *
     * @return
     */
    public static Map<Program, List<Defects4jTestcase>> getResultFromFile(
        String name) throws FileNotFoundException {

        final TestcaseJam testcaseJam = FileUtils.loadObject(resolveResultFilePath(name), TestcaseJam.class);
        return StreamEx
            .of(testcaseJam.getInner())
            .mapToEntry(TestcaseForProgram::getProgram, TestcaseForProgram::getTestcases)
            .toImmutableMap();
    }

    private static Path resolveResultFilePath(String programName) {
        return OUTPUT_DIR.resolve(programName + ".json");
    }

    @Value
    private static class TestcaseJam {
        List<TestcaseForProgram> inner;
    }

    @Value
    private static class TestcaseForProgram {
        Program program;
        List<Defects4jTestcase> testcases;
    }
}
