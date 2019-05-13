package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 对于 GB 以上的数据，使用 json 进行读取和存储时可能会爆内存。
 * 使用本类和 {@link BigDataLoader} 来分批次处理
 */
public class BigDataSaver<T> implements Closeable {

    private final JsonWriter writer;
    private final Gson gson;
    private final Class<T> itemClass;

    private boolean closed = false;

    /**
     * 创建一个 saver，保存到指定路径
     *
     * @param path
     * @param propertyName 比如要写入一个 RunResultJam 对象时，使用本方法
     *                     传入参数名 runResultForPrograms
     * @param itemClass    保存的每个对象的类型
     */
    public BigDataSaver(String path, String propertyName, Class<T> itemClass) throws IOException {
        writer = new JsonWriter(new BufferedWriter(new FileWriter(path)));
        writer.setIndent("  ");
        gson = JsonUtils.newSavingGsonInstance();
        this.itemClass = itemClass;

        writer.beginObject().name(propertyName);
        writer.beginArray();
    }

    public <U> BigDataSaver(String path, Class<U> jamType, Class<T> itemClass) throws IOException {
        this(path, jamType.getDeclaredFields()[0].getName(), itemClass);
    }

    public void saveObject(Object object) {
        gson.toJson(object, itemClass, writer);
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            writer.endArray().endObject();
            writer.close();
            closed = true;
        }
    }
}
