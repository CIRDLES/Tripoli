package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractDataView;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.linePlots.ComboPlotBuilder;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;


public class BasicScatterAndLinePlot extends AbstractPlot {

    private final ComboPlotBuilder comboPlotBuilder;
    private double[] yAxisData2;

    /**
     * @param bounds
     * @param comboPlotBuilder
     */
    public BasicScatterAndLinePlot(Rectangle bounds, ComboPlotBuilder comboPlotBuilder) {
        super(bounds, 75, 25,
                comboPlotBuilder.getTitle(),
                comboPlotBuilder.getxAxisLabel(),
                comboPlotBuilder.getyAxisLabel());
        this.comboPlotBuilder = comboPlotBuilder;
    }

    @Override
    public void preparePanel() {
        xAxisData = comboPlotBuilder.getxData();
        yAxisData = comboPlotBuilder.getyData();
        yAxisData2 = comboPlotBuilder.getyData2();

        minX = xAxisData[0];
        maxX = xAxisData[xAxisData.length - 1];

        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (int i = 0; i < yAxisData.length; i++) {
            minY = StrictMath.min(minY, yAxisData[i]);
            maxY = StrictMath.max(maxY, yAxisData[i]);
            minY = StrictMath.min(minY, yAxisData2[i]);
            maxY = StrictMath.max(maxY, yAxisData2[i]);
            if (comboPlotBuilder.isyData2OneSigma()) {
                minY = StrictMath.min(minY, -yAxisData2[i]);
            }
        }

        setDisplayOffsetY(0.0);
        setDisplayOffsetX(0.0);

        prepareExtents();
        calculateTics();
        repaint();
    }

    public void prepareExtents() {
        double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.01);
        if (0.0 == xMarginStretch) {
            xMarginStretch = maxX * 0.01;
        }
        minX -= xMarginStretch;
        maxX += xMarginStretch;

        double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.01);
        maxY += yMarginStretch;
        minY -= yMarginStretch;
    }

    public static AbstractPlot generatePlot(Rectangle bounds, ComboPlotBuilder comboPlotBuilder) {
        return new BasicScatterAndLinePlot(bounds, comboPlotBuilder);
    }
    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);
    }

    public void plotData(GraphicsContext g2d) {
        // scatter plot
        g2d.setLineWidth(0.75);
        g2d.setStroke(Paint.valueOf("Black"));
        for (int i = 0; i < xAxisData.length; i++) {
            g2d.strokeOval(mapX(xAxisData[i]) - 2.0f, mapY(yAxisData[i]) - 2.0f, 4.0f, 4.0f);
        }

        // new line plot from yAxisData2
        g2d.setStroke(Paint.valueOf("red"));
        g2d.beginPath();
        g2d.moveTo(mapX(xAxisData[0]), mapY(yAxisData2[0]));
        for (int i = 0; i < xAxisData.length; i++) {
            // line tracing through points
            g2d.lineTo(mapX(xAxisData[i]), mapY(yAxisData2[i]));
        }
        g2d.stroke();

        if (comboPlotBuilder.isyData2OneSigma()) {
            g2d.beginPath();
            g2d.moveTo(mapX(xAxisData[0]), mapY(-yAxisData2[0]));
            for (int i = 0; i < xAxisData.length; i++) {
                // line tracing through points
                g2d.lineTo(mapX(xAxisData[i]), mapY(-yAxisData2[i]));
            }
            g2d.stroke();
        }
    }

    @Override
    public void plotStats(GraphicsContext g2d) {

    }
}