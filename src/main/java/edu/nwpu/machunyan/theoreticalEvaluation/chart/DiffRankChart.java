package edu.nwpu.machunyan.theoreticalEvaluation.chart;

import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankForProgram;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankForSide;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankForStatement;
import edu.nwpu.machunyan.theoreticalEvaluation.analyze.pojo.DiffRankJam;
import edu.nwpu.machunyan.theoreticalEvaluation.chart.renderer.DiffRenderer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;

public class DiffRankChart {

    /**
     * 获取同一个公式的所有版本的 chart
     *
     * @param jam
     * @param chartTitle
     * @return
     */
    public static JFreeChart resolveRankDotChart(DiffRankJam jam, String chartTitle) {

        final XYSeriesCollection dataset = new XYSeriesCollection();
        final XYSeries leftData = new XYSeries(jam.getDiffRankForPrograms().get(0).getLeftRankTitle());
        final XYSeries rightData = new XYSeries(jam.getDiffRankForPrograms().get(0).getRightRankTitle());
        dataset.addSeries(leftData);
        dataset.addSeries(rightData);

        int idx = 0;

        for (DiffRankForProgram diffRankForProgram : jam.getDiffRankForPrograms()) {

            for (DiffRankForStatement diffRankForStatement : diffRankForProgram.getDiffRankForStatements()) {

                final DiffRankForSide left = diffRankForStatement.getLeft();
                final DiffRankForSide right = diffRankForStatement.getRight();
                if (left.getRank() == -1 || right.getRank() == -1) {
                    continue;
                }

                leftData.add(idx, left.getRank());
                rightData.add(idx, right.getRank());
                ++idx;
            }
        }

        final JFreeChart scatterPlot = ChartFactory.createScatterPlot(
            chartTitle,
            "Faulty Statements in Each Versions (" + (idx + 1) + " total)",
            "Rank",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false);

        final XYPlot xyPlot = scatterPlot.getXYPlot();
        // 让排名靠上的数据在图表上也更高
        xyPlot.getRangeAxis().setInverted(true);
        // 隐藏 x 轴数值（因为这个值没有意义）
        xyPlot.getDomainAxis().setTickLabelsVisible(false);
        // TODO: 调整这里的样式
        // y 轴最小值设为1（因为排名最高就是1）
//        xyPlot.getRangeAxis().setLowerBound(1);
        // 防止 x,y 轴最小值侧顶住边框（否则太难看）
//        xyPlot.getDomainAxis().setUpperMargin(0.1);
//        xyPlot.getRangeAxis().setUpperMargin(0.1);
        xyPlot.getDomainAxis().setLowerBound(-0.5);
        // 始终显示 y 轴最小值

        // 使用类似 candle 图的样式来表示变化
        final DiffRenderer renderer = new DiffRenderer();
        xyPlot.setRenderer(renderer);

        scatterPlot.setBackgroundPaint(Color.WHITE);

        return scatterPlot;
    }
}
