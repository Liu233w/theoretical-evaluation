package edu.nwpu.machunyan.theoreticalEvaluation.analyze

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactor
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorItem
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModel
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class SuspiciousnessFactorResolverTest : FreeSpec({

    "resolve" - {

        "能通过论文上的数据 1" {
            val vtm = VectorTableModel("program", arrayListOf(
                null,
                VectorTableModelRecord(1, 1, 1, 1, 1),
                VectorTableModelRecord(2, 0, 0, 2, 2),
                VectorTableModelRecord(3, 1, 1, 1, 1)
            ))

            val result = SuspiciousnessFactorResolver(SuspiciousnessFactorFormulas::op).resolve(vtm)
            result shouldBe SuspiciousnessFactor("program", "", arrayListOf(
                SuspiciousnessFactorItem(1, 0.6666666666666667),
                SuspiciousnessFactorItem(2, 1.3333333333333335),
                SuspiciousnessFactorItem(3, 0.6666666666666667)
            ))
        }
    }
})
