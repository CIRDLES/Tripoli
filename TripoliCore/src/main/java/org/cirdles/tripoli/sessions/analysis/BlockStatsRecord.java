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

package org.cirdles.tripoli.sessions.analysis;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.Serializable;

public record BlockStatsRecord(
        int blockID,
        boolean blockIncluded,
        boolean isRatio,
        boolean isInverted, double[] cycleMeansData,
        boolean[] cyclesIncluded,
        double mean,
        double variance,
        double standardDeviation,
        double standardError
) implements Serializable {
    /**
     * // BLOCK MODE Functions calculate stats on logRatios (of Ratios) and user functions:
     * see https://docs.google.com/document/d/14PPEDEJPylNMavpJDpYSuemNb0gF5dz_To3Ek1Y_Agw/edit#bookmark=id.3tts8ahgz00i
     *
     * @param blockID
     * @param blockIncluded
     * @param isRatio
     * @param isInverted
     * @param cycleMeansData
     * @return
     */
    public static BlockStatsRecord generateBlockStatsRecord(int blockID, boolean blockIncluded, boolean isRatio, boolean isInverted, double[] cycleMeansData, boolean[] cyclesIncluded) {
        DescriptiveStatistics descriptiveStatisticsBlockStats = new DescriptiveStatistics();
        for (int i = 0; i < cycleMeansData.length; i++) {
            if (cyclesIncluded[i]) {
                if (isRatio) {
                    // trap for negative ratios == no log possible
                    if (cycleMeansData[i] > 0.0) {
                        if (isInverted) {
                            descriptiveStatisticsBlockStats.addValue(-StrictMath.log(cycleMeansData[i]));
                        } else {
                            descriptiveStatisticsBlockStats.addValue(StrictMath.log(cycleMeansData[i]));
                        }
                    } else {
                        // Issue #214 decided to keep <= 0 value and convert ratio to userfunction before code gets here
                        cyclesIncluded[i] = false;
                    }
                } else {
                    descriptiveStatisticsBlockStats.addValue(cycleMeansData[i]);
                }
            }
        }
        double mean = descriptiveStatisticsBlockStats.getMean();
        double variance = descriptiveStatisticsBlockStats.getVariance();
        double standardDeviation = descriptiveStatisticsBlockStats.getStandardDeviation();
        double standardError = StrictMath.sqrt(variance / descriptiveStatisticsBlockStats.getN());

        return new BlockStatsRecord(
                blockID,
                blockIncluded,
                isRatio,
                isInverted,
                cycleMeansData,
                cyclesIncluded,
                mean,
                variance,
                standardDeviation,
                standardError
        );
    }
}