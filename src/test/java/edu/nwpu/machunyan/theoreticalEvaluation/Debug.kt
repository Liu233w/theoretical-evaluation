package edu.nwpu.machunyan.theoreticalEvaluation

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.*
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorForProgram
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgram
import edu.nwpu.machunyan.theoreticalEvaluation.application.ResolveDefects4jTestcase
import edu.nwpu.machunyan.theoreticalEvaluation.application.Run
import edu.nwpu.machunyan.theoreticalEvaluation.runner.RunningScheduler
import edu.nwpu.machunyan.theoreticalEvaluation.runner.TestcaseResolver
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.Defects4jCoverageParser
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.GccReadFromStdIoInput
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.GccReadFromStdIoRunner
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.TestcaseItem
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils
import one.util.streamex.IntStreamEx
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.IntStream
import java.util.stream.Stream

fun main() {

    extractTestcases()
}

fun extractTestcases() {
    val resultFromFile = ResolveDefects4jTestcase.getResultFromFile("Chart")
    resultFromFile.filterKeys { it.title == "19b" }
        .entries.first().value
        .filter { it.testcaseClass == "org.jfree.chart.axis.junit.CategoryAxisTests" && it.testcaseMethod == "testCloning" }
        .let {
            println(it)
        }
}

fun tcasSf() {
    val resultJam = Run.getResultFromFile("tcas")
    val vtm = VectorTableModelResolver.resolve(resultJam)
    val sf = SuspiciousnessFactorResolver
        .builder()
        .formula(SuspiciousnessFactorFormulas::o)
        .formulaTitle("o")
        .build()
        .resolve(vtm)
}

fun filterTcasTestcase(): List<TestcaseItem> {

    val resultJam = Run.getResultFromFile("tcas")
    val v1 = resultJam.runResultForPrograms
        .first { it.programTitle == "v1" }

    // 行号等于语句编号
    v1.statementMap.mapList.forEachIndexed { index, it ->
        if (index == 0) return@forEachIndexed
        assert(it.statementIndex == it.startRow)
    }

    val testcase = TestcaseResolver.resolve("tcas")

    val testcase2 = testcase.filterIndexed { idx, _ ->
        val runResultForTestcase = v1.runResults[idx]
        !runResultForTestcase.isCorrect
            && runResultForTestcase.coverage.getCoverageForStatement(75) == 0
    }

    testcase2.forEach(::println)
    return testcase2
}

fun testDefects4jCoverageParser() {
    val res = Files.readString(Paths.get("./target/outputs/defects4j-temp-coverage-result/org.apache.commons.lang3.AnnotationUtilsTest_testAnnotationsOfDifferingTypes.xml"))
    val defects4jStatementMap = Defects4jCoverageParser.Defects4jStatementMap()
    val coverage = Defects4jCoverageParser.generateCoverageFromString(res, defects4jStatementMap)
    println(coverage)
}

fun reRunTcas() {
    val testcase = filterTcasTestcase()

    val results = RunningScheduler
        .builder().build()
        .runAndGetResults(
            { GccReadFromStdIoRunner().also { it.isDebug = true } },
            Program("tcas-v1", FileUtils.getFilePathFromResources("tcas/versions/v1/tcas.c").toString()),
            testcase.map { GccReadFromStdIoInput(it.params, it.output) })
    results.forEach(::println)
}

val testcasesFromPaper = buildRunResultsFromMatrix(arrayOf(
    arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
    arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 0),
    arrayOf(1, 1, 0, 0, 0, 0, 1, 1, 1),
    arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
    arrayOf(0, 0, 1, 0, 1, 1, 1, 1, 1),
    arrayOf(0, 0, 1, 0, 1, 1, 1, 1, 1),
    arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
    arrayOf(1, 1, 1, 1, 1, 1, 0, 0, 1),
    arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
    arrayOf(0, 0, 1, 1, 0, 1, 1, 1, 1)
), arrayOf(1, 1, 1, 1, 1, 1, 0, 0, 0))

val formula = SuspiciousnessFactorFormulas::o
val formulaTitle = "o"

fun diffTestcaseWeightFromPaper() {

    val sfUnweighted = resolveSfFromPaper()

    val weight = TestcaseWeightResolver
        .builder()
        .sfFormula(formula)
        .formulaTitle(formulaTitle)
        .useParallel(false)
        .build()
        .resolve(testcasesFromPaper)
    val sfWeighted = resolveSfFromPaper(weight)

    val diff = DiffRankResolver.resolve(
        sfUnweighted,
        sfWeighted,
        "",
        "")

    println(diff)
}

fun resolveSfFromPaper(weight: TestcaseWeightForProgram? = null): SuspiciousnessFactorForProgram {

    val vtm = if (weight == null) {
        VectorTableModelResolver.resolve(testcasesFromPaper)
    } else {
        VectorTableModelResolver.resolveWithWeights(testcasesFromPaper, weight.testcaseWeights)
    }
    return SuspiciousnessFactorResolver
        .builder()
        .formula(formula)
        .formulaTitle(formulaTitle)
        .build()
        .resolve(vtm)
}

fun diffTcasVtm() {

    val resultJam = Run.getResultFromFile("tcas")
    val runResultForProgram = resultJam.runResultForPrograms.find { it.programTitle == "v1" }!!

    val vtm = VectorTableModelResolver.resolve(runResultForProgram)

    val statementCount = runResultForProgram.statementMap.statementCount

    val res = IntStreamEx.range(0, runResultForProgram.runResults.size)
        .mapToObj { i -> VectorTableModelResolver.resolve(buildStreamSkipAt(runResultForProgram.runResults, i), statementCount) }
        .filter {
            assert(it.size == vtm.records.size)
            for (i in 0..it.size) {
                if (it[i] != vtm.records[i]) {
                    return@filter true
                }
            }
            return@filter false
        }
        .toImmutableList()

    val ap = res.map {
        AveragePerformanceResolver.resolve(it, formula)
    }

    println(res)
}

fun buildStreamSkipAt(runResults: List<RunResultForTestcase>, index: Int): Stream<RunResultForTestcase> {
    return IntStream.range(0, runResults.size)
        .filter { i -> i != index }
        .mapToObj(runResults::get)
}
