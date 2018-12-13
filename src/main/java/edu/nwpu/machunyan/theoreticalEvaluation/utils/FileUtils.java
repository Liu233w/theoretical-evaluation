package edu.nwpu.machunyan.theoreticalEvaluation.utils;

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
}
