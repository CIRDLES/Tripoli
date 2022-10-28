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

package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.visualizationUtilities.histograms.HistogramRecord;

/**
 * @author James F. Bowring
 */
public class HistogramSinglePlot extends AbstractPlot {

    private final HistogramRecord histogramRecord;
    private double binWidth;
    private boolean doFrameBins;

    private HistogramSinglePlot(Rectangle bounds, HistogramRecord histogramRecord) {
        super(bounds, 40, 25, histogramRecord.title(), histogramRecord.xAxisLabel(), histogramRecord.yAxisLabel());
        this.histogramRecord = histogramRecord;
    }

    public static AbstractPlot generatePlot(Rectangle bounds, HistogramRecord histogramRecord){
        return new HistogramSinglePlot(bounds, histogramRecord);
    }

    @Override
    public void preparePanel() {
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

        dataColor = histogramRecord.dataColor();

        prepareExtents();
        calculateTics();
        this.repaint();
    }

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);

        // plot bins
        g2d.setFill(dataColor);
        g2d.setLineWidth(2.0);
        doFrameBins = (mapX(xAxisData[1]) - mapX(xAxisData[0])) > 1.0;
        binWidth = histogramRecord.binWidth();
        plotData(g2d);
    }

    public void prepareExtents() {
        double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.25);
        minX -= xMarginStretch;
        maxX += xMarginStretch;

        double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.25);
        maxY += yMarginStretch;
        // minY stays 0.0
    }

    @Override
    public void plotData(GraphicsContext g2d) {
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
}