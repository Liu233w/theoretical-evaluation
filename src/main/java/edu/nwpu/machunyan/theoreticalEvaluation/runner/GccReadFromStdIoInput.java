package edu.nwpu.machunyan.theoreticalEvaluation.runner;

import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.IProgramInput;
import lombok.Data;

/**
 * 从命令行读入，检测输出是否正确
 */
@Data
public class GccReadFromStdIoInput implements IProgramInput {

    private final String[] input;

    private final String shouldOutputFromStdOut;

    public GccReadFromStdIoInput(String[] input, String shouldOutputFromStdOut) {
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
