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

/**
 * @author James F. Bowring
 */
public class MCMCPlotBuildersTask extends Task<String> implements LoggingCallbackInterface, PlotBuildersTaskInterface {
    private final AnalysisInterface analysis;
    private int blockNumber;
    // ensemble plots
    private PlotBuilder[] ratiosHistogramBuilder;
    private PlotBuilder[] baselineHistogramBuilder;
    private PlotBuilder[] dalyFaradayGainHistogramBuilder;
    private PlotBuilder[] signalNoiseHistogramBuilder;
    private PlotBuilder[] meanIntensityLineBuilder;

    private PlotBuilder[] convergeRatioLineBuilder;

    private PlotBuilder observedDataLineBuilder;
    private PlotBuilder residualDataLineBuilder;

    private PlotBuilder[] convergeBLFaradayLineBuilder;

    private PlotBuilder[] convergeErrWeightedMisfitLineBuilder;
    private PlotBuilder[] convergeErrRawMisfitLineBuilder;

    private PlotBuilder convergeIntensityLinesBuilder;

    private PlotBuilder[] convergeNoiseFaradayLineBuilder;

    public MCMCPlotBuildersTask(AnalysisInterface analysis, int blockNumber) {
        this.analysis = analysis;
        this.blockNumber = blockNumber;
    }

    @Override
    public PlotBuilder[] getRatiosHistogramBuilder() {
        return ratiosHistogramBuilder;
    }

    @Override
    public PlotBuilder[] getBaselineHistogramBuilder() {
        return baselineHistogramBuilder;
    }

    @Override
    public PlotBuilder[] getDalyFaradayGainHistogramBuilder() {
        return dalyFaradayGainHistogramBuilder;
    }

    @Override
    public PlotBuilder[] getSignalNoiseHistogramBuilder() {
        return signalNoiseHistogramBuilder;
    }

    @Override
    public PlotBuilder[] getMeanIntensityLineBuilder() {
        return meanIntensityLineBuilder;
    }

    @Override
    public PlotBuilder[] getConvergeRatioLineBuilder() {
        return convergeRatioLineBuilder;
    }

    @Override
    public PlotBuilder getObservedDataLineBuilder() {
        return observedDataLineBuilder;
    }

    @Override
    public PlotBuilder getResidualDataLineBuilder() {
        return residualDataLineBuilder;
    }

    @Override
    public PlotBuilder[] getConvergeBLFaradayLineBuilder() {
        return convergeBLFaradayLineBuilder;
    }

    @Override
    public PlotBuilder[] getConvergeErrWeightedMisfitLineBuilder() {
        return convergeErrWeightedMisfitLineBuilder;
    }

    @Override
    public PlotBuilder[] getConvergeErrRawMisfitLineBuilder() {
        return convergeErrRawMisfitLineBuilder;
    }

    @Override
    public PlotBuilder getConvergeIntensityLinesBuilder() {
        return convergeIntensityLinesBuilder;
    }

    @Override
    public PlotBuilder[] getConvergeNoiseFaradayLineBuilder() {
        return convergeNoiseFaradayLineBuilder;
    }

    @Override
    public String call() throws Exception {
        PlotBuilder[][] plots = analysis.updatePlotsByBlock(blockNumber, this);
        ratiosHistogramBuilder = plots[0];
        baselineHistogramBuilder = plots[1];
        dalyFaradayGainHistogramBuilder = plots[2];
        signalNoiseHistogramBuilder = plots[3];
        meanIntensityLineBuilder = plots[4];

        convergeRatioLineBuilder = plots[5];

        convergeBLFaradayLineBuilder = plots[6];

        convergeErrWeightedMisfitLineBuilder = plots[8];
        convergeErrRawMisfitLineBuilder = plots[9];
        convergeIntensityLinesBuilder = plots[10][0];

        convergeNoiseFaradayLineBuilder = plots[11];

        observedDataLineBuilder = plots[13][0];
        residualDataLineBuilder = plots[14][0];

        return analysis.getDataFilePathString() + "\n\n\tDONE - view tabs for various plots";
    }

    @Override
    public void receiveLoggingSnippet(String loggingSnippet) {
        updateValue(loggingSnippet);
    }
}