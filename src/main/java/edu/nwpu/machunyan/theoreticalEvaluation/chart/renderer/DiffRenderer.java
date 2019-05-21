package edu.nwpu.machunyan.theoreticalEvaluation.chart.renderer;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class DiffRenderer extends XYLineAndShapeRenderer
    implements XYItemRenderer {

    public DiffRenderer() {
        super(false, true);
    }

    @Override
    public void drawItem(
        Graphics2D g2,
        XYItemRendererState state,
        Rectangle2D dataArea,
        PlotRenderingInfo info,
        XYPlot plot,
        ValueAxis domainAxis,
        ValueAxis rangeAxis,
        XYDataset dataset,
        int series,
        int item,
        CrosshairState crosshairState,
        int pass) {

        super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState, pass);

        if (series > 0) {
            // 两个一起画，不管其他的了
            return;
        }

        // 剩下的是从 candle renderer 里抄过来的，稍微改了一下

        // 只绘制 vertical 的情况
        PlotOrientation orientation = plot.getOrientation();
        if (orientation != PlotOrientation.VERTICAL) {
            return;
        }

        double x = dataset.getXValue(0, item);
        // before
        double yOpen = dataset.getYValue(0, item);
        // after
        double yClose = dataset.getYValue(1, item);

        RectangleEdge domainEdge = plot.getDomainAxisEdge();
        double xx = domainAxis.valueToJava2D(x, dataArea, domainEdge);

        RectangleEdge edge = plot.getRangeAxisEdge();
        double yyOpen = rangeAxis.valueToJava2D(yOpen, dataArea, edge);
        double yyClose = rangeAxis.valueToJava2D(yClose, dataArea, edge);

        double stickWidth = 1;

        Paint p = getItemPaint(0, item);
        Stroke s = getItemStroke(0, item);
        if (!Double.isNaN(yyClose) && yyOpen >= yyClose) {
            // 排名上升（好事）
            p = getItemPaint(1, item);
            s = getItemStroke(1, item);
        }

        g2.setStroke(s);
        g2.setPaint(p);

        double yyMaxOpenClose = Math.max(yyOpen, yyClose);
        double yyMinOpenClose = Math.min(yyOpen, yyClose);

        // draw the body
        Rectangle2D body;
        if (Double.isNaN(yOpen) || Double.isNaN(yClose)) {
            // 如果有两个值不存在，就画一条穿过整个区域的竖线
            body = new Rectangle2D.Double(xx - stickWidth / 2, 0,
                stickWidth, dataArea.getHeight());
        } else {
            body = new Rectangle2D.Double(xx - stickWidth / 2, yyMinOpenClose,
                stickWidth, yyMaxOpenClose - yyMinOpenClose);
        }

        g2.fill(body);
        g2.draw(body);
    }
}
