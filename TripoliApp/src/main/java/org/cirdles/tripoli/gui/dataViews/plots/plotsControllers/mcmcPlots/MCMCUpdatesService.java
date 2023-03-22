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

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * @author James F. Bowring
 */
public class MCMCUpdatesService extends Service<String> {
    private final int blockID;

    private Task<String> plotBuilderTask;

    public MCMCUpdatesService(int blockID) {
        this.blockID = blockID;
    }

    @Override
    protected Task<String> createTask() {
        plotBuilderTask = new MCMCPlotBuildersTask(blockID);
        return plotBuilderTask;
    }

    public Task<String> getPlotBuilderTask() {
        return plotBuilderTask;
    }
}