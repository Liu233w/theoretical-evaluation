package edu.nwpu.machunyan.theoreticalEvaluation.runner.data

import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class CoverageTest : FreeSpec({

    "getAllCoverageInformation" - {

        "应该能正确输出结果" {
            Coverage(hashMapOf(1 to 2, 4 to 1))
                .getAllCoverageInformation(5)
                .shouldBe(arrayListOf(0, 2, 0, 0, 1, 0))
        }

    }
})
