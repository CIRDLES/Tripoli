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
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.DataModelDriverExperiment;
import org.cirdles.tripoli.utilities.callBacks.LoggingCallbackInterface;
import org.cirdles.tripoli.visualizationUtilities.Histogram;

import java.nio.file.Path;

/**
 * @author James F. Bowring
 */
public class GetRJMCMCUpdatesTask extends Task<String> implements LoggingCallbackInterface {
    private Histogram histogram;

    public Histogram getHistogram() {
        return histogram;
    }

    @Override
    protected String call() throws Exception {
        org.cirdles.commons.util.ResourceExtractor RESOURCE_EXTRACTOR = new ResourceExtractor(Tripoli.class);
        Path dataFile = RESOURCE_EXTRACTOR
                .extractResourceAsFile("/org/cirdles/tripoli/dataProcessors/dataSources/synthetic/SyntheticDataset_05.txt").toPath();

        histogram = DataModelDriverExperiment.driveModelTest(dataFile, this);

        return "DONE";
    }

    @Override
    public void receiveLoggingSnippet(String loggingSnippet) {
        updateValue(loggingSnippet);
    }
}