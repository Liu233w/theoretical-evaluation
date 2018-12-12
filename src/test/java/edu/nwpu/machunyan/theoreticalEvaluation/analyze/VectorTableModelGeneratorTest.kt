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

/**
 * 从论文中表示的矩阵中生成 RunResults，输入的第一个维度表示行，第二个维度表示列。
 * oc 表示正确与否。
 */
private fun buildRunResultsFromMatrix(matrix: Array<Array<Int>>, oc: Array<Int>): ArrayList<SingleRunResult> {

    val mockProgram = Program("mock program", "...")
    val mockStatementMap = StatementMap.ofLineBasedStatementMap(matrix.size, "don't need file path")
    val mockProgramInput = ProgramInput { "mock ProgramInput" }

    val result = ArrayList<SingleRunResult>(matrix[0].size)
    for (i in 0 until matrix[0].size) {
        result.add(SingleRunResult(mockProgram, mockProgramInput, oc[i] == 1, Coverage(), mockStatementMap))
    }

    // row: s -- statement; column: t -- singleRun
    matrix.forEachIndexed { row, line ->
        line.forEachIndexed { column, item ->
            result[column].coverage.setCoverageForStatement(row, item)
        }
    }

    return result
}

private fun buildVectorModelTableFromMatrix(vararg records: Array<Int>): ArrayList<VectorTableModelRecord> {
    return ArrayList(records.mapIndexed { index, record ->
        VectorTableModelRecord(index, record[0], record[1], record[2], record[3])
    }).apply { add(0, null) }
}