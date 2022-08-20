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

package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.RJMCMCPlots;

import javafx.concurrent.Task;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.rjmcmc.DataModelDriverExperiment;
import org.cirdles.tripoli.utilities.callbacks.LoggingCallbackInterface;
import org.cirdles.tripoli.visualizationUtilities.AbstractPlotBuilder;

import java.nio.file.Path;

/**
 * @author James F. Bowring
 */
public class RJMCMCPlotBuildersTask extends Task<String> implements LoggingCallbackInterface {
    private Path dataFile;
    // ensemble plots
    private AbstractPlotBuilder ratiosHistogramBuilder;
    private AbstractPlotBuilder baselineHistogramBuilder;
    private AbstractPlotBuilder dalyFaradayGainHistogramBuilder;
    private AbstractPlotBuilder signalNoiseHistogramBuilder;
    private AbstractPlotBuilder meanIntensityLineBuilder;

    private AbstractPlotBuilder convergeRatioLineBuilder;

    private AbstractPlotBuilder observedDataLineBuilder;
    private AbstractPlotBuilder residualDataLineBuilder;

    public RJMCMCPlotBuildersTask(Path dataFile) {
        this.dataFile = dataFile;
    }

    public AbstractPlotBuilder getRatiosHistogramBuilder() {
        return ratiosHistogramBuilder;
    }

    public AbstractPlotBuilder getBaselineHistogramBuilder() {
        return baselineHistogramBuilder;
    }

    public AbstractPlotBuilder getDalyFaradayGainHistogramBuilder() {
        return dalyFaradayGainHistogramBuilder;
    }

    public AbstractPlotBuilder getSignalNoiseHistogramBuilder() {
        return signalNoiseHistogramBuilder;
    }

    public AbstractPlotBuilder getMeanIntensityLineBuilder() {
        return meanIntensityLineBuilder;
    }

    public AbstractPlotBuilder getConvergeRatioLineBuilder() {
        return convergeRatioLineBuilder;
    }

    public AbstractPlotBuilder getObservedDataLineBuilder() {
        return observedDataLineBuilder;
    }

    public AbstractPlotBuilder getResidualDataLineBuilder() {
        return residualDataLineBuilder;
    }

    @Override
    protected String call() throws Exception {
        AbstractPlotBuilder[] plots = DataModelDriverExperiment.driveModelTest(dataFile, this);
        ratiosHistogramBuilder = plots[0];
        baselineHistogramBuilder = plots[1];
        dalyFaradayGainHistogramBuilder = plots[2];
        signalNoiseHistogramBuilder = plots[3];
        meanIntensityLineBuilder = plots[4];

        convergeRatioLineBuilder = plots[5];

        observedDataLineBuilder = plots[10];
        residualDataLineBuilder = plots[11];
        return "DONE";
    }

    @Override
    public void receiveLoggingSnippet(String loggingSnippet) {
        updateValue(loggingSnippet);
    }
}