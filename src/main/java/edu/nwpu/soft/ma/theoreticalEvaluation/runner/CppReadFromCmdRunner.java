package edu.nwpu.soft.ma.theoreticalEvaluation.runner;

import edu.nwpu.soft.ma.theoreticalEvaluation.dataClass.Program;
import edu.nwpu.soft.ma.theoreticalEvaluation.dataClass.ProgramInput;
import edu.nwpu.soft.ma.theoreticalEvaluation.dataClass.SingleRunResult;

import static edu.nwpu.soft.ma.theoreticalEvaluation.utils.ParameterUtils.checkNull;

public class CppReadFromCmdRunner implements CoverageRunner {

    private Program program = null;

    @Override
    public void prepare(Program program) {
        checkNull(program);
        this.program = program;
        // TODO: 编译程序
    }

    @Override
    public SingleRunResult runWithInput(ProgramInput programInput) {
        // TODO
        return null;
    }

    @Override
    public void cleanUp() {
        // 不需要清理
    }
}
