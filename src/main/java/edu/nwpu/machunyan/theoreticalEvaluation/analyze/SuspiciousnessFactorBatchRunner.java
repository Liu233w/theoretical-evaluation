package edu.nwpu.machunyan.theoreticalEvaluation.analyze;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 用于批量生成多个公式的运行结果
 */
public class SuspiciousnessFactorBatchRunner {

    /**
     * 在vtm上运行每个 resolver，将结果收集到一起
     *
     * @param jam
     * @param resolvers
     * @return
     */
    public static SuspiciousnessFactorJam runAll(VectorTableModelJam jam, List<SuspiciousnessFactorResolver> resolvers) {
        final List<SuspiciousnessFactorForProgram> collect = resolvers.stream()
            .map(resolver -> resolver.resolve(jam))
            .map(SuspiciousnessFactorJam::getResultForPrograms)
            .map(Collection::stream)
            .reduce(Stream::concat)
            .orElseGet(Stream::empty)
            .collect(Collectors.toList());
        return new SuspiciousnessFactorJam(collect);
    }

    /**
     * 从 jam 中将不同的公式运行的结果提取出来
     *
     * @param jam
     * @return
     */
    public static MultipleFormulaSuspiciousnessFactorJam collectAsMultipleFormula(SuspiciousnessFactorJam jam) {

        final List<SuspiciousnessFactorForProgram> prevResult = jam.getResultForPrograms();

        final Set<String> programTitles = prevResult.stream()
            .map(SuspiciousnessFactorForProgram::getProgramTitle)
            .collect(Collectors.toSet());
        final Set<String> formulaTitles = prevResult.stream()
            .map(SuspiciousnessFactorForProgram::getFormula)
            .collect(Collectors.toSet());

        final List<MultipleFormulaSuspiciousnessFactorForProgram> collect = programTitles.stream()
            .map(programTitle -> {

                @SuppressWarnings("OptionalGetWithoutIsPresent") final int
                    statementCount = prevResult.stream()
                    .filter(a -> a.getProgramTitle().equals(programTitle))
                    .findAny()
                    .get()
                    .getResultForStatements().size();

                final List<MultipleFormulaSuspiciousnessFactorItem> result = IntStream
                    .range(1, statementCount + 1)
                    .mapToObj(i -> new MultipleFormulaSuspiciousnessFactorItem(i, new HashMap<>()))
                    .collect(Collectors.toList());

                prevResult.stream()
                    .filter(a -> a.getProgramTitle().equals(programTitle))
                    .forEach(suspiciousnessFactorForProgram -> {

                        suspiciousnessFactorForProgram
                            .getResultForStatements()
                            .forEach(statement -> {
                                result.get(statement.getStatementIndex() - 1)
                                    .getFormulaTitleToResult()
                                    .put(suspiciousnessFactorForProgram.getFormula(), statement.getSuspiciousnessFactor());
                            });
                    });

                return new MultipleFormulaSuspiciousnessFactorForProgram(programTitle, result);
            })
            .collect(Collectors.toList());

        return new MultipleFormulaSuspiciousnessFactorJam(collect, formulaTitles);
    }
}
