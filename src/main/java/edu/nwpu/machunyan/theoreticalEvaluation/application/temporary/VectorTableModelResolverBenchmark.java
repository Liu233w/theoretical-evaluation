package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModelResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModelForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.application.Run;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

/*
Benchmark   Mode  Cnt  Score   Error  Units
Before      avgt    5  4.480 ± 0.176  ms/op
After       avgt    5  4.726 ± 0.303  ms/op
*/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class VectorTableModelResolverBenchmark {

    private RunResultForProgram runResultForProgram;

    public static void main(String[] args) throws RunnerException {
        final Options options = new OptionsBuilder()
            .include(VectorTableModelResolverBenchmark.class.getSimpleName())
            .forks(1)
            .build();
        new Runner(options).run();
    }

    @Setup
    public void setup() throws FileNotFoundException {
        final RunResultJam tot_info = Run.getResultFromFile("tot_info");
        runResultForProgram = tot_info.getRunResultForPrograms().get(0);
    }

    @Benchmark
    public void perfile() {
        final VectorTableModelForProgram resolve = VectorTableModelResolver.resolve(runResultForProgram);
    }
}
