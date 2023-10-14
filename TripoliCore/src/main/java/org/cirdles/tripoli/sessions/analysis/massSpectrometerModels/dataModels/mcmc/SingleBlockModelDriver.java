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

import com.google.common.primitives.Doubles;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputSingleBlockRecord;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.species.SpeciesRecordInterface;
import org.cirdles.tripoli.utilities.callbacks.LoggingCallbackInterface;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.mathUtilities.SplineBasisModel;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.io.IOException;
import java.util.*;

import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockModelInitForMCMC.initializeModelForSingleBlockMCMC;

/**
 * @author James F. Bowring
 */
public enum SingleBlockModelDriver {
    ;

    public static PlotBuilder[][] buildAndRunModelForSingleBlock(int blockID, AnalysisInterface analysis, LoggingCallbackInterface loggingCallback) throws TripoliException, IOException {
        MassSpecExtractedData massSpecExtractedData = analysis.getMassSpecExtractedData();
        AnalysisMethod analysisMethod = analysis.getAnalysisMethod();
        PlotBuilder[][] plotBuilder = new PlotBuilder[0][0];

        SingleBlockRawDataSetRecord singleBlockRawDataSetRecord = prepareSingleBlockDataForMCMC(blockID, massSpecExtractedData, analysisMethod);
        SingleBlockModelInitForMCMC.SingleBlockModelRecordWithCov singleBlockInitialModelRecordWithCov;
        try {
            singleBlockInitialModelRecordWithCov = initializeModelForSingleBlockMCMC(analysis, analysisMethod, singleBlockRawDataSetRecord, true);
        } catch (RecoverableCondition e) {
            throw new TripoliException("Ojalgo RecoverableCondition");
        }

        if (null != singleBlockInitialModelRecordWithCov) {
            MCMCProcess mcmcProcess = MCMCProcess.createMCMCProcess(analysis, singleBlockRawDataSetRecord, singleBlockInitialModelRecordWithCov);
            mcmcProcess.initializeMCMCProcess();
            plotBuilder = mcmcProcess.applyInversionWithAdaptiveMCMC(loggingCallback);
        }

        return plotBuilder;
    }

