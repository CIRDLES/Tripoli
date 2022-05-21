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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors;

import org.cirdles.tripoli.sessions.analysis.analysisMethods.sequenceTables.SequenceCell;
import org.cirdles.tripoli.sessions.analysis.analysisMethods.sequenceTables.SequenceTable;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.MassSpecOutputDataRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.species.SpeciesRecordInterface;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface DataSourceProcessorInterface {
    MassSpecOutputDataRecord prepareInputDataModelFromFile(Path inputDataFile) throws IOException;

    default AccumulatedData accumulateBaselineDataPerSequenceTableSpecs(
            String[] sequenceIDs, double[][] detectorData, SequenceTable tableSpecs, boolean faraday) {
        List<Double> dataAccumulatorList = new ArrayList<>();
        List<Integer> isotopeIndicesForDataAccumulatorList = new ArrayList<>();
        List<Integer> baseLineFlagsForDataAccumulatorList = new ArrayList<>();
        List<Integer> signalIndexForDataAccumulatorList = new ArrayList<>();

        // this map is in ascending detector order
        Map<Detector, List<SequenceCell>> detectorToSequenceCellMap = tableSpecs.getMapOfDetectorsToSequenceCells();
        int signalIndex = 1;
        for (Detector detector : detectorToSequenceCellMap.keySet()) {
            if (detector.isFaraday() == faraday) {
                int detectorDataColumnIndex = detector.getOrdinalIndex();
                signalIndexForDataAccumulatorList.add(signalIndex);
                signalIndex++;
                String baselineName = "Bl1";
                for (int detectorDataRowIndex = 0; detectorDataRowIndex < sequenceIDs.length; detectorDataRowIndex++) {
                    if (sequenceIDs[detectorDataRowIndex].toUpperCase(Locale.ROOT).compareTo(baselineName.toUpperCase(Locale.ROOT)) == 0) {
                        dataAccumulatorList.add(detectorData[detectorDataRowIndex][detectorDataColumnIndex]);
                        isotopeIndicesForDataAccumulatorList.add(0);
                        baseLineFlagsForDataAccumulatorList.add(1);
                    }
                }
            }
        }
        return new AccumulatedData(
                dataAccumulatorList,
                isotopeIndicesForDataAccumulatorList,
                baseLineFlagsForDataAccumulatorList,
                signalIndexForDataAccumulatorList);
    }

    default AccumulatedData accumulateDataPerSequenceTableSpecs(
            String[] sequenceIDs, int[] blockNumbers, List<Integer> blockListWithoutDuplicates, double[][] detectorData, SequenceTable tableSpecs, List<SpeciesRecordInterface> speciesList, boolean faraday) {
        List<Double> dataAccumulatorList = new ArrayList<>();
        List<Integer> isotopeIndicesForDataAccumulatorList = new ArrayList<>();
        List<Integer> baseLineFlagsForDataAccumulatorList = new ArrayList<>();
        List<Integer> signalIndexForDataAccumulatorList = new ArrayList<>();

        // this map is in ascending detector order
        Map<Detector, List<SequenceCell>> detectorToSequenceCellMap = tableSpecs.getMapOfDetectorsToSequenceCells();
        for (Integer blockNumber : blockListWithoutDuplicates) {
            // speciesList is in ascending order
            for (SpeciesRecordInterface species : speciesList) {
                for (Detector detector : detectorToSequenceCellMap.keySet()) {
                    if (detector.isFaraday() == faraday) {
                        // need to retrieve cells of detector sorted by isotope mass ascending
                        List<SequenceCell> detectorCellsByMass = detectorToSequenceCellMap.get(detector)
                                .stream()
                                .filter(speciesCell -> (speciesCell.getIncludedSpecies().contains(species))).toList();
                        for (SequenceCell sequenceCell : detectorCellsByMass) {
                            int detectorDataColumnIndex = detector.getOrdinalIndex();
                            String sequenceName = sequenceCell.getSequenceName();
                            for (int detectorDataRowIndex = 0; detectorDataRowIndex < sequenceIDs.length; detectorDataRowIndex++) {
                                if ((sequenceIDs[detectorDataRowIndex].toUpperCase(Locale.ROOT).compareTo(sequenceName.toUpperCase(Locale.ROOT)) == 0)
                                        && blockNumbers[detectorDataRowIndex] == blockNumber) {
                                    dataAccumulatorList.add(detectorData[detectorDataRowIndex][detectorDataColumnIndex]);
                                    isotopeIndicesForDataAccumulatorList.add(speciesList.indexOf(species) + 1);
                                    baseLineFlagsForDataAccumulatorList.add(0);
                                }
                            }
                        }
                    }
                }
            }
        }
        return new AccumulatedData(
                dataAccumulatorList,
                isotopeIndicesForDataAccumulatorList,
                baseLineFlagsForDataAccumulatorList,
                signalIndexForDataAccumulatorList);
    }

    record AccumulatedData(
            List<Double> dataAccumulatorList,
            List<Integer> isotopeIndicesForDataAccumulatorList,
            List<Integer> baseLineFlagsForDataAccumulatorList,
            List<Integer> signalIndexForDataAccumulatorList
    ) {
    }
}