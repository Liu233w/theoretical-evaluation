package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * 对于 GB 以上的数据，使用 json 进行读取和存储时可能会爆内存。
 * 使用本类和 {@link BigDataSaver} 来分批次处理
 * <p>
 * json 文件必须按照 Jam 的格式存储。Loader 将不检查格式是否正确。
 */
public class BigDataLoader<T> implements Iterable<T> {

    private final String path;
    private final Class<T> itemClass;

    public BigDataLoader(String path, Class<T> itemClass) throws FileNotFoundException {
        this.itemClass = itemClass;
        this.path = path;
        if (!Files.exists(Paths.get(path))) {
            throw new FileNotFoundException("Can't find file " + path);
        }
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        final JsonReader reader;
        try {
            reader = new JsonReader(new FileReader(path));
            reader.beginObject();
            reader.nextName();
            reader.beginArray();
        } catch (IOException e) {
            throw new JsonIOException(e);
        }

        final Gson gson = new Gson();

        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                try {
                    final boolean hasNext = reader.hasNext();
                    if (!hasNext) {
                        reader.close();
                    }
                    return hasNext;
                } catch (IOException e) {
                    throw new JsonIOException(e);
                }
            }

            @Override
            public T next() {
                return gson.fromJson(reader, itemClass);
            }
        };
    }
}
