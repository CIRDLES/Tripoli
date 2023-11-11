/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.histograms.HistogramRecord;

/**
 * @author James F. Bowring
 */
public class HistogramSinglePlot extends AbstractPlot {

    protected HistogramRecord histogramRecord;
    protected double binWidth;
    private PlotWallPane parentWallPane;

    protected HistogramSinglePlot(Rectangle bounds, HistogramRecord histogramRecord, PlotWallPane parentWallPane) {
        super(bounds,
                40, 25,
                new String[]{histogramRecord.title()[0]
                        + "  " + "x\u0304" + "=" + String.format("%8.5g", histogramRecord.mean()).trim()
                        , "\u00B1" + String.format("%8.5g", histogramRecord.standardDeviation()).trim()},
                histogramRecord.xAxisLabel(),
                histogramRecord.yAxisLabel());
        this.histogramRecord = histogramRecord;
        this.parentWallPane = parentWallPane;
        // these can be changed by user in plot
        binWidth = histogramRecord.binWidth();
    }

    public static AbstractPlot generatePlot(Rectangle bounds, HistogramRecord histogramRecord, PlotWallPane parentWallPane) {
        return new HistogramSinglePlot(bounds, histogramRecord, parentWallPane);
    }

    public PlotWallPane getParentWallPane() {
        return parentWallPane;
    }

    @Override
    public void preparePanel(boolean reScaleX, boolean reScaleY) {
        xAxisData = histogramRecord.binCenters();
        minX = xAxisData[0];
        maxX = xAxisData[xAxisData.length - 1];

        yAxisData = histogramRecord.binCounts();
        // special case histogram
        minY = 0.0;
        maxY = -Double.MAX_VALUE;

        for (double yAxisDatum : yAxisData) {
            maxY = StrictMath.max(maxY, yAxisDatum);
        }

        displayOffsetX = 0.0;
        displayOffsetY = 0.0;

        prepareExtents(true, true);
        calculateTics();
        showYaxis = false;
        repaint();
    }

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);
    }

    public void prepareExtents(boolean reScaleX, boolean reScaleY) {
        double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.0);
        minX -= xMarginStretch;
        maxX += xMarginStretch;

        double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.10);
        maxY += yMarginStretch;
        // minY stays 0.0
    }

    /**
     * @param g2d
     */
    @Override
    public void showLegend(GraphicsContext g2d) {

    }

    @Override
    public void plotData(GraphicsContext g2d) {
        // plot bins
        g2d.setFill(dataColor.color());
        g2d.setLineWidth(2.0);
        boolean doFrameBins = false; //1.0 < (mapX(xAxisData[1]) - mapX(xAxisData[0]));

        for (int i = 0; i < xAxisData.length; i++) {
            double plottedBinWidth = mapX(xAxisData[1]) - mapX(xAxisData[0]);
            if (mapX(xAxisData[i] + binWidth / 2.0) > (leftMargin + plotWidth)) {
                plottedBinWidth = leftMargin + plotWidth - (mapX(xAxisData[i] - binWidth / 2.0));
            }
            if (mapX(xAxisData[i] - binWidth / 2.0) < (leftMargin)) {
                plottedBinWidth = mapX(xAxisData[i] + binWidth / 2.0) - leftMargin;
            }
            if (mapY(yAxisData[i]) <= (topMargin + plotHeight)) {
                g2d.fillRect(
                        Math.max(mapX(xAxisData[i] - binWidth / 2.0), leftMargin) + (doFrameBins ? 1.0 : 0.0),
                        Math.max(mapY(yAxisData[i]), topMargin),
                        plottedBinWidth - (doFrameBins ? 1.0 : -0.5),
                        Math.min(plotHeight,
                                Math.min(mapY(0.0) - topMargin,
                                        Math.min(mapY(0.0) - mapY(yAxisData[i]), topMargin + plotHeight - mapY(yAxisData[i])))));
            }
        }
    }

    public void plotStats(GraphicsContext g2d) {

        Paint saveFill = g2d.getFill();
        // copied from OGTripoli for giggles
        g2d.setFill(Color.rgb(255, 251, 194));
        g2d.setGlobalAlpha(0.6);
        double mean = histogramRecord.mean();
        double stdDev = histogramRecord.standardDeviation();
        double twoSigmaWidth = 2.0 * stdDev;
        double plottedTwoSigmaWidth = mapX(mean + stdDev) - mapX(mean - stdDev);
        if (mapX(mean + twoSigmaWidth / 2.0) > (leftMargin + plotWidth)) {
            plottedTwoSigmaWidth = leftMargin + plotWidth - (mapX(mean - twoSigmaWidth / 2.0));
        }
        if (mapX(mean - twoSigmaWidth / 2.0) < (leftMargin)) {
            plottedTwoSigmaWidth = mapX(mean + twoSigmaWidth / 2.0) - leftMargin;
        }
        if (mapY(maxY) <= (topMargin + plotHeight)) {
            g2d.fillRect(
                    Math.max(mapX(mean - twoSigmaWidth / 2.0), leftMargin),
                    Math.max(mapY(maxY), topMargin),
                    plottedTwoSigmaWidth,
                    Math.min(plotHeight,
                            Math.min(mapY(0.0) - topMargin,
                                    Math.min(mapY(0.0) - mapY(maxY), topMargin + plotHeight - mapY(maxY)))));
        }
        g2d.setFill(saveFill);
        g2d.setGlobalAlpha(1.0);

        Paint saveStroke = g2d.getStroke();
        double saveLineWidth = g2d.getLineWidth();
        g2d.setLineWidth(1.0);
        g2d.setStroke(Paint.valueOf("Black"));
        g2d.strokeLine(mapX(histogramRecord.mean()), Math.min(mapY(0.0), mapY(minY)), mapX(histogramRecord.mean()), Math.max(mapY(maxY), topMargin));
        g2d.setStroke(saveStroke);
        g2d.setLineWidth(saveLineWidth);
    }
}