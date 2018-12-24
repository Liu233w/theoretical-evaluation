package edu.nwpu.machunyan.theoreticalEvaluation.analyze

import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class OrderedVectorTableModelTest : FreeSpec({

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

    "能够生成正确的排序" {
        val orderedVectorTableModel = OrderedVectorTableModel.fromVectorTableModel(vectorTableModel)

        orderedVectorTableModel.vectorTableModel shouldBe arrayListOf(
                VectorTableModelRecord(1, 0, 0, 3, 6),
                VectorTableModelRecord(4, 0, 0, 3, 6),
                VectorTableModelRecord(7, 0, 0, 3, 6),
                VectorTableModelRecord(9, 0, 0, 3, 6),
                VectorTableModelRecord(5, 0, 3, 3, 3),
                VectorTableModelRecord(6, 0, 3, 3, 3),
                VectorTableModelRecord(10, 0, 3, 3, 3),
                VectorTableModelRecord(3, 0, 4, 3, 2),
                VectorTableModelRecord(2, 1, 0, 2, 6),
                VectorTableModelRecord(8, 2, 0, 1, 6)
        )

    }

    "能够正确划分集合" {
        val orderedVectorTableModel = OrderedVectorTableModel.fromVectorTableModel(vectorTableModel)

        orderedVectorTableModel.iIset1BeginPosition shouldBe 4
        orderedVectorTableModel.iIset2BeginPosition shouldBe 8
    }
})