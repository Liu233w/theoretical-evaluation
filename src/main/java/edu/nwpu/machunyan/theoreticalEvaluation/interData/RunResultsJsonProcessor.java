package edu.nwpu.machunyan.theoreticalEvaluation.interData;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.ProgramRunResultJam;

public class RunResultsJsonProcessor {

    /**
     * 将一个程序的所有版本的运行结果导出成 json object
     *
     * @param result
     * @return
     */
    public static JsonElement bumpToJson(ProgramRunResultJam result) {
        return new Gson().toJsonTree(result);
    }

    public static ProgramRunResultJam loadFromJson(JsonElement jsonElement) {
        return new Gson().fromJson(jsonElement, ProgramRunResultJam.class);
    }
}
