package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;


public class BasicScatterPlot extends AbstractPlot {

    private final LinePlotBuilder intensityLinePlotBuilder;

    /**
     * @param bounds
     * @param intensityLinePlotBuilder
     */
    private BasicScatterPlot(Rectangle bounds, LinePlotBuilder intensityLinePlotBuilder) {
        super(bounds, 75, 25,
                intensityLinePlotBuilder.getTitle(),
                intensityLinePlotBuilder.getxAxisLabel(),
                intensityLinePlotBuilder.getyAxisLabel());
        ;
        this.intensityLinePlotBuilder = intensityLinePlotBuilder;
    }

    public static AbstractPlot generatePlot(Rectangle bounds, LinePlotBuilder intensityLinePlotBuilder) {
        return new BasicScatterPlot(bounds, intensityLinePlotBuilder);
    }

    @Override
    public void preparePanel(boolean reScaleX, boolean reScaleY) {
        xAxisData = intensityLinePlotBuilder.getxData();
        minX = xAxisData[0];
        maxX = xAxisData[xAxisData.length - 1];

        yAxisData = intensityLinePlotBuilder.getyData();
        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (int i = 0; i < yAxisData.length; i++) {
            minY = StrictMath.min(minY, yAxisData[i]);
            maxY = StrictMath.max(maxY, yAxisData[i]);
        }

        displayOffsetX = 0.0;
        displayOffsetY = 0.0;

        prepareExtents(true, true);
        calculateTics();
        repaint();
    }

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);
    }

    public void prepareExtents(boolean reScaleX, boolean reScaleY) {
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

    /**
     * @param g2d
     */
    @Override
    public void showLegend(GraphicsContext g2d) {

    }

    @Override
    public void plotData(GraphicsContext g2d) {
        g2d.setFill(dataColor.color());
        g2d.setStroke(dataColor.color());
        g2d.setLineWidth(1.0);

        for (int i = 0; i < xAxisData.length; i++) {
            if (pointInPlot(xAxisData[i], yAxisData[i])) {
                double dataX = mapX(xAxisData[i]);
                double dataY = mapY(yAxisData[i]);
//                double dataYplusSigma = mapY(yAxisData[i] + oneSigma[i]);
//                double dataYminusSigma = mapY(yAxisData[i] - oneSigma[i]);

                g2d.fillOval(dataX - 2.5, dataY - 2.5, 5, 5);
//                g2d.strokeLine(dataX, dataY, dataX, dataYplusSigma);
//                g2d.strokeLine(dataX, dataY, dataX, dataYminusSigma);
            }
        }
    }


    /**
     * @param g2d
     */
    @Override
    public void plotStats(GraphicsContext g2d) {

    }
}