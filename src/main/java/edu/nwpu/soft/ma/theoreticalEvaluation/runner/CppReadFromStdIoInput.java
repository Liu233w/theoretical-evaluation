package edu.nwpu.soft.ma.theoreticalEvaluation.runner;

import edu.nwpu.soft.ma.theoreticalEvaluation.runningDatas.ProgramInput;
import lombok.Data;

/**
 * 从命令行读入，检测输出是否正确
 */
@Data
public class CppReadFromStdIoInput implements ProgramInput {

    private final String[] input;

    private final String shouldOutputFromStdOut;

    public CppReadFromStdIoInput(String[] input, String shouldOutputFromStdOut) {
        this.input = input;
        this.shouldOutputFromStdOut = shouldOutputFromStdOut;
    }

    @Override
    public String getInputDescription() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("input: ");
        for (String str :
                input) {
            stringBuilder.append(str);
            stringBuilder.append(", ");
        }
        return stringBuilder.toString();
    }
}
