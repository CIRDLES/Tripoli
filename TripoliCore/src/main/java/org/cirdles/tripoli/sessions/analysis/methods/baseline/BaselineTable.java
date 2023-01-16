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

package org.cirdles.tripoli.sessions.analysis.methods.baseline;

import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author James F. Bowring
 */
public class BaselineTable implements Serializable {

    @Serial
    private static final long serialVersionUID = 6152558186543823004L;

    // DetectorName maps to map of baseline name to baselineCells
    private Map<Detector, List<BaselineCell>> mapOfDetectorsToBaselineCells;

    private int sequenceCount;

    private BaselineTable() {
        this.mapOfDetectorsToBaselineCells = new TreeMap<>();
    }

    public static BaselineTable createEmptyBaselineTable() {
        BaselineTable baselineTable = new BaselineTable();

        return baselineTable;
    }

    public BaselineCell accessBaselineCellForDetector(Detector detector, String baselineName, int baselineIndex) {
        List<BaselineCell> targetList = mapOfDetectorsToBaselineCells.get(detector);
        BaselineCell baselineCell = BaselineCell.initializeBaselineCell(baselineName, baselineIndex);
        if (targetList == null) {
            targetList = new ArrayList<>();
            targetList.add(baselineCell);
            mapOfDetectorsToBaselineCells.put(detector, targetList);
        }
        if (!targetList.contains(baselineCell)) {
            targetList.add(baselineCell);
        }
        List<BaselineCell> targetCellList = targetList
                .stream()
                .filter(cell -> ((cell.getBaselineID().compareToIgnoreCase(baselineName) == 0))).toList();

        return targetCellList.get(0);
    }

    public Map<Detector, List<BaselineCell>> getMapOfDetectorsToBaselineCells() {
        return mapOfDetectorsToBaselineCells;
    }

    public void setMapOfDetectorsToBaselineCells(Map<Detector, List<BaselineCell>> mapOfDetectorsToBaselineCells) {
        this.mapOfDetectorsToBaselineCells = mapOfDetectorsToBaselineCells;
    }

    public int getSequenceCount() {
        return sequenceCount;
    }

    public void setSequenceCount(int sequenceCount) {
        this.sequenceCount = sequenceCount;
    }
}