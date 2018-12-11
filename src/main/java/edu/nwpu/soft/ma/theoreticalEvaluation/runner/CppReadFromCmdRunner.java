package edu.nwpu.soft.ma.theoreticalEvaluation.runner;

import edu.nwpu.soft.ma.theoreticalEvaluation.dataClass.*;
import edu.nwpu.soft.ma.theoreticalEvaluation.utils.ArrayUtils;
import edu.nwpu.soft.ma.theoreticalEvaluation.utils.StreamUtils;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class CppReadFromCmdRunner implements CoverageRunner {

    private Program program = null;

    private Path filePath;
    private String filePathStr;
    private StatementMap statementMap;

    @Override
    public void prepare(@NonNull Program program) throws CoverageRunnerException {
        this.program = program;

        filePathStr = program.getPath();
        final String[] cmd = {"g++", "-fprofile-arcs", "-ftest-coverage", filePathStr};

        try {

            // 初始化 StatementMap，顺便检查文件是否存在。
            filePath = Paths.get(filePathStr);
            statementMap = getSimpleStatementMap();

            Process process = Runtime.getRuntime().exec(cmd);
            waitProcessAndAssertReturnCode(process);

        } catch (IOException | InterruptedException e) {
            throw new CoverageRunnerException(e);
        }
    }

    @Override
    public SingleRunResult runWithInput(ProgramInput programInput) throws CoverageRunnerException {

        if (!(programInput instanceof CppReadFromCmdInput)) {
            throw new CoverageRunnerException("input should be an instance of CppReadFromCmdInput");
        }
        final CppReadFromCmdInput typedInput = (CppReadFromCmdInput) programInput;

        final Path path = Paths.get(program.getPath());
        final Path dir = path.getParent();
        final Path executable = dir.resolve("a");

        final String[] command = new String[typedInput.getInput().length + 1];
        command[0] = executable.toString();
        ArrayUtils.fillBy(command, typedInput.getInput(), 1);

        try {
            final Process process = Runtime.getRuntime().exec(command);
            waitProcessAndAssertReturnCode(process);

            final InputStream inputStream = process.getInputStream();
            final String output = StreamUtils.convertStreamToString(inputStream);

            final Coverage coverage = GcovParser.generateCoverageFromFile(filePath);

            return new SingleRunResult(
                    program,
                    programInput,
                    output.equals(typedInput.getShouldOutputFromStdOut()),
                    coverage,
                    statementMap);

        } catch (IOException | InterruptedException e) {
            throw new CoverageRunnerException(e);
        }
    }

    private static void waitProcessAndAssertReturnCode(Process process) throws InterruptedException, CoverageRunnerException {

        int returnCode = process.waitFor();

        if (returnCode != 0) {
            throw new CoverageRunnerException("execution failed with return code : " + returnCode);
        }
    }

    /**
     * 生成一个简单的 statementMap，其中每一项对应源代码中的一行。
     *
     * @return
     * @throws IOException
     */
    private StatementMap getSimpleStatementMap() throws IOException {
        final int lineNumber = Files.readAllLines(filePath).size();
        final StatementMap statementMap = new StatementMap(StatementMapType.LINE_BASED);

        final ArrayList<StatementInfo> mapList = statementMap.getMapList();
        mapList.ensureCapacity(lineNumber + 1);
        mapList.add(null);
        for (int i = 1; i <= lineNumber; i++) {
            mapList.add(new StatementInfo(i, filePathStr, i, i));
        }

        return statementMap;
    }

    @Override
    public void cleanUp() {
        // 不需要清理
    }
}
