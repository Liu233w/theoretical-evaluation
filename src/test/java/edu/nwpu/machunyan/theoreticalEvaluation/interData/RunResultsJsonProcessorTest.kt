package edu.nwpu.machunyan.theoreticalEvaluation.interData

import edu.nwpu.machunyan.theoreticalEvaluation.runner.GccReadFromStdIoInput
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.Coverage
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.Program
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.RunResultFromRunner
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.StatementMap
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class RunResultsJsonProcessorTest : FreeSpec({

    "能把输出的结果读回来" {

        val program = Program("test program", "test path")
        val statementMap = StatementMap.ofLineBasedStatementMap(10, "test path for statementMap")
        val input = arrayListOf<RunResultFromRunner>(
                RunResultFromRunner(
                        program,
                        GccReadFromStdIoInput(arrayOf("1", "2"), "1,2"),
                        true,
                        Coverage(hashMapOf(1 to 1, 2 to 1)),
                        statementMap
                ),
                RunResultFromRunner(
                        program,
                        GccReadFromStdIoInput(arrayOf("3", "4"), "3,4"),
                        false,
                        Coverage(hashMapOf(3 to 2, 4 to 2)),
                        statementMap
                ),
                RunResultFromRunner(
                        program,
                        GccReadFromStdIoInput(arrayOf("5", "6"), "5,6"),
                        false,
                        Coverage(hashMapOf(5 to 3, 6 to 3)),
                        statementMap
                )
        )

        val jsonObject = RunResultsJsonProcessor.bumpToJson(input, GccReadFromStdIoInput::class.java)
        val loadFromJson = RunResultsJsonProcessor.loadFromJson(jsonObject, GccReadFromStdIoInput::class.java)

        loadFromJson shouldBe input
    }
})