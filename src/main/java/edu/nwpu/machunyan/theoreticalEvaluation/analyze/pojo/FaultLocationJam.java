package edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo;

import lombok.Value;

import java.util.List;

@Value
public class FaultLocationJam {

    List<FaultLocationForProgram> faultLocationForPrograms;
}
