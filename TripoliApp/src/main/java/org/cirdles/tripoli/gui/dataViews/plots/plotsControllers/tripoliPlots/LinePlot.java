package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;

public class LinePlot extends AbstractPlot {
    private final LinePlotBuilder linePlotBuilder;


    private LinePlot(Rectangle bounds, LinePlotBuilder linePlotBuilder) {
        super(bounds, 75, 25,
                linePlotBuilder.getTitle(),
                linePlotBuilder.getxAxisLabel(),
                linePlotBuilder.getyAxisLabel());
        this.linePlotBuilder = linePlotBuilder;
    }

    public static AbstractPlot generatePlot(Rectangle bounds, LinePlotBuilder linePlotBuilder) {
        return new LinePlot(bounds, linePlotBuilder);
    }

    @Override
    public void preparePanel() {
        xAxisData = linePlotBuilder.getxData();
        minX = xAxisData[0];
        maxX = xAxisData[xAxisData.length - 1];

        yAxisData = linePlotBuilder.getyData();
        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (int i = 0; i < yAxisData.length; i++) {
            minY = StrictMath.min(minY, yAxisData[i]);
            maxY = StrictMath.max(maxY, yAxisData[i]);
        }

        displayOffsetX = 0.0;
        displayOffsetY = 0.0;

        prepareExtents();
        calculateTics();
        repaint();
    }

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);
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

    @Override
    public void plotData(GraphicsContext g2d) {
        g2d.setLineWidth(2.0);
        // new line plot
        g2d.setStroke(dataColor.color());
        g2d.beginPath();
        g2d.moveTo(mapX(xAxisData[0]), mapY(yAxisData[0]));
        for (int i = 0; i < xAxisData.length; i++) {
            if (pointInPlot(xAxisData[i], yAxisData[i])) {
                // line tracing through points
                g2d.lineTo(mapX(xAxisData[i]), mapY(yAxisData[i]));
            } else {
                // out of bounds
                g2d.moveTo(mapX(xAxisData[i]), mapY(yAxisData[i]));
            }
        }
        g2d.stroke();
    }

    @Override
    public void plotStats(GraphicsContext g2d) {

    }
}