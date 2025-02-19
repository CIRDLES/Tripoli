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

package org.cirdles.tripoli.plots.analysisPlotBuilders;

import org.cirdles.tripoli.plots.compoundPlotBuilders.PlotBlockCyclesRecord;

import java.io.Serializable;
import java.util.Map;

public record AnalysisBlockCyclesRecord(
        Map<Integer, PlotBlockCyclesRecord> mapBlockIdToBlockCyclesRecord,
        Map<Integer, Integer> mapOfBlockIdToProcessStatus,
        int cyclesPerBlock,
        int[] xAxisBlockIDs,
        String[] title,
        boolean isRatio,
        boolean isInverted) implements Serializable {

    public String[] updatedTitle() {
        String[] retVal = title.clone();
        if (isInverted && isRatio) {
            String[] nameSplit = retVal[0].split("/");
            retVal[0] = nameSplit[1] + "/" + nameSplit[0];
        }
        return retVal;
    }
}