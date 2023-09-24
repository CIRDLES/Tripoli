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
                75, 25,
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
    public void preparePanel(boolean reScaleX, boolean reScaleY) {
        xAxisData = histogramSessionRecord.blockIds();
        minX = 0.0;
        maxX = histogramSessionRecord.blockCount() + 1;

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

        prepareExtents(true, true);
        calculateTics();
        repaint();
    }

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);
    }

    public void prepareExtents(boolean reScaleX, boolean reScaleY) {
        double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.10);
        minY -= yMarginStretch;
        maxY += yMarginStretch;
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
                double dataYplusSigma = mapY(yAxisData[i] + oneSigma[i]);
                double dataYminusSigma = mapY(yAxisData[i] - oneSigma[i]);

                g2d.fillOval(dataX - 2.5, dataY - 2.5, 5, 5);
                g2d.strokeLine(dataX, dataY, dataX, dataYplusSigma);
                g2d.strokeLine(dataX, dataY, dataX, dataYminusSigma);
            }
        }
    }

    public void plotStats(GraphicsContext g2d) {

        Paint saveFill = g2d.getFill();
        // todo: promote color to constant
        g2d.setFill(Color.rgb(255, 251, 194));
        g2d.setGlobalAlpha(0.6);
        double mean = histogramSessionRecord.sessionMean();
        double stdDev = histogramSessionRecord.sessionOneSigma();

        double leftX = mapX(minX);
        if (leftX < leftMargin) leftX = leftMargin;
        double rightX = mapX(maxX);
        if (rightX > leftMargin + plotWidth) rightX = leftMargin + plotWidth;
        double plottedTwoSigmaHeight = Math.min(mapY(mean - stdDev), topMargin + plotHeight) - Math.max(mapY(mean + stdDev), topMargin);

        g2d.fillRect(leftX, Math.max(mapY(mean + stdDev), topMargin), rightX - leftX, plottedTwoSigmaHeight);
        g2d.setFill(saveFill);
        g2d.setGlobalAlpha(1.0);

        boolean meanIsPlottable = (mapY(mean) >= topMargin) && (mapY(mean) <= topMargin + plotHeight);
        if (meanIsPlottable) {
            g2d.setStroke(Color.RED);
            g2d.setLineWidth(1.0);
            g2d.strokeLine(leftX, mapY(mean), rightX, mapY(mean));
        }

    }
}