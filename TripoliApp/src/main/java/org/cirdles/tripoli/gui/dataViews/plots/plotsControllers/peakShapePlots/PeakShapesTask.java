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

package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.peakShapePlots;

import javafx.concurrent.Task;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.peakShapes.BeamShapeTestDriver;
import org.cirdles.tripoli.utilities.callbacks.LoggingCallbackInterface;

import java.nio.file.Path;

/**
 * @author James F. Bowring
 */
public class PeakShapesTask extends Task<String> implements LoggingCallbackInterface {
    private final Path dataFile;
    private PlotBuilder beamShapePlotBuilder;
    private PlotBuilder gBeamPlotBuilder;

    private double peakWidth;

    public PeakShapesTask(Path dataFile) {
        this.dataFile = dataFile;
    }

    public PlotBuilder getBeamShapePlotBuilder() {
        return beamShapePlotBuilder;
    }

    public PlotBuilder getGBeamPlotBuilder() {
        return gBeamPlotBuilder;
    }

    public double getPeakWidth() {
        return peakWidth;
    }

    @Override
    protected String call() throws Exception {
        PlotBuilder[] linePlots = BeamShapeTestDriver.modelTest(dataFile, this);
        beamShapePlotBuilder = linePlots[0];
        gBeamPlotBuilder = linePlots[1];
        peakWidth = BeamShapeTestDriver.getMeasBeamWidthAMU();
        return "DONE";
    }

    @Override
    public void receiveLoggingSnippet(String loggingSnippet) {
        updateValue(loggingSnippet);
    }
}
