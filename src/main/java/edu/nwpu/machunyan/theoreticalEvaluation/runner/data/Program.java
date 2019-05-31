package edu.nwpu.machunyan.theoreticalEvaluation.runner.data;

import lombok.Value;

/**
 * 储存用来分析的程序的元信息，包含了程序名称和位置。
 */
@Value
public class Program {

    /**
     * 程序的标题信息，用于区分程序或版本
     */
    String title;

    /**
     * 程序的路径。可能是文件或文件夹。由分析器决定。
     */
    String path;
}
