package edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RunResultJam {

    List<RunResultForProgram> runResultForPrograms;
}
