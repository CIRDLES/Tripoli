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

import org.cirdles.tripoli.species.IsotopicRatio;
import org.cirdles.tripoli.species.SpeciesRecordInterface;

import java.io.Serializable;
import java.util.Map;

public record SingleBlockModelRecord(
        int blockID,
        int faradayCount,
        int cycleCount,
        int isotopeCount,
        SpeciesRecordInterface highestAbundanceSpecies,
        double[] baselineMeansArray,
        double[] baselineStandardDeviationsArray,
        double detectorFaradayGain,
        Map<Integer, Integer> mapDetectorOrdinalToFaradayIndex,
        double[] logRatios,
        Map<SpeciesRecordInterface, boolean[]> mapOfSpeciesToActiveCycles,
        //TODO: convert to logratios?
        Map<IsotopicRatio, Map<Integer, double[]>> mapLogRatiosToCycleStats,
        double[] dataModelArray,
        double[] dataSignalNoiseArray,
        double[] I0,
        double[] intensities
) implements Serializable {

    public double[] getOnPeakDataModelFaradayArray(int countOfBaselineDataEntries, int countOfFaradayDataEntries) {
        double[] onPeakDataModelFaradayArray = new double[countOfFaradayDataEntries];
        System.arraycopy(dataModelArray, countOfBaselineDataEntries, onPeakDataModelFaradayArray, 0, countOfFaradayDataEntries);
        return onPeakDataModelFaradayArray;
    }

    public double[] getOnPeakDataModelPhotoMultiplierArray(int countOfBaselineDataEntries, int countOfFaradayDataEntries) {
        double[] onPeakDataModelPhotoMultiplierArray = new double[countOfFaradayDataEntries];
        System.arraycopy(dataModelArray, countOfBaselineDataEntries + countOfFaradayDataEntries,
                onPeakDataModelPhotoMultiplierArray, 0, dataModelArray.length - countOfBaselineDataEntries - countOfFaradayDataEntries);
        return onPeakDataModelPhotoMultiplierArray;
    }

    public int sizeOfModel() {
        return logRatios().length + I0.length + faradayCount() + 1;
    }

    public double[] assembleCycleMeansForRatio(IsotopicRatio ratio) {
        Map<Integer, double[]> mapCycleToStats = mapLogRatiosToCycleStats.get(ratio);
        double[] cycleMeans = new double[cycleCount];
        if (mapCycleToStats != null) {
            for (int cycleIndex = 0; cycleIndex < mapCycleToStats.keySet().size(); cycleIndex++) {
                cycleMeans[cycleIndex] = mapCycleToStats.get(cycleIndex)[0];
            }
        } else {
            cycleMeans = calculateDerivedRatioMean(ratio);
        }
        return cycleMeans;
    }

    public double[] assembleCycleStdDevForRatio(IsotopicRatio ratio) {
        Map<Integer, double[]> mapCycleToStats = mapLogRatiosToCycleStats().get(ratio);
        double[] cycleStdDev = new double[cycleCount];
        if (mapCycleToStats != null) {
            for (int cycleIndex = 0; cycleIndex < mapCycleToStats.keySet().size(); cycleIndex++) {
                cycleStdDev[cycleIndex] = mapCycleToStats.get(cycleIndex)[1];
            }
        } else {
            cycleStdDev = calculateDerivedRatioOneSigma(ratio);
        }
        return cycleStdDev;
    }

    private double[] calculateDerivedRatioMean(IsotopicRatio derivedRatio) {
        double[] cycleMeans = new double[cycleCount];
        SpeciesRecordInterface numerator = derivedRatio.getNumerator();
        SpeciesRecordInterface denominator = derivedRatio.getDenominator();

        if (numerator.equals(highestAbundanceSpecies)) {
            IsotopicRatio targetRatio = new IsotopicRatio(denominator, highestAbundanceSpecies, false);
            Map<Integer, double[]> mapCycleToStats = mapLogRatiosToCycleStats().get(targetRatio);
            for (int cycleIndex = 0; cycleIndex < cycleCount; cycleIndex++) {
                cycleMeans[cycleIndex] = 1.0 / mapCycleToStats.get(cycleIndex)[0];
            }
        } else {
            IsotopicRatio numRatio = null;
            IsotopicRatio denRatio = null;
            for (IsotopicRatio isoRatio : mapLogRatiosToCycleStats.keySet()) {
                if (isoRatio.getNumerator().equals(numerator)) {
                    numRatio = isoRatio;
                }
                if (isoRatio.getNumerator().equals(denominator)) {
                    denRatio = isoRatio;
                }
            }
            Map<Integer, double[]> mapCycleToStatsNumRatio = mapLogRatiosToCycleStats().get(numRatio);
            Map<Integer, double[]> mapCycleToStatsDenRatio = mapLogRatiosToCycleStats().get(denRatio);
            for (int cycleIndex = 0; cycleIndex < cycleCount; cycleIndex++) {
                cycleMeans[cycleIndex] = mapCycleToStatsNumRatio.get(cycleIndex)[0] / mapCycleToStatsDenRatio.get(cycleIndex)[0];
            }
        }

        return cycleMeans;
    }

    private double[] calculateDerivedRatioOneSigma(IsotopicRatio derivedRatio) {
        double[] cycleOneSigmas = new double[cycleCount];
        SpeciesRecordInterface numerator = derivedRatio.getNumerator();
        SpeciesRecordInterface denominator = derivedRatio.getDenominator();
// need to recalculate
        if (numerator.equals(highestAbundanceSpecies)) {
            IsotopicRatio targetRatio = new IsotopicRatio(denominator, highestAbundanceSpecies, false);
//            Map<Integer, double[]> mapCycleToStats = mapLogRatiosToCycleStats().get(targetRatio);
//            for (int cycleIndex = 0; cycleIndex < cycleCount; cycleIndex++){
//                cycleOneSigmas[cycleIndex] = 1.0 / mapCycleToStats.get(cycleIndex)[1];
//            }
        } else {


        }

        return cycleOneSigmas;
    }
}