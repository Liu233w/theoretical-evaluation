package edu.nwpu.machunyan.theoreticalEvaluation.application.temporary;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.SuspiciousnessFactorFormulas;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.TestcaseWeightResolver;
import edu.nwpu.machunyan.theoreticalEvaluation.application.RunTotInfo;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;

import java.io.FileNotFoundException;

public class TestcaseWeightResolverBenchmark {

    public static void main(String[] args) throws FileNotFoundException {

        final RunResultJam imports = RunTotInfo.getRunResultsFromSavedFile();

        final TestcaseWeightResolver resolver = new TestcaseWeightResolver(SuspiciousnessFactorFormulas::o);

        // Code took 7.4316307 seconds
        System.out.println("a program");
        bench(() -> resolver.resolve(imports.getRunResultForPrograms().get(0)));

        // Code took 34.8370996 seconds
        System.out.println("all program");
        bench(() -> resolver.resolve(imports));
    }

    private static void bench(Runnable runnable) {

        final long endTime, startTime = System.nanoTime();
        runnable.run();
        endTime = System.nanoTime();
        System.out.println("Code took " + (endTime - startTime) / 1000000000.0 + " seconds");
    }
}
