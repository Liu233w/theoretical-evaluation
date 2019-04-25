package edu.nwpu.machunyan.theoreticalEvaluation.analyze

import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class TestcaseWeightResolverTest : FreeSpec({

    "resolveTestcaseWeight" - {
        "能通过论文上的测试" {

            val testcases = buildRunResultsFromMatrix(arrayOf(
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

            val result = TestcaseWeightResolver
                .builder()
                .formulaTitle("op")
                .sfFormula(SuspiciousnessFactorFormulas::op)
                .build()
                .resolve(testcases)

            result.testcaseWeights[1 - 1].testcaseWeight shouldBe 1.0
            result.testcaseWeights[2 - 1].testcaseWeight shouldBe 1.0
            result.testcaseWeights[3 - 1].testcaseWeight shouldBe (1.09 plusOrMinus 0.000001)
            result.testcaseWeights[4 - 1].testcaseWeight shouldBe (1.09 plusOrMinus 0.000001)
            result.testcaseWeights[5 - 1].testcaseWeight shouldBe 1.0
            result.testcaseWeights[6 - 1].testcaseWeight shouldBe (1.09 plusOrMinus 0.000001)
            result.testcaseWeights[7 - 1].testcaseWeight shouldBe 1.0
            result.testcaseWeights[8 - 1].testcaseWeight shouldBe 1.0
            result.testcaseWeights[9 - 1].testcaseWeight shouldBe (1.73 plusOrMinus 0.000001)
        }

        "能通过另一篇论文上的测试" - {
//            val testcases = buildRunResultsFromMatrix(
//                    arrayOf(
//                            arrayOf(1, 0, 1, 0),
//                            arrayOf(1, 1, 1, 1),
//                            arrayOf(1, 0, 1, 0)
//                    ),
//                    arrayOf(0, 0, 1, 1)
//            )
//
//            val result = TestcaseWeightResolver.resolveTestcaseWeight(testcases)
//
//            println(result)
//
//            val vtm = VectorTableModelGenerator.generateFromRunResult(testcases)
//            val suspiciousnessFactorMatrixOrdered = SuspiciousnessFactorResolver.getSuspiciousnessFactorMatrixOrdered(vtm) {
//                it.calculateSuspiciousnessFactorAsOp()
//            }

//            val vtmWeighted = VectorTableModelGenerator.generateFromRunResultWithWeight(testcases, result)
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
