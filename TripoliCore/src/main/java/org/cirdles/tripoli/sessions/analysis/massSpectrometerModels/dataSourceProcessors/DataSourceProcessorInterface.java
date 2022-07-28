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

import org.cirdles.tripoli.sessions.analysis.analysisMethods.AnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.analysisMethods.sequenceTables.SequenceCell;
import org.cirdles.tripoli.sessions.analysis.analysisMethods.sequenceTables.SequenceTable;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.rjmcmc.MassSpecOutputDataRecord;
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
            String[] sequenceIDs, double[][] detectorData, AnalysisMethod analysisMethod, boolean faraday) {
        SequenceTable tableSpecs = analysisMethod.getSequenceTable();
        List<Double> dataAccumulatorList = new ArrayList<>();
        List<Double> timeAccumulatorList = new ArrayList<>();
        List<Integer> timeIndAccumulatorList = new ArrayList<>();
        List<Integer> blockIndicesForDataAccumulatorList = new ArrayList<>();
        List<Integer> isotopeIndicesForDataAccumulatorList = new ArrayList<>();
        List<int[]> isotopeFlagsForDataAccumulatorList = new ArrayList<>();
        List<Integer> detectorIndicesForDataAccumulatorList = new ArrayList<>();
        List<int[]> detectorFlagsForDataAccumulatorList = new ArrayList<>();
        List<Integer> baseLineFlagsForDataAccumulatorList = new ArrayList<>();
        List<Integer> axialFlagsForDataAccumulatorList = new ArrayList<>();
        List<Integer> signalIndexForDataAccumulatorList = new ArrayList<>();

        // matlab matrices Far_ind and Ax_ind NOT USED FOR BASELINE
        double[][] isotopeIndicesPerFaradayOrAxial = new double[0][0];

        // this map is in ascending detector order
        Map<Detector, List<SequenceCell>> detectorToSequenceCellMap = tableSpecs.getMapOfDetectorsToSequenceCells();
        int signalIndex = 1;
        int detectorIndex = 0;
        for (Detector detector : detectorToSequenceCellMap.keySet()) {
            if (detector.isFaraday() == faraday) {
                int detectorDataColumnIndex = detector.getOrdinalIndex();
                String baselineName = "Bl1";
                for (int detectorDataRowIndex = 0; detectorDataRowIndex < sequenceIDs.length; detectorDataRowIndex++) {
                    if (sequenceIDs[detectorDataRowIndex].toUpperCase(Locale.ROOT).compareTo(baselineName.toUpperCase(Locale.ROOT)) == 0) {
                        dataAccumulatorList.add(detectorData[detectorDataRowIndex][detectorDataColumnIndex]);
                        timeAccumulatorList.add(0.0);
                        timeIndAccumulatorList.add(0);

                        isotopeIndicesForDataAccumulatorList.add(0);

                        blockIndicesForDataAccumulatorList.add(0);

                        // all zeroes here
                        int[] isotopeFlags = new int[analysisMethod.getSpeciesList().size()];
                        isotopeFlagsForDataAccumulatorList.add(isotopeFlags);

                        // detectors are indexed starting at 1
                        if (faraday) {
                            detectorIndicesForDataAccumulatorList.add(detectorIndex + 1);
                        } else {
                            // not a Faraday = Daly so index is count of faradays plus 1
                            detectorIndicesForDataAccumulatorList.add(detectorToSequenceCellMap.keySet().size());
                        }

                        int[] detectorFlags = new int[detectorToSequenceCellMap.keySet().size()];
                        detectorFlags[detectorIndex] = 1;
                        detectorFlagsForDataAccumulatorList.add(detectorFlags);

                        baseLineFlagsForDataAccumulatorList.add(1);

                        axialFlagsForDataAccumulatorList.add(0);
                        signalIndexForDataAccumulatorList.add(signalIndex);
                    }
                }
                detectorIndex++;
                signalIndex++;
            }
        }
        return new AccumulatedData(
                dataAccumulatorList,
                timeAccumulatorList,
                timeIndAccumulatorList,
                blockIndicesForDataAccumulatorList,
                isotopeIndicesForDataAccumulatorList,
                isotopeFlagsForDataAccumulatorList,
                detectorIndicesForDataAccumulatorList,
                detectorFlagsForDataAccumulatorList,
                baseLineFlagsForDataAccumulatorList,
                axialFlagsForDataAccumulatorList,
                signalIndexForDataAccumulatorList,
                isotopeIndicesPerFaradayOrAxial);
    }

    default AccumulatedData accumulateDataPerSequenceTableSpecs(
            String[] sequenceIDs, int[] blockNumbers, List<Integer> blockListWithoutDuplicates, double[][] detectorData, double[] timeStamp, AnalysisMethod analysisMethod, boolean faraday) {
        SequenceTable tableSpecs = analysisMethod.getSequenceTable();
        List<SpeciesRecordInterface> speciesList = analysisMethod.getSpeciesList();
        List<Double> dataAccumulatorList = new ArrayList<>();
        List<Double> timeAccumulatorList = new ArrayList<>();
        List<Integer> timeIndAccumulatorList = new ArrayList<>();
        List<Integer> blockIndicesForDataAccumulatorList = new ArrayList<>();
        List<Integer> isotopeIndicesForDataAccumulatorList = new ArrayList<>();
        List<int[]> isotopeFlagsForDataAccumulatorList = new ArrayList<>();
        List<Integer> detectorIndicesForDataAccumulatorList = new ArrayList<>();
        List<int[]> detectorFlagsForDataAccumulatorList = new ArrayList<>();
        List<Integer> baseLineFlagsForDataAccumulatorList = new ArrayList<>();
        List<Integer> axialFlagsForDataAccumulatorList = new ArrayList<>();
        List<Integer> signalIndexForDataAccumulatorList = new ArrayList<>();

        // this map is in ascending detector order
        Map<Detector, List<SequenceCell>> detectorToSequenceCellMap = tableSpecs.getMapOfDetectorsToSequenceCells();

        // matlab matrices Far_ind and Ax_ind
        double[][] isotopeIndicesPerFaradayOrAxial;
        if (faraday) {
            isotopeIndicesPerFaradayOrAxial = new double[sequenceIDs.length][detectorToSequenceCellMap.keySet().size() - 1];
        } else {
            isotopeIndicesPerFaradayOrAxial = new double[sequenceIDs.length][1];
        }

        for (Integer blockNumber : blockListWithoutDuplicates) {
            // speciesList is in ascending order
            for (SpeciesRecordInterface species : speciesList) {
                int detectorIndex = 0;
                for (Detector detector : detectorToSequenceCellMap.keySet()) {
                    if (detector.isFaraday() == faraday) {
                        // need to retrieve cells of detector sorted by isotope mass ascending
                        List<SequenceCell> detectorCellsByMass = detectorToSequenceCellMap.get(detector)
                                .stream()
                                .filter(speciesCell -> (speciesCell.getIncludedSpecies().contains(species))).toList();
                        for (SequenceCell sequenceCell : detectorCellsByMass) {
                            int detectorDataColumnIndex = detector.getOrdinalIndex();
                            int lastBaseLineIndex = 0;
                            String sequenceName = sequenceCell.getSequenceName();
                            for (int detectorDataRowIndex = 0; detectorDataRowIndex < sequenceIDs.length; detectorDataRowIndex++) {
                                if ((sequenceIDs[detectorDataRowIndex].toUpperCase(Locale.ROOT).compareTo(sequenceName.toUpperCase(Locale.ROOT)) == 0)
                                        && blockNumbers[detectorDataRowIndex] == blockNumber) {
                                    dataAccumulatorList.add(detectorData[detectorDataRowIndex][detectorDataColumnIndex]);
                                    timeAccumulatorList.add(timeStamp[detectorDataRowIndex]);
                                    timeIndAccumulatorList.add(detectorDataRowIndex - lastBaseLineIndex);

                                    blockIndicesForDataAccumulatorList.add(blockNumber);

                                    // isotopes indexed starting at 1
                                    isotopeIndicesForDataAccumulatorList.add(speciesList.indexOf(species) + 1);

                                    int[] isotopeFlags = new int[analysisMethod.getSpeciesList().size()];
                                    isotopeFlags[speciesList.indexOf(species)] = 1;
                                    isotopeFlagsForDataAccumulatorList.add(isotopeFlags);

                                    // detectors are indexed starting at 1
                                    if (faraday) {
                                        detectorIndicesForDataAccumulatorList.add(detectorIndex + 1);
                                    } else {
                                        // not a Faraday = Daly so index is count of faradays plus 1
                                        detectorIndicesForDataAccumulatorList.add(detectorToSequenceCellMap.keySet().size());
                                    }

                                    int[] detectorFlags = new int[detectorToSequenceCellMap.keySet().size()];
                                    if (faraday) {
                                        detectorFlags[detectorIndex] = 1;
                                    } else {
                                        // not a Faraday = Daly so 1 goes in last cell
                                        detectorFlags[detectorToSequenceCellMap.keySet().size() - 1] = 1;
                                    }
                                    detectorFlagsForDataAccumulatorList.add(detectorFlags);

                                    baseLineFlagsForDataAccumulatorList.add(0);

                                    axialFlagsForDataAccumulatorList.add(faraday ? 0 : 1);

                                    isotopeIndicesPerFaradayOrAxial[detectorDataRowIndex][detectorIndex] = speciesList.indexOf(species) + 1;
                                    signalIndexForDataAccumulatorList.add(speciesList.indexOf(species) + 1);

                                } else if ((sequenceIDs[detectorDataRowIndex].toUpperCase(Locale.ROOT).compareTo("BL1") == 0)
                                        && blockNumbers[detectorDataRowIndex] == blockNumber) {
                                    lastBaseLineIndex = detectorDataRowIndex;
                                }
                            }
                        }
                        detectorIndex++;
                    }

                }

            }

        }
        return new AccumulatedData(
                dataAccumulatorList,
                timeAccumulatorList,
                timeIndAccumulatorList,
                blockIndicesForDataAccumulatorList,
                isotopeIndicesForDataAccumulatorList,
                isotopeFlagsForDataAccumulatorList,
                detectorIndicesForDataAccumulatorList,
                detectorFlagsForDataAccumulatorList,
                baseLineFlagsForDataAccumulatorList,
                axialFlagsForDataAccumulatorList,
                signalIndexForDataAccumulatorList,
                isotopeIndicesPerFaradayOrAxial);
    }

    record AccumulatedData(
            List<Double> dataAccumulatorList,
            List<Double> timeAccumulatorList,
            List<Integer> timeIndAccumulatorList,
            List<Integer> blockIndicesForDataAccumulatorList,
            List<Integer> isotopeIndicesForDataAccumulatorList,
            List<int[]> isotopeFlagsForDataAccumulatorList,
            List<Integer> detectorIndicesForDataAccumulatorList,
            List<int[]> detectorFlagsForDataAccumulatorList,
            List<Integer> baseLineFlagsForDataAccumulatorList,
            List<Integer> axialFlagsForDataAccumulatorList,
            List<Integer> signalIndexForDataAccumulatorList,
            double[][] isotopeIndicesPerFaradayOrAxial
    ) {
    }
}