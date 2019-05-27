package edu.nwpu.machunyan.theoreticalEvaluation.chart;

import edu.nwpu.machunyan.theoreticalEvaluation.chart.pojo.EffectSizeItem;
import lombok.Value;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class EffectSizeChart {

    /**
     * @param input programName -> formulaTitle -> effectSize
     * @return
     */
    public static JFreeChart resolveGroupByProgram(List<EffectSizeItem> input, String chartTitle) {

        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (EffectSizeItem item : input) {
            dataset.addValue(item.getEffectSize(), item.getFormulaTitle(), item.getProgramName());
        }

        final JFreeChart chart = ChartFactory.createBarChart(
            chartTitle,
            "Program",
            "Effect Size",
            dataset);

        return chart;
    }

    public static JFreeChart resolveBoxChartGroupByFormula(List<EffectSizeItem> input, String chartTitle) {

        final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

        final Map<String, List<EffectSizeItem>> formulaToResult = StreamEx
            .of(input)
            .groupingBy(EffectSizeItem::getFormulaTitle);

        formulaToResult.forEach((formula, effectSizeItems) -> {
            final List<Double> list = StreamEx
                .of(effectSizeItems)
                .map(EffectSizeItem::getEffectSize)
                .toImmutableList();
            dataset.add(list, "", formula);
        });

        final JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
            chartTitle,
            "Formula",
            "Effect Size",
            dataset,
            false
        );
        return chart;
    }

    public static AverageResult resolveEffectSizeAverageByFormula(
        List<EffectSizeItem> input,
        String chartTitle) {

        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        final Map<String, List<EffectSizeItem>> formulaToItem = StreamEx
            .of(input)
            .groupingBy(EffectSizeItem::getFormulaTitle);

        final Map<String, Double> resMap = EntryStream
            .of(formulaToItem)
            .mapValues(list -> StreamEx
                .of(list)
                .mapToDouble(EffectSizeItem::getEffectSize)
                .average()
                .getAsDouble())
            .peekKeyValue((formula, average) -> {
                dataset.addValue(average, "default", formula);
            })
            .toMap();

        final JFreeChart chart = ChartFactory.createBarChart(
            chartTitle,
            "Formula",
            "Average Effect Size",
            dataset);

        final BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
        DecimalFormat decimalformat = new DecimalFormat("##.##");
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", decimalformat));
        renderer.setDefaultItemLabelsVisible(true);

        return new AverageResult(resMap, chart);
    }

    @Value
    public static class AverageResult {
        Map<String, Double> formulaToAverage;
        JFreeChart chart;
    }
}
