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

package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots;

import javafx.concurrent.Task;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.utilities.callbacks.LoggingCallbackInterface;

import static org.cirdles.tripoli.constants.TripoliConstants.*;

/**
 * @author James F. Bowring
 */
public class MCMCPlotBuildersTask extends Task<String> implements LoggingCallbackInterface, PlotBuildersTaskInterface {
    public static AnalysisInterface analysis;
    private final int blockID;
    //  plotBuilders
    private PlotBuilder[] ratiosHistogramBuilder;
    private PlotBuilder[] baselineHistogramBuilder;
    private PlotBuilder[] dalyFaradayGainHistogramBuilder;
    private PlotBuilder[] signalNoiseHistogramBuilder;
    private PlotBuilder[] meanIntensityVsKnotsMultiLineBuilder;

    private PlotBuilder[] convergeRatioLineBuilder;

    private PlotBuilder[] observedDataLineBuilder;
    private PlotBuilder[] residualDataLineBuilder;

    private PlotBuilder[] convergeBLFaradayLineBuilder;

    private PlotBuilder[] convergeErrWeightedMisfitLineBuilder;
    private PlotBuilder[] convergeErrRawMisfitLineBuilder;

    private PlotBuilder[] convergeIntensityLinesBuilder;

    private PlotBuilder[] convergeNoiseFaradayLineBuilder;

    private PlotBuilder[] observedDataWithSubsetsLineBuilder;
    private PlotBuilder[] peakShapesBuilder;

    // TODO: refactor to all plotBuilders
    private PlotBuilder[][] plotBuilders;

    public MCMCPlotBuildersTask(int blockID) {
        this.blockID = blockID;
    }

    public PlotBuilder[][] getPlotBuilders() {
        return plotBuilders;
    }

    public boolean healthyPlotbuilder() {
        return (ratiosHistogramBuilder != null);
    }

    @Override
    public PlotBuilder[] getPeakShapesBuilder() {
        return peakShapesBuilder.clone();
    }

    @Override
    public PlotBuilder[] getRatiosHistogramBuilder() {
        return ratiosHistogramBuilder.clone();
    }

    @Override
    public PlotBuilder[] getBaselineHistogramBuilder() {
        return baselineHistogramBuilder.clone();
    }

    @Override
    public PlotBuilder[] getDalyFaradayGainHistogramBuilder() {
        return dalyFaradayGainHistogramBuilder.clone();
    }

    @Override
    public PlotBuilder[] getMeanIntensityVsKnotsMultiLineBuilder() {
        return meanIntensityVsKnotsMultiLineBuilder.clone();
    }

    @Override
    public PlotBuilder[] getConvergeRatioLineBuilder() {
        return convergeRatioLineBuilder.clone();
    }

    @Override
    public PlotBuilder[] getObservedDataLineBuilder() {
        return observedDataLineBuilder.clone();
    }

    @Override
    public PlotBuilder[] getResidualDataLineBuilder() {
        return residualDataLineBuilder.clone();
    }

    @Override
    public PlotBuilder[] getConvergeBLFaradayLineBuilder() {
        return convergeBLFaradayLineBuilder.clone();
    }

    @Override
    public PlotBuilder[] getConvergeErrWeightedMisfitLineBuilder() {
        return convergeErrWeightedMisfitLineBuilder.clone();
    }

    @Override
    public PlotBuilder[] getConvergeErrRawMisfitLineBuilder() {
        return convergeErrRawMisfitLineBuilder.clone();
    }

    @Override
    public PlotBuilder[] getConvergeIntensityLinesBuilder() {
        return convergeIntensityLinesBuilder;
    }

    public PlotBuilder[] getObservedDataWithSubsetsLineBuilder() {
        return observedDataWithSubsetsLineBuilder;
    }

    @Override
    public synchronized String call() throws Exception {
        plotBuilders = analysis.updatePlotsByBlock(blockID, this);

        peakShapesBuilder = analysis.updatePeakPlotsByBlock(blockID);
        ratiosHistogramBuilder = plotBuilders[PLOT_INDEX_RATIOS];
        baselineHistogramBuilder = plotBuilders[PLOT_INDEX_BASELINES];
        dalyFaradayGainHistogramBuilder = plotBuilders[PLOT_INDEX_DFGAINS];
        meanIntensityVsKnotsMultiLineBuilder = plotBuilders[PLOT_INDEX_MEANINTENSITIES];

        convergeRatioLineBuilder = plotBuilders[5];

        convergeBLFaradayLineBuilder = plotBuilders[6];

        convergeErrWeightedMisfitLineBuilder = plotBuilders[8];
        convergeErrRawMisfitLineBuilder = plotBuilders[9];
        convergeIntensityLinesBuilder = plotBuilders[10];

        //convergeNoiseFaradayLineBuilder = plotBuilders[11];

        observedDataLineBuilder = plotBuilders[13];
        residualDataLineBuilder = plotBuilders[14];

        observedDataWithSubsetsLineBuilder = plotBuilders[15];

        return analysis.getDataFilePathString() + "Block # " + blockID + "\n\n\tDONE - view tabs for various plotBuilders";
    }

    @Override
    public void receiveLoggingSnippet(String loggingSnippet) {
        updateValue(loggingSnippet);
        analysis.uppdateLogsByBlock(blockID, loggingSnippet);
    }

    public int getBlockID() {
        return blockID;
    }
}