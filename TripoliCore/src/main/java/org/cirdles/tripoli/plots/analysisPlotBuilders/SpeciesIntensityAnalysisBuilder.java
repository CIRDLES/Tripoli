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

package org.cirdles.tripoli.plots.analysisPlotBuilders;

import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;

public class SpeciesIntensityAnalysisBuilder extends PlotBuilder {

    private final double[] xData;
    private final double[][] yData;
    private final double[][] ampResistance;
    private final double[][] baseLine;
    private final double[][] dfGain;
    private final int[] xAxisBlockIDs;
    private final boolean showResiduals;

    private AnalysisInterface analysis;
    private boolean[][] onPeakDataIncludedAllBlocks;
    private double[][] onPeakDataSignalNoiseArray;

    protected SpeciesIntensityAnalysisBuilder(boolean showResiduals, AnalysisInterface analysis, double[] xData,
                                              boolean[][] onPeakDataIncludedAllBlocks, int[] xAxisBlockIDs, double[][] yData,
                                              double[][] ampResistance, double[][] baseLine, double[][] dfGain,
                                              double[][] onPeakDataSignalNoiseArray, String[] title, String xAxisLabel, String yAxisLabel) {
        super(title, xAxisLabel, yAxisLabel, true);
        this.showResiduals = showResiduals;
        this.analysis = analysis;
        this.xData = xData;
        this.xAxisBlockIDs = xAxisBlockIDs;
        this.yData = yData;
        this.onPeakDataIncludedAllBlocks = onPeakDataIncludedAllBlocks;
        this.ampResistance = ampResistance;
        this.baseLine = baseLine;
        this.dfGain = dfGain;
        this.onPeakDataSignalNoiseArray = onPeakDataSignalNoiseArray;
    }

    public static SpeciesIntensityAnalysisBuilder initializeSpeciesIntensityAnalysisPlot(
            boolean showResiduals, AnalysisInterface analysis, double[] xData, boolean[][] onPeakDataIncludedAllBlocks,
            int[] xAxisBlockIDs, double[][] yData, double[][] ampResistance, double[][] baseLine, double[][] dfGain,
            double[][] onPeakDataSignalNoiseArray, String[] title, String xAxisLabel, String yAxisLabel) {
        return new SpeciesIntensityAnalysisBuilder(showResiduals, analysis, xData, onPeakDataIncludedAllBlocks,
                xAxisBlockIDs, yData, ampResistance, baseLine, dfGain, onPeakDataSignalNoiseArray, title, xAxisLabel, yAxisLabel);
    }

    public AnalysisInterface getAnalysis() {
        return analysis;
    }

    public double[] getxData() {
        return xData;
    }

    public int[] getxAxisBlockIDs() {
        return xAxisBlockIDs;
    }

    public double[][] getyData() {
        return yData;
    }

    public boolean[][] getOnPeakDataIncludedAllBlocks() {
        return onPeakDataIncludedAllBlocks;
    }

    public void setOnPeakDataIncludedAllBlocks(boolean[][] onPeakDataIncludedAllBlocks) {
        this.onPeakDataIncludedAllBlocks = onPeakDataIncludedAllBlocks;
    }

    public double[][] getAmpResistance() {
        return ampResistance;
    }

    public double[][] getBaseLine() {
        return baseLine;
    }

    public double[][] getDfGain() {
        return dfGain;
    }

    public boolean isShowResiduals() {
        return showResiduals;
    }

    public double[][] getOnPeakDataSignalNoiseArray() {
        return onPeakDataSignalNoiseArray;
    }

    public record PlotSpecsSpeciesIntensityAnalysis(
            boolean[] speciesChecked,
            boolean showFaradays,
            boolean showPMs,
            boolean showModels,
            boolean baselineCorr,
            boolean gainCorr,
            boolean logScale
    ) {
    }

}