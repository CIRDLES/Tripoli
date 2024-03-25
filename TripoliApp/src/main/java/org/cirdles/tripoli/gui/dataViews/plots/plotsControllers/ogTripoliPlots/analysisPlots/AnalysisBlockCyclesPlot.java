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

import com.google.common.base.Strings;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPaneInterface;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.analysisPlotBuilders.AnalysisBlockCyclesRecord;
import org.cirdles.tripoli.plots.compoundPlotBuilders.PlotBlockCyclesRecord;
import org.cirdles.tripoli.sessions.analysis.AnalysisStatsRecord;
import org.cirdles.tripoli.sessions.analysis.BlockStatsRecord;
import org.cirdles.tripoli.sessions.analysis.GeometricMeanStatsRecord;
import org.cirdles.tripoli.utilities.mathUtilities.MathUtilities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;

import static java.lang.StrictMath.log;
import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.*;
import static org.cirdles.tripoli.sessions.analysis.Analysis.RUN;
import static org.cirdles.tripoli.sessions.analysis.Analysis.SKIP;
import static org.cirdles.tripoli.sessions.analysis.GeometricMeanStatsRecord.generateGeometricMeanStats;

/**
 * @author James F. Bowring
 */
public class AnalysisBlockCyclesPlot extends AbstractPlot implements AnalysisBlockCyclesPlotI {
    private final AnalysisBlockCyclesRecord analysisBlockCyclesRecord;
    private Map<Integer, PlotBlockCyclesRecord> mapBlockIdToBlockCyclesRecord;
    private double[] oneSigmaForCycles;
    private boolean logScale;
    private boolean[] zoomFlagsXY;
    private PlotWallPaneInterface parentWallPane;
    private boolean isRatio;
    private boolean blockMode;
    private AnalysisStatsRecord analysisStatsRecord;
    private int sculptBlockID;

    private AnalysisBlockCyclesPlot(Rectangle bounds, AnalysisBlockCyclesRecord analysisBlockCyclesRecord, PlotWallPane parentWallPane) {
        super(bounds,
                210, 25,
                new String[]{analysisBlockCyclesRecord.title()[0]
                        + "  " + "x\u0304" + "= 0" //+ String.format("%8.8g", analysisBlockCyclesRecord.analysisMean()).trim()
                        , "\u00B1" + " 0"},//String.format("%8.5g", analysisBlockCyclesRecord.analysisOneSigma()).trim()},
                "Blocks & Cycles by Time",
                analysisBlockCyclesRecord.isRatio() ? "Ratio" : "Function");
        this.analysisBlockCyclesRecord = analysisBlockCyclesRecord;
        this.logScale = false;
        this.zoomFlagsXY = new boolean[]{true, true};
        this.parentWallPane = parentWallPane;
        this.isRatio = analysisBlockCyclesRecord.isRatio();
        this.blockMode = true;

        setOnMouseClicked(new MouseClickEventHandler());
    }

    public static AbstractPlot generatePlot(Rectangle bounds, AnalysisBlockCyclesRecord analysisBlockCyclesRecord, PlotWallPane parentWallPane) {
        return new AnalysisBlockCyclesPlot(bounds, analysisBlockCyclesRecord, parentWallPane);
    }

    public AnalysisBlockCyclesRecord getAnalysisBlockCyclesRecord() {
        return analysisBlockCyclesRecord;
    }

    public PlotWallPaneInterface getParentWallPane() {
        return parentWallPane;
    }

    public void setLogScale(boolean logScale) {
        this.logScale = logScale;
    }

    @Override
    public boolean getBlockMode() {
        return blockMode;
    }

    public void setBlockMode(boolean blockMode) {
        this.blockMode = blockMode;
    }

    public void setZoomFlagsXY(boolean[] zoomFlagsXY) {
        this.zoomFlagsXY = zoomFlagsXY;
    }

    public Map<Integer, PlotBlockCyclesRecord> getMapBlockIdToBlockCyclesRecord() {
        return mapBlockIdToBlockCyclesRecord;
    }

