package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
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
        return Paths.get(FileUtils.class.getClassLoader().getResource(".").toURI()).resolve(filePathRelativeToResources);
    }

    /**
     * 如果 path 是一个文件夹（没有后缀名），将创建它和它的所有父文件夹。否则创建它的父文件夹。
     * 如果相应的文件夹存在，什么都不做。
     *
     * @param path
     */
    public static void ensurePathDir(Path path) {
        final boolean isDir = path.getFileName().toString().lastIndexOf('.') == -1;
        if (isDir) {
            path.toFile().mkdirs();
        } else {
            path.getParent().toFile().mkdirs();
        }
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
    public static <T> T loadObject(Path path, Class<T> type) throws FileNotFoundException {
        return loadObject(path, type, new Gson());
    }

    public static <T> T loadObject(Path path, Class<T> type, Gson gson) throws FileNotFoundException {
        return gson.fromJson(new FileReader(path.toFile()), type);
    }

    public static <T> T loadObject(Path path, TypeToken<T> token) throws FileNotFoundException {
        return new Gson().fromJson(new FileReader(path.toFile()), token.getType());
    }

    public static <T> T loadObject(String path, TypeToken<T> token) throws FileNotFoundException {
        return loadObject(Paths.get(path), token);
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
    public static <T> T loadObject(String path, Class<T> type) throws FileNotFoundException {
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

        final Gson gson = JsonUtils.newSavingGsonInstance();

        final File outputFile = path.toFile();
        outputFile.getParentFile().mkdirs();

        try (FileWriter fileWriter = new FileWriter(outputFile)) {
            gson.toJson(obj, fileWriter);
        }
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

    /**
     * 将 string 保存到指定路径中
     *
     * @param path
     * @param str
     * @throws IOException
     */
    public static void saveString(String path, String str) throws IOException {
        saveString(Paths.get(path), str);
    }

    public static void saveString(Path path, String str) throws IOException {
        ensurePathDir(path);
        Files.write(path, str.getBytes());
    }
}
