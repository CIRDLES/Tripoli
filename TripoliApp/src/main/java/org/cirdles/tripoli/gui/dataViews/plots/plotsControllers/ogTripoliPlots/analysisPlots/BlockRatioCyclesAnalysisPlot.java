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

package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.analysisPlotBuilders.BlockAnalysisRatioCyclesRecord;
import org.cirdles.tripoli.plots.compoundPlotBuilders.BlockRatioCyclesRecord;

import java.util.Map;

import static java.lang.StrictMath.log;

/**
 * @author James F. Bowring
 */
public class BlockRatioCyclesAnalysisPlot extends AbstractPlot {
    private final BlockAnalysisRatioCyclesRecord blockAnalysisRatioCyclesRecord;
    private Map<Integer, BlockRatioCyclesRecord> mapBlockIdToBlockRatioCyclesRecord;
    private double[] oneSigmaForCycles;
    private double sessionMean;
    private double sessionOneSigmaAbs;
    private double sessionDalyFaradayGainMean;
    private double sessionDalyFaradayGainOneSigmaAbs;
    private boolean logScale;
    private boolean[] zoomFlagsXY;
    private PlotWallPane parentWallPane;

    private BlockRatioCyclesAnalysisPlot(Rectangle bounds, BlockAnalysisRatioCyclesRecord blockAnalysisRatioCyclesRecord, PlotWallPane parentWallPane) {
        super(bounds,
                75, 25,
                new String[]{blockAnalysisRatioCyclesRecord.title()[0]
                        + "  " + "X\u0305" + "=" + String.format("%8.8g", blockAnalysisRatioCyclesRecord.analysisMean()).trim()
                        , "\u00B1" + String.format("%8.5g", blockAnalysisRatioCyclesRecord.analysisOneSigma()).trim()},
                blockAnalysisRatioCyclesRecord.xAxisLabel(),
                blockAnalysisRatioCyclesRecord.yAxisLabel());
        this.blockAnalysisRatioCyclesRecord = blockAnalysisRatioCyclesRecord;
        this.logScale = false;
        this.zoomFlagsXY = new boolean[]{true, true};
        this.parentWallPane = parentWallPane;
    }

    public static AbstractPlot generatePlot(Rectangle bounds, BlockAnalysisRatioCyclesRecord blockAnalysisRatioCyclesRecord, PlotWallPane parentWallPane) {
        return new BlockRatioCyclesAnalysisPlot(bounds, blockAnalysisRatioCyclesRecord, parentWallPane);
    }

    public BlockAnalysisRatioCyclesRecord getBlockRatioCyclesSessionRecord() {
        return blockAnalysisRatioCyclesRecord;
    }

    public PlotWallPane getParentWallPane() {
        return parentWallPane;
    }

    public void setLogScale(boolean logScale) {
        this.logScale = logScale;
    }

    public void setZoomFlagsXY(boolean[] zoomFlagsXY) {
        this.zoomFlagsXY = zoomFlagsXY;
    }

    public Map<Integer, BlockRatioCyclesRecord> getMapBlockIdToBlockRatioCyclesRecord() {
        return mapBlockIdToBlockRatioCyclesRecord;
    }

