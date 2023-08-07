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

import org.cirdles.tripoli.species.SpeciesRecordInterface;
import org.ojalgo.matrix.store.Primitive64Store;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author James F. Bowring
 */
public record SingleBlockDataSetRecord(
        int blockNumber,
        SingleBlockDataRecord baselineDataSetMCMC,
        SingleBlockDataRecord onPeakFaradayDataSetMCMC,
        SingleBlockDataRecord onPeakPhotoMultiplierDataSetMCMC,
        Primitive64Store blockKnotInterpolationStore,
        int[] blockCycleArray,
        double[] blockRawDataArray,
        int[] blockDetectorOrdinalIndicesArray,
        int[] blockIsotopeOrdinalIndicesArray,
        int[] blockTimeIndicesArray,
        int[] onPeakStartingIndicesOfCycles,
        Map<SpeciesRecordInterface, boolean[]> mapOfSpeciesToActiveCycles,
        Map<String, List<Double>> blockMapIdsToDataTimes) implements Serializable {
    public int getCountOfBaselineIntensities() {
        return baselineDataSetMCMC().intensityAccumulatorList().size();
    }

    public int getCountOfOnPeakFaradayIntensities() {
        return onPeakFaradayDataSetMCMC().intensityAccumulatorList().size();
    }

    /**
     * @author James F. Bowring
     */
    public record SingleBlockDataRecord(
            int blockNumber,
            List<Integer> detectorOrdinalIndicesAccumulatorList,
            List<Integer> cycleAccumulatorList,
            List<Double> intensityAccumulatorList,
            List<Double> timeAccumulatorList,
            List<Integer> timeIndexAccumulatorList,
            List<Integer> isotopeOrdinalIndicesAccumulatorList,
            java.util.Map<String, List<Double>> blockMapOfIdsToData) implements Serializable {


    }


}