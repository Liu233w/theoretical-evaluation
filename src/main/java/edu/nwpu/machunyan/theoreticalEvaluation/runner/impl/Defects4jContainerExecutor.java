package edu.nwpu.machunyan.theoreticalEvaluation.runner.impl;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ExecState;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.CoverageRunnerException;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import lombok.Value;
import one.util.streamex.StreamEx;

import java.io.Closeable;
import java.util.*;

/**
 * 持有一个 docker container 实例，用来运行 defects4j 有关的命令。
 */
public class Defects4jContainerExecutor implements Closeable {

    /*
    笔记：
    在执行新的测试之后，之前放在 failing_tests 里的内容会被清空

     */

    private static final String IMAGE_NAME = "liu233w/defects4j";
    private static final String BASE_DIR = "/app/";

    private DockerClient client;
    private String containerId;

    private Defects4jContainerExecutor(
        DockerClient client, String containerId) {

        this.client = client;
        this.containerId = containerId;
    }

    public static Defects4jContainerExecutor getInstance()
        throws CoverageRunnerException {

        try {
            final DefaultDockerClient client = new DefaultDockerClient("unix:///var/run/docker.sock");

            final List<Container> containers = client.listContainers(
                DockerClient.ListContainersParam.withLabel("DEFECTS4J_EXECUTOR"),
                DockerClient.ListContainersParam.withStatusExited()
            );
            if (containers.size() > 0) {
                final Container container = containers.get(0);
                client.startContainer(container.id());
                return new Defects4jContainerExecutor(client, container.id());

            } else {

                client.pull(IMAGE_NAME);

                final ContainerConfig containerConfig = ContainerConfig.builder()
                    .image(IMAGE_NAME)
                    .cmd("sh", "-c", "while :; do sleep 1; done")
                    .labels(Collections.singletonMap("DEFECTS4J_EXECUTOR", "1"))
                    .build();
                final String containerId = client.createContainer(containerConfig).id();
                client.startContainer(containerId);
                return new Defects4jContainerExecutor(client, containerId);
            }

        } catch (DockerException | InterruptedException e) {
            throw new CoverageRunnerException("exception from docker.", e);
        }
    }

    public void close() {

        if (client == null) {
            return;
        }

        try {
            client.killContainer(containerId);
        } catch (DockerException | InterruptedException e) {
            e.printStackTrace();
        }
        client.close();

        client = null;
    }

    private static String resolveSrcDir(String programName, String version) {
        return BASE_DIR + programName + "_" + version + "/";
    }

    public Map<Program, List<Defects4jTestcase>> resolveTestcases(
        String programName)
        throws CoverageRunnerException {
        return resolveTestcases(programName, null);
    }

    /**
     * 获取所有的版本及其测试用例
     *
     * @param programName 程序名称，例如 Lang
     * @return
     * @throws CoverageRunnerException
     */
    public Map<Program, List<Defects4jTestcase>> resolveTestcases(
        String programName,
        TestcaseResolvingProgressHandler progressHandler)
        throws CoverageRunnerException {

        final String bugNumStr = exec("defects4j info -p " + programName + " | grep 'Number of bugs:' | awk '{print $NF}'")
            .trim();
        final int bugNum = Integer.parseInt(bugNumStr);

        if (progressHandler != null) {
            progressHandler.reportVersionCount(bugNum);
        }

        // 创建父文件夹
        exec("mkdir -p " + BASE_DIR);

        final HashMap<Program, List<Defects4jTestcase>> result = new HashMap<>();
        for (int i = 0; i < bugNum; i++) {
            final String version = (i + 1) + "b";
            final String dir = resolveSrcDir(programName, version);

            if (progressHandler != null) {
                final Optional<List<Defects4jTestcase>> optional = progressHandler.tryGet(version);
                if (optional.isPresent()) {
                    result.put(new Program(version, dir), optional.get());
                    continue;
                }
            }

            checkout(programName, version);
            exec("defects4j test -w " + dir);
            final String allTests = exec("cat " + dir + "all_tests");

            final List<Defects4jTestcase> testcases = StreamEx
                .of(allTests.split("\n"))
                // 一行的格式：
                // testOneArgNull(org.apache.commons.lang3.AnnotationUtilsTest)
                .map(line -> {
                    final int lb = line.indexOf("(");
                    final String method = line.substring(0, lb);
                    final String clazz = line.substring(lb + 1, line.length() - 1);
                    return new Defects4jTestcase(clazz, method);
                })
                .toImmutableList();

            if (progressHandler != null) {
                progressHandler.report(version, testcases, i);
            }

            result.put(new Program(version, dir), testcases);
        }

        return result;
    }

