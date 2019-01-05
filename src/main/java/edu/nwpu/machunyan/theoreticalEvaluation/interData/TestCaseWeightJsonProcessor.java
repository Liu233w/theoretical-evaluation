package edu.nwpu.machunyan.theoreticalEvaluation.interData;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.JsonUtils;

public class TestCaseWeightJsonProcessor {

    public static JsonElement dumpToJson(TestcaseWeightJam jam) {
        return JsonUtils.toJson(jam);
    }

    public static TestcaseWeightJam loadAllFromJson(JsonElement element) {
        return new Gson().fromJson(element, TestcaseWeightJam.class);
    }
}
