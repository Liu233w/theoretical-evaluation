package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.AveragePerformanceResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.VectorTableModelResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.VectorTableModelForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.application.Run;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import lombok.Lombok;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

/*
运行结果：
Benchmark                                                                                    Mode  Cnt  Score   Error  Units
theoreticalEvaluation.application.temporary.AveragePerformanceResolverBenchmark.newResolver  avgt    5  0.017 ± 0.001  ms/op
theoreticalEvaluation.application.temporary.AveragePerformanceResolverBenchmark.oldResolver  avgt    5  0.024 ± 0.003  ms/op
 */

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class AveragePerformanceResolverBenchmark {

    public static void main(String[] args) throws RunnerException {
        final Options options = new OptionsBuilder()
            .include(AveragePerformanceResolverBenchmark.class.getSimpleName())
            .forks(1)
            .build();
        new Runner(options).run();
    }

    private VectorTableModelForProgram vtm;

    @Setup
    public void setup() {
        try {
            final RunResultJam runResult = Run.getResultFromFile("tot_info");
            vtm = VectorTableModelResolver.resolve(runResult.getRunResultForPrograms().get(0));
        } catch (FileNotFoundException e) {
            Lombok.sneakyThrow(e);
        }
    }

    @Benchmark
    public void newResolver() {
        final double newAp = AveragePerformanceResolver.resolve(vtm, SuspiciousnessFactorFormulas::op);
    }

    @Benchmark
    public void oldResolver() {
        final double newAp = OldAveragePerformanceResolver.resolve(vtm, SuspiciousnessFactorFormulas::op);
    }
}
