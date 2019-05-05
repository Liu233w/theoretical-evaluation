package edu.nwpu.machunyan.theoreticalEvaluation.runner.impl;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ExecState;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.CoverageRunnerException;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import one.util.streamex.StreamEx;
import sun.jvm.hotspot.utilities.Assert;

import java.io.Closeable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 持有一个 docker container 实例，用来运行 defects4j 有关的命令。
 */
public class Defects4jContainerExecutor implements Closeable {

    private static final String IMAGE_NAME = "liu233w/defects4j";
    private static final String BASE_DIR = "/app/";

    private static Defects4jContainerExecutor instance = null;

    private DockerClient client;
    private String containerId;

    private Defects4jContainerExecutor(
        DockerClient client, String containerId) {

        this.client = client;
        this.containerId = containerId;
    }

    public static synchronized Defects4jContainerExecutor getInstance()
        throws CoverageRunnerException {

        if (instance == null) {

            try {
                final DefaultDockerClient client = new DefaultDockerClient("unix:///var/run/docker.sock");

                final List<Container> containers = client.listContainers(
                    DockerClient.ListContainersParam.withLabel("DEFECTS4J_EXECUTOR"),
                    DockerClient.ListContainersParam.withStatusExited()
                );
                if (containers.size() > 0) {
                    final Container container = containers.get(0);
                    client.startContainer(container.id());
                    instance = new Defects4jContainerExecutor(client, container.id());

                } else {

                    client.pull(IMAGE_NAME);

                    final ContainerConfig containerConfig = ContainerConfig.builder()
                        .image(IMAGE_NAME)
                        .cmd("sh", "-c", "while :; do sleep 1; done")
                        .labels(Collections.singletonMap("DEFECTS4J_EXECUTOR", "1"))
                        .build();
                    final String containerId = client.createContainer(containerConfig).id();
                    client.startContainer(containerId);
                    instance = new Defects4jContainerExecutor(client, containerId);
                }

            } catch (DockerException | InterruptedException e) {
                throw new CoverageRunnerException("exception from docker.", e);
            }
        }

        return instance;
    }

    public void close() {
        try {
            client.killContainer(containerId);
//            client.removeContainer(containerId);
        } catch (DockerException | InterruptedException e) {
            e.printStackTrace();
        }
        client.close();
    }

    public static String resolveDir(String programName, String version) {
        return BASE_DIR + programName + "_" + version + "/";
    }

    /**
     * 获取所有的版本及其测试用例
     *
     * @param programName 程序名称，例如 Lang
     * @return
     * @throws CoverageRunnerException
     */
    public Map<Program, List<Defects4jTestcase>> resolveTestcases(String programName)
        throws CoverageRunnerException {

        final String bugNumStr = exec("defects4j info -p " + programName + " | grep 'Number of bugs:' | awk '{print $NF}'")
            .trim();
        final int bugNum = Integer.parseInt(bugNumStr);

        final HashMap<Program, List<Defects4jTestcase>> result = new HashMap<>();
        for (int i = 0; i < bugNum; i++) {
            final String version = (i + 1) + "b";
            final String dir = resolveDir(programName, version);
            exec("defects4j checkout -p " + programName + " -v " + version
                + " -w " + dir);
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

            result.put(new Program(version, dir), testcases);
        }

        return result;
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

        try {
            final String id = client
                .execCreate(containerId, new String[]{"bash", "-c", cmd},
                    DockerClient.ExecCreateParam.attachStdout(),
                    DockerClient.ExecCreateParam.attachStderr())
                .id();

            final String res = client.execStart(id).readFully();

            final ExecState execState = client.execInspect(id);
            Assert.that(!execState.running(), "not running");
            if (execState.exitCode() != 0) {
                throw new CoverageRunnerException("return code not zero from docker. \nMessage:\n" + res);
            }

            return res;

        } catch (DockerException | InterruptedException e) {
            throw new CoverageRunnerException("exception from docker.", e);
        }
    }
}
