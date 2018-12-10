package edu.nwpu.soft.ma.theoreticalEvaluation.utils;

public class ParameterUtils {

    public static void checkNull(Object obj) {
        if (obj == null) {
            throw new NullPointerException("Parameter should not be null");
        }
    }
}
