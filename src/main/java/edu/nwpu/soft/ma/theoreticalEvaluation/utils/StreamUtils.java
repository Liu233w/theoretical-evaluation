package edu.nwpu.soft.ma.theoreticalEvaluation.utils;

import java.io.InputStream;
import java.util.Scanner;

public class StreamUtils {

    /**
     * 从流中读取所有的内容到字符串中
     *
     * @param is
     * @return
     */
    public static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
