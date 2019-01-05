package edu.nwpu.machunyan.theoreticalEvaluation.runningDatas

import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class StatementMapTest : FreeSpec({

    "ofLineBasedStatementMap 应该能得到正确结果" {

        val result = StatementMap.ofLineBasedStatementMap(3, "path...")

        result shouldBe
            StatementMap(StatementMapType.LINE_BASED, arrayListOf(
                null,
                StatementInfo(1, "path...", 1, 1),
                StatementInfo(2, "path...", 2, 2),
                StatementInfo(3, "path...", 3, 3)
            ))
    }

    "getStatementCount 应该能得到正确的结果" {

        val map = StatementMap.ofLineBasedStatementMap(3, "path...")
        map.statementCount shouldBe 3
    }
})
