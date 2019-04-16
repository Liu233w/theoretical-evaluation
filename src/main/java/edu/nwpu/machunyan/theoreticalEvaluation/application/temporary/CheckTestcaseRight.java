package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.CoverageRunnerException;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.IProgramInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.RunningScheduler;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.TestcaseResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.RunResultFromRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.GccReadFromStdIoInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.GccReadFromStdIoRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.TestcaseItem;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import lombok.Cleanup;
import me.tongfei.progressbar.ProgressBar;
import one.util.streamex.StreamEx;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

/**
 * 使用本项目的 runner 来运行测试程序的正确代码，来检测 cases.json 的正确性
 */
public class CheckTestcaseRight {

    private static final String sourceFile = "print_tokens/origin/orig/print_tokens.c";

    private static final String programName = "print_tokens";

    public static void main(String[] args) throws URISyntaxException, IOException, CoverageRunnerException {

        final Path file = FileUtils.getFilePathFromResources(sourceFile);
        final List<TestcaseItem> list = TestcaseResolver.resolve(programName);

        final IProgramInput[] inputs = StreamEx
            .of(list)
            .map(a -> new GccReadFromStdIoInput(
                a.getParams(),
                a.getInput(),
                a.getOutput()
            ))
            .map(a -> (IProgramInput) a)
            .toArray(IProgramInput[]::new);

        @Cleanup final ProgressBar progressBar = new ProgressBar("", inputs.length);

        final RunningScheduler scheduler = new RunningScheduler(
            new Program(programName, file.toString()),
            GccReadFromStdIoRunner::new,
            inputs,
            progressBar
        );
        final RunResultFromRunner[] result = scheduler.runAndGetResults();

        StreamEx
            .of(result)
            .filter(a -> !a.isCorrect())
            .forEach(a -> {
                final GccReadFromStdIoInput input = (GccReadFromStdIoInput) a.getInput();
                System.out.println(input);
            });
    }
}
