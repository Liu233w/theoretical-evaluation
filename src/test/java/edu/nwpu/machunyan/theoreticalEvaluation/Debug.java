package edu.nwpu.machunyan.theoreticalEvaluation;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;

import java.lang.reflect.Field;

public class Debug {
    public static void main(String[] args) {

        classOfBigDataSaver();
    }

    private static void classOfBigDataSaver() {

        final Field[] fields = RunResultJam.class.getFields();

        System.out.println(fields);
    }
}
