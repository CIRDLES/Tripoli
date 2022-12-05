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

import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputSingleBlockRecord;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

/**
 * @author James F. Bowring
 */
public enum DataPrepForMCMC {
    ;

    public static void prepareSingleBlockDataForMCMC(int blockNumber, MassSpecExtractedData massSpecExtractedData) {
        MassSpecOutputSingleBlockRecord massSpecOutputSingleBlockRecord = massSpecExtractedData.getBlocksData().get(blockNumber);
        MatrixStore<Double> blockKnotInterpolationMatrixStore = generateLinearKnotsMatrix(massSpecOutputSingleBlockRecord);


    }

    private static MatrixStore<Double> generateLinearKnotsMatrix(MassSpecOutputSingleBlockRecord massSpecOutputSingleBlockRecord) {
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
        return storeFactory.rows(interpMatArrayForBlock).limits(knotCount, onPeakDataEntriesCount).transpose();
    }
}