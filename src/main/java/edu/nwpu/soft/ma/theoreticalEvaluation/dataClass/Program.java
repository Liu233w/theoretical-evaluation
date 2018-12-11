package edu.nwpu.soft.ma.theoreticalEvaluation.dataClass;

import lombok.Data;

/**
 * 一个源程序代码，用于储存用来分析的程序的元信息
 */
@Data
public class Program {

    /**
     * 程序的标题信息，用于区分程序或版本
     */
    private final String title;

    /**
     * 程序的路径。可能是文件或文件夹。由分析器决定。
     */
    private final String path;
}
