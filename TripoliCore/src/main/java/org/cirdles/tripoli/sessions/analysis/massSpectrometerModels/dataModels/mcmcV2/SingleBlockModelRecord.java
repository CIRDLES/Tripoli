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

import java.io.Serializable;
import java.util.Map;

public record SingleBlockModelRecord(
        double[] baselineMeansArray,
        double[] baselineStandardDeviationsArray,
        double detectorFaradayGain,
        Map<Integer, Integer> mapDetectorOrdinalToFaradayIndex,
        double[] logRatios,
        double[] signalNoiseSigma,
        double[] dataArray,
        double[] dataWithNoBaselineArray,
        double[] dataSignalNoiseArray,
        double[] I0,
        double[] intensities,
        int faradayCount,
        int isotopeCount
) implements Serializable {
    public SingleBlockModelRecord clone() {
        return new SingleBlockModelRecord(
                baselineMeansArray.clone(),
                baselineStandardDeviationsArray.clone(),
                detectorFaradayGain,
                mapDetectorOrdinalToFaradayIndex,
                logRatios.clone(),
                signalNoiseSigma.clone(),
                dataArray.clone(),
                dataWithNoBaselineArray.clone(),
                dataSignalNoiseArray.clone(),
                I0.clone(),
                intensities.clone(),
                faradayCount,
                isotopeCount
        );
    }
}