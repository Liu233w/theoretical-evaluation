package edu.nwpu.machunyan.theoreticalEvaluation.analyze

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorForProgram
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorForStatement
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModel
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModelForStatement
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class SuspiciousnessFactorResolverTest : FreeSpec({

    "resolve" - {

        "能通过论文上的数据 1" {
            val vtm = VectorTableModel("program", arrayListOf(
                null,
                VectorTableModelForStatement(1, 1, 1, 1, 1),
                VectorTableModelForStatement(2, 0, 0, 2, 2),
                VectorTableModelForStatement(3, 1, 1, 1, 1)
            ))

            val result = SuspiciousnessFactorResolver(SuspiciousnessFactorFormulas::op).resolve(vtm)
            result shouldBe SuspiciousnessFactorForProgram("program", "", arrayListOf(
                SuspiciousnessFactorForStatement(1, 0.6666666666666667),
                SuspiciousnessFactorForStatement(2, 1.3333333333333335),
                SuspiciousnessFactorForStatement(3, 0.6666666666666667)
            ))
        }
    }
})
