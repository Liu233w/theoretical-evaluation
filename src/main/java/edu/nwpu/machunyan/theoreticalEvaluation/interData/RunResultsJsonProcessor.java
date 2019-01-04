package edu.nwpu.machunyan.theoreticalEvaluation.interData;

import com.google.gson.*;
import edu.nwpu.machunyan.theoreticalEvaluation.runningDatas.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RunResultsJsonProcessor {

    /**
     * 将一个程序的运行结果导出成 json object。每个结果都必须是相同的程序运行出来的，因此都具有一致的
     * {@link RunResultFromRunner#getProgram()} 和 {@link RunResultFromRunner#getStatementMap()}
     *
     * @param runResults 输入。至少要有一个元素
     * @param inputType  {@link RunResultFromRunner#getInput()} 得到的对象的实际类型，需要用这个来序列化
     * @return
     */
    public static JsonObject bumpToJson(List<RunResultFromRunner> runResults, Type inputType) {

        final Gson gson = new Gson();

        final Program program = runResults.get(0).getProgram();
        final StatementMap statementMap = runResults.get(0).getStatementMap();

        final JsonObject result = new JsonObject();
        result.add("program", gson.toJsonTree(program));
        result.add("statementMap", gson.toJsonTree(statementMap));

        final JsonArray cases = new JsonArray();
        for (RunResultFromRunner runResultFromRunner :
                runResults) {

            final JsonObject runCase = new JsonObject();
            runCase.add("correct", new JsonPrimitive(runResultFromRunner.isCorrect()));
            runCase.add("input", gson.toJsonTree(runResultFromRunner.getInput(), inputType));
            runCase.add("coverage", gson.toJsonTree(runResultFromRunner.getCoverage()));

            cases.add(runCase);
        }

        result.add("runCases", cases);

        return result;
    }

    /**
     * 从 JsonObject 导入运行数据。
     *
     * @param input     {@link RunResultsJsonProcessor#bumpToJson(List, Type)} 中得到的 json 数据
     * @param inputType {@link RunResultFromRunner#getInput()} 得到的对象的实际类型，需要用这个来反序列化
     * @return
     */
    public static ArrayList<RunResultFromRunner> loadFromJson(JsonObject input, Type inputType) {

        final Gson gson = new Gson();

        final Program program = gson.fromJson(input.get("program"), Program.class);
        final StatementMap statementMap = gson.fromJson(input.get("statementMap"), StatementMap.class);

        final ArrayList<RunResultFromRunner> result = new ArrayList<>();

        final JsonArray runCases = input.getAsJsonArray("runCases");
        for (JsonElement runCase :
                runCases) {
            final JsonObject caseObject = runCase.getAsJsonObject();

            final boolean correct = caseObject.get("correct").getAsBoolean();
            final IProgramInput programInput = gson.fromJson(caseObject.get("input"), inputType);
            final Coverage coverage = gson.fromJson(caseObject.get("coverage"), Coverage.class);

            result.add(new RunResultFromRunner(program, programInput, correct, coverage, statementMap));
        }

        return result;
    }
}
