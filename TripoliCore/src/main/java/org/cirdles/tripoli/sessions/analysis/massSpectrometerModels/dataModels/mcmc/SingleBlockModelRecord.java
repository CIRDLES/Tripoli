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

import java.io.Serializable;
import java.util.Map;

public record SingleBlockModelRecord(
        int blockNumber,
        double[] baselineMeansArray,
        double[] baselineStandardDeviationsArray,
        double detectorFaradayGain,
        Map<Integer, Integer> mapDetectorOrdinalToFaradayIndex,
        double[] logRatios,
        Map<Integer, Map<Integer, double[]>> mapLogRatiosToCycleStats,
        double[] dataArray,
        double[] dataWithNoBaselineArray,
        double[] dataSignalNoiseArray,
        double[] I0,
        double[] intensities,
        int faradayCount,
        int isotopeCount
) implements Serializable {

    public int sizeOfModel() {
        return logRatios().length + I0.length + faradayCount() + 1;
    }

    public double[] assembleCycleMeansForLogRatio(int logRatioIndex) {
        Map<Integer, double[]> mapCycleToStats = mapLogRatiosToCycleStats.get(logRatioIndex);
        double[] cycleMeans = new double[mapCycleToStats.keySet().size()];
        for (int cycleIndex = 0; cycleIndex < mapCycleToStats.keySet().size(); cycleIndex++) {
            cycleMeans[cycleIndex] = mapCycleToStats.get(cycleIndex)[0];
        }
        return cycleMeans;
    }

    public double[] assembleCycleStdDevForLogRatio(int logRatioIndex) {
        Map<Integer, double[]> mapCycleToStats = mapLogRatiosToCycleStats().get(logRatioIndex);
        double[] cycleStdDev = new double[mapCycleToStats.keySet().size()];
        for (int cycleIndex = 0; cycleIndex < mapCycleToStats.keySet().size(); cycleIndex++) {
            cycleStdDev[cycleIndex] = mapCycleToStats.get(cycleIndex)[1];
        }
        return cycleStdDev;
    }
}