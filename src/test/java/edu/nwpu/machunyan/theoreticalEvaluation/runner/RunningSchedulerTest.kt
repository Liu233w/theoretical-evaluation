package edu.nwpu.machunyan.theoreticalEvaluation.runner

import edu.nwpu.machunyan.theoreticalEvaluation.getTestFilePath
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Program
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.RunResultFromRunner
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.GccReadFromStdIoInput
import edu.nwpu.machunyan.theoreticalEvaluation.runner.impl.GccReadFromStdIoRunner
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrowExactly
import io.kotlintest.specs.FreeSpec
import java.util.function.Supplier

class RunningSchedulerTest : FreeSpec({

    "runAndGetResults" - {

        "能够得到正确结果" {

            val results = RunningScheduler
                .builder().build()
                .runAndGetResults(
                    ::GccReadFromStdIoRunner,
                    Program("1.cpp", getTestFilePath("1.cpp").toString()),
                    listOf(
                        GccReadFromStdIoInput(arrayOf("1"), "input 1\n"),
                        GccReadFromStdIoInput(arrayOf("2"), "else\n"),
                        GccReadFromStdIoInput(arrayOf("2"), "wrong\n")
                    )
                )

            results[0].isCorrect shouldBe true
            results[1].isCorrect shouldBe true
            results[2].isCorrect shouldBe false

            for (i in 0..1) {
                results[i].program.title shouldBe "1.cpp"
                results[i].statementMap.statementCount shouldBe 24
                results[i].coverage.getCoverageForStatement(14) shouldBe 1
            }
        }

        "能够正确地重试" {

            class MockSupplier() : Supplier<ICoverageRunner> {
                var times = 0
                override fun get(): ICoverageRunner {
                    return object : ICoverageRunner {
                        override fun prepare(program: Program?) {
                            if (times < 2) {
                                ++times
                                throw CoverageRunnerException()
                            }
                        }

                        override fun runWithInput(programInput: IProgramInput?): RunResultFromRunner? {
                            return null
                        }

                        override fun cleanUp() {
                            //nothing
                        }
                    }
                }
            }

            val mockProgram = Program("", "")
            val mockInput = listOf<IProgramInput>()

            shouldThrowExactly<CoverageRunnerException> {
                RunningScheduler
                    .builder()
                    .retry(1)
                    .build()
                    .runAndGetResults(MockSupplier(), mockProgram, mockInput)
            }

            val results = RunningScheduler
                .builder()
                .retry(2)
                .build()
                .runAndGetResults(MockSupplier(), mockProgram, mockInput)
            results shouldBe arrayListOf()
        }
    }
})