    /**
     * 运行一个测试用例并获取其结果（包括覆盖率文件和结果是否正确）
     *
     * @param programName
     * @param version
     * @param testcase
     * @return
     * @throws CoverageRunnerException
     */
    public CoverageRunResult runTestcaseAndGetResult(
        String programName,
        String version,
        Defects4jTestcase testcase)
        throws CoverageRunnerException {

        final String dir = resolveSrcDir(programName, version);
        exec("defects4j coverage -w " + dir + " -t "
            + testcase.getTestcaseClass() + "::" + testcase.getTestcaseMethod());

        final boolean isCorrect = getFileLength(dir + "failing_tests") == 0;
        final String coverageXml = exec("cat " + dir + "coverage.xml");

        return new CoverageRunResult(isCorrect, coverageXml);
    }

    /**
     * 编译一个程序
     *
     * @param programName
     * @param version
     * @throws CoverageRunnerException
     */
    public void compile(String programName, String version) throws CoverageRunnerException {
        checkout(programName, version);
        exec("defects4j compile -w " + resolveSrcDir(programName, version));
    }

    /**
     * 获取文件的长度，必须确保文件存在
     *
     * @param path
     * @return
     * @throws CoverageRunnerException
     */
    private int getFileLength(String path) throws CoverageRunnerException {
        return Integer.parseInt(exec("du -k " + path + " | cut -f1").trim());
    }

    /**
     * 生成相应的源代码，如果已经存在，则不会生成
     *
     * @param programName
     * @param version     defects4j 要求的版本号格式 (1b, 2f 等等)
     * @throws CoverageRunnerException
     */
    private void checkout(String programName, String version) throws CoverageRunnerException {

        final String dir = resolveSrcDir(programName, version);
        exec(String.format("if [ ! -d '%s' ] ; then mkdir -p %s && defects4j checkout -p %s -v %s -w %s ; fi",
            dir, dir, programName, version, dir));
    }

    /**
     * 在容器中执行命令，并等待执行结束
     *
     * @param cmd
     * @return
     * @throws DockerException
     * @throws InterruptedException
     */
    private String exec(String cmd)
        throws CoverageRunnerException {

        LogUtils.logFine("command send to docker: " + cmd);

        try {
            final String id = client
                .execCreate(containerId, new String[]{"bash", "-c", cmd},
                    DockerClient.ExecCreateParam.attachStdout(),
                    DockerClient.ExecCreateParam.attachStderr())
                .id();

            final String res = client.execStart(id).readFully();

            final ExecState execState = client.execInspect(id);
            if (execState.running()) {
                throw new CoverageRunnerException("still running");
            }
            if (execState.exitCode() != 0) {
                throw new CoverageRunnerException("return code not zero from docker. \nMessage:\n" + res);
            }

            return res;

        } catch (DockerException | InterruptedException e) {
            throw new CoverageRunnerException("exception from docker.", e);
        }
    }

    /**
     * 处理 {@link Defects4jContainerExecutor#resolveTestcases(String, TestcaseResolvingProgressHandler)} 的进度
     */
    public interface TestcaseResolvingProgressHandler {

        /**
         * 尝试直接获取结果
         *
         * @param version 版本号
         * @return
         */
        Optional<List<Defects4jTestcase>> tryGet(String version);

        /**
         * 汇报当前的运行结果
         *
         * @param version
         * @param testcases
         * @param index
         */
        void report(String version, List<Defects4jTestcase> testcases, int index);

        /**
         * 汇报总的版本数量
         *
         * @param number
         */
        void reportVersionCount(int number);
    }

    /**
     * 一个测试用例的运行结果
     */
    @Value
    public static class CoverageRunResult {
        /**
         * 是否执行正确
         */
        boolean isCorrect;
        /**
         * 生成出来的 coverage xml
         */
        String coverageXml;
    }
}
