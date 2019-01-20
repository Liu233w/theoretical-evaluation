package edu.nwpu.machunyan.theoreticalEvaluation.analyze

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class SuspiciousnessFactorHelperTest : FreeSpec({

    "collectAsMultipleFormula" - {

        "能得到正确结果" {

            val input = SuspiciousnessFactorJam(listOf(
                SuspiciousnessFactorForProgram(
                    "v1",
                    "f1",
                    listOf(
                        SuspiciousnessFactorForStatement(1, 0.1),
                        SuspiciousnessFactorForStatement(2, 0.2)
                    )
                ),
                SuspiciousnessFactorForProgram(
                    "v1",
                    "f2",
                    listOf(
                        SuspiciousnessFactorForStatement(1, 1.1),
                        SuspiciousnessFactorForStatement(2, 1.2)
                    )
                ),
                SuspiciousnessFactorForProgram(
                    "v2",
                    "f1",
                    listOf(
                        SuspiciousnessFactorForStatement(1, 2.1),
                        SuspiciousnessFactorForStatement(2, 2.2)
                    )
                ),
                SuspiciousnessFactorForProgram(
                    "v2",
                    "f2",
                    listOf(
                        SuspiciousnessFactorForStatement(1, 3.1),
                        SuspiciousnessFactorForStatement(2, 3.2)
                    )
                )
            ))

            val result = SuspiciousnessFactorHelper.collectAsMultipleFormula(input)

            result shouldBe MultipleFormulaSuspiciousnessFactorJam(listOf(
                MultipleFormulaSuspiciousnessFactorForProgram(
                    "v1",
                    listOf(
                        MultipleFormulaSuspiciousnessFactorForStatement(
                            1,
                            mapOf(
                                "f1" to 0.1,
                                "f2" to 1.1
                            )
                        ),
                        MultipleFormulaSuspiciousnessFactorForStatement(
                            2,
                            mapOf(
                                "f1" to 0.2,
                                "f2" to 1.2
                            )
                        )
                    )
                ),
                MultipleFormulaSuspiciousnessFactorForProgram(
                    "v2",
                    listOf(
                        MultipleFormulaSuspiciousnessFactorForStatement(
                            1,
                            mapOf(
                                "f1" to 2.1,
                                "f2" to 3.1
                            )
                        ),
                        MultipleFormulaSuspiciousnessFactorForStatement(
                            2,
                            mapOf(
                                "f1" to 2.2,
                                "f2" to 3.2
                            )
                        )
                    )
                )
            ), setOf("f1", "f2"))
        }

        "对于排序的输入也可以得到正确结果" {

            val input = SuspiciousnessFactorJam(listOf(
                SuspiciousnessFactorForProgram(
                    "v1",
                    "f1",
                    listOf(
                        SuspiciousnessFactorForStatement(2, 0.2),
                        SuspiciousnessFactorForStatement(1, 0.1)
                    )
                ),
                SuspiciousnessFactorForProgram(
                    "v1",
                    "f2",
                    listOf(
                        SuspiciousnessFactorForStatement(1, 1.1),
                        SuspiciousnessFactorForStatement(2, 1.2)
                    )
                ),
                SuspiciousnessFactorForProgram(
                    "v2",
                    "f1",
                    listOf(
                        SuspiciousnessFactorForStatement(1, 2.1),
                        SuspiciousnessFactorForStatement(2, 2.2)
                    )
                ),
                SuspiciousnessFactorForProgram(
                    "v2",
                    "f2",
                    listOf(
                        SuspiciousnessFactorForStatement(2, 3.2),
                        SuspiciousnessFactorForStatement(1, 3.1)
                    )
                )
            ))

            val result = SuspiciousnessFactorHelper.collectAsMultipleFormula(input)

            result shouldBe MultipleFormulaSuspiciousnessFactorJam(listOf(
                MultipleFormulaSuspiciousnessFactorForProgram(
                    "v1",
                    listOf(
                        MultipleFormulaSuspiciousnessFactorForStatement(
                            1,
                            mapOf(
                                "f1" to 0.1,
                                "f2" to 1.1
                            )
                        ),
                        MultipleFormulaSuspiciousnessFactorForStatement(
                            2,
                            mapOf(
                                "f1" to 0.2,
                                "f2" to 1.2
                            )
                        )
                    )
                ),
                MultipleFormulaSuspiciousnessFactorForProgram(
                    "v2",
                    listOf(
                        MultipleFormulaSuspiciousnessFactorForStatement(
                            1,
                            mapOf(
                                "f1" to 2.1,
                                "f2" to 3.1
                            )
                        ),
                        MultipleFormulaSuspiciousnessFactorForStatement(
                            2,
                            mapOf(
                                "f1" to 2.2,
                                "f2" to 3.2
                            )
                        )
                    )
                )
            ), setOf("f1", "f2"))
        }
    }

    "diff" - {

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

            SuspiciousnessFactorHelper.diff(left, right) shouldBe
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