    static SingleBlockRawDataSetRecord prepareSingleBlockDataForMCMC(int blockNumber, MassSpecExtractedData massSpecExtractedData, AnalysisMethod analysisMethod) {
        SingleBlockRawDataSetRecord singleBlockRawDataSetRecord = null;
        MassSpecOutputSingleBlockRecord massSpecOutputSingleBlockRecord = massSpecExtractedData.getBlocksData().get(blockNumber);
        if (massSpecOutputSingleBlockRecord != null) {
            Primitive64Store blockKnotInterpolationStore;
            if (analysisMethod.isUseLinearKnots()) {
                // TODO: the following line invokes a replication of the linear knots from Burdick's matlab code
                blockKnotInterpolationStore = generateLinearKnotsMatrixReplicaOfBurdickMatLab(massSpecOutputSingleBlockRecord);
            } else {
                blockKnotInterpolationStore = generateKnotsMatrixForBlock(massSpecOutputSingleBlockRecord, 3);
            }
            SingleBlockRawDataSetRecord.SingleBlockRawDataRecord baselineDataSetMCMC =
                    SingleBlockDataAccumulatorMCMC.accumulateBaselineDataPerBaselineTableSpecs(massSpecOutputSingleBlockRecord, analysisMethod);
            SingleBlockRawDataSetRecord.SingleBlockRawDataRecord onPeakFaradayDataSetMCMC =
                    SingleBlockDataAccumulatorMCMC.accumulateOnPeakDataPerSequenceTableSpecs(massSpecOutputSingleBlockRecord, analysisMethod, true);
            SingleBlockRawDataSetRecord.SingleBlockRawDataRecord onPeakPhotoMultiplierDataSetMCMC =
                    SingleBlockDataAccumulatorMCMC.accumulateOnPeakDataPerSequenceTableSpecs(massSpecOutputSingleBlockRecord, analysisMethod, false);

            List<Integer> cycleList = new ArrayList<>();
            cycleList.addAll(baselineDataSetMCMC.cycleAccumulatorList());
            cycleList.addAll(onPeakFaradayDataSetMCMC.cycleAccumulatorList());
            cycleList.addAll(onPeakPhotoMultiplierDataSetMCMC.cycleAccumulatorList());
            int[] blockCycleArray = cycleList.stream().mapToInt(i -> i).toArray();

            List<Double> blockIntensityList = new ArrayList<>();
            blockIntensityList.addAll(baselineDataSetMCMC.intensityAccumulatorList());
            blockIntensityList.addAll(onPeakFaradayDataSetMCMC.intensityAccumulatorList());
            blockIntensityList.addAll(onPeakPhotoMultiplierDataSetMCMC.intensityAccumulatorList());
            double[] blockRawDataArray = Doubles.toArray(blockIntensityList);

            List<Integer> blockDetectorOrdinalIndicesList = new ArrayList<>();
            blockDetectorOrdinalIndicesList.addAll(baselineDataSetMCMC.detectorOrdinalIndicesAccumulatorList());
            blockDetectorOrdinalIndicesList.addAll(onPeakFaradayDataSetMCMC.detectorOrdinalIndicesAccumulatorList());
            blockDetectorOrdinalIndicesList.addAll(onPeakPhotoMultiplierDataSetMCMC.detectorOrdinalIndicesAccumulatorList());
            int[] blockDetectorOrdinalIndicesArray = blockDetectorOrdinalIndicesList.stream().mapToInt(i -> i).toArray();

            List<Integer> blockIsotopeOrdinalIndicesList = new ArrayList<>();
            blockIsotopeOrdinalIndicesList.addAll(baselineDataSetMCMC.isotopeOrdinalIndicesAccumulatorList());
            blockIsotopeOrdinalIndicesList.addAll(onPeakFaradayDataSetMCMC.isotopeOrdinalIndicesAccumulatorList());
            blockIsotopeOrdinalIndicesList.addAll(onPeakPhotoMultiplierDataSetMCMC.isotopeOrdinalIndicesAccumulatorList());
            int[] blockIsotopeOrdinalIndicesArray = blockIsotopeOrdinalIndicesList.stream().mapToInt(i -> i).toArray();

            List<Integer> blockTimeIndicesList = new ArrayList<>();
            blockTimeIndicesList.addAll(baselineDataSetMCMC.timeIndexAccumulatorList());
            blockTimeIndicesList.addAll(onPeakFaradayDataSetMCMC.timeIndexAccumulatorList());
            blockTimeIndicesList.addAll(onPeakPhotoMultiplierDataSetMCMC.timeIndexAccumulatorList());
            int[] blockTimeIndicesArray = blockTimeIndicesList.stream().mapToInt(i -> i).toArray();

            int[] onPeakStartingIndicesOfCycles = massSpecOutputSingleBlockRecord.onPeakStartingIndicesOfCycles();

            Map<String, List<Double>> blockMapIdsToDataTimes = new TreeMap<>();
            for (String id : onPeakFaradayDataSetMCMC.blockMapOfIdsToData().keySet()) {
                if (!blockMapIdsToDataTimes.containsKey(id)) {
                    blockMapIdsToDataTimes.put(id, new ArrayList<>());
                }
            }
            for (String id : onPeakPhotoMultiplierDataSetMCMC.blockMapOfIdsToData().keySet()) {
                if (!blockMapIdsToDataTimes.containsKey(id)) {
                    blockMapIdsToDataTimes.put(id, new ArrayList<>());
                }
            }
            for (String id : blockMapIdsToDataTimes.keySet()) {
                if (onPeakFaradayDataSetMCMC.blockMapOfIdsToData().get(id) != null) {
                    blockMapIdsToDataTimes.get(id).addAll(onPeakFaradayDataSetMCMC.blockMapOfIdsToData().get(id));
                }
                if (onPeakPhotoMultiplierDataSetMCMC.blockMapOfIdsToData().get(id) != null) {
                    blockMapIdsToDataTimes.get(id).addAll(onPeakPhotoMultiplierDataSetMCMC.blockMapOfIdsToData().get(id));
                }
            }

            boolean[] activeCycles = new boolean[onPeakStartingIndicesOfCycles.length];
            Arrays.fill(activeCycles, true);
            List<SpeciesRecordInterface> species = analysisMethod.getSpeciesList();
            Map<SpeciesRecordInterface, boolean[]> mapOfSpeciesToActiveCycles = new TreeMap<>();
            for (SpeciesRecordInterface specie : species) {
                mapOfSpeciesToActiveCycles.put(specie, activeCycles.clone());
            }

            singleBlockRawDataSetRecord =
                    new SingleBlockRawDataSetRecord(blockNumber, baselineDataSetMCMC, onPeakFaradayDataSetMCMC, onPeakPhotoMultiplierDataSetMCMC, blockKnotInterpolationStore.toRawCopy2D(),
                            blockCycleArray, blockRawDataArray, blockDetectorOrdinalIndicesArray, blockIsotopeOrdinalIndicesArray, blockTimeIndicesArray,
                            onPeakStartingIndicesOfCycles, mapOfSpeciesToActiveCycles, blockMapIdsToDataTimes);
        }
        return singleBlockRawDataSetRecord;
    }

    private static Primitive64Store generateKnotsMatrixForBlock(
            MassSpecOutputSingleBlockRecord massSpecOutputSingleBlockRecord, int basisDegree) {

        int knotCount = massSpecOutputSingleBlockRecord.onPeakStartingIndicesOfCycles().length + 1;
        double[] timeStamps = massSpecOutputSingleBlockRecord.onPeakTimeStamps();

        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        Primitive64Store bBaseOutput = SplineBasisModel.bBase(
                storeFactory.rows(massSpecOutputSingleBlockRecord.onPeakTimeStamps()),
                timeStamps[0],
                timeStamps[timeStamps.length - 1],
                knotCount - basisDegree,
                basisDegree);

        return bBaseOutput;
    }

    private static Primitive64Store generateLinearKnotsMatrixReplicaOfBurdickMatLab(MassSpecOutputSingleBlockRecord massSpecOutputSingleBlockRecord) {
        // build InterpMat for block using linear approach
        // the general approach for a block is to create a knot at the start of each cycle and
        // linearly interpolate between knots to create fractional placement of each recorded timestamp
        // which takes the form of (1 - fractional distance of time with knot range, fractional distance of time with knot range)

        int[] onPeakStartingIndicesOfCycles = massSpecOutputSingleBlockRecord.onPeakStartingIndicesOfCycles();
        int cycleCount = onPeakStartingIndicesOfCycles.length;
        int knotCount = cycleCount + 1;
        int onPeakDataEntriesCount = massSpecOutputSingleBlockRecord.onPeakCycleNumbers().length;

        double[][] interpMatArrayForBlock = new double[knotCount][onPeakDataEntriesCount];
        for (int cycleIndex = 0; cycleIndex < cycleCount; cycleIndex++) {
            boolean lastCycle = false;
            int startOfCycleIndex = onPeakStartingIndicesOfCycles[cycleIndex];
            int startOfNextCycleIndex;
            if (cycleIndex == cycleCount - 1) {
                // last cycle
                startOfNextCycleIndex = onPeakDataEntriesCount - 1;
                lastCycle = true;
            } else {
                startOfNextCycleIndex = onPeakStartingIndicesOfCycles[cycleIndex + 1];
            }

            double[] timeStamp = massSpecOutputSingleBlockRecord.onPeakTimeStamps();
            int countOfEntries = onPeakStartingIndicesOfCycles[cycleIndex] - onPeakStartingIndicesOfCycles[0];
            double deltaTimeStamp = timeStamp[startOfNextCycleIndex] - timeStamp[startOfCycleIndex];

            for (int timeIndex = startOfCycleIndex; timeIndex < startOfNextCycleIndex; timeIndex++) {
                interpMatArrayForBlock[cycleIndex][(timeIndex - startOfCycleIndex) + countOfEntries] =
                        1.0 - (timeStamp[timeIndex] - timeStamp[startOfCycleIndex]) / deltaTimeStamp;
                interpMatArrayForBlock[cycleIndex + 1][(timeIndex - startOfCycleIndex) + countOfEntries] =
                        (timeStamp[timeIndex] - timeStamp[startOfCycleIndex]) / deltaTimeStamp;
            }

            if (lastCycle) {
                interpMatArrayForBlock[cycleIndex][countOfEntries + startOfNextCycleIndex - startOfCycleIndex] = 0.0;
                interpMatArrayForBlock[cycleIndex + 1][countOfEntries + startOfNextCycleIndex - startOfCycleIndex] = 1.0;
            }
        }
        // generate matrix and then transpose it to match matlab
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        storeFactory.rows(interpMatArrayForBlock).limits(knotCount, onPeakDataEntriesCount).transpose().toRawCopy2D();

        return Primitive64Store.FACTORY.rows(storeFactory.rows(interpMatArrayForBlock).limits(knotCount, onPeakDataEntriesCount).transpose().toRawCopy2D());
    }
}