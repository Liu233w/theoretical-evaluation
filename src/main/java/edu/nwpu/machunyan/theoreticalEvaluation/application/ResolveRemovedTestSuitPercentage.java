package edu.nwpu.machunyan.theoreticalEvaluation.application;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.TestSuitSubsetJam;
import edu.nwpu.machunyan.theoreticalEvaluation.application.utils.ProgramDefination;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.pojo.RunResultJam;
import one.util.streamex.StreamEx;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 * 试验程序和公式中，被移除的测试用例占总测试用例的百分比
 */
public class ResolveRemovedTestSuitPercentage {

    private static final String path = "./target/outputs/removed-testcase.jpg";

    private static final List<String> formulas = Arrays.asList(
        "tarantula",
        "ochiai",
        "ochiai2",
        "op",
        "sbi",
        "jaccard",
        "kulcznski1",
        "dStar2",
        "o");

    public static void main(String[] args) throws IOException, URISyntaxException {

        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (String program : ProgramDefination.PROGRAM_LIST) {

            final RunResultJam runResultJam = Run.getResultFromFile(program);
            final int allCount = StreamEx
                .of(runResultJam.getRunResultForPrograms())
                .mapToInt(a -> a.getRunResults().size())
                .sum();

            for (String formula : formulas) {
                final TestSuitSubsetJam subset = ResolveTestSuitSubset.getResultFromFile(program, formula);
                final int subsetCount = StreamEx
                    .of(subset.getTestSuitSubsetForPrograms())
                    .mapToInt(a -> a.getToOldSetMap().length)
                    .sum();
                final double res = 1.0 * (allCount - subsetCount) / allCount;
                dataset.addValue(res, formula, program);
            }
        }

        final JFreeChart chart = ChartFactory.createBarChart(
            "Removed Testcase Percentage (higher the better)",
            "Programs",
            "Percentage",
            dataset);

//        final BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
//        DecimalFormat decimalformat = new DecimalFormat("##.##");
//        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", decimalformat));
//        renderer.setDefaultItemLabelsVisible(true);

        ChartUtils.saveChartAsJPEG(
            Paths.get(path).toFile(),
            chart,
            1200,
            800);
    }

}
