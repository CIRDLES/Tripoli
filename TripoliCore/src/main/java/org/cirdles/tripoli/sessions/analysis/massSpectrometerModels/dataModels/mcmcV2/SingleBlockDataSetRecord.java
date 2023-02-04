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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmcV2;

import org.apache.commons.lang3.ArrayUtils;
import org.ojalgo.matrix.store.Primitive64Store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author James F. Bowring
 */
record SingleBlockDataSetRecord(
        SingleBlockDataRecord baselineDataSetMCMC,
        SingleBlockDataRecord onPeakFaradayDataSetMCMC,
        SingleBlockDataRecord onPeakPhotoMultiplierDataSetMCMC,
        Primitive64Store blockKnotInterpolationStore
) implements Serializable {
    int getCountOfBaselineIntensities() {
        return baselineDataSetMCMC().intensityAccumulatorList().size();
    }

    int getCountOfOnPeakFaradayIntensities() {
        return onPeakFaradayDataSetMCMC().intensityAccumulatorList().size();
    }

    double[] getBlockIntensityArray() {
        List<Double> blockIntensityList = new ArrayList<>();
        blockIntensityList.addAll(baselineDataSetMCMC().intensityAccumulatorList());
        blockIntensityList.addAll(onPeakFaradayDataSetMCMC().intensityAccumulatorList());
        blockIntensityList.addAll(onPeakPhotoMultiplierDataSetMCMC().intensityAccumulatorList());

        Double[] blockIntensityArray = blockIntensityList.toArray(new Double[0]);
        return ArrayUtils.toPrimitive(blockIntensityArray);
    }

    int[] getBlockDetectorOrdinalIndicesArray() {
        List<Integer> blockDetectorOrdinalIndicesList = new ArrayList<>();
        blockDetectorOrdinalIndicesList.addAll(baselineDataSetMCMC().detectorOrdinalIndicesAccumulatorList());
        blockDetectorOrdinalIndicesList.addAll(onPeakFaradayDataSetMCMC().detectorOrdinalIndicesAccumulatorList());
        blockDetectorOrdinalIndicesList.addAll(onPeakPhotoMultiplierDataSetMCMC().detectorOrdinalIndicesAccumulatorList());

        Integer[] blockDetectorOrdinalIndicesArray = blockDetectorOrdinalIndicesList.toArray(new Integer[0]);
        return ArrayUtils.toPrimitive(blockDetectorOrdinalIndicesArray);
    }

    int[] getBlockIsotopeOrdinalIndicesArray() {
        List<Integer> blockIsotopeOrdinalIndicesList = new ArrayList<>();
        blockIsotopeOrdinalIndicesList.addAll(baselineDataSetMCMC().isotopeOrdinalIndicesAccumulatorList());
        blockIsotopeOrdinalIndicesList.addAll(onPeakFaradayDataSetMCMC().isotopeOrdinalIndicesAccumulatorList());
        blockIsotopeOrdinalIndicesList.addAll(onPeakPhotoMultiplierDataSetMCMC().isotopeOrdinalIndicesAccumulatorList());

        Integer[] blockIsotopeOrdinalIndicesArray = blockIsotopeOrdinalIndicesList.toArray(new Integer[0]);
        return ArrayUtils.toPrimitive(blockIsotopeOrdinalIndicesArray);
    }

    int[] getBlockTimeIndicesArray() {
        List<Integer> blockTimeIndicesList = new ArrayList<>();
        blockTimeIndicesList.addAll(baselineDataSetMCMC().timeIndexAccumulatorList());
        blockTimeIndicesList.addAll(onPeakFaradayDataSetMCMC().timeIndexAccumulatorList());
        blockTimeIndicesList.addAll(onPeakPhotoMultiplierDataSetMCMC().timeIndexAccumulatorList());

        Integer[] blockTimeIndicesArray = blockTimeIndicesList.toArray(new Integer[0]);
        return ArrayUtils.toPrimitive(blockTimeIndicesArray);
    }

    /**
     * @author James F. Bowring
     */
    record SingleBlockDataRecord(
            List<Integer> detectorOrdinalIndicesAccumulatorList,
            List<Double> intensityAccumulatorList,
            List<Double> timeAccumulatorList,
            List<Integer> timeIndexAccumulatorList,
            List<Integer> isotopeOrdinalIndicesAccumulatorList
    ) implements Serializable {
    }


}