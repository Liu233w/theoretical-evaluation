package edu.nwpu.soft.ma.theoreticalEvaluation.runner

import edu.nwpu.soft.ma.theoreticalEvaluation.runningDatas.Coverage
import edu.nwpu.soft.ma.theoreticalEvaluation.utils.getTestFilePath
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class GcovParserTest : FreeSpec({

    "basic test - 自己写的测试代码" {
        assertParserOutput(
                "basicGcovFile.gcov",
                6 to 1,
                10 to 1,
                12 to 1,
                14 to 1,
                15 to 1,
                16 to 1,
                21 to 1,
                23 to 1,
                24 to 3
        )
    }

    "complex test - 在 gcov 官网上找到的 gcov 文件" - {

        "不带任何参数的输入" {
            assertComplexFile("complexGcovFile.gcov")
        }

        "! 暂时不支持的输入" - {

            """
            下列三个都不支持。在使用 GcovParser 时需要传入不带任何参数的 gcov 生成的文件
            下面三个测试不运行
            """.trimIndent()

            "每个模板分别计算" {
                assertComplexFile("demangledGcovFile.gcov")
            }

            "每个模板, block 分别计算" {
                assertComplexFile("blockGcovFile.gcov")
            }

            "每个模板, block, branch 分别计算" {
                assertComplexFile("branchGcovFile.gcov")
            }
        }
    }
})

// 官网上的三个文件的评测结果应该都是一样的，放到这里避免编写重复代码
private fun assertComplexFile(fileName: String) {
    assertParserOutput(
            fileName,
            7 to 1,
            8 to 2,
            18 to 1,
            21 to 1,
            23 to 1,
            24 to 1,
            25 to 1,
            27 to 11,
            28 to 10,
            30 to 1,
            32 to 1,
            35 to 1,
            36 to 1
    )
}

private fun assertParserOutput(fileName: String, vararg shouldResultInMap: Pair<Int, Int>) {

    val coverage = GcovParser.generateCoverageFromFile(getTestFilePath(fileName))
    coverage.shouldBe(Coverage(hashMapOf(*shouldResultInMap)))
}
