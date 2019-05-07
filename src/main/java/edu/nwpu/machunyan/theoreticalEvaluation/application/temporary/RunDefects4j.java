package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.application.ResolveDefects4jTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.CoverageRunnerException;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.Defects4jContainerExecutor;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.Defects4jTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import one.util.streamex.EntryStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

public class RunDefects4j {

    public static void main(String[] args) throws CoverageRunnerException, IOException {

        String programName = "Lang";
        String version = "1b";

        final Defects4jContainerExecutor executor = Defects4jContainerExecutor.newInstance();
        Runtime.getRuntime().addShutdownHook(new Thread(executor::close));

        final Map<Program, List<Defects4jTestcase>> lang = ResolveDefects4jTestcase.getResultFromFile(programName);
        final List<Defects4jTestcase> testcases = EntryStream.of(lang)
            .filterKeys(a -> a.getTitle().equals(version))
            .findAny()
            .get()
            .getValue();

        executor.compile(programName, version);


        for (Defects4jTestcase testcase : testcases) {

            final String path = "./target/outputs/defects4j-temp-coverage-result/"
                + testcase.getTestcaseClass() + "_" + testcase.getTestcaseMethod()
                + ".xml";

            if (Files.exists(Paths.get(path))) {
                continue;
            }

            final Defects4jContainerExecutor.CoverageRunResult res = executor.runTestcaseAndGetResult(programName, version, testcase);
            FileUtils.saveString(path, res.getCoverageXml());
            Files.write(
                Paths.get("./target/outputs/defects4j-temp-coverage-result/result.txt"),
                (testcase.getTestcaseClass() + "::" + testcase.getTestcaseMethod() + " " + res.isCorrect() + "\n").getBytes(),
                StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }
    }
}
