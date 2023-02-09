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

package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmc2Plots;

import javafx.concurrent.Task;
import org.cirdles.tripoli.plots.AbstractPlotBuilder;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.DataModelDriverExperiment;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.utilities.callbacks.LoggingCallbackInterface;

import java.nio.file.Path;

import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmcV2.SingleBlockModelDriver.buildAndRunModelForSingleBlock;

/**
 * @author James F. Bowring
 */
public class MCMC2PlotBuildersTask extends Task<String> implements LoggingCallbackInterface, PlotBuildersTaskInterface {
    private final AnalysisInterface analysis;
    // ensemble plots
    private AbstractPlotBuilder[] ratiosHistogramBuilder;
    private AbstractPlotBuilder[] baselineHistogramBuilder;
    private AbstractPlotBuilder[] dalyFaradayGainHistogramBuilder;
    private AbstractPlotBuilder[] signalNoiseHistogramBuilder;
    private AbstractPlotBuilder[] meanIntensityLineBuilder;

    private AbstractPlotBuilder[] convergeRatioLineBuilder;

    private AbstractPlotBuilder observedDataLineBuilder;
    private AbstractPlotBuilder residualDataLineBuilder;

    private AbstractPlotBuilder[] convergeBLFaradayLineBuilder;

    private AbstractPlotBuilder[] convergeErrWeightedMisfitLineBuilder;
    private AbstractPlotBuilder[] convergeErrRawMisfitLineBuilder;

    private AbstractPlotBuilder convergeIntensityLinesBuilder;

    private AbstractPlotBuilder[] convergeNoiseFaradayLineBuilder;

    public MCMC2PlotBuildersTask(AnalysisInterface analysis) {
        this.analysis = analysis;
    }

    @Override
    public AbstractPlotBuilder[] getRatiosHistogramBuilder() {
        return ratiosHistogramBuilder;
    }

    @Override
    public AbstractPlotBuilder[] getBaselineHistogramBuilder() {
        return baselineHistogramBuilder;
    }

    @Override
    public AbstractPlotBuilder[] getDalyFaradayGainHistogramBuilder() {
        return dalyFaradayGainHistogramBuilder;
    }

    @Override
    public AbstractPlotBuilder[] getSignalNoiseHistogramBuilder() {
        return signalNoiseHistogramBuilder;
    }

    @Override
    public AbstractPlotBuilder[] getMeanIntensityLineBuilder() {
        return meanIntensityLineBuilder;
    }

    @Override
    public AbstractPlotBuilder[] getConvergeRatioLineBuilder() {
        return convergeRatioLineBuilder;
    }

    @Override
    public AbstractPlotBuilder getObservedDataLineBuilder() {
        return observedDataLineBuilder;
    }

    @Override
    public AbstractPlotBuilder getResidualDataLineBuilder() {
        return residualDataLineBuilder;
    }

    @Override
    public AbstractPlotBuilder[] getConvergeBLFaradayLineBuilder() {
        return convergeBLFaradayLineBuilder;
    }

    @Override
    public AbstractPlotBuilder[] getConvergeErrWeightedMisfitLineBuilder() {
        return convergeErrWeightedMisfitLineBuilder;
    }

    @Override
    public AbstractPlotBuilder[] getConvergeErrRawMisfitLineBuilder() {
        return convergeErrRawMisfitLineBuilder;
    }

    @Override
    public AbstractPlotBuilder getConvergeIntensityLinesBuilder() {
        return convergeIntensityLinesBuilder;
    }

    @Override
    public AbstractPlotBuilder[] getConvergeNoiseFaradayLineBuilder() {
        return convergeNoiseFaradayLineBuilder;
    }

    @Override
    public String call() throws Exception {
        AbstractPlotBuilder[][] plots =  buildAndRunModelForSingleBlock(1, analysis, this);
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