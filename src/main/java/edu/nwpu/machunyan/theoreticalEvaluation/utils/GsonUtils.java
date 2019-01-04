package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class GsonUtils {

    /**
     * 从任意对象生成json对象。必须可以使用反射来读取数据。
     *
     * @param obj
     * @return
     */
    public static JsonElement toJsonTree(Object obj) {
        return new Gson().toJsonTree(obj);
    }
}
