package edu.nwpu.machunyan.theoreticalEvaluation.analyze

import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.ProgramRunResult
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultItem
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.Coverage
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.StatementMap
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import java.util.stream.Collectors
import java.util.stream.IntStream

/**
 * 从论文中表示的矩阵中生成 RunResults，输入的第一个维度表示行，第二个维度表示列。
 * oc 表示正确与否。
 */
fun buildRunResultsFromMatrix(matrix: Array<Array<Int>>, oc: Array<Int>): ProgramRunResult {

    val mockStatementMap = StatementMap.ofLineBasedStatementMap(matrix.size, "don't need file path")

    val result = IntStream.range(0, matrix[0].size)
        .mapToObj { i -> RunResultItem(oc[i] == 1, Coverage(), null) }
        .collect(Collectors.toList())

    // row: s -- statement; column: t -- singleRun
    matrix.forEachIndexed { row, line ->
        line.forEachIndexed { column, item ->
            result[column].coverage.setCoverageForStatement(row + 1, item)
        }
    }

    return ProgramRunResult("mock program", mockStatementMap, result)
}

/**
 * 从矩阵生成结果。第 0 个是 null。剩下的每一个 VectorTableModelRecord 对应矩阵的一行
 */
fun buildVectorModelTableFromMatrix(vararg records: Array<Int>): ArrayList<VectorTableModelRecord> {
    return ArrayList(records.mapIndexed { index, record ->
        VectorTableModelRecord(index + 1, record[0], record[1], record[2], record[3])
    }).apply { add(0, null) }
}

class AnalyzeUtilsTest : FreeSpec({

    "首先保证测试用的辅助函数是正确的" - {

        "buildVectorModelTableFromMatrix" {
            buildVectorModelTableFromMatrix(
                arrayOf(1, 2, 3, 4),
                arrayOf(5, 6, 7, 8),
                arrayOf(9, 10, 11, 12)
            ) shouldBe arrayListOf(
                null,
                VectorTableModelRecord(1, 1, 2, 3, 4),
                VectorTableModelRecord(2, 5, 6, 7, 8),
                VectorTableModelRecord(3, 9, 10, 11, 12)
            )
        }

        "buildRunResultsFromMatrix" {

            val mockStatementMap = StatementMap.ofLineBasedStatementMap(3, "don't need file path")

            buildRunResultsFromMatrix(arrayOf(
                arrayOf(0, 0, 0),
                arrayOf(1, 1, 1),
                arrayOf(0, 0, 0)
            ), arrayOf(1, 0, 0)) shouldBe ProgramRunResult(
                "mock program",
                mockStatementMap,
                arrayListOf(
                    RunResultItem(true, Coverage(hashMapOf(1 to 0, 2 to 1, 3 to 0)), null),
                    RunResultItem(false, Coverage(hashMapOf(1 to 0, 2 to 1, 3 to 0)), null),
                    RunResultItem(false, Coverage(hashMapOf(1 to 0, 2 to 1, 3 to 0)), null)
                )
            )
        }
    }
})
