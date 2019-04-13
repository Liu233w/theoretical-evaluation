package edu.nwpu.machunyan.theoreticalEvaluation.utils;

public class LogUtils {

    public static void logInfo(String input) {
        System.out.println(input);
    }

    public static void logFine(String input) {
        System.out.println(input);
    }

    public static void logError(String input) {
        System.err.println(input);
    }

    public static void logError(Throwable e) {
        e.printStackTrace();
    }

}