    @Override
    public void preparePanel(boolean reScaleX, boolean reScaleY) {
        // process blocks
        mapBlockIdToBlockRatioCyclesRecord = blockAnalysisRatioCyclesRecord.mapBlockIdToBlockRatioCyclesRecord();
        int cyclesPerBlock = blockAnalysisRatioCyclesRecord.cyclesPerBlock();

        if (reScaleX) {
            xAxisData = new double[mapBlockIdToBlockRatioCyclesRecord.size() * cyclesPerBlock];
            for (int i = 0; i < xAxisData.length; i++) {
                xAxisData[i] = i + 1;
            }

            displayOffsetX = 0.0;

            minX = 1.0;
            maxX = xAxisData.length;
        }

        yAxisData = new double[mapBlockIdToBlockRatioCyclesRecord.size() * cyclesPerBlock];
        oneSigmaForCycles = new double[mapBlockIdToBlockRatioCyclesRecord.size() * cyclesPerBlock];
        for (Map.Entry<Integer, BlockRatioCyclesRecord> entry : mapBlockIdToBlockRatioCyclesRecord.entrySet()) {
            BlockRatioCyclesRecord blockRatioCyclesRecord = entry.getValue();
            if (blockRatioCyclesRecord != null) {
                int availableCyclesPerBlock = blockRatioCyclesRecord.cycleRatioMeansData().length;
                System.arraycopy(blockRatioCyclesRecord.cycleRatioMeansData(), 0, yAxisData, (blockRatioCyclesRecord.blockID() - 1) * cyclesPerBlock, availableCyclesPerBlock);
                System.arraycopy(blockRatioCyclesRecord.cycleRatioOneSigmaData(), 0, oneSigmaForCycles, (blockRatioCyclesRecord.blockID() - 1) * cyclesPerBlock, availableCyclesPerBlock);
            }
        }

        plotAxisLabelY = "Ratio";
        if (logScale) {
            for (int i = 0; i < yAxisData.length; i++) {
                yAxisData[i] = (yAxisData[i] > 0.0) ? log(yAxisData[i]) : 0.0;
                // TODO: uncertainties for logs
                oneSigmaForCycles[i] = 0.0;
            }
            plotAxisLabelY = "Log Ratio";
        }

        if (reScaleY) {
            // calculate ratio and unct across all included blocks
            minY = Double.MAX_VALUE;
            maxY = -Double.MAX_VALUE;

            for (int i = 0; i < yAxisData.length; i++) {
                int blockID = (i / mapBlockIdToBlockRatioCyclesRecord.get(1).cyclesIncluded().length) + 1;
                // TODO: handle logratio uncertainties
                if ((mapBlockIdToBlockRatioCyclesRecord.get(blockID) != null)) {
                    minY = StrictMath.min(minY, yAxisData[i] - oneSigmaForCycles[i]);
                    maxY = StrictMath.max(maxY, yAxisData[i] + oneSigmaForCycles[i]);
                }
            }

            displayOffsetY = 0.0;


        }
        prepareExtents(reScaleX, reScaleY);
        showXaxis = false;
        showStats = true;
        calcStats();
        calculateTics();
        repaint();
    }

