package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.ProgramDefination;
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
import edu.nwpu.machunyan.theoreticalEvaluation.utils.LogUtils;
import lombok.Cleanup;
import me.tongfei.progressbar.ProgressBar;
import one.util.streamex.StreamEx;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 使用本项目的 runner 来运行测试程序的正确代码，来检测 cases.json 的正确性
 */
public class CheckTestcaseRight {

    public static void main(String[] args) throws URISyntaxException, IOException, CoverageRunnerException {

        for (ProgramDefination.ProgramDir program :
            ProgramDefination.GCC_RUN_LIST) {

            LogUtils.logInfo("Working on " + program.getProgramDir());

            String sourceFile = program.getProgramDir() + "/origin/" + program.getProgramName();
            if (program.getProgramDir().equals("print_tokens")) {
                sourceFile = program.getProgramDir() + "/origin/orig/" + program.getProgramName();
            }
            final String programName = program.getProgramDir();

            final Path file = FileUtils.getFilePathFromResources(sourceFile);
            final List<TestcaseItem> list = TestcaseResolver.resolve(programName);

            final List<IProgramInput> inputs = StreamEx
                .of(list)
                .map(a -> new GccReadFromStdIoInput(
                    a.getParams(),
                    a.getInput(),
                    a.getOutput()
                ))
                .map(a -> (IProgramInput) a)
                .toImmutableList();

            @Cleanup final ProgressBar progressBar = new ProgressBar("", inputs.size());

            final ArrayList<RunResultFromRunner> result = RunningScheduler
                .builder()
                .progressBar(progressBar)
                .build()
                .runAndGetResults(
                    new GccReadFromStdIoRunner(),
                    new Program(programName, file.toString()),
                    inputs);

            StreamEx
                .of(result)
                .filter(a -> !a.isCorrect())
                .forEach(a -> {
                    final GccReadFromStdIoInput input = (GccReadFromStdIoInput) a.getInput();
                    System.out.println(input);
                });
        }
    }
}
