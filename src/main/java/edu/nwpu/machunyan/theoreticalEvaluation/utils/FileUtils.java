package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
}
