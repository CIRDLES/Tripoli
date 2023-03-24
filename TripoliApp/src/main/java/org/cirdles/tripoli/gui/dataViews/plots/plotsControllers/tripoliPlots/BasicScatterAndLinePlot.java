package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.linePlots.ComboPlotBuilder;

import java.util.ArrayList;
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

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);
    }

    public void plotData(GraphicsContext g2d) {
        // scatter plot
        g2d.setLineWidth(0.75);
        g2d.setStroke(Paint.valueOf("Black"));
        for (int i = 0; i < xAxisData.length; i += plottingStep) {
            if (pointInPlot(xAxisData[i], yAxisData[i])) {
                g2d.strokeOval(mapX(xAxisData[i]) - 2.0f, mapY(yAxisData[i]) - 2.0f, 4.0f, 4.0f);
            }
        }

        g2d.setFont(Font.font("SansSerif", 18));
        if (!comboPlotBuilder.getBlockMapOfIdsToData().isEmpty()) {
            int colorIndex = 0;
            List<Double> xAxisDataList = new ArrayList<>();
            for (double d : xAxisData) xAxisDataList.add(d);
            Collections.sort(xAxisDataList);
            for (String sequenceID : comboPlotBuilder.getBlockMapOfIdsToData().keySet()) {
                g2d.setFill(Paint.valueOf(TRIPOLI_PALLETTE_FOUR[colorIndex]));
                List<Double> timeList = comboPlotBuilder.getBlockMapOfIdsToData().get(sequenceID);
                Collections.sort(timeList);
                for (double time : timeList) {
                    int timeIndex = xAxisDataList.indexOf(time);
                    do {
                        if ((0 == timeIndex % plottingStep) && pointInPlot(xAxisData[timeIndex], yAxisData[timeIndex])) {
                            g2d.fillOval(mapX(xAxisData[timeIndex]) - 2.0f, mapY(yAxisData[timeIndex]) - 2.0f, 4.0f, 4.0f);
                        }
                        timeIndex++;
                    } while ((timeIndex < xAxisData.length) && xAxisData[timeIndex - 1] == xAxisData[timeIndex]);
                }

                // legend
                g2d.fillText(sequenceID, leftMargin + 10, topMargin + 20 * (colorIndex + 1));
                colorIndex++;
            }

        }

        // new line plot from yAxisData2
        g2d.setStroke(Paint.valueOf("red"));
        g2d.beginPath();
        g2d.moveTo(mapX(xAxisData[0]), mapY(yAxisData2[0]));
        for (int i = 0; i < xAxisData.length; i += plottingStep) {
            if (pointInPlot(xAxisData[i], yAxisData2[i])) {
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
            g2d.moveTo(mapX(xAxisData[0]), mapY(-yAxisData2[0]));
            for (int i = 0; i < xAxisData.length; i += plottingStep) {
                if (pointInPlot(xAxisData[i], -yAxisData2[i])) {
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