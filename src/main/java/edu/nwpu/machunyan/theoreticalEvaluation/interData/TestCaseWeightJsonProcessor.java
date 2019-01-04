package edu.nwpu.machunyan.theoreticalEvaluation.interData;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestCaseWeightJam;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.GsonUtils;

public class TestCaseWeightJsonProcessor {

    public static JsonElement dumpToJson(TestCaseWeightJam jam) {
        return GsonUtils.toJsonTree(jam);
    }

    public static TestCaseWeightJam loadAllFromJson(JsonElement element) {
        return new Gson().fromJson(element, TestCaseWeightJam.class);
    }
}
