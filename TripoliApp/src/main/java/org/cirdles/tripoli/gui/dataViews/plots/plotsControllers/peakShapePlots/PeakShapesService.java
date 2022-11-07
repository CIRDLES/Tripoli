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

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.nio.file.Path;

/**
 * @author James F. Bowring
 */
public class PeakShapesService extends Service<String> {
    private final Path dataFile;
    private Task<String> peakShapesTask;

    public PeakShapesService(Path dataFile) {
        this.dataFile = dataFile;
    }

    public Task<String> getPeakShapesTask() {
        return peakShapesTask;
    }

    @Override
    protected Task<String> createTask() {
        peakShapesTask = new PeakShapesTask(dataFile);
        return peakShapesTask;
    }
}