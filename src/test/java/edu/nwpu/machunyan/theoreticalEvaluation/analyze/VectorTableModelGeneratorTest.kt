package edu.nwpu.machunyan.theoreticalEvaluation.analyze

import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.*
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class VectorTableModelGeneratorTest : FreeSpec({

    "generateVectorTableModelFromRunResult" - {

        "能得到正确结果" {
            val input = buildRunResultsFromMatrix(arrayOf(
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

            val vectorTableModel = VectorTableModelGenerator.generateVectorTableModelFromRunResult(input)

            vectorTableModel shouldBe buildVectorModelTableFromMatrix(
                    arrayOf(0, 0, 3, 6),
                    arrayOf(1, 0, 2, 6),
                    arrayOf(0, 4, 3, 2),
                    arrayOf(0, 0, 3, 6),
                    arrayOf(0, 3, 3, 3),
                    arrayOf(0, 3, 3, 3),
                    arrayOf(0, 0, 3, 6),
                    arrayOf(2, 0, 1, 6),
                    arrayOf(0, 0, 3, 6),
                    arrayOf(0, 3, 3, 3)
            )
        }

    }
})

val mockProgramInput = IProgramInput { "mock ProgramInput" }
val mockProgram = Program("mock program", "...")
