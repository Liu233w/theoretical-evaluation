package edu.nwpu.machunyan.theoreticalEvaluation.interData;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestCaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.JsonUtils;

public class TestCaseWeightJsonProcessor {

    public static JsonElement dumpToJson(TestCaseWeightJam jam) {
        return JsonUtils.toJson(jam);
    }

    public static TestCaseWeightJam loadAllFromJson(JsonElement element) {
        return new Gson().fromJson(element, TestCaseWeightJam.class);
    }
}
