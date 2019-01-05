package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    /**
     * 根据名称获取 resource 文件夹中的文件的路径。可以转换成绝对路径。
     *
     * @param filePathRelativeToResources
     * @return
     * @throws URISyntaxException
     */
    public static Path getFilePathFromResources(String filePathRelativeToResources) throws URISyntaxException {
        return Paths.get(ClassLoader.getSystemResource(filePathRelativeToResources).toURI());
    }

    /**
     * 把 json 按照美化的格式输出到指定的文件。会自动创建父文件夹。
     *
     * @param json
     * @param path
     */
    public static void printJsonToFile(Path path, JsonElement json) throws IOException {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File outputFile = path.toFile();
        outputFile.getParentFile().mkdirs();
        try (FileWriter fileWriter = new FileWriter(outputFile)) {
            gson.toJson(json, fileWriter);
        }
    }

    /**
     * 从路径中读取json
     *
     * @param path
     * @return
     * @throws FileNotFoundException
     */
    public static JsonElement getJsonFromFile(String path) throws FileNotFoundException {
        final File file = Paths.get(path).toFile();
        return new JsonParser().parse(new FileReader(file));
    }

    /**
     * 从路径读取指定类型的对象
     *
     * @param path
     * @param type
     * @param <T>
     * @return
     * @throws FileNotFoundException
     */
    public static <T> T loadObject(Path path, Type type) throws FileNotFoundException {
        return JsonUtils.fromJson(new FileReader(path.toFile()), type);
    }

    /**
     * 从路径读取指定类型的对象
     *
     * @param <T>
     * @param path
     * @param type
     * @return
     * @throws FileNotFoundException
     */
    public static <T> T loadObject(String path, Type type) throws FileNotFoundException {
        return loadObject(Paths.get(path), type);
    }

    /**
     * 将对象转换成 json 并保存到指定路径
     *
     * @param path
     * @param obj
     * @throws IOException
     */
    public static void saveObject(Path path, Object obj) throws IOException {
        printJsonToFile(path, JsonUtils.toJson(obj));
    }

    /**
     * 将对象转换成 json 并保存到指定路径
     *
     * @param path
     * @param obj
     * @throws IOException
     */
    public static void saveObject(String path, Object obj) throws IOException {
        saveObject(Paths.get(path), obj);
    }
}
