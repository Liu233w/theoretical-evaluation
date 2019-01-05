package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SuspiciousnessFactorJam {
    List<SuspiciousnessFactor> factorInPrograms;
}
