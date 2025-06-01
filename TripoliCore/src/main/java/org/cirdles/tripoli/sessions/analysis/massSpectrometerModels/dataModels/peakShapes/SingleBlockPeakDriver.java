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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.peakShapes;

import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.ojalgo.RecoverableCondition;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public enum SingleBlockPeakDriver {
    ;

    public static final PlotBuilder[] PLOT_BUILDERS = new PlotBuilder[0];

    public static PlotBuilder[] buildForSinglePeakBlock(int blockNumber, Map<Integer, List<File>> peakGroups) throws TripoliException {
        PlotBuilder[] plotBuilders;
        if (null == peakGroups.get(blockNumber)) {
            plotBuilders = PLOT_BUILDERS;
        } else {
            plotBuilders = new PlotBuilder[peakGroups.get(blockNumber).size()];
            try {
                for (int i = 0; i < peakGroups.get(blockNumber).size(); ++i) {
                    File peakFile = peakGroups.get(blockNumber).get(i);
                    PeakShapeProcess peakShapeProcess = PeakShapeProcess.createPeakShapeProcess(peakFile.toPath());
                    peakShapeProcess.initializePeakShapeProcess();
                    plotBuilders[i] = peakShapeProcess.beamShapeCollectorWidth(blockNumber);
                }
            } catch (RecoverableCondition | IOException e) {
                throw new TripoliException("Ojalgo RecoverableCondition");
            }
        }

        return plotBuilders;
    }


}