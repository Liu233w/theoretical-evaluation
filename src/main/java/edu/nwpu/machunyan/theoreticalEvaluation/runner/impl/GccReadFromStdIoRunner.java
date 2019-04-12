package edu.nwpu.machunyan.theoreticalEvaluation.runner.impl;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.CoverageRunnerException;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.ICoverageRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.IProgramInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Coverage;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.RunResultFromRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.StatementMap;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.StreamUtils;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 使用 gcov 执行 c++ 源代码的覆盖率检测。程序从程序参数读取输入，从标准输出输出结果。
 */
public class GccReadFromStdIoRunner implements ICoverageRunner {

    private Program program = null;

    private Path filePath;
    private Path directoryPath;
    private StatementMap statementMap;

    private Path executable;
    private Path gcovFile;
    private Path gcdaFile;

    /**
     * 工厂方法
     *
     * @return
     */
    public static GccReadFromStdIoRunner newInstance() {
        return new GccReadFromStdIoRunner();
    }

    /**
     * 根据文件的后缀名来确定要使用的编译器。（cpp和cc使用 g++，c使用 gcc）
     *
     * @param fileName 文件名，可以包含文件的完整路径
     * @return
     * @throws CoverageRunnerException 如果无法判断此后缀名时抛出
     */
    public static String decideCompilerFromFileExtension(String fileName) throws CoverageRunnerException {

        if (fileName.matches("^.*\\.(cpp|cc)$")) {
            return "g++";
        } else if (fileName.matches("^.*\\.c")) {
            return "gcc";
        } else {
            throw new CoverageRunnerException("runner doesn't support such file extension");
        }
    }

    @Override
    public synchronized void prepare(@NonNull Program program) throws CoverageRunnerException {
        this.program = program;

        final String[] cmd = {};

        try {

            // 初始化 StatementMap，顺便检查文件是否存在。
            filePath = Paths.get(program.getPath());
            statementMap = getSimpleStatementMap();
            directoryPath = filePath.getParent();

            final String compiler = decideCompilerFromFileExtension(program.getPath());

            waitToRunCommandAndGetProcess(new String[]{
                compiler, "-I.", "-fprofile-arcs", "-ftest-coverage", filePath.toString()
            });

            final String[] exeFileNames = new String[]{"a", "a.out", "a.exe"};
            for (String name : exeFileNames) {
                executable = directoryPath.resolve(name);
                final File file = executable.toFile();
                if (file.exists()) {
                    file.setExecutable(true);
                    return;
                }
            }

            final String fileName = filePath.getFileName().toString();
            gcovFile = directoryPath.resolve(fileName + ".gcov");
            gcdaFile = directoryPath.resolve(fileName.replaceAll("\\.cpp|\\.c|\\.cc", "") + ".gcda");

        } catch (IOException | InterruptedException e) {
            throw new CoverageRunnerException(e);
        }
    }

    @Override
    public synchronized RunResultFromRunner runWithInput(IProgramInput programInput) throws CoverageRunnerException {

        if (!(programInput instanceof GccReadFromStdIoInput)) {
            throw new CoverageRunnerException("input should be an instance of GccReadFromStdIoInput");
        }
        final GccReadFromStdIoInput typedInput = (GccReadFromStdIoInput) programInput;

        final String[] command = new String[typedInput.getInputFromCommand().length + 1];
        command[0] = executable.toString();
        System.arraycopy(typedInput.getInputFromCommand(), 0, command, 1, typedInput.getInputFromCommand().length);

        try {

            // remove gcda file to get rid of previous running result
            gcdaFile.toFile().delete();

            // run program and get output
            // 返回非 0 值也是测试的一部分，这里不应该抛出异常
            final Process process = waitToRunCommandAndGetProcess(command, typedInput.getInputFromStdIn(), false);
            final InputStream inputStream = process.getInputStream();
            final String output = StreamUtils.convertStreamToString(inputStream);

            // run gcov
            waitToRunCommandAndGetProcess(new String[]{"gcov", filePath.toString()});

            final Coverage coverage = GcovParser.generateCoverageFromFile(gcovFile);

            return new RunResultFromRunner(
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
        return waitToRunCommandAndGetProcess(command, "", true);
    }

    private Process waitToRunCommandAndGetProcess(String[] command, String input, boolean throwWhenNotExitWithZero) throws InterruptedException, IOException, CoverageRunnerException {

        Process process = Runtime.getRuntime().exec(command, null, directoryPath.toFile());
        final OutputStream outputStream = process.getOutputStream();
        outputStream.write(input.getBytes());
        outputStream.flush();
        outputStream.close();

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
