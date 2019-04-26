package edu.nwpu.machunyan.theoreticalEvaluation

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorResolver
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModelResolver
import edu.nwpu.machunyan.theoreticalEvaluation.application.Run
import edu.nwpu.machunyan.theoreticalEvaluation.runner.RunningScheduler
import edu.nwpu.machunyan.theoreticalEvaluation.runner.TestcaseResolver
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.GccReadFromStdIoInput
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.GccReadFromStdIoRunner
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.TestcaseItem
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils

fun main() {

    reRunTcas()
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

fun reRunTcas() {
    val testcase = filterTcasTestcase()
    val scheduler = RunningScheduler(
        Program("tcas-v1", FileUtils.getFilePathFromResources("tcas/versions/v1/tcas.c").toString()),
        GccReadFromStdIoRunner::newInstance,
        testcase.map { GccReadFromStdIoInput(it.params, it.output) },
        null,
        true)
    val results = scheduler.runAndGetResults()
    results.forEach(::println)
}
