package edu.nwpu.machunyan.theoreticalEvaluation.runner

import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.*
import edu.nwpu.machunyan.theoreticalEvaluation.getTestFilePath
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class CppReadFromStdIoRunnerTest : FreeSpec({

    "test execution - 执行此测试需要计算机安装有 g++ 和 gcov" - {

        "输入 1" {
            // arrange
            val runner = CppReadFromStdIoRunner()
            val testFilePath = getTestFilePath("1.cpp").toString()

            // act
            runner.prepare(Program("test cpp: 1.cpp", testFilePath))

            val singleRunResult = runner.runWithInput(
                    CppReadFromStdIoInput(
                            arrayOf("1"),
                            "input 1\n"))

            runner.cleanUp()

            // assert
            singleRunResult shouldBe SingleRunResult(
                    Program("test cpp: 1.cpp", testFilePath),
                    CppReadFromStdIoInput(arrayOf("1"), "input 1\n"),
                    true,
                    Coverage(hashMapOf(
                            6 to 1,
                            10 to 1,
                            12 to 1,
                            14 to 1,
                            15 to 1,
                            16 to 1,
                            21 to 1,
                            23 to 1,
                            24 to 3
                    )),
                    StatementMap(StatementMapType.LINE_BASED).apply {
                        mapList.add(null)
                        for (i in 1..24) {
                            mapList.add(StatementInfo(i, testFilePath, i, i))
                        }
                    }
            )
        }

        "输入 2" {
            // arrange
            val runner = CppReadFromStdIoRunner()
            val testFilePath = getTestFilePath("1.cpp").toString()

            // act
            runner.prepare(Program("test cpp: 1.cpp", testFilePath))

            val singleRunResult = runner.runWithInput(
                    CppReadFromStdIoInput(
                            arrayOf("2"),
                            "else\n"))

            runner.cleanUp()

            // assert
            singleRunResult shouldBe SingleRunResult(
                    Program("test cpp: 1.cpp", testFilePath),
                    CppReadFromStdIoInput(arrayOf("2"), "else\n"),
                    true,
                    Coverage(hashMapOf(
                            6 to 1,
                            10 to 1,
                            12 to 1,
                            14 to 1,
                            15 to 1,
                            18 to 1,
                            21 to 1,
                            23 to 1,
                            24 to 3
                    )),
                    StatementMap(StatementMapType.LINE_BASED).apply {
                        mapList.add(null)
                        for (i in 1..24) {
                            mapList.add(StatementInfo(i, testFilePath, i, i))
                        }
                    }
            )
        }
    }

    "runWithInput" - {

        "在多次运行时，后一次运行不会受前一次运行的影响" {

            val runner = CppReadFromStdIoRunner()
            runner.prepare(Program("1.cpp", getTestFilePath("1.cpp").toString()))

            val input = CppReadFromStdIoInput(arrayOf("1"), "input 1\n")

            // 第一次运行
            runner.runWithInput(input).coverage
                    .getCoverageForStatement(14) shouldBe 1

            // 第二次运行
            runner.runWithInput(input).coverage
                    .getCoverageForStatement(14) shouldBe 1
        }

    }

    "decideCompilerFromFileExtension - 能够正确得到指定的编译器名称" - {

        "cpp" {
            CppReadFromStdIoRunner.decideCompilerFromFileExtension("a.cpp")
                    .shouldBe("g++")
        }

        "cc" {
            CppReadFromStdIoRunner.decideCompilerFromFileExtension("a.cc")
                    .shouldBe("g++")
        }

        "c" {
            CppReadFromStdIoRunner.decideCompilerFromFileExtension("a.c")
                    .shouldBe("gcc")
        }
    }
})