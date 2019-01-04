package edu.nwpu.machunyan.theoreticalEvaluation.analyze

import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class SuspiciousnessFactorResolverTest : FreeSpec({

    "getSuspiciousnessFactorMatrixOrdered" - {

        "能通过论文上的数据 1" {
            val vtm = arrayListOf(
                    null,
                    VectorTableModelRecord(1, 1, 1, 1, 1),
                    VectorTableModelRecord(2, 0, 0, 2, 2),
                    VectorTableModelRecord(3, 1, 1, 1, 1)
            )
            val result = SuspiciousnessFactorResolver.getSuspiciousnessFactorMatrixOrdered(
                    vtm
            ) { it -> it.calculateSuspiciousnessFactorAsO() }
            result shouldBe arrayListOf(
                    SuspiciousnessFactorRecord(1, 0.5),
                    SuspiciousnessFactorRecord(2, 0.5),
                    SuspiciousnessFactorRecord(3, 0.5)
            )
        }
    }
})
