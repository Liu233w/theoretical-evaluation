package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.ProgramDefination;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForTestcase;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 将所有运行结果中运行结果为 0 的数据删除。
 * 因为是用 map 储存的，entry不存在的语句即为 0，不需要额外指出。
 * 这样可以节省结果的存储空间。
 */
public class ZipRunResults {

    private static final String baseDir = "./target/outputs/run-results/";

    public static void main(String[] args) throws IOException {

        for (ProgramDefination.ProgramDir dir : ProgramDefination.GCC_RUN_LIST) {
            process(dir.getProgramDir());
        }

        for (String name : ProgramDefination.DEFECTS4J_RUN_LIST) {
            process(name);
        }
    }

    private static void process(String programName) throws IOException {

        final Path path = Paths.get(baseDir).resolve(programName + ".json");
        if (!Files.exists(path)) {
            return;
        }

        final RunResultJam jam = FileUtils.loadObject(path, RunResultJam.class);

        for (RunResultForProgram runResultForProgram : jam.getRunResultForPrograms()) {
            for (RunResultForTestcase runResultForTestcase : runResultForProgram.getRunResults()) {
                runResultForTestcase.getCoverage().zipData();
            }
        }

        FileUtils.saveObject(path, jam);
    }
}
