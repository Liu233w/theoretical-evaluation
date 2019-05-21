package edu.nwpu.machunyan.theoreticalEvaluation.chart;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankForSide;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankForStatement;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankJam;
import lombok.NonNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultMultiValueCategoryDataset;

import java.awt.*;

public class DiffRankChart {

    /**
     * 获取同一个公式的所有版本的 chart
     *
     * @param jam
     * @param chartTitle
     * @return
     */
    public static JFreeChart resolveRankBarChart(DiffRankJam jam, String chartTitle) {

        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (DiffRankForProgram diffRankForProgram : jam.getDiffRankForPrograms()) {

            final String programTitle = diffRankForProgram.getProgramTitle();
            final String leftRankTitle = diffRankForProgram.getLeftRankTitle();
            final String rightRankTitle = diffRankForProgram.getRightRankTitle();

            for (DiffRankForStatement diffRankForStatement : diffRankForProgram.getDiffRankForStatements()) {


                final DiffRankForSide left = diffRankForStatement.getLeft();
                final DiffRankForSide right = diffRankForStatement.getRight();
                if (left.getRank() == -1 || right.getRank() == -1) {
                    continue;
                }

                dataset.addValue(left.getRank(), leftRankTitle, programTitle);
                dataset.addValue(right.getRank(), rightRankTitle, programTitle);
            }
        }

        final JFreeChart barChart = ChartFactory.createBarChart(
            chartTitle,
            "",
            "Rank",
            dataset);
        // 让排名靠上的数据在图表上也更高
        barChart.getXYPlot().getRangeAxis().setInverted(true);

        barChart.setBackgroundPaint(Color.WHITE);

        return barChart;
    }
}
