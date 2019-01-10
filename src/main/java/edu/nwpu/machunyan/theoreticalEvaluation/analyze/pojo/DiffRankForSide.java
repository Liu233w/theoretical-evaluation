package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 一侧的排名信息
 */
@Data
@AllArgsConstructor
public class DiffRankForSide {

    int rank;

    double suspiciousnessFactor;
}
