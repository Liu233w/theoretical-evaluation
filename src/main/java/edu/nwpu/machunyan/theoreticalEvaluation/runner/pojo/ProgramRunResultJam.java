package edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ProgramRunResultJam {

    private final List<ProgramRunResult> programRunResults;

    public ProgramRunResultJam(List<ProgramRunResult> programRunResults) {
        this.programRunResults = programRunResults;
    }
}
