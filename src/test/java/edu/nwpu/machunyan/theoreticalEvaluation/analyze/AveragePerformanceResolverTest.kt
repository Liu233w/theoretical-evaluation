package edu.nwpu.machunyan.theoreticalEvaluation.analyze

import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class AveragePerformanceResolverTest : FreeSpec({

    val vectorTableModel = buildVectorModelTableFromMatrix(
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
    val orderedVectorTableModel = OrderedVectorTableModel.fromVectorTableModel(vectorTableModel)

    "resolvePartition" - {
        "能通过论文中的测试数据" {
            val partitions = AveragePerformanceResolver.resolvePartition(orderedVectorTableModel)
            partitions shouldBe arrayListOf(3)
        }

        "在 II-set1 只有一个分区（且所有元素都是分区元素）时，能够得到正确结果" {
            val input = OrderedVectorTableModel.fromVectorTableModel(buildVectorModelTableFromMatrix(
                    arrayOf(0, 0, 3, 6),
                    arrayOf(0, 0, 3, 6),
                    arrayOf(0, 0, 3, 6),
                    arrayOf(0, 0, 3, 6),
                    arrayOf(0, 3, 3, 3), // II-set1 begin
                    arrayOf(0, 3, 3, 3),
                    arrayOf(0, 3, 3, 3), // II-set1 end
                    arrayOf(1, 0, 2, 6),
                    arrayOf(2, 0, 1, 6)
            ))

            AveragePerformanceResolver.resolvePartition(input) shouldBe arrayListOf(3)
        }
    }

    "resolveAveragePerformance" - {
        "能通过论文中的测试数据" - {
            //  II −set1 = s5, s6, s10, s3, x = 4, z = 4, k1 = 3, k2 = 3, k3 = 3, and k4 = 4 for PG1 in Fig. 1,
            //  so y = 1, ∆1 = 3, and the average performance of O for PG1 is ((4+4)2−4+3)/2×10×8 = 63/160.

            val result = AveragePerformanceResolver.resolveAveragePerformance(orderedVectorTableModel)
            result shouldBe (63.0 / 160 plusOrMinus 0.0000001)
        }
    }
})