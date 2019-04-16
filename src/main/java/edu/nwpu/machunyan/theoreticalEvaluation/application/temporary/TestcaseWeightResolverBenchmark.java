package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestcaseWeightResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestcaseWeightForProgram;
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
比较重构之前和之后的运行速度
Benchmark   Mode  Cnt     Score     Error  Units
Before      avgt    5  4940.753 ± 599.973  ms/op
After       avgt    5  6766.989 ± 191.981  ms/op
 */

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class TestcaseWeightResolverBenchmark {

    public static void main(String[] args) throws RunnerException {
        final Options options = new OptionsBuilder()
            .include(TestcaseWeightResolverBenchmark.class.getSimpleName())
            .forks(1)
            .build();
        new Runner(options).run();
    }

    private RunResultForProgram runResultForProgram;
    private TestcaseWeightResolver resolver;

    @Setup
    public void setup() throws FileNotFoundException {
        final RunResultJam imports = Run.getResultFromFile("tot_info");
        runResultForProgram = imports.getRunResultForPrograms()[0];

        resolver = new TestcaseWeightResolver(SuspiciousnessFactorFormulas::op);
    }

    @Benchmark
    public void profile() {

        final TestcaseWeightForProgram result = resolver.resolve(runResultForProgram);
    }
}
