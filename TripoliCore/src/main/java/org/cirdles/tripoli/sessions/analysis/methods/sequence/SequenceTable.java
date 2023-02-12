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

package org.cirdles.tripoli.sessions.analysis.methods.sequence;

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
public class SequenceTable implements Serializable {
    @Serial
    private static final long serialVersionUID = -3937544780355208600L;

    // DetectorName maps to map of sequencename to sequencecell
    private Map<Detector, List<SequenceCell>> mapOfDetectorsToSequenceCells;

    private int sequenceCount;

    private SequenceTable() {
        mapOfDetectorsToSequenceCells = new TreeMap<>();
    }

    public static SequenceTable createEmptySequenceTable() {
        /* Notes:
        Each row is a sequence (S1, S2, S3)
        Each column is a detector from the detector setup.
        Each cell is a user-input mass (double precision, units of u aka Dalton).
                    User input could be manually typed or filled from a dropdown menu of “Isotopes of Interest”.
        To determine which species (isotopologues, isobars) are going into that collector, use a formula based on the MassSpec Model (™)

         */
        SequenceTable sequenceTable = new SequenceTable();

        return sequenceTable;
    }

    public SequenceCell accessSequenceCellForDetector(Detector detector, String sequenceName, int sequenceIndex, List<String> baselineRefsList) {
        List<SequenceCell> targetList = mapOfDetectorsToSequenceCells.get(detector);
        SequenceCell sequenceCell = SequenceCell.initializeSequenceCell(sequenceName, sequenceIndex);
        sequenceCell.setBaselineReferences(baselineRefsList);
        if (null == targetList) {
            targetList = new ArrayList<>();
            targetList.add(sequenceCell);
            mapOfDetectorsToSequenceCells.put(detector, targetList);
        }
        if (!targetList.contains(sequenceCell)) {
            targetList.add(sequenceCell);
        }
        List<SequenceCell> targetCellList = targetList
                .stream()
                .filter(cell -> ((0 == cell.getSequenceId().compareToIgnoreCase(sequenceName)))).toList();

        return targetCellList.get(0);
    }

    public Map<Detector, List<SequenceCell>> getMapOfDetectorsToSequenceCells() {
        return mapOfDetectorsToSequenceCells;
    }

    public void setMapOfDetectorsToSequenceCells(Map<Detector, List<SequenceCell>> mapOfDetectorsToSequenceCells) {
        this.mapOfDetectorsToSequenceCells = mapOfDetectorsToSequenceCells;
    }

    public int getSequenceCount() {
        return sequenceCount;
    }

    public void setSequenceCount(int sequenceCount) {
        this.sequenceCount = sequenceCount;
    }

    public List<Detector> findFaradayDetectorsUsed() {
        List<Detector> faradayDetectorsUsed = new ArrayList<>();
        for (Detector detector : mapOfDetectorsToSequenceCells.keySet()) {
            if (detector.isFaraday()) {
                faradayDetectorsUsed.add(detector);
            }
        }
        return faradayDetectorsUsed;
    }

    /**
     * There is only one photoMultiplier
     *
     * @return
     */
    public Detector findPhotoMultiplierDetectorUsed() {
        Detector photoMultiplierDetectorsUsed = null;
        for (Detector detector : mapOfDetectorsToSequenceCells.keySet()) {
            if (!detector.isFaraday()) {
                photoMultiplierDetectorsUsed = detector;
                break;
            }
        }
        return photoMultiplierDetectorsUsed;
    }
}