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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @param blockID
 * @param baselineIntensities
 * @param baselineIDs
 * @param baselineCycleNumbers
 * @param baselineIntegrationNumbers
 * @param baselineTimeStamps
 * @param baselineMasses
 * @param onPeakIntensities
 * @param onPeakIDs
 * @param onPeakCycleNumbers
 * @param onPeakIntegrationNumbers
 * @param onPeakTimeStamps
 * @param onPeakMasses
 * @param onPeakStartingIndicesOfCycles
 */
public record MassSpecOutputBlockRecordFull(
        int blockID,
        double[][] baselineIntensities,
        String[] baselineIDs,
        Map<String, List<Integer>> mapOfBaselineIdsToIndices,
        int[] baselineCycleNumbers,
        int[] baselineIntegrationNumbers,
        double[] baselineTimeStamps,
        double[] baselineMasses,
        double[][] onPeakIntensities,
        String[] onPeakIDs,
        Map<String, List<Integer>> mapOfOnPeakIdsToIndices,
        int[] onPeakCycleNumbers,
        int[] onPeakIntegrationNumbers,
        double[] onPeakTimeStamps,
        double[] onPeakMasses,
        int[] onPeakStartingIndicesOfCycles
) implements Serializable {

}