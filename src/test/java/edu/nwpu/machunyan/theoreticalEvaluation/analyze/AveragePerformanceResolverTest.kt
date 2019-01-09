package edu.nwpu.machunyan.theoreticalEvaluation.analyze

import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class AveragePerformanceResolverTest : FreeSpec({

    "resolve" - {

        "能通过论文中的测试数据" {

            val vtmList = buildVectorModelTableFromMatrix(
                //     nf np ef ep      sf
                arrayOf(0, 0, 3, 6), // 0
                arrayOf(0, 0, 3, 6), // 0
                arrayOf(0, 0, 3, 6), // 0
                arrayOf(0, 0, 3, 6), // 0
                arrayOf(0, 3, 3, 3), // 3
                arrayOf(0, 3, 3, 3), // 3
                arrayOf(0, 3, 3, 3), // 3
                arrayOf(0, 4, 3, 2), // 4
                // ------ 排除 -----
                arrayOf(1, 0, 2, 6), // -1
                arrayOf(2, 0, 1, 6)  // -1
            )

            // sf: 0, 0, 0, 0, 3, 3, 3, 4, (-1, -1)
            // ordered sf: 4, 3, 3, 3, 0, 0, 0, 0, (-1, -1)
            // pm: 0, (1+(2/2))/10, 0.2, 0.2, (4+(3/2))/10, 0.55, 0.55, 0.55, (...,...)
            // average pm: (0.2*3+0.55*4)/8 = (0.6+2.2)/8 = 0.35

            val result = AveragePerformanceResolver.resolve(vtmList, SuspiciousnessFactorFormulas::o)
            result shouldBe (63.0 / 160 plusOrMinus 0.0000001)
        }
    }
})
