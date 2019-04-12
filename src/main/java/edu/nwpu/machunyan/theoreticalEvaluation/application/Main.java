package edu.nwpu.machunyan.theoreticalEvaluation.application;

/**
 * 从命令行获取要执行的类名
 */
public class Main {
    public static void main(String[] args) throws ReflectiveOperationException {
        if (args.length != 1) {
            System.out.println("specify a class name. eg:");
            System.out.println("sh run-image.sh RunTotInfo");
            System.out.println("sh run-image.sh temporary.DiffMultipleFormulaSf");
            return;
        }

        final String basePackage = "edu.nwpu.machunyan.theoreticalEvaluation.application.";

        final Class<?> aClass = Class.forName(basePackage + args[0]);
        aClass.getMethod("main", String[].class).invoke(null, (Object) new String[]{});
    }
}
