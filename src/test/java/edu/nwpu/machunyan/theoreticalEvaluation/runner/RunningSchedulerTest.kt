package edu.nwpu.machunyan.theoreticalEvaluation.runner

import edu.nwpu.machunyan.theoreticalEvaluation.getTestFilePath
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.Program
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class RunningSchedulerTest : FreeSpec({

    "runAndGetResults - 能够得到正确结果" {

        val results = RunningScheduler(
                Program("1.cpp", getTestFilePath("1.cpp").toString()),
                GccReadFromStdIoRunner::newInstance,
                listOf(
                        GccReadFromStdIoInput(arrayOf("1"), "input 1\n"),
                        GccReadFromStdIoInput(arrayOf("2"), "else\n"),
                        GccReadFromStdIoInput(arrayOf("2"), "wrong\n")
                ))
                .runAndGetResults()

        results[0].isCorrect shouldBe true
        results[1].isCorrect shouldBe true
        results[2].isCorrect shouldBe false

        for (i in 0..1) {
            results[i].program.title shouldBe "1.cpp"
            results[i].statementMap.statementCount shouldBe 24
            results[i].coverage.getCoverageForStatement(14) shouldBe 1
        }
    }
})