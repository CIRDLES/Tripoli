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
import org.cirdles.tripoli.visualizationUtilities.histograms.HistogramBuilder;

import java.nio.file.Path;

/**
 * @author James F. Bowring
 */
public class RJMCMCUpdatesTask extends Task<String> implements LoggingCallbackInterface {
    private Path dataFile;
    private HistogramBuilder histogramBuilder;

    public RJMCMCUpdatesTask(Path dataFile) {
        this.dataFile = dataFile;
    }

    public HistogramBuilder getHistogram() {
        return histogramBuilder;
    }

    @Override
    protected String call() throws Exception {
        histogramBuilder = DataModelDriverExperiment.driveModelTest(dataFile, this);

        return "DONE";
    }

    @Override
    public void receiveLoggingSnippet(String loggingSnippet) {
        updateValue(loggingSnippet);
    }
}