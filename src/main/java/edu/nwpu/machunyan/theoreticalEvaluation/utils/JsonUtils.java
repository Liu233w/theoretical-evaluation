package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtils {

    /**
     * 生成一个指定输出格式的 Gson 实例
     *
     * @return
     */
    public static Gson newSavingGsonInstance() {
        return new GsonBuilder()
            .setPrettyPrinting()
            .serializeSpecialFloatingPointValues()
            .create();
    }
}
