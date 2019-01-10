package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.Data;
import lombok.Value;

/**
 * 一侧的排名信息
 */
@Value
public class DiffRankForSide {

    int rank;

    double suspiciousnessFactor;
}
