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

package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers;

import javafx.concurrent.Task;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.peakShapes.BeamDataOutputDriverExperiment;
import org.cirdles.tripoli.utilities.callBacks.LoggingCallbackInterface;
import org.cirdles.tripoli.visualizationUtilities.linePlots.LinePlotBuilder;

import java.nio.file.Path;

/**
 * @author James F. Bowring
 */
public class GetPeakShapesTask extends Task<String> implements LoggingCallbackInterface {
    private Path dataFile;
    private LinePlotBuilder beamShapePlotBuilder;
    private LinePlotBuilder gBeamPlotBuilder;

    public GetPeakShapesTask(Path dataFile) {
        this.dataFile = dataFile;
    }

    public LinePlotBuilder getBeamShapePlotBuilder() {
        return beamShapePlotBuilder;
    }

    public LinePlotBuilder getGBeamPlotBuilder() {
        return gBeamPlotBuilder;
    }

    @Override
    protected String call() throws Exception {
        LinePlotBuilder[] linePlots = BeamDataOutputDriverExperiment.modelTest(dataFile, this);
        beamShapePlotBuilder = linePlots[0];
        gBeamPlotBuilder = linePlots[1];

        return "DONE";
    }

    @Override
    public void receiveLoggingSnippet(String loggingSnippet) {
        updateValue(loggingSnippet);
    }
}