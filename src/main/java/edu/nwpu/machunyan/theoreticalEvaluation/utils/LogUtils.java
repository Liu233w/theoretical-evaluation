package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import com.google.gson.Gson;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

public class LogUtils {

    private static final Path ERROR_LOGFILE = Paths.get("./target/outputs/error.log");

    // 在每次打印日志之前都清除当前行，防止进度条显示在每一行日志里

    public static void logInfo(String input) {
        System.out.print("\033[2K"); // Erase line content
        System.out.print("\r"); // Move to the line begin
        System.out.println(input);
    }

    public static void logFine(String input) {
        System.out.print("\033[2K"); // Erase line content
        System.out.print("\r"); // Move to the line begin
        System.out.println(input);
    }

    public static void logError(String input) {
        System.out.print("\033[2K"); // Erase line content
        System.out.print("\r"); // Move to the line begin
        System.err.println(input);

        FileUtils.ensurePathDir(ERROR_LOGFILE);
        try {
            Files.write(ERROR_LOGFILE, (new Date() + ":\n" + input + "\n-------------\n").getBytes(),
                StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logError(Throwable e) {
        final String stackTrace = ExceptionUtils.getStackTrace(e);
        logError(stackTrace);
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

    public static ProgressBar newProgressBarInstance(String task, int initialMax) {
        return new ProgressBarBuilder()
            .setTaskName(task)
            .setInitialMax(initialMax)
            .setStyle(ProgressBarStyle.ASCII)
            .build();
    }
}