    @Override
    public void calculateTics() {
        super.calculateTics();
        zoomChunkX = zoomFlagsXY[0] ? zoomChunkX : 0.0;
        zoomChunkY = zoomFlagsXY[1] ? zoomChunkY : 0.0;
    }

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);
    }

    public void prepareExtents(boolean reScaleX, boolean reScaleY) {
        if (reScaleX) {
            minX -= 2;
            maxX += 2;
        }

        if (reScaleY) {
            double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.10);
            if (yMarginStretch == 0.0) {
                yMarginStretch = yAxisData[0] / 100.0;
            }
            minY -= yMarginStretch;
            maxY += yMarginStretch;
        }
    }

    @Override
    public void plotData(GraphicsContext g2d) {
        g2d.setFill(dataColor.color());
        g2d.setStroke(dataColor.color());
        g2d.setLineWidth(1.0);

        int cyclesPerBlock = blockAnalysisRatioCyclesRecord.cyclesPerBlock();

        for (int i = 0; i < xAxisData.length; i++) {
            if (pointInPlot(xAxisData[i], yAxisData[i])) {
                int blockID = (int) ((xAxisData[i] - 0.7) / cyclesPerBlock) + 1;
                if (mapBlockIdToBlockRatioCyclesRecord.get(blockID) != null) {
                    g2d.setFill(dataColor.color());
                    g2d.setStroke(dataColor.color());
                    if (!mapBlockIdToBlockRatioCyclesRecord.get(blockID).blockIncluded()) {
                        g2d.setFill(Color.RED);
                        g2d.setStroke(Color.RED);
                    }
                    double dataX = mapX(xAxisData[i]);
                    double dataY = mapY(yAxisData[i]);
                    double dataYplusSigma = mapY(yAxisData[i] + oneSigmaForCycles[i]);
                    double dataYminusSigma = mapY(yAxisData[i] - oneSigmaForCycles[i]);

                    if (yAxisData[i] > 0) {
                        g2d.fillOval(dataX - 2.0, dataY - 2.0, 4, 4);
                        g2d.strokeLine(dataX, dataY, dataX, dataYplusSigma);
                        g2d.strokeLine(dataX, dataY, dataX, dataYminusSigma);
                    } else {
                        g2d.strokeOval(dataX - 2.0, dataY - 2.0, 4, 4);
                    }
                }
            }
        }
        // block delimiters
        g2d.setStroke(Color.BLACK);
        g2d.setLineWidth(0.5);
        int blockID = 1;
        for (int i = 0; i < xAxisData.length; i += cyclesPerBlock) {
            if (xInPlot(xAxisData[i])) {
                double dataX = mapX(xAxisData[i] - 0.5);
                g2d.strokeLine(dataX, topMargin + plotHeight, dataX, topMargin);
                showBlockID(g2d, blockID, mapX(xAxisData[i]));
            }
            blockID++;
        }
        double dataX = mapX(xAxisData[xAxisData.length - 1] + 0.5);
        g2d.strokeLine(dataX, topMargin + plotHeight, dataX, topMargin);

    }

    private void showBlockID(GraphicsContext g2d, int blockID, double xPosition) {
        boolean processed = (mapBlockIdToBlockRatioCyclesRecord.get(blockID) != null) ? mapBlockIdToBlockRatioCyclesRecord.get(blockID).processed() : false;
        Paint savedPaint = g2d.getFill();
        if (processed) {
            g2d.setFill(Paint.valueOf("GREEN"));
        } else {
            g2d.setFill(Paint.valueOf("BLACK"));
        }

        g2d.setFont(Font.font("SansSerif", 10));

        g2d.fillText("BL#" + Integer.toString(blockID), xPosition, topMargin + plotHeight + 10);
        g2d.setFill(savedPaint);
    }

    public void plotStats(GraphicsContext g2d) {
        calcStats();

        Paint saveFill = g2d.getFill();
        // TODO: promote color to constant
        g2d.setFill(Color.rgb(255, 251, 194));
        g2d.setGlobalAlpha(0.6);
        double mean = sessionMean;
        double stdDev = sessionOneSigmaAbs;

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

    public void calcStats() {
        DescriptiveStatistics descriptiveStatsIncludedCycles = new DescriptiveStatistics();
        DescriptiveStatistics descriptiveStatsIncludedDFGains = new DescriptiveStatistics();
        for (int i = 0; i < yAxisData.length; i++) {
            int blockID = (i / mapBlockIdToBlockRatioCyclesRecord.get(1).cyclesIncluded().length) + 1;
            if (mapBlockIdToBlockRatioCyclesRecord.get(blockID) != null) {
                if (mapBlockIdToBlockRatioCyclesRecord.get(blockID).blockIncluded() && (yAxisData[i] != 0)) {
                    descriptiveStatsIncludedCycles.addValue(yAxisData[i]);
                }
                if (mapBlockIdToBlockRatioCyclesRecord.get(blockID).blockIncluded()) {
                    descriptiveStatsIncludedDFGains.addValue(mapBlockIdToBlockRatioCyclesRecord.get(blockID).dalyFaradayGain());
                }
            }
        }
        sessionMean = descriptiveStatsIncludedCycles.getMean();

        sessionOneSigmaAbs = descriptiveStatsIncludedCycles.getStandardDeviation();

        sessionDalyFaradayGainMean = descriptiveStatsIncludedDFGains.getMean();
        blockAnalysisRatioCyclesRecord.isotopicRatio().setAnalysisDalyFaradayGainMean(sessionDalyFaradayGainMean);

        sessionDalyFaradayGainOneSigmaAbs = descriptiveStatsIncludedDFGains.getStandardDeviation();
        blockAnalysisRatioCyclesRecord.isotopicRatio().setAnalysisDalyFaradayGainOneSigmaAbs(sessionDalyFaradayGainOneSigmaAbs);

        plotTitle =
                new String[]{blockAnalysisRatioCyclesRecord.title()[0]
                        + "  " + "X\u0305" + "=" + String.format("%8.8g", sessionMean).trim()
                        , "\u00B1" + String.format("%8.5g", sessionOneSigmaAbs).trim()};
    }

    public void setupPlotContextMenu() {
        // no menu for now
        plotContextMenu = new ContextMenu();
    }
}