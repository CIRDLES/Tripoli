package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.linePlots.MultiLinePlotBuilder;

import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.TRIPOLI_MOVING_SHADE;


public class MultiLineIntensityPlot extends AbstractPlot {

    private double[][] xData;
    private double[][] yData;

    private PlotWallPane parentWallPane;

    /**
     * @param bounds
     * @param plotBuilder
     */
    private MultiLineIntensityPlot(Rectangle bounds, MultiLinePlotBuilder plotBuilder, PlotWallPane parentWallPane) {
        super(bounds, 75, 25,
                plotBuilder.getTitle(),
                plotBuilder.getxAxisLabel(),
                plotBuilder.getyAxisLabel());
        this.plotBuilder = plotBuilder;
        this.parentWallPane = parentWallPane;
    }

    public static AbstractPlot generatePlot(Rectangle bounds, MultiLinePlotBuilder multiLinePlotBuilder, PlotWallPane parentWallPane) {
        return new MultiLineIntensityPlot(bounds, multiLinePlotBuilder, parentWallPane);
    }

    public PlotWallPane getParentWallPane() {
        return parentWallPane;
    }

    @Override
    public void preparePanel(boolean reScaleX, boolean reScaleY) {
        xData = ((MultiLinePlotBuilder) plotBuilder).getxData();
        xAxisData = xData[0];
        minX = (xData[0][0]);
        maxX = (xData[0][xData[0].length - 1]);

        yData = ((MultiLinePlotBuilder) plotBuilder).getyData();
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

    public void prepareExtents(boolean reScaleX, boolean reScaleY) {
        double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.01);
        if (0.0 == xMarginStretch) {
            xMarginStretch = maxX * 0.01;
        }
        minX -= xMarginStretch;
        maxX += xMarginStretch;

        double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.1);
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
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);
    }

    @Override
    public void plotData(GraphicsContext g2d) {
        Text text = new Text();
        text.setFont(Font.font("SansSerif", 12));
        int textWidth = 0;

        g2d.setLineWidth(2.0);
        // new line plots
        // specify cool heatmap
        double rgbStartRed = 151.0 / 255.0;
        double rgbStartGreen = 248.0 / 255.0;
        double rgbStartBlue = 253.0 / 255.0;
        double rgbEndRed = 231.0 / 255.0;
        double rgbEndGreen = 56.0 / 255.0;
        double rgbEndBlue = 244.0 / 255.0;
        double redDelta = (rgbEndRed - rgbStartRed) / yData.length;
        double greenDelta = (rgbEndGreen - rgbStartGreen) / yData.length;
        double blueDelta = (rgbEndBlue - rgbStartBlue) / yData.length;

        for (int y = 0; y < yData.length; y++) {
            g2d.setStroke(Color.color(rgbStartRed + redDelta * y, rgbStartGreen + greenDelta * y, rgbStartBlue + blueDelta * y));
            g2d.beginPath();
            boolean startedPlot = false;
            for (int i = 0; i < xAxisData.length; i++) {
                if (pointInPlot((xAxisData[i]), yData[y][i])) {
                    if (!startedPlot) {
                        g2d.moveTo(mapX((xAxisData[i])), mapY(yData[y][i]));
                        startedPlot = true;
                    }
                    // line tracing through points
                    g2d.lineTo(mapX((xAxisData[i])), mapY(yData[y][i]));
                } else {
                    // out of bounds
                    g2d.moveTo(mapX((xAxisData[i])), mapY(yData[y][i]) < topMargin ? topMargin : topMargin + plotHeight);
                }
            }
            g2d.stroke();
        }
    }

    /**
     * @param g2d
     */
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