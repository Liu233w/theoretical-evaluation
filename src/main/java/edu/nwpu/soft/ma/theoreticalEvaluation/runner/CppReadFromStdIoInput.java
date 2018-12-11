package edu.nwpu.soft.ma.theoreticalEvaluation.runner;

import edu.nwpu.soft.ma.theoreticalEvaluation.dataClass.ProgramInput;

/**
 * 从命令行读入，检测输出是否正确
 */
public class CppReadFromStdIoInput implements ProgramInput {

    private String[] input;

    private String shouldOutputFromStdOut;

    public CppReadFromStdIoInput(String[] input, String shouldOutputFromStdOut) {
        this.input = input;
        this.shouldOutputFromStdOut = shouldOutputFromStdOut;
    }

    public String[] getInput() {
        return input;
    }

    public String getShouldOutputFromStdOut() {
        return shouldOutputFromStdOut;
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
