package edu.nwpu.machunyan.theoreticalEvaluation.analyze

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankForSide
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankForStatement
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.SuspiciousnessFactorForStatement
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class DiffRankResolverTest : FreeSpec({

    "resolve" - {

        "能得到正确结果" {

            val left = listOf(
                SuspiciousnessFactorForStatement(1, 9.0),
                SuspiciousnessFactorForStatement(2, 8.0),
                SuspiciousnessFactorForStatement(3, 7.0),
                SuspiciousnessFactorForStatement(4, 6.0)
            )
            val right = listOf(
                SuspiciousnessFactorForStatement(1, 7.5),
                SuspiciousnessFactorForStatement(2, 8.0),
                SuspiciousnessFactorForStatement(3, 7.0),
                SuspiciousnessFactorForStatement(4, 6.0)
            )

            DiffRankResolver.resolve(left, right, DiffRankFilters.rankNotEqualForStatements()) shouldBe
                listOf(
                    DiffRankForStatement(1,
                        DiffRankForSide(1, 9.0),
                        DiffRankForSide(2, 7.5)),
                    DiffRankForStatement(2,
                        DiffRankForSide(2, 8.0),
                        DiffRankForSide(1, 8.0))
                )
        }
    }
})
