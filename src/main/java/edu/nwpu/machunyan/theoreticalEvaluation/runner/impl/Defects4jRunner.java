package edu.nwpu.machunyan.theoreticalEvaluation.runner.impl;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.CoverageRunnerException;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.ICoverageRunner;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.IProgramInput;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Coverage;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.RunResultFromRunner;

public class Defects4jRunner implements ICoverageRunner {

    private final Defects4jContainerExecutor executor;
    private final String projectName;

    private String version;
    private Defects4jCoverageParser.Defects4jStatementMap statementMap;
    private Program program;

    /**
     * @param executor    用于执行 defects4j 命令的对象
     * @param projectName 项目名称，比如 Lang
     */
    public Defects4jRunner(Defects4jContainerExecutor executor, String projectName) {
        this.executor = executor;
        this.projectName = projectName;
    }

    @Override
    public synchronized void prepare(Program program) throws CoverageRunnerException {

        this.program = program;
        version = program.getTitle();
        statementMap = new Defects4jCoverageParser.Defects4jStatementMap();

        executor.compile(projectName, version);
    }

    @Override
    public synchronized RunResultFromRunner runWithInput(IProgramInput programInput) throws CoverageRunnerException {

        if (!(programInput instanceof Defects4jTestcase)) {
            throw new CoverageRunnerException("input should be an instance of Defects4jTestcase");
        }

        final Defects4jTestcase testcase = (Defects4jTestcase) programInput;
        final Defects4jContainerExecutor.CoverageRunResult coverageRunResult = executor.runTestcaseAndGetResult(projectName, version, testcase);

        final Coverage coverage = Defects4jCoverageParser.generateCoverageFromString(coverageRunResult.getCoverageXml(), statementMap);

        return new RunResultFromRunner(
            program,
            programInput,
            coverageRunResult.isCorrect(),
            coverage,
            statementMap.resolveStatementMap());
    }

    @Override
    public void cleanUp() throws CoverageRunnerException {
        // do nothing
    }
}
