package edu.nwpu.machunyan.theoreticalEvaluation.analyze

import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

/*
class TestCaseWeightResolverTest : FreeSpec({

    "resolveTestCaseWeight" - {
        "能通过论文上的测试" {

            val testCases = buildRunResultsFromMatrix(arrayOf(
                    arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
                    arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 0),
                    arrayOf(1, 1, 0, 0, 0, 0, 1, 1, 1),
                    arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
                    arrayOf(0, 0, 1, 0, 1, 1, 1, 1, 1),
                    arrayOf(0, 0, 1, 0, 1, 1, 1, 1, 1),
                    arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
                    arrayOf(1, 1, 1, 1, 1, 1, 0, 0, 1),
                    arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
                    arrayOf(0, 0, 1, 1, 0, 1, 1, 1, 1)
            ), arrayOf(1, 1, 1, 1, 1, 1, 0, 0, 0))

            val result = TestCaseWeightResolver.resolveTestCaseWeight(testCases)

            result[1 - 1] shouldBe 1.0
            result[2 - 1] shouldBe 1.0
            result[3 - 1] shouldBe (1.09 plusOrMinus 0.000001)
            result[4 - 1] shouldBe (1.09 plusOrMinus 0.000001)
            result[5 - 1] shouldBe 1.0
            result[6 - 1] shouldBe (1.09 plusOrMinus 0.000001)
            result[7 - 1] shouldBe 1.0
            result[8 - 1] shouldBe 1.0
            result[9 - 1] shouldBe (1.73 plusOrMinus 0.000001)
        }

        "能通过另一篇论文上的测试" - {
            val testCases = buildRunResultsFromMatrix(
                    arrayOf(
                            arrayOf(1, 0, 1, 0),
                            arrayOf(1, 1, 1, 1),
                            arrayOf(1, 0, 1, 0)
                    ),
                    arrayOf(0, 0, 1, 1)
            )

            val result = TestCaseWeightResolver.resolveTestCaseWeight(testCases)

            println(result)

            val vtm = VectorTableModelGenerator.generateFromRunResult(testCases)
            val suspiciousnessFactorMatrixOrdered = SuspiciousnessFactorResolver.getSuspiciousnessFactorMatrixOrdered(vtm) {
                it.calculateSuspiciousnessFactorAsOp()
            }

//            val vtmWeighted = VectorTableModelGenerator.generateFromRunResultWithWeight(testCases, result)
//            val suspiciousnessFactorMatrixOrderedWithWeight = SuspiciousnessFactorResolver.getSuspiciousnessFactorMatrixOrdered(vtmWeighted) {
//                it.calculateSuspiciousnessFactorAsOp()
//            }
//
//            println("before weight")
//            println(suspiciousnessFactorMatrixOrdered)
//            println("after weight")
//            println(suspiciousnessFactorMatrixOrderedWithWeight)
        }
    }
})
*/