package edu.nwpu.machunyan.theoreticalEvaluation.runner;

import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.IProgramInput;
import lombok.Value;

/**
 * 从命令行读入，检测输出是否正确
 */
@Value
public class GccReadFromStdIoInput implements IProgramInput {

    private final String[] inputFromCommand;

    private final String inputFromStdIn;

    private final String shouldOutputFromStdOut;

    public GccReadFromStdIoInput(String[] inputFromCommand, String shouldOutputFromStdOut) {
        this(inputFromCommand, "", shouldOutputFromStdOut);
    }

    public GccReadFromStdIoInput(String[] inputFromCommand, String inputFromStdIn, String shouldOutputFromStdOut) {
        this.inputFromCommand = inputFromCommand;
        this.inputFromStdIn = inputFromStdIn;
        this.shouldOutputFromStdOut = shouldOutputFromStdOut;
    }

    @Override
    public String getInputDescription() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("inputFromCommand: ");
        for (String str :
            inputFromCommand) {
            stringBuilder.append(str);
            stringBuilder.append(", ");
        }
        stringBuilder.append(";");
        stringBuilder.append("input: ");
        stringBuilder.append(inputFromStdIn, 0, 10);
        return stringBuilder.toString();
    }
}
