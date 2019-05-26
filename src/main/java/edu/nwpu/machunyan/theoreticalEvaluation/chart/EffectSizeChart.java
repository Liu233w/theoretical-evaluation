package edu.nwpu.machunyan.theoreticalEvaluation.chart;

import edu.nwpu.machunyan.theoreticalEvaluation.chart.pojo.EffectSizeItem;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.List;

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
}
