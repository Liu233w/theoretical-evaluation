package edu.nwpu.machunyan.theoreticalEvaluation.runner

import edu.nwpu.machunyan.theoreticalEvaluation.getTestFilePath
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.GccReadFromStdIoInput
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.GccReadFromStdIoRunner
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class RunningResultResolverTest : FreeSpec({

    "runProgramForAllVersions" - {

        "能输出正确结果" {

            val programs = listOf(
                Program("1.cpp", getTestFilePath("1.cpp").toString())
            )
            val inputs = listOf(
                GccReadFromStdIoInput(arrayOf("1"), "input 1\n"),
                GccReadFromStdIoInput(arrayOf("2"), "else\n"),
                GccReadFromStdIoInput(arrayOf("2"), "wrong\n")
            )

            val result = RunningResultResolver.runProgramForAllVersions(programs, inputs, ::GccReadFromStdIoRunner)

            result.runResultForPrograms.size shouldBe 1

            val programRunResult = result.runResultForPrograms[0]
            programRunResult.programTitle shouldBe "1.cpp"
            programRunResult.statementMap.statementCount shouldBe 24

            val results = programRunResult.runResults
            results[0].isCorrect shouldBe true
            results[1].isCorrect shouldBe true
            results[2].isCorrect shouldBe false
            for (i in 0..1) {
                results[i].statementMap shouldBe null
                results[i].coverage.getCoverageForStatement(14) shouldBe 1
            }
        }
    }
})
