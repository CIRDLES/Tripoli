package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;

import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.TRIPOLI_MOVING_SHADE;

public class LinePlot extends AbstractPlot {

    private PlotWallPane parentWallPane;

    private LinePlot(Rectangle bounds, LinePlotBuilder plotBuilder, PlotWallPane parentWallPane) {
        super(bounds, 75, 25,
                plotBuilder.getTitle(),
                plotBuilder.getxAxisLabel(),
                plotBuilder.getyAxisLabel());
        this.plotBuilder = plotBuilder;
        this.parentWallPane = parentWallPane;
    }

    public static AbstractPlot generatePlot(Rectangle bounds, LinePlotBuilder linePlotBuilder, PlotWallPane parentWallPane) {
        return new LinePlot(bounds, linePlotBuilder, parentWallPane);
    }

    public PlotWallPane getParentWallPane() {
        return parentWallPane;
    }

    @Override
    public void preparePanel(boolean reScaleX, boolean reScaleY) {
        xAxisData = ((LinePlotBuilder) plotBuilder).getxData();
        minX = xAxisData[0];
        maxX = xAxisData[xAxisData.length - 1];

        yAxisData = ((LinePlotBuilder) plotBuilder).getyData();
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

    @Override
    public void plotData(GraphicsContext g2d) {
        g2d.setLineWidth(1.0);
        // new line plot
        g2d.setStroke(dataColor.color());
        g2d.beginPath();
        boolean startedPlot = false;
        for (int i = 0; i < xAxisData.length; i++) {
            if (pointInPlot(xAxisData[i], yAxisData[i])) {
                if (!startedPlot) {
                    g2d.moveTo(mapX(xAxisData[i]), mapY(yAxisData[i]));
                    startedPlot = true;
                }
                // line tracing through points
                g2d.lineTo(mapX(xAxisData[i]), mapY(yAxisData[i]));
            } else {
                // out of bounds
                g2d.moveTo(mapX(xAxisData[i]), mapY(yAxisData[i]) < topMargin ? topMargin : topMargin + plotHeight);
            }
        }
        g2d.stroke();
    }

    @Override
    public void plotStats(GraphicsContext g2d) {

    }

    public void plotLeftShade(GraphicsContext g2d) {
        g2d.setFill(TRIPOLI_MOVING_SHADE);
        g2d.fillRect(mapX(0), mapY(maxY), mapX(plotBuilder.getShadeWidthForModelConvergence()) - mapX(0), mapY(minY) - mapY(maxY));

        g2d.setFill(Color.RED);
        g2d.fillArc(mapX(plotBuilder.getShadeWidthForModelConvergence()) - 40, (mapY(minY) - mapY(maxY)) / 2 + mapY(maxY) - 20, 40, 40, -75, 150, ArcType.CHORD);
    }

}