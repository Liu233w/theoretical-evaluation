package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class LogUtils {

    private static final Path ERROR_LOGFILE = Paths.get("./target/outputs/error.log");

    public static void logInfo(String input) {
        System.out.println(input);
    }

    public static void logFine(String input) {
        System.out.println(input);
    }

    public static void logError(String input) {
        System.err.println(input);

        FileUtils.ensurePathDir(ERROR_LOGFILE);
        try {
            Files.write(ERROR_LOGFILE, (input + "\n-------------\n").getBytes(),
                StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logError(Throwable e) {
        e.printStackTrace();
    }

    /**
     * 如果输入中包含了空格或者换行符，在打印时可能不易读。用这个方法将其转换成 json 字符串的形式。
     *
     * @param input
     * @return
     */
    public static String getReadableSpacerString(String input) {
        return new Gson().toJson(input);
    }
}
