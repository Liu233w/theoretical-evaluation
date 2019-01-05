package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.Reader;
import java.lang.reflect.Type;

public class JsonUtils {

    /**
     * 从任意对象生成json对象。必须可以使用反射来读取数据。
     *
     * @param obj
     * @return
     */
    public static JsonElement toJson(Object obj) {
        return new Gson().toJsonTree(obj);
    }

    /**
     * 从 json 对象读取数据
     *
     * @param json
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T fromJson(JsonElement json, Type type) {
        return new Gson().fromJson(json, type);
    }

    /**
     * 从 json 对象读取数据
     *
     * @param reader
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T fromJson(Reader reader, Type type) {
        return new Gson().fromJson(reader, type);
    }
}
