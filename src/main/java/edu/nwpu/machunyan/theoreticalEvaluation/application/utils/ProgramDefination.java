package edu.nwpu.machunyan.theoreticalEvaluation.application.utils;

import lombok.Value;

/**
 * 定义了所有的程序，便于其他的类引用
 */
public class ProgramDefination {

    public static final String[] PROGRAM_LIST = new String[]{
        "my_sort",
        "schedule2",
        "tcas",
        "tot_info",
        "replace",
        "print_tokens",
        "expression_parser",
    };

    /**
     * 要执行的所有程序
     */
    public static final ProgramDir[] GCC_RUN_LIST = new ProgramDir[]{
        new ProgramDir("my_sort", "my_sort.c"),
        new ProgramDir("schedule2", "schedule2.c"),
        new ProgramDir("tcas", "tcas.c"),
        new ProgramDir("tot_info", "tot_info.c"),
        new ProgramDir("replace", "replace.c"),
        new ProgramDir("print_tokens", "print_tokens.c"),
        new ProgramDir("expression_parser", "expression_parser.c"),
    };

    public static final String[] DEFECTS4J_RUN_LIST = new String[]{
        "Chart",
        "Math",
        "Mockito",
        "Time",
        "Lang",
        "Closure",
    };

    @Value
    public static class ProgramDir {
        /**
         * resources 文件夹中存放程序文件的文件夹名
         */
        String programDir;
        /**
         * 程序的源代码名称
         */
        String programName;
    }
}
