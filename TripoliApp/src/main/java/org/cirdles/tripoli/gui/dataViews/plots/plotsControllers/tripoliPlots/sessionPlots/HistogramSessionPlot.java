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

package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.sessionPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.sessionPlots.HistogramSessionRecord;

/**
 * @author James F. Bowring
 */
public class HistogramSessionPlot extends AbstractPlot {

    private final HistogramSessionRecord histogramSessionRecord;
    private double[] oneSigma;

    private HistogramSessionPlot(Rectangle bounds, HistogramSessionRecord histogramSessionRecord) {
        super(bounds,
                40, 25,
                new String[]{histogramSessionRecord.title()[0]
                        + "  " + "X\u0305" + "=" + String.format("%8.5g", histogramSessionRecord.sessionMean()).trim()
                        , "\u00B1" + String.format("%8.5g", histogramSessionRecord.sessionOneSigma()).trim()},
                histogramSessionRecord.xAxisLabel(),
                histogramSessionRecord.yAxisLabel());
        this.histogramSessionRecord = histogramSessionRecord;
    }

    public static AbstractPlot generatePlot(Rectangle bounds, HistogramSessionRecord histogramSessionRecord) {
        return new HistogramSessionPlot(bounds, histogramSessionRecord);
    }

    @Override
    public void preparePanel() {
        xAxisData = histogramSessionRecord.blockIds();
        minX = 0.0;
        maxX = xAxisData[xAxisData.length - 1] + 1;

        yAxisData = histogramSessionRecord.blockMeans();
        oneSigma = histogramSessionRecord.blockOneSigmas();
        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (int i = 0; i < yAxisData.length; i++) {
            minY = StrictMath.min(minY, yAxisData[i] - oneSigma[i]);
            maxY = StrictMath.max(maxY, yAxisData[i] + oneSigma[i]);
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
        double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.10);
        minY -= yMarginStretch;
        maxY += yMarginStretch;
    }

    @Override
    public void plotData(GraphicsContext g2d) {
        g2d.setFill(dataColor.color());
        g2d.setLineWidth(1.0);

        for (int i = 0; i < xAxisData.length; i++) {
            g2d.fillOval(mapX(xAxisData[i]) - 2.5, mapY(yAxisData[i]) - 2.5, 5, 5);
            g2d.strokeLine(mapX(xAxisData[i]), mapY(yAxisData[i]), mapX(xAxisData[i]), mapY(yAxisData[i] + oneSigma[i]));
            g2d.strokeLine(mapX(xAxisData[i]), mapY(yAxisData[i]), mapX(xAxisData[i]), mapY(yAxisData[i] - oneSigma[i]));
        }
    }

    public void plotStats(GraphicsContext g2d) {

        Paint saveFill = g2d.getFill();
        g2d.setFill(Color.rgb(255, 251, 194));
        g2d.setGlobalAlpha(0.6);
        double mean = histogramSessionRecord.sessionMean();
        double stdDev = histogramSessionRecord.sessionOneSigma();
//        double twoSigmaWidth = 2.0 * stdDev;
        double plottedTwoSigmaHeight = mapY(mean - stdDev) - mapY(mean + stdDev);
//        if (mapX(mean + twoSigmaWidth / 2.0) > (leftMargin + plotWidth)) {
//            plottedTwoSigmaWidth = leftMargin + plotWidth - (mapX(mean - twoSigmaWidth / 2.0));
//        }
//        if (mapX(mean - twoSigmaWidth / 2.0) < (leftMargin)) {
//            plottedTwoSigmaWidth = mapX(mean + twoSigmaWidth / 2.0) - leftMargin;
//        }
//        if (mapY(maxY) <= (topMargin + plotHeight)) {
//            g2d.fillRect(
//                    Math.max(mapX(mean - twoSigmaWidth / 2.0), leftMargin),
//                    Math.max(mapY(maxY), topMargin),
//                    plottedTwoSigmaWidth,
//                    Math.min(plotHeight,
//                            Math.min(mapY(0.0) - topMargin,
//                                    Math.min(mapY(0.0) - mapY(maxY), topMargin + plotHeight - mapY(maxY)))));
//        }

        g2d.fillRect(mapX(0.0), mapY(mean + stdDev), mapX(xAxisData[xAxisData.length - 1] + 1.0) - mapX(0.0), plottedTwoSigmaHeight);
        g2d.setFill(saveFill);
        g2d.setGlobalAlpha(1.0);

        g2d.setStroke(Color.RED);
        g2d.setLineWidth(1.0);
        g2d.strokeLine(mapX(0.0), mapY(histogramSessionRecord.sessionMean()), mapX(xAxisData[xAxisData.length - 1] + 1.0), mapY(histogramSessionRecord.sessionMean()));

    }
}