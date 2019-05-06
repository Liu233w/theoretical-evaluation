package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.application.ResolveDefects4jTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.CoverageRunnerException;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.Defects4jContainerExecutor;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.Defects4jTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;
import one.util.streamex.EntryStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RunDefects4j {

    public static void main(String[] args) throws CoverageRunnerException, IOException {

        String programName = "Lang";
        String version = "1b";

        final Defects4jContainerExecutor executor = Defects4jContainerExecutor.getInstance();
        final Map<Program, List<Defects4jTestcase>> lang = ResolveDefects4jTestcase.getResultFromFile(programName);
        final List<Defects4jTestcase> testcases = EntryStream.of(lang)
            .filterKeys(a -> a.getTitle().equals(version))
            .findAny()
            .get()
            .getValue();

        executor.compile(programName, version);

        for (Defects4jTestcase testcase : testcases) {
            final Defects4jContainerExecutor.CoverageRunResult res = executor.runTestcaseAndGetResult(programName, version, testcase);

            FileUtils.saveString("./target/outputs/defects4j-temp-coverage-result/"
                    + testcase.getTestcaseClass() + "_" + testcase.getTestcaseMethod() + "_" + res.isCorrect()
                    + ".xml",
                res.getCoverageXml());
        }
    }
}
