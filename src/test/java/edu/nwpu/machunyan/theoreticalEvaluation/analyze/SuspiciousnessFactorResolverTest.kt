package edu.nwpu.machunyan.theoreticalEvaluation.analyze

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorForProgram
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorForStatement
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModel.Pojo.VectorTableModelForProgram
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class SuspiciousnessFactorResolverTest : FreeSpec({

    "resolve" - {

        "能通过论文上的数据 1" {
            val vtm = VectorTableModelForProgram("program", arrayListOf(
                null,
                VectorTableModel.Pojo.ForStatement(1, 1, 1, 1, 1),
                VectorTableModel.Pojo.ForStatement(2, 0, 0, 2, 2),
                VectorTableModel.Pojo.ForStatement(3, 1, 1, 1, 1)
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