    @Override
    public void preparePanel(boolean reScaleX, boolean reScaleY) {
        // process blocks
        mapBlockIdToBlockCyclesRecord = analysisBlockCyclesRecord.mapBlockIdToBlockCyclesRecord();
        int cyclesPerBlock = analysisBlockCyclesRecord.cyclesPerBlock();

        if (reScaleX) {
            xAxisData = new double[mapBlockIdToBlockCyclesRecord.size() * cyclesPerBlock];
            for (int i = 0; i < xAxisData.length; i++) {
                xAxisData[i] = i + 1;
            }

            displayOffsetX = 0.0;

            minX = 1.0;
            maxX = xAxisData.length;
        }

        yAxisData = new double[mapBlockIdToBlockCyclesRecord.size() * cyclesPerBlock];
        oneSigmaForCycles = new double[mapBlockIdToBlockCyclesRecord.size() * cyclesPerBlock];
        boolean doInvert = analysisBlockCyclesRecord.isInverted() && analysisBlockCyclesRecord.isRatio();
        for (Map.Entry<Integer, PlotBlockCyclesRecord> entry : mapBlockIdToBlockCyclesRecord.entrySet()) {
            PlotBlockCyclesRecord plotBlockCyclesRecord = entry.getValue();
            if (plotBlockCyclesRecord != null) {
                int availableCyclesPerBlock = plotBlockCyclesRecord.cycleMeansData().length;
                if (doInvert) {
                    System.arraycopy(plotBlockCyclesRecord.invertedCycleMeansData(), 0, yAxisData, (plotBlockCyclesRecord.blockID() - 1) * cyclesPerBlock, availableCyclesPerBlock);
                } else {
                    System.arraycopy(plotBlockCyclesRecord.cycleMeansData(), 0, yAxisData, (plotBlockCyclesRecord.blockID() - 1) * cyclesPerBlock, availableCyclesPerBlock);
                }
                System.arraycopy(plotBlockCyclesRecord.cycleOneSigmaData(), 0, oneSigmaForCycles, (plotBlockCyclesRecord.blockID() - 1) * cyclesPerBlock, availableCyclesPerBlock);
            }
        }

        plotAxisLabelY = analysisBlockCyclesRecord.isRatio() ? "Ratio" : "Function";
        if (logScale && analysisBlockCyclesRecord.isRatio()) {
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
                int blockID = (i / mapBlockIdToBlockCyclesRecord.get(1).cyclesIncluded().length) + 1;
                // TODO: handle logratio uncertainties
                if ((mapBlockIdToBlockCyclesRecord.get(blockID) != null)) {
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

    /**
     * @param g2d
     */
    @Override
    public void showLegend(GraphicsContext g2d) {
        int textLeft = 5;
        int textTop = 18;
        int textDeltaY = 22;

        Font normalFourteen = Font.font("Courier New", FontWeight.BOLD, 16);
        Font normalEight = Font.font("SansSerif", FontWeight.NORMAL, 8);

        g2d.setFill(Paint.valueOf("RED"));
        g2d.setFont(Font.font("SansSerif", 16));
        String title = analysisBlockCyclesRecord.updatedTitle()[0];
        if (isRatio && logScale) {
            title = "LogRatio " + title;
        }
        if (isRatio && !logScale) {
            title = "Ratio " + title;
        }
        g2d.fillText(title, textLeft, textTop);

        g2d.setFill(Paint.valueOf("BLACK"));

        if (isRatio && !logScale) {
            g2d.setFont(normalFourteen);
            String twoSigString;
            int countOfTrailingDigitsForSigFig;

            if (blockMode) {
                g2d.fillText("Block Mode:", textLeft + 5, textTop += 2 * textDeltaY);
                double geoWeightedMeanRatio = StrictMath.exp(analysisStatsRecord.blockModeWeightedMean());

                if (!Double.isNaN(geoWeightedMeanRatio)) {
                    double geoWeightedMeanRatioPlusOneSigma = StrictMath.exp(analysisStatsRecord.blockModeWeightedMean() + analysisStatsRecord.blockModeWeightedMeanOneSigma());
                    double geoWeightedMeanRatioMinusOneSigma = StrictMath.exp(analysisStatsRecord.blockModeWeightedMean() - analysisStatsRecord.blockModeWeightedMeanOneSigma());
                    double geoWeightedMeanRatioPlusOneSigmaPct = (geoWeightedMeanRatioPlusOneSigma - geoWeightedMeanRatio) / geoWeightedMeanRatio * 100.0;
                    double geoWeightedMeanRatioMinusOneSigmaPct = (geoWeightedMeanRatio - geoWeightedMeanRatioMinusOneSigma) / geoWeightedMeanRatio * 100.0;
                    countOfTrailingDigitsForSigFig = countOfTrailingDigitsForSigFig((geoWeightedMeanRatioPlusOneSigma - geoWeightedMeanRatio) * 2.0, 2);
                    double plusSigmaPct = (new BigDecimal(geoWeightedMeanRatioPlusOneSigmaPct).setScale(countOfTrailingDigitsForSigFig, RoundingMode.HALF_UP)).doubleValue();
                    double minusSigmaPct = (new BigDecimal(geoWeightedMeanRatioMinusOneSigmaPct).setScale(countOfTrailingDigitsForSigFig, RoundingMode.HALF_UP)).doubleValue();

                    twoSigString = " " + (new BigDecimal(geoWeightedMeanRatio).setScale(countOfTrailingDigitsForSigFig, RoundingMode.HALF_UP)).toPlainString();
                    twoSigString = appendTrailingZeroIfNeeded(twoSigString, countOfTrailingDigitsForSigFig);
                    g2d.fillText("x\u0304  =" + twoSigString, textLeft + 10, textTop += textDeltaY);
                    boolean meanIsPlottable = (mapY(geoWeightedMeanRatio) >= topMargin) && (mapY(geoWeightedMeanRatio) <= topMargin + plotHeight);
                    if (meanIsPlottable) {
                        g2d.setStroke(OGTRIPOLI_MEAN);
                        g2d.strokeLine(Math.max(mapX(xAxisData[0]), leftMargin) - 25, mapY(geoWeightedMeanRatio), Math.min(mapX(xAxisData[xAxisData.length - 1]), leftMargin + plotWidth), mapY(geoWeightedMeanRatio));
                        g2d.setStroke(Paint.valueOf("BLACK"));
                    }

                    String sigmaPctString;
                    String sigmaMinusPctString = "";
                    if (plusSigmaPct == minusSigmaPct) {
                        sigmaPctString = " " + plusSigmaPct;
                    } else {
                        sigmaPctString = "+" + plusSigmaPct;
                        sigmaMinusPctString = "-" + minusSigmaPct;
                    }

                    sigmaPctString = appendTrailingZeroIfNeeded(sigmaPctString, countOfTrailingDigitsForSigFig);
                    sigmaMinusPctString = appendTrailingZeroIfNeeded(sigmaMinusPctString, countOfTrailingDigitsForSigFig);

                    g2d.fillText("%\u03C3  =" + sigmaPctString, textLeft + 0, textTop += textDeltaY);
                    g2d.fillText("x\u0304", textLeft + 20, textTop + 6);
                    if (sigmaMinusPctString.length() > 0) {
                        g2d.fillText("     " + sigmaMinusPctString, textLeft + 0, textTop += textDeltaY);
                    }


                    double chiSquared = analysisStatsRecord.blockModeChiSquared();
                    if (Double.isNaN(chiSquared)) {
                        twoSigString = "NaN";
                    } else if (Double.isInfinite(chiSquared)) {
                        twoSigString = "Infinite";
                    } else {
                        twoSigString = ((chiSquared >= 10) ? "" : " ") + (new BigDecimal(chiSquared).setScale(countOfTrailingDigitsForSigFig, RoundingMode.HALF_UP)).toPlainString();
                    }
                    g2d.fillText("\u03C7  =" + twoSigString, textLeft + 10, textTop += textDeltaY);
                    g2d.setFont(normalEight);
                    g2d.fillText("red", textLeft + 18, textTop + 6);
                    g2d.fillText("2", textLeft + 20, textTop - 8);
                    g2d.setFont(normalFourteen);

                    int countIncluded = analysisStatsRecord.countOfIncludedBlocks();
                    g2d.fillText("n  = " + countIncluded + "/" + analysisStatsRecord.blockStatsRecords().length, textLeft + 10, textTop += textDeltaY);

                } else {
                    g2d.fillText("Bad Data", textLeft + 5, textTop += 2 * textDeltaY);
                }
            } else { // cycle mode of ratio
                g2d.fillText("Cycle Mode:", textLeft + 5, textTop += 2 * textDeltaY);

                /*
                Round the (1-sigma percent) standard error and (1-sigma percent) standard deviation to two significant decimal places.
                 If there is a (+ and -) display on either because they are different, round both to the number of decimal places
                 belonging to the smallest increment.  So, below, 0.85 gets rounded to the hundredths to get two significant figures.,
                 For the Percent standard deviation, +10.0 and -9.1 get rounded to the tenths decimal place, matching the smallest
                 increment (tenths vs. the ones places for the +10).
                 Use two significant figures of the 1-sigma absolute standard error to determine where to round the mean.
                 */
                GeometricMeanStatsRecord geometricMeanStatsRecord =
                        generateGeometricMeanStats(analysisStatsRecord.cycleModeMean(), analysisStatsRecord.cycleModeStandardDeviation(), analysisStatsRecord.cycleModeStandardError());
                double geoMean = geometricMeanStatsRecord.geoMean();
                double geoMeanPlusOneStandardDeviation = geometricMeanStatsRecord.geoMeanPlusOneStdDev();
                double geoMeanMinusOneStandardDeviation = geometricMeanStatsRecord.geoMeanMinusOneStdDev();
                double geoMeanPlusOneStandardError = geometricMeanStatsRecord.geoMeanPlusOneStdErr();
                double geoMeanMinusOneStandardError = geometricMeanStatsRecord.geoMeanMinusOneStdErr();

                if (!Double.isNaN(geoMean)) {
                    countOfTrailingDigitsForSigFig = countOfTrailingDigitsForSigFig((geoMeanPlusOneStandardDeviation - geoMean), 2);

                    twoSigString = "" + (new BigDecimal(geoMean).setScale(countOfTrailingDigitsForSigFig, RoundingMode.HALF_UP)).toPlainString();
                    twoSigString = appendTrailingZeroIfNeeded(twoSigString, countOfTrailingDigitsForSigFig);
                    g2d.fillText("x\u0304  = " + twoSigString, textLeft + 10, textTop += textDeltaY);

                    double geoMeanRatioPlusOneStdErrPct = (geoMeanPlusOneStandardError - geoMean) / geoMean * 100.0;
                    double geoMeanRatioMinusOneStdErrPct = (geoMean - geoMeanMinusOneStandardError) / geoMean * 100.0;
                    double smallerGeoMeanRatioOneStdErrPct = Math.min(geoMeanRatioPlusOneStdErrPct, geoMeanRatioMinusOneStdErrPct);
                    int countOfTrailingDigitsForStdErrPct = countOfTrailingDigitsForSigFig(smallerGeoMeanRatioOneStdErrPct, 2);
                    double plusErrPct = (new BigDecimal(geoMeanRatioPlusOneStdErrPct).setScale(countOfTrailingDigitsForStdErrPct, RoundingMode.HALF_UP)).doubleValue();
                    double minusErrPct = (new BigDecimal(geoMeanRatioMinusOneStdErrPct).setScale(countOfTrailingDigitsForStdErrPct, RoundingMode.HALF_UP)).doubleValue();

                    String errPctString;
                    String errMinusPctString = "";
                    if (plusErrPct == minusErrPct) {
                        errPctString = " " + plusErrPct;
                    } else {
                        errPctString = "+" + plusErrPct;
                        errMinusPctString = "-" + minusErrPct;
                    }

                    errPctString = appendTrailingZeroIfNeeded(errPctString, countOfTrailingDigitsForStdErrPct);
                    errMinusPctString = appendTrailingZeroIfNeeded(errMinusPctString, countOfTrailingDigitsForStdErrPct);

                    g2d.fillText("%\u03C3  =" + errPctString, textLeft + 0, textTop += textDeltaY);
                    g2d.fillText("x\u0304", textLeft + 20, textTop + 6);
                    if (errMinusPctString.length() > 0) {
                        g2d.fillText("     " + errMinusPctString, textLeft + 0, textTop += textDeltaY);
                    }

                    double geoMeanRatioPlusOneSigmaPct = (geoMeanPlusOneStandardDeviation - geoMean) / geoMean * 100.0;
                    double geoMeanRatioMinusOneSigmaPct = (geoMean - geoMeanMinusOneStandardDeviation) / geoMean * 100.0;
                    double smallerGeoMeanRatioForOneSigmaPct = Math.min(geoMeanRatioPlusOneSigmaPct, geoMeanRatioMinusOneSigmaPct);
                    int countOfTrailingDigitsForOneSigmaPct = countOfTrailingDigitsForSigFig(smallerGeoMeanRatioForOneSigmaPct, 2);
                    double plusSigmaPct = (new BigDecimal(geoMeanRatioPlusOneSigmaPct).setScale(countOfTrailingDigitsForOneSigmaPct, RoundingMode.HALF_UP)).doubleValue();
                    double minusSigmaPct = (new BigDecimal(geoMeanRatioMinusOneSigmaPct).setScale(countOfTrailingDigitsForOneSigmaPct, RoundingMode.HALF_UP)).doubleValue();

                    String sigmaPctString;
                    String sigmaMinusPctString = "";
                    if (plusSigmaPct == minusSigmaPct) {
                        sigmaPctString = " " + plusSigmaPct;
                    } else {
                        sigmaPctString = "+" + plusSigmaPct;
                        sigmaMinusPctString = "-" + minusSigmaPct;
                    }

                    sigmaPctString = appendTrailingZeroIfNeeded(sigmaPctString, countOfTrailingDigitsForOneSigmaPct);
                    sigmaMinusPctString = appendTrailingZeroIfNeeded(sigmaMinusPctString, countOfTrailingDigitsForOneSigmaPct);

                    g2d.fillText("%\u03C3  =" + sigmaPctString, textLeft + 0, textTop += textDeltaY);
                    if (sigmaMinusPctString.length() > 0) {
                        g2d.fillText("     " + sigmaMinusPctString, textLeft + 0, textTop += textDeltaY);
                    }

                    int countIncluded = analysisStatsRecord.countOfIncludedCycles();
                    g2d.fillText("n  = " + countIncluded + "/" + analysisStatsRecord.countOfTotalCycles(), textLeft + 10, textTop += textDeltaY);

                    boolean meanIsPlottable = (mapY(geoMean) >= topMargin) && (mapY(geoMean) <= topMargin + plotHeight);
                    if (meanIsPlottable) {
                        g2d.setStroke(OGTRIPOLI_MEAN);
                        g2d.strokeLine(Math.max(mapX(xAxisData[0]), leftMargin) - 25, mapY(geoMean), Math.min(mapX(xAxisData[xAxisData.length - 1]), leftMargin + plotWidth), mapY(geoMean));
                        g2d.setStroke(Paint.valueOf("BLACK"));
                    }

                } else {
                    g2d.fillText("Bad Data", textLeft + 5, textTop += 2 * textDeltaY);
                }
            }
        } else { // logratio or function
            g2d.setFont(normalFourteen);
            String meanSigned;
            String twoSigString;
            int countOfTrailingDigitsForSigFig;

            if (blockMode) {
                g2d.fillText("Block Mode:", textLeft + 5, textTop += 2 * textDeltaY);

                double weighteMeanOneSigma = analysisStatsRecord.blockModeWeightedMeanOneSigma();
                countOfTrailingDigitsForSigFig = countOfTrailingDigitsForSigFig(weighteMeanOneSigma * 2.0, 2);

                double weightedMean = analysisStatsRecord.blockModeWeightedMean();
                if (!Double.isNaN(weightedMean)) {
                    meanSigned = (weightedMean < 0) ? " " : "";
                    String twoSigStringMean = "" + (new BigDecimal(weightedMean).setScale(countOfTrailingDigitsForSigFig, RoundingMode.HALF_UP)).toPlainString();
                    g2d.fillText("x\u0304  = " + twoSigStringMean, textLeft + 10, textTop += textDeltaY);
                    boolean meanIsPlottable = (mapY(weightedMean) >= topMargin) && (mapY(weightedMean) <= topMargin + plotHeight);
                    if (meanIsPlottable) {
                        g2d.setStroke(OGTRIPOLI_MEAN);
                        g2d.strokeLine(Math.max(mapX(xAxisData[0]), leftMargin) - 25, mapY(weightedMean), Math.min(mapX(xAxisData[xAxisData.length - 1]), leftMargin + plotWidth), mapY(weightedMean));
                        g2d.setStroke(Paint.valueOf("BLACK"));
                    }

                    twoSigString = meanSigned + (new BigDecimal(weighteMeanOneSigma).setScale(countOfTrailingDigitsForSigFig, RoundingMode.HALF_UP)).toPlainString();
                    if (countOfTrailingDigitsForSigFig == 0) {
                        twoSigString = Strings.padStart(twoSigString, twoSigStringMean.length(), ' ');
                    } else {
                        twoSigString = appendTrailingZeroIfNeeded(twoSigString, countOfTrailingDigitsForSigFig);
                    }
                    g2d.fillText("\u03C3  = " + twoSigString, textLeft + 10, textTop += textDeltaY);
                    g2d.fillText("x\u0304", textLeft + 18, textTop + 6);

                    double plottedOneSigmaHeight = Math.min(mapY(weightedMean - weighteMeanOneSigma), topMargin + plotHeight) - Math.max(mapY(weightedMean + weighteMeanOneSigma), topMargin);
                    g2d.setFill(OGTRIPOLI_ONESIGMA_SEMI);
                    g2d.fillRect(Math.max(mapX(xAxisData[0]), leftMargin),
                            Math.max(mapY(weightedMean + weighteMeanOneSigma), topMargin),
                            Math.min(mapX(xAxisData[xAxisData.length - 1]), leftMargin + plotWidth) - Math.max(mapX(xAxisData[0]), leftMargin),
                            plottedOneSigmaHeight);
                    g2d.setFill(Paint.valueOf("BLACK"));

                    double chiSquared = analysisStatsRecord.blockModeChiSquared();
                    twoSigString = meanSigned + ((chiSquared >= 10.0) ? "" : " ") + (new BigDecimal(chiSquared).setScale(countOfTrailingDigitsForSigFig, RoundingMode.HALF_UP)).toPlainString();
                    if (countOfTrailingDigitsForSigFig == 0) {
                        twoSigString = Strings.padStart(twoSigString.trim(), twoSigStringMean.length() + 1, ' ');
                    } else {
                        twoSigString = appendTrailingZeroIfNeeded(twoSigString, countOfTrailingDigitsForSigFig);
                    }
                    g2d.fillText("\u03C7  =" + twoSigString, textLeft + 10, textTop += textDeltaY);
                    g2d.setFont(normalEight);
                    g2d.fillText("red", textLeft + 18, textTop + 6);
                    g2d.fillText("2", textLeft + 20, textTop - 8);
                    g2d.setFont(normalFourteen);

                    int countIncluded = analysisStatsRecord.countOfIncludedBlocks();
                    g2d.fillText("n  = " + countIncluded + "/" + analysisStatsRecord.blockStatsRecords().length, textLeft + 10, textTop += textDeltaY);
                } else {
                    g2d.fillText("Bad Data", textLeft + 5, textTop += 2 * textDeltaY);
                }

            } else { // cycle mode of logratio or function
                /*
                Round the (1-sigma absolute) standard error to two significant decimal places (e.g., 0.0085 below).
                Round the mean and the standard deviation to the same number of decimal places.
                 */
                g2d.setFont(normalFourteen);
                g2d.fillText("Cycle Mode:", textLeft + 5, textTop += textDeltaY * 2);
                double cycleModeStandardError = analysisStatsRecord.cycleModeStandardError();
                countOfTrailingDigitsForSigFig = countOfTrailingDigitsForSigFig(Math.abs(cycleModeStandardError), 2);

                double cycleModeMean = analysisStatsRecord.cycleModeMean();
                if (!Double.isNaN(cycleModeMean)) {
                    meanSigned = (cycleModeMean < 0) ? "  " : " ";
                    String twoSigStringMean = " " + (new BigDecimal(cycleModeMean).setScale(countOfTrailingDigitsForSigFig, RoundingMode.HALF_UP)).toPlainString();
                    String checkForTrailingZero = String.format("%,1." + countOfTrailingDigitsForSigFig + "f", Double.parseDouble(twoSigStringMean));
                    if (checkForTrailingZero.substring(checkForTrailingZero.length() - 1).compareTo("0") == 0) {
                        twoSigStringMean += "0";
                    }
                    g2d.fillText("x\u0304  = " + twoSigStringMean, textLeft + 10, textTop += textDeltaY);

                    twoSigString = meanSigned + (new BigDecimal(cycleModeStandardError).setScale(countOfTrailingDigitsForSigFig, RoundingMode.HALF_UP)).toPlainString();
                    if (countOfTrailingDigitsForSigFig == 0) {
                        twoSigString = Strings.padStart(twoSigString, twoSigStringMean.length(), ' ');
                    } else {
                        twoSigString = appendTrailingZeroIfNeeded(twoSigString, countOfTrailingDigitsForSigFig);
                    }
                    g2d.fillText("\u03C3  = " + twoSigString, textLeft + 10, textTop += textDeltaY);
                    g2d.fillText("x\u0304", textLeft + 18, textTop + 6);

                    double cycleModeStandardDeviation = analysisStatsRecord.cycleModeStandardDeviation();
                    twoSigString = meanSigned + (new BigDecimal(cycleModeStandardDeviation).setScale(countOfTrailingDigitsForSigFig, RoundingMode.HALF_UP)).toPlainString();
                    if (countOfTrailingDigitsForSigFig == 0) {
                        twoSigString = Strings.padStart(twoSigString, twoSigStringMean.length(), ' ');
                    } else {
                        twoSigString = appendTrailingZeroIfNeeded(twoSigString, countOfTrailingDigitsForSigFig);
                    }
                    g2d.fillText("\u03C3  = " + twoSigString, textLeft + 10, textTop += textDeltaY);

                    int countOfIncludedCycles = analysisStatsRecord.countOfIncludedCycles();
                    int countOfTotalCycles = analysisStatsRecord.countOfTotalCycles();
                    g2d.fillText("n  = " + countOfIncludedCycles + "/" + countOfTotalCycles, textLeft + 10, textTop += textDeltaY);
                } else {
                    g2d.fillText("Bad Data", textLeft + 5, textTop += 2 * textDeltaY);
                }
            }
        }
        g2d.fillText("Legend:", textLeft + 5, textTop += textDeltaY * 2);
        g2d.setFill(OGTRIPOLI_TWOSIGMA);
        g2d.fillRect(textLeft + 10, textTop + textDeltaY, 25, 50);
        g2d.setFill(Paint.valueOf("BLACK"));
        g2d.fillText("2\u03C3", textLeft + 15, textTop + 2 * textDeltaY);

        g2d.setFill(OGTRIPOLI_ONESIGMA);
        g2d.fillRect(textLeft + 35, textTop + textDeltaY + 25, 25, 25);
        g2d.setFill(Paint.valueOf("BLACK"));
        g2d.fillText("\u03C3", textLeft + 42, textTop + 2.9 * textDeltaY);

        g2d.setFill(OGTRIPOLI_TWOSTDERR);
        g2d.fillRect(textLeft + 60, textTop + textDeltaY + 25, 25, 25);
        g2d.setFill(Paint.valueOf("BLACK"));
        g2d.fillText("2\u03C3", textLeft + 62, textTop + 2.9 * textDeltaY);
        g2d.setFont(normalEight);
        g2d.fillText("x\u0304", textLeft + 80, textTop + 2.9 * textDeltaY + 6);
        g2d.setFont(normalFourteen);

        g2d.setStroke(OGTRIPOLI_MEAN);
        g2d.setLineWidth(1.5);
        g2d.strokeLine(textLeft + 5, textTop + textDeltaY + 50, textLeft + 90, textTop + textDeltaY + 50);
        g2d.fillText("x\u0304", textLeft + 95, textTop + 2.9 * textDeltaY + 12);
    }

    private String appendTrailingZeroIfNeeded(String valueString, int countOfTrailingDigits) {
        String retVal = valueString;
        if (!valueString.isBlank()) {
            String checkForTrailingZero = String.format("%,1." + countOfTrailingDigits + "f", Double.parseDouble(valueString));
            if (checkForTrailingZero.substring(checkForTrailingZero.length() - 1).compareTo("0") == 0) {
                retVal += "0";
            }
        }
        return retVal;
    }

    /**
     * The numerical outputs should be in a font size larger than menus, and vertically aligned to improve readability.
     * Specifically, the equals signs in all three expressions and the decimals in all three numbers should be vertically aligned.
     * The numbers should be in a monospaced font so that they align vertically as well.
     * If the standard error is less than 10, it should be rounded to two significant figures,
     * and the mean and standard deviation should be rounded to the same number of decimal places.
     * If the standard error is greater than 10, round all results to the nearest integer.
     */
    private int countOfTrailingDigitsForSigFig(double standardError, int sigFig) {
        int countOfTrailingDigitsForSigFig = 0;
        if (Math.abs(standardError) < 10.0) {
            double rounded = MathUtilities.roundedToSize(standardError, sigFig);
            DecimalFormat df = new DecimalFormat("#");
            df.setMaximumFractionDigits(8);
            String roundedString = df.format(rounded);
            int dotIndex = roundedString.indexOf(".");
            countOfTrailingDigitsForSigFig = Math.max(roundedString.length() - dotIndex - 1, sigFig);
        }
        return countOfTrailingDigitsForSigFig;
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

        int cyclesPerBlock = analysisBlockCyclesRecord.cyclesPerBlock();

        for (int i = 0; i < xAxisData.length; i++) {
            int blockID = (int) ((xAxisData[i] - 0.7) / cyclesPerBlock) + 1;
            if (pointInPlot(xAxisData[i], yAxisData[i]) && (mapBlockIdToBlockCyclesRecord.get(blockID) != null)) {
                g2d.setFill(dataColor.color());
                g2d.setStroke(dataColor.color());
                if (!mapBlockIdToBlockCyclesRecord.get(blockID).blockIncluded()) {
                    g2d.setFill(Color.RED);
                    g2d.setStroke(Color.RED);
                }
                double dataX = mapX(xAxisData[i]);
                double dataY = mapY(yAxisData[i]);
                // TODO: refine for ratio mode
                double dataYplusSigma = mapY(yAxisData[i] + oneSigmaForCycles[i]);
                double dataYminusSigma = mapY(yAxisData[i] - oneSigmaForCycles[i]);

                if (yAxisData[i] > 0) {
                    g2d.fillOval(dataX - 2.0, dataY - 2.0, 4, 4);
                    g2d.strokeLine(dataX, dataY, dataX, dataYplusSigma);
                    g2d.strokeLine(dataX, dataY, dataX, dataYminusSigma);
                } else {
                    g2d.fillOval(dataX - 2.0, dataY - 2.0, 4, 4);
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
        boolean processed = (mapBlockIdToBlockCyclesRecord.get(blockID) != null) ? mapBlockIdToBlockCyclesRecord.get(blockID).processed() : false;
        Paint savedPaint = g2d.getFill();
        if (processed) {
            g2d.setFill(Paint.valueOf("GREEN"));
        } else {
            g2d.setFill(Paint.valueOf("BLACK"));
        }

        g2d.setFont(Font.font("SansSerif", FontWeight.EXTRA_BOLD, 10));

        g2d.fillText("" + blockID, xPosition, topMargin + plotHeight + 10);
        g2d.setFill(savedPaint);
    }

    public void plotStats(GraphicsContext g2d) {
        calcStats();
        BlockStatsRecord[] blockStatsRecords = analysisStatsRecord.blockStatsRecords();

        Paint saveFill = g2d.getFill();
        // TODO: promote color to constant
        g2d.setFill(Color.rgb(255, 251, 194));
        //g2d.setGlobalAlpha(0.6);

        if (blockMode) {
            int totalCycles = 0;
            for (int blockIndex = 0; blockIndex < blockStatsRecords.length; blockIndex++) {
                int cyclesPerBlock = blockStatsRecords[blockIndex].cycleMeansData().length;
                double leftX = mapX(xAxisData[totalCycles] - 0.5);
                if (leftX < leftMargin) leftX = leftMargin;
                double rightX = mapX(xAxisData[totalCycles + cyclesPerBlock - 1] + 0.5);
                if (rightX > leftMargin + plotWidth) rightX = leftMargin + plotWidth;

                BlockStatsRecord blockStatsRecord = blockStatsRecords[blockIndex];
                double mean = blockStatsRecords[blockIndex].mean();
                double meanPlusOneStandardDeviation = mean + blockStatsRecord.standardDeviation();
                double meanPlusTwoStandardDeviation = mean + 2.0 * blockStatsRecord.standardDeviation();
                double meanPlusTwoStandardError = mean + 2.0 * blockStatsRecord.standardError();
                double meanMinusOneStandardDeviation = mean - blockStatsRecord.standardDeviation();
                double meanMinusTwoStandardDeviation = mean - 2.0 * blockStatsRecord.standardDeviation();
                double meanMinusTwoStandardError = mean - 2.0 * blockStatsRecord.standardError();

                if (isRatio && !logScale) {
                    GeometricMeanStatsRecord geometricMeanStatsRecord = generateGeometricMeanStats(mean, blockStatsRecord.standardDeviation(), blockStatsRecord.standardError());
                    mean = geometricMeanStatsRecord.geoMean();
                    meanPlusOneStandardDeviation = geometricMeanStatsRecord.geoMeanPlusOneStdDev();
                    meanPlusTwoStandardDeviation = geometricMeanStatsRecord.geomeanPlusTwoStdDev();
                    meanPlusTwoStandardError = geometricMeanStatsRecord.geoMeanPlusTwoStdErr();
                    meanMinusOneStandardDeviation = geometricMeanStatsRecord.geoMeanMinusOneStdDev();
                    meanMinusTwoStandardDeviation = geometricMeanStatsRecord.geoMeanMinusTwoStdDev();
                    meanMinusTwoStandardError = geometricMeanStatsRecord.geoMeanMinusTwoStdErr();
                }

                double plottedTwoSigmaHeight = Math.min(mapY(meanMinusTwoStandardDeviation), topMargin + plotHeight) - Math.max(mapY(meanPlusTwoStandardDeviation), topMargin);
                g2d.setFill(OGTRIPOLI_TWOSIGMA);
                g2d.fillRect(leftX, Math.max(mapY(meanPlusTwoStandardDeviation), topMargin), rightX - leftX, plottedTwoSigmaHeight);

                double plottedOneSigmaHeight = Math.min(mapY(meanMinusOneStandardDeviation), topMargin + plotHeight) - Math.max(mapY(meanPlusOneStandardDeviation), topMargin);
                g2d.setFill(OGTRIPOLI_ONESIGMA);
                g2d.fillRect(leftX, Math.max(mapY(meanPlusOneStandardDeviation), topMargin), rightX - leftX, plottedOneSigmaHeight);

                double plottedTwoStdErrHeight = Math.min(mapY(meanMinusTwoStandardError), topMargin + plotHeight) - Math.max(mapY(meanPlusTwoStandardError), topMargin);
                g2d.setFill(OGTRIPOLI_TWOSTDERR);
                g2d.fillRect(leftX, Math.max(mapY(meanPlusTwoStandardError), topMargin), rightX - leftX, plottedTwoStdErrHeight);

                boolean meanIsPlottable = (mapY(mean) >= topMargin) && (mapY(mean) <= topMargin + plotHeight);
                if (meanIsPlottable && (leftX <= rightX)) {
                    g2d.setStroke(OGTRIPOLI_MEAN);
                    g2d.setLineWidth(1.5);
                    g2d.strokeLine(leftX, mapY(mean), rightX, mapY(mean));
                }
                totalCycles = totalCycles + cyclesPerBlock;
            }

        } else { //cycle mode
            double mean = analysisStatsRecord.cycleModeMean();
            double stdDev = analysisStatsRecord.cycleModeStandardDeviation();
            double stdErr = analysisStatsRecord.cycleModeStandardError();
            double meanPlusOneStandardDeviation = mean + stdDev;
            double meanPlusTwoStandardDeviation = mean + 2.0 * stdDev;
            double meanPlusTwoStandardError = mean + 2.0 * stdErr;
            double meanMinusOneStandardDeviation = mean - stdDev;
            double meanMinusTwoStandardDeviation = mean - 2.0 * stdDev;
            double meanMinusTwoStandardError = mean - 2.0 * stdErr;

            double leftX = mapX(xAxisData[0] - 0.5);
            if (leftX < leftMargin) leftX = leftMargin;
            double rightX = mapX(xAxisData[xAxisData.length - 1] + 0.5);
            if (rightX > leftMargin + plotWidth) rightX = leftMargin + plotWidth;

            if (isRatio && !logScale) {
                GeometricMeanStatsRecord geometricMeanStatsRecord = generateGeometricMeanStats(mean, stdDev, stdErr);
                mean = geometricMeanStatsRecord.geoMean();
                meanPlusOneStandardDeviation = geometricMeanStatsRecord.geoMeanPlusOneStdDev();
                meanPlusTwoStandardDeviation = geometricMeanStatsRecord.geomeanPlusTwoStdDev();
                meanPlusTwoStandardError = geometricMeanStatsRecord.geoMeanPlusTwoStdErr();
                meanMinusOneStandardDeviation = geometricMeanStatsRecord.geoMeanMinusOneStdDev();
                meanMinusTwoStandardDeviation = geometricMeanStatsRecord.geoMeanMinusTwoStdDev();
                meanMinusTwoStandardError = geometricMeanStatsRecord.geoMeanMinusTwoStdErr();
            }

            double plottedTwoSigmaHeight = Math.min(mapY(meanMinusTwoStandardDeviation), topMargin + plotHeight) - Math.max(mapY(meanPlusTwoStandardDeviation), topMargin);
            g2d.setFill(OGTRIPOLI_TWOSIGMA);
            g2d.fillRect(leftX, Math.max(mapY(meanPlusTwoStandardDeviation), topMargin), rightX - leftX, plottedTwoSigmaHeight);

            double plottedOneSigmaHeight = Math.min(mapY(meanMinusOneStandardDeviation), topMargin + plotHeight) - Math.max(mapY(meanPlusOneStandardDeviation), topMargin);
            g2d.setFill(OGTRIPOLI_ONESIGMA);
            g2d.fillRect(leftX, Math.max(mapY(meanPlusOneStandardDeviation), topMargin), rightX - leftX, plottedOneSigmaHeight);

            double plottedTwoStdErrHeight = Math.min(mapY(meanMinusTwoStandardError), topMargin + plotHeight) - Math.max(mapY(meanPlusTwoStandardError), topMargin);
            g2d.setFill(OGTRIPOLI_TWOSTDERR);
            g2d.fillRect(leftX, Math.max(mapY(meanPlusTwoStandardError), topMargin), rightX - leftX, plottedTwoStdErrHeight);

            boolean meanIsPlottable = (mapY(mean) >= topMargin) && (mapY(mean) <= topMargin + plotHeight);
            if (meanIsPlottable && (leftX <= rightX)) {
                g2d.setStroke(OGTRIPOLI_MEAN);
                g2d.setLineWidth(1.5);
                g2d.strokeLine(leftX, mapY(mean), rightX, mapY(mean));
            }
        }
        g2d.setFill(saveFill);
        g2d.setGlobalAlpha(1.0);
    }

    public void calcStats() {
        // Jan 2024 new approach - two modes: block mode and cycle mode
        // BLOCK MODE will be default - calculate and plot stats for each block
        int blockCount = mapBlockIdToBlockCyclesRecord.size();
        BlockStatsRecord[] blockStatsRecords = new BlockStatsRecord[blockCount];
        int arrayIndex = 0;
        for (Map.Entry<Integer, PlotBlockCyclesRecord> entry : mapBlockIdToBlockCyclesRecord.entrySet()) {
            PlotBlockCyclesRecord plotBlockCyclesRecord = entry.getValue();
            if (plotBlockCyclesRecord != null) {
                blockStatsRecords[arrayIndex] = BlockStatsRecord.generateBlockStatsRecord(
                        entry.getKey(), plotBlockCyclesRecord.blockIncluded(), isRatio, analysisBlockCyclesRecord.isInverted(), plotBlockCyclesRecord.cycleMeansData(), plotBlockCyclesRecord.cyclesIncluded());
            }
            arrayIndex++;
        }
        analysisStatsRecord = AnalysisStatsRecord.generateAnalysisStatsRecord(blockStatsRecords);

//        // CYCLE MODE
//        DescriptiveStatistics descriptiveStatsIncludedCycles = new DescriptiveStatistics();
//        for (int i = 0; i < yAxisData.length; i++) {
//            int blockID = (i / mapBlockIdToBlockCyclesRecord.get(1).cyclesIncluded().length) + 1;
//            if (mapBlockIdToBlockCyclesRecord.get(blockID) != null) {
//                if (mapBlockIdToBlockCyclesRecord.get(blockID).blockIncluded() && (yAxisData[i] != 0)) {
//                    descriptiveStatsIncludedCycles.addValue(yAxisData[i]);
//                }
//            }
//        }
//        analysisMean = descriptiveStatsIncludedCycles.getMean();
//
//        analysisOneSigmaAbs = descriptiveStatsIncludedCycles.getStandardDeviation();

//        plotTitle =
//                new String[]{analysisBlockCyclesRecord.title()[0]
//                        + "  " + "x\u0304" + "=" + String.format("%8.8g", analysisMean).trim()
//                        , "\u00B1" + String.format("%8.5g", analysisOneSigmaAbs).trim()};
    }

    public void setupPlotContextMenu() {
        // no menu for now
        plotContextMenu = new ContextMenu();
    }

    public void resetData() {
        for (Integer blockID : mapBlockIdToBlockCyclesRecord.keySet()) {
            mapBlockIdToBlockCyclesRecord.put(
                    blockID,
                    mapBlockIdToBlockCyclesRecord.get(blockID).setBlockIncluded());
            analysisBlockCyclesRecord.mapOfBlockIdToProcessStatus().put(sculptBlockID, RUN);
        }
    }

    private int determineSculptBlock(double mouseX) {
        double mouseTime = convertMouseXToValue(mouseX);
        int xAxisIndexOfMouse = Math.min(xAxisData.length - 1, Math.abs(Arrays.binarySearch(xAxisData, mouseTime)));
        double t0 = xAxisData[xAxisIndexOfMouse];
        double t2 = xAxisData[(xAxisIndexOfMouse >= 2) ? (xAxisIndexOfMouse - 2) : 0];
        int sculptBlockIDCalc = analysisBlockCyclesRecord.xAxisBlockIDs()[(xAxisIndexOfMouse >= 2) ? (xAxisIndexOfMouse - 2) : 0];
        if (((t0 - t2) > 5.0) && (Math.abs(mouseTime - t2) > Math.abs(mouseTime - t0))) {
            // in between blocks
            sculptBlockIDCalc = analysisBlockCyclesRecord.xAxisBlockIDs()[xAxisIndexOfMouse];
        }
        return sculptBlockIDCalc;
    }

    class MouseClickEventHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent mouseEvent) {
            plotContextMenu.hide();
            boolean isPrimary = (0 == mouseEvent.getButton().compareTo(MouseButton.PRIMARY));

//            if (mouseInHouse(mouseEvent.getX(), mouseEvent.getY())) {
//                if (isPrimary && mouseEvent.isShiftDown()) {
//                    AnalysisBlockCyclesPlot analysisBlockCyclesPlot = (AnalysisBlockCyclesPlot) mouseEvent.getSource();
//                    // determine blockID
//                    double xValue = convertMouseXToValue(mouseEvent.getX());
//                    int blockID = (int) ((xValue - 0.7) / analysisBlockCyclesPlot.getBlockRatioCyclesSessionRecord().cyclesPerBlock()) + 1;
//                    AnalysisBlockCyclesPlot sourceAnalysisBlockCyclesPlot = (AnalysisBlockCyclesPlot) mouseEvent.getSource();
//                    ((PlotWallPane) sourceAnalysisBlockCyclesPlot.getParentWallPane()).synchronizeBlockToggle(blockID);
//                } else if (!isPrimary){
//
//                }


//            boolean isPrimary = (0 == mouseEvent.getButton().compareTo(MouseButton.PRIMARY));
//            if (2 == mouseEvent.getClickCount()) {
//            }


            if (isPrimary && mouseEvent.isControlDown() && (mouseInHouse(mouseEvent.getX(), mouseEvent.getY()))) {
                // turn off / on block
                sculptBlockID = determineSculptBlock(mouseEvent.getX());

                mapBlockIdToBlockCyclesRecord.put(
                        sculptBlockID,
                        mapBlockIdToBlockCyclesRecord.get(sculptBlockID).toggleBlockIncluded());
                boolean included = mapBlockIdToBlockCyclesRecord.get(sculptBlockID).blockIncluded();
                analysisBlockCyclesRecord.mapOfBlockIdToProcessStatus().put(sculptBlockID, included ? RUN : SKIP);
                repaint();
            }
        }
    }
}