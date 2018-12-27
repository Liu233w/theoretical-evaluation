package edu.nwpu.machunyan.theoreticalEvaluation.interData;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.StreamUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestCaseWeightJsonProcessor {

    /**
     * 将一个版本的程序的运行结果输出成 json 对象。
     *
     * @param weights
     * @return
     */
    public static JsonArray dumpToJson(List<Double> weights) {
        return IntStream.range(0, weights.size())
                .mapToObj(i -> {
                    final JsonObject jsonObject = new JsonObject();
                    jsonObject.add("testcase-index", new JsonPrimitive(i));
                    jsonObject.add("testcase-weight", new JsonPrimitive(weights.get(i)));
                    return jsonObject;
                })
                .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
    }

    /**
     * 把一个数据和它的版本号一起输出成 json
     *
     * @param weights
     * @param versionName
     * @return
     */
    public static JsonObject dumpToJsonWithVersion(List<Double> weights, String versionName) {
        final JsonArray weightArray = dumpToJson(weights);
        final JsonObject recordForVersion = new JsonObject();
        recordForVersion.add("version", new JsonPrimitive(versionName));
        recordForVersion.add("weight-for-testcases", weightArray);
        return recordForVersion;
    }

    /**
     * 将多个数据和版本号一起输出成 json
     *
     * @param weights key 为版本号，json为权重数据
     * @return
     */
    public static JsonArray dumpToJson(Map<String, List<Double>> weights) {
        return weights.entrySet().stream()
                .map(entry -> dumpToJsonWithVersion(entry.getValue(), entry.getKey()))
                .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
    }

    /**
     * 从一个包含版本和权重数据的json对象中取得数据
     *
     * @param weightAndVersion
     * @return key为版本号，value为权重数据
     */
    public static Map<String, List<Double>> loadAllFromJson(JsonArray weightAndVersion) {
        return StreamUtils.asStream(weightAndVersion.iterator())
                .map(JsonElement::getAsJsonObject)
                .collect(Collectors.toMap(
                        item -> item.getAsJsonPrimitive("version").getAsString(),
                        item -> StreamUtils.asStream(item.getAsJsonArray("weight-for-testcases").iterator())
                                .map(JsonElement::getAsJsonObject)
                                .map(a -> a.getAsJsonPrimitive("testcase-weight"))
                                .map(JsonPrimitive::getAsDouble)
                                .collect(Collectors.toList())
                ));
    }
}
