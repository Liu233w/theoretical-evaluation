package edu.nwpu.machunyan.theoreticalEvaluation.runner;

import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.*;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.StreamUtils;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 使用 gcov 执行 c++ 源代码的覆盖率检测。程序从程序参数读取输入，从标准输出输出结果。
 */
public class CppReadFromStdIoRunner implements ICoverageRunner {

    private Program program = null;

    private Path filePath;
    private Path directoryPath;
    private StatementMap statementMap;

    private Path executable;
    private Path gcovFile;
    private Path gcdaFile;

    @Override
    public void prepare(@NonNull Program program) throws CoverageRunnerException {
        this.program = program;

        final String[] cmd = {};

        try {

            // 初始化 StatementMap，顺便检查文件是否存在。
            filePath = Paths.get(program.getPath());
            statementMap = getSimpleStatementMap();
            directoryPath = filePath.getParent();

            waitToRunCommandAndGetProcess(new String[]{
                    "g++", "-fprofile-arcs", "-ftest-coverage", filePath.toString()
            });

            executable = directoryPath.resolve("a");

            final String fileName = filePath.getFileName().toString();
            gcovFile = directoryPath.resolve(fileName + ".gcov");
            gcdaFile = directoryPath.resolve(fileName.replaceAll("\\.cpp|\\.c|\\.cc", "") + ".gcda");

        } catch (IOException | InterruptedException e) {
            throw new CoverageRunnerException(e);
        }
    }

    @Override
    public SingleRunResult runWithInput(IProgramInput programInput) throws CoverageRunnerException {

        if (!(programInput instanceof CppReadFromStdIoInput)) {
            throw new CoverageRunnerException("input should be an instance of CppReadFromStdIoInput");
        }
        final CppReadFromStdIoInput typedInput = (CppReadFromStdIoInput) programInput;

        final String[] command = new String[typedInput.getInput().length + 1];
        command[0] = executable.toString();
        System.arraycopy(typedInput.getInput(), 0, command, 1, typedInput.getInput().length);

        try {

            // remove gcda file to get rid of previous running result
            gcdaFile.toFile().delete();

            // run program and get output
            // 返回非 0 值也是测试的一部分，这里不应该抛出异常
            final Process process = waitToRunCommandAndGetProcess(command, false);
            final InputStream inputStream = process.getInputStream();
            final String output = StreamUtils.convertStreamToString(inputStream);

            // run gcov
            waitToRunCommandAndGetProcess(new String[]{"gcov", filePath.toString()});

            final Coverage coverage = GcovParser.generateCoverageFromFile(gcovFile);

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

    private Process waitToRunCommandAndGetProcess(String[] command) throws InterruptedException, IOException, CoverageRunnerException {
        return waitToRunCommandAndGetProcess(command, true);
    }

    private Process waitToRunCommandAndGetProcess(String[] command, boolean throwWhenNotExitWithZero) throws InterruptedException, IOException, CoverageRunnerException {

        Process process = Runtime.getRuntime().exec(command, null, directoryPath.toFile());

        int returnCode = process.waitFor();

        if (returnCode != 0 && throwWhenNotExitWithZero) {
            throw new CoverageRunnerException("execution failed with return code : " + returnCode + "\n" +
                    "Outputs: \n" +
                    "std out: \n" + StreamUtils.convertStreamToString(process.getInputStream()) + "\n" +
                    "std err: \n" + StreamUtils.convertStreamToString(process.getErrorStream()));
        }

        return process;
    }

    /**
     * 生成一个简单的 statementMap，其中每一项对应源代码中的一行。
     *
     * @return
     * @throws IOException
     */
    private StatementMap getSimpleStatementMap() throws IOException {

        final int lineCount = Files.readAllLines(filePath).size();
        final String filePathStr = filePath.toString();

        return StatementMap.ofLineBasedStatementMap(lineCount, filePathStr);
    }

    @Override
    public void cleanUp() {
        // 不需要清理
    }
}
