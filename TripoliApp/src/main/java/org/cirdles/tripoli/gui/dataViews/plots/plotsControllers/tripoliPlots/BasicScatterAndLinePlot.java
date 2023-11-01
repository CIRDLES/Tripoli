package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.linePlots.ComboPlotBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.TRIPOLI_PALLETTE_FOUR;


public class BasicScatterAndLinePlot extends AbstractPlot {

    private final ComboPlotBuilder comboPlotBuilder;
    private final int plottingStep;
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
        plottingStep = 10;
    }

    public static AbstractPlot generatePlot(Rectangle bounds, ComboPlotBuilder comboPlotBuilder) {
        return new BasicScatterAndLinePlot(bounds, comboPlotBuilder);
    }

    @Override
    public void preparePanel(boolean reScaleX, boolean reScaleY) {
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
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);
    }

    public void plotData(GraphicsContext g2d) {
        // scatter plot
        Arrays.sort(xAxisData);

        g2d.setLineWidth(1.25);
        g2d.setStroke(Paint.valueOf("Black"));
        for (int i = 0; i < xAxisData.length; i += plottingStep) {
            if (pointInPlot(xAxisData[i], yAxisData[i])) {
                g2d.strokeOval(mapX(xAxisData[i]) - 2.0f, mapY(yAxisData[i]) - 2.0f, 4.0f, 4.0f);
            }
        }

        g2d.setFont(Font.font("SansSerif", 18));
        if (!comboPlotBuilder.getBlockMapOfIdsToData().isEmpty()) {
            int colorIndex = 0;
            for (String sequenceID : comboPlotBuilder.getBlockMapOfIdsToData().keySet()) {
                g2d.setFill(Paint.valueOf(TRIPOLI_PALLETTE_FOUR[colorIndex]));
                List<Double> timeList = comboPlotBuilder.getBlockMapOfIdsToData().get(sequenceID);
                Collections.sort(timeList);
                int countOfSequences = comboPlotBuilder.getBlockMapOfIdsToData().keySet().size();
                for (double time : timeList) {
                    // binarySearch does not guarantee which of equals it chooses
                    int timeIndex = Arrays.binarySearch(xAxisData, time);
                    // handle repeated time values due to multiple sequences
                    int timeIndexPlottable = 0;
                    for (int timeIndex2 = timeIndex - countOfSequences; timeIndex2 < timeIndex + countOfSequences; timeIndex2++) {
                        if ((0 <= timeIndex2) && (timeIndex2 < xAxisData.length) && (xAxisData[timeIndex2] == xAxisData[timeIndex]) && (0 == timeIndex2 % plottingStep)) {
                            timeIndexPlottable = timeIndex2;
                            break;
                        }
                    }
                    if (pointInPlot(xAxisData[timeIndexPlottable], yAxisData[timeIndexPlottable])) {
                        try {
                            g2d.fillOval(mapX(xAxisData[timeIndexPlottable]) - 2.0f, mapY(yAxisData[timeIndexPlottable]) - 2.0f, 4.0f, 4.0f);
                        } catch (Exception e) {
                            System.err.println("Bad time at timeIndexPlottable = " + timeIndexPlottable);
                        }
                    }
                }

                // legend
                g2d.fillText(sequenceID, leftMargin + 10, topMargin + 20 * (colorIndex + 1));
                colorIndex++;
            }

        }

        // new line plot from yAxisData2
        g2d.setStroke(Paint.valueOf("red"));
        g2d.beginPath();
        boolean startedPlot = false;
        for (int i = 0; i < xAxisData.length; i += plottingStep) {
            if (pointInPlot(xAxisData[i], yAxisData2[i])) {
                if (!startedPlot) {
                    g2d.moveTo(mapX(xAxisData[i]), mapY(yAxisData2[i]));
                    startedPlot = true;
                }
                // line tracing through points
                g2d.lineTo(mapX(xAxisData[i]), mapY(yAxisData2[i]));
            } else {
                // out of bounds
                g2d.moveTo(mapX(xAxisData[i]), mapY(yAxisData2[i]) < topMargin ? topMargin : topMargin + plotHeight);
            }
        }
        g2d.stroke();

        if (comboPlotBuilder.isyData2OneSigma()) {
            g2d.beginPath();
            startedPlot = false;
            for (int i = 0; i < xAxisData.length; i += plottingStep) {
                if (pointInPlot(xAxisData[i], -yAxisData2[i])) {
                    if (!startedPlot) {
                        g2d.moveTo(mapX(xAxisData[i]), mapY(-yAxisData2[i]));
                        startedPlot = true;
                    }
                    // line tracing through points
                    g2d.lineTo(mapX(xAxisData[i]), mapY(-yAxisData2[i]));
                } else {
                    // out of bounds
                    g2d.moveTo(mapX(xAxisData[i]), mapY(-yAxisData2[i]) < topMargin ? topMargin : topMargin + plotHeight);
                }
            }
            g2d.stroke();
        }
    }

    @Override
    public void plotStats(GraphicsContext g2d) {

    }
}