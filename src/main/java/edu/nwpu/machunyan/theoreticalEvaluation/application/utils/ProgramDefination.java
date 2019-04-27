package edu.nwpu.machunyan.theoreticalEvaluation.application.utils;

import lombok.Value;
import one.util.streamex.StreamEx;

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
    public static final ProgramDir[] RUN_LIST = StreamEx
        .of(PROGRAM_LIST)
        .map(program -> new ProgramDir(program, program + ".c"))
        .toArray(ProgramDir[]::new);

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
