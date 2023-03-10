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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc;

import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputSingleBlockRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.methods.baseline.BaselineCell;
import org.cirdles.tripoli.sessions.analysis.methods.baseline.BaselineTable;
import org.cirdles.tripoli.sessions.analysis.methods.sequence.SequenceCell;
import org.cirdles.tripoli.sessions.analysis.methods.sequence.SequenceTable;
import org.cirdles.tripoli.species.SpeciesRecordInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.cirdles.tripoli.constants.ConstantsTripoliCore.ONE_COULOMB;

/**
 * @author James F. Bowring
 */
public enum SingleBlockDataAccumulatorMCMC {
    ;

    public static SingleBlockDataSetRecord.SingleBlockDataRecord accumulateBaselineDataPerBaselineTableSpecs(
            MassSpecOutputSingleBlockRecord massSpecOutputSingleBlockRecord, AnalysisMethod analysisMethod) {

        BaselineTable baselineTable = analysisMethod.getBaselineTable();
        // TODO: Find out why the 5-isotope example baseline table does not have all entries, meaning need to use sequenceTable
        List<Integer> detectorOrdinalIndicesAccumulatorList = new ArrayList<>();
        List<Double> intensityAccumulatorList = new ArrayList<>();
        List<Double> timeAccumulatorList = new ArrayList<>();
        List<Integer> timeIndexAccumulatorList = new ArrayList<>();
        List<Integer> isotopeOrdinalIndicesAccumulatorList = new ArrayList<>();

        double[][] baselineIntensities = massSpecOutputSingleBlockRecord.baselineIntensities();
        double[] baseLineTimeStamps = massSpecOutputSingleBlockRecord.baselineTimeStamps();
        Map<String, List<Integer>> mapOfBaselineIdsToIndices = massSpecOutputSingleBlockRecord.mapOfBaselineIdsToIndices();

        // this map is in ascending detector order
        Map<Detector, List<BaselineCell>> detectorToBaselineCellMap = baselineTable.getMapOfDetectorsToBaselineCells();
        for (Detector detector : detectorToBaselineCellMap.keySet()) {
            if (detector.isFaraday()) {
                int detectorDataColumnIndex = detector.getOrdinalIndex();
                List<BaselineCell> baselineCells = detectorToBaselineCellMap.get(detector);
                for (BaselineCell baselineCell : baselineCells) {
                    String baselineID = baselineCell.getBaselineID();
                    List<Integer> baselineIndices = mapOfBaselineIdsToIndices.get(baselineID);
                    Collections.sort(baselineIndices);
                    for (Integer index : baselineIndices) {
                        detectorOrdinalIndicesAccumulatorList.add(detectorDataColumnIndex);
                        double intensity = baselineIntensities[index][detectorDataColumnIndex];
                        double amplifierResistance = detector.getAmplifierResistanceInOhms();
                        if (MassSpectrometerContextEnum.PHOENIX == analysisMethod.getMassSpectrometerContext()) {
                            // convert all volts to counts to bring all files into alignment
                            intensity = intensity * (ONE_COULOMB / amplifierResistance);
                        }
                        intensityAccumulatorList.add(intensity);
                        timeAccumulatorList.add(baseLineTimeStamps[index]);
                        timeIndexAccumulatorList.add(index);
                        isotopeOrdinalIndicesAccumulatorList.add(analysisMethod.getSpeciesList().size());
                    }
                }
            }
        }

        return new SingleBlockDataSetRecord.SingleBlockDataRecord(
                massSpecOutputSingleBlockRecord.blockNumber(),
                detectorOrdinalIndicesAccumulatorList,
                intensityAccumulatorList,
                timeAccumulatorList,
                timeIndexAccumulatorList,
                isotopeOrdinalIndicesAccumulatorList);
    }

    public static SingleBlockDataSetRecord.SingleBlockDataRecord accumulateOnPeakDataPerSequenceTableSpecs(
            MassSpecOutputSingleBlockRecord massSpecOutputSingleBlockRecord, AnalysisMethod analysisMethod, boolean isFaraday) {

        SequenceTable sequenceTable = analysisMethod.getSequenceTable();
        List<SpeciesRecordInterface> speciesList = analysisMethod.getSpeciesList();

        List<Integer> detectorOrdinalIndicesAccumulatorList = new ArrayList<>();
        List<Double> intensityAccumulatorList = new ArrayList<>();
        List<Double> timeAccumulatorList = new ArrayList<>();
        List<Integer> timeIndexAccumulatorList = new ArrayList<>();
        List<Integer> isotopeOrdinalIndicesAccumulatorList = new ArrayList<>();

        double[][] onPeakIntensities = massSpecOutputSingleBlockRecord.onPeakIntensities();
        double[] onPeakTimeStamps = massSpecOutputSingleBlockRecord.onPeakTimeStamps();
        Map<String, List<Integer>> mapOfOnPeakIdsToIndices = massSpecOutputSingleBlockRecord.mapOfOnPeakIdsToIndices();

        // this map is in ascending detector order
        Map<Detector, List<SequenceCell>> detectorToSequenceCellMap = sequenceTable.getMapOfDetectorsToSequenceCells();
        for (Detector detector : detectorToSequenceCellMap.keySet()) {
            if (detector.isFaraday() == isFaraday) {
                int detectorDataColumnIndex = detector.getOrdinalIndex();
                List<SequenceCell> sequenceCells = detectorToSequenceCellMap.get(detector);
                for (SequenceCell sequenceCell : sequenceCells) {
                    String onPeakID = sequenceCell.getSequenceId();
                    SpeciesRecordInterface targetSpecies = sequenceCell.getTargetSpecies();
                    int speciesOrdinalIndex = speciesList.indexOf(targetSpecies) + 1;
                    List<Integer> onPeakIndices = mapOfOnPeakIdsToIndices.get(onPeakID);
                    Collections.sort(onPeakIndices);
                    for (Integer index : onPeakIndices) {
                        detectorOrdinalIndicesAccumulatorList.add(detectorDataColumnIndex);
                        double intensity = onPeakIntensities[index][detectorDataColumnIndex];
                        double amplifierResistance = detector.getAmplifierResistanceInOhms();
                        if (MassSpectrometerContextEnum.PHOENIX == analysisMethod.getMassSpectrometerContext() && isFaraday) {
                            // convert all volts to counts to bring all files into alignment
                            intensity = intensity * (ONE_COULOMB / amplifierResistance);
                        }
                        intensityAccumulatorList.add(intensity);
                        timeAccumulatorList.add(onPeakTimeStamps[index]);
                        timeIndexAccumulatorList.add(index);
                        isotopeOrdinalIndicesAccumulatorList.add(speciesOrdinalIndex);
                    }
                }
            }
        }

        return new SingleBlockDataSetRecord.SingleBlockDataRecord(
                massSpecOutputSingleBlockRecord.blockNumber(),
                detectorOrdinalIndicesAccumulatorList,
                intensityAccumulatorList,
                timeAccumulatorList,
                timeIndexAccumulatorList,
                isotopeOrdinalIndicesAccumulatorList);
    }

}