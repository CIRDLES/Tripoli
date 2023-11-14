package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.linePlots.MultiLinePlotBuilder;


public class MultiLinePlot extends AbstractPlot {

    private final MultiLinePlotBuilder multiLinePlotBuilder;
    private double[][] xData;
    private double[][] yData;

    /**
     * @param bounds
     * @param multiLinePlotBuilder
     */
    private MultiLinePlot(Rectangle bounds, MultiLinePlotBuilder multiLinePlotBuilder) {
        super(bounds, 75, 25,
                multiLinePlotBuilder.getTitle(),
                multiLinePlotBuilder.getxAxisLabel(),
                multiLinePlotBuilder.getyAxisLabel());
        this.multiLinePlotBuilder = multiLinePlotBuilder;
    }

    public static AbstractPlot generatePlot(Rectangle bounds, MultiLinePlotBuilder multiLinePlotBuilder) {
        return new MultiLinePlot(bounds, multiLinePlotBuilder);
    }

    @Override
    public void preparePanel(boolean reScaleX, boolean reScaleY) {
        xData = multiLinePlotBuilder.getxData();
        xAxisData = xData[0];
        minX = xAxisData[0];
        maxX = xAxisData[xAxisData.length - 1];

        yData = multiLinePlotBuilder.getyData();
        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (int i = 0; i < yData.length; i++) {
            for (int j = 0; j < yData[i].length; j++) {
                minY = StrictMath.min(minY, yData[i][j]);
                maxY = StrictMath.max(maxY, yData[i][j]);
            }
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
        // new line plots
        g2d.setLineWidth(1.0);
        g2d.setStroke(dataColor.color());
        boolean isMarkerInLastLine = multiLinePlotBuilder.isMarkerInLastLine();

        for (int lineIndex = 0; lineIndex < yData.length - (isMarkerInLastLine ? 1 : 0); lineIndex++) {
            g2d.setLineDashes(8);
            g2d.beginPath();
            g2d.moveTo(mapX(xData[lineIndex][0]), mapY(yData[lineIndex][0]));
            for (int i = 0; i < xData[lineIndex].length - (isMarkerInLastLine ? 1 : 0); i++) {
                if (pointInPlot(xData[lineIndex][i], yData[lineIndex][i])) {
                    // line tracing through points
                    g2d.lineTo(mapX(xData[lineIndex][i]), mapY(yData[lineIndex][i]));
                } else {
                    // out of bounds
                    g2d.moveTo(mapX(xData[lineIndex][i]), mapY(yData[lineIndex][i]));
                }
            }
            g2d.stroke();
        }

        if (isMarkerInLastLine) {
            int markerDataIndex = xData.length - 1;
            for (int i = 0; i < xData[markerDataIndex].length; i++) {
                if (pointInPlot(xData[markerDataIndex][i], yData[markerDataIndex][i])) {
                    g2d.setFill(Color.RED);
                    g2d.setLineWidth(2.0);
                    g2d.fillOval(mapX(xData[markerDataIndex][i]) - 2.5, mapY(yData[markerDataIndex][i]) - 2.5, 5.0, 5.0);
                }
            }
        }
        g2d.setLineDashes(0);
    }


    @Override
    public void plotStats(GraphicsContext g2d) {

    }
}