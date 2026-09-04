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
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.plots.compoundPlotBuilders.PlotBlockCyclesRecord;
import org.cirdles.tripoli.utilities.mathUtilities.FormatterForSigFigN;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.StrictMath.abs;
import static org.cirdles.tripoli.sessions.analysis.GeometricMeanStatsRecord.generateGeometricMeanStats;

public record AnalysisStatsRecord(
        boolean isRatio,
        BlockStatsRecord[] blockStatsRecords,
        double blockModeWeightedMean, // see package org.cirdles.tripoli.utilities.mathUtilities.weightedMeans;
        double blockModeWeightedMeanOneSigma,
        double blockModeChiSquared,
        int countOfIncludedBlocks,
        double cycleModeMean,
        double cycleModeVariance,
        double cycleModeStandardDeviation,
        double cycleModeStandardError,
        boolean[] cycleModeIncluded,
        double[] cycleModeData,
        int countOfTotalCycles,
        int countOfIncludedCycles) implements Serializable {

    /// TODO: fix signature as userFunction holds all
    public static BlockStatsRecord[] generateAnalysisBlockStatsRecords(UserFunction userFunction, Map<Integer, PlotBlockCyclesRecord> mapBlockIdToBlockCyclesRecord) {
        // Jan 2024 new approach - two modes: block mode and cycle mode
        // BLOCK MODE will be default - calculate and plot stats for each block
        // October 2024 - Block mode is being abandoned for now
        int blockCount = mapBlockIdToBlockCyclesRecord.size();
        BlockStatsRecord[] blockStatsRecords = new BlockStatsRecord[blockCount];
        int arrayIndex = 0;
        for (Map.Entry<Integer, PlotBlockCyclesRecord> entry : mapBlockIdToBlockCyclesRecord.entrySet()) {
            PlotBlockCyclesRecord plotBlockCyclesRecord = entry.getValue();
            if (plotBlockCyclesRecord != null) {
                blockStatsRecords[arrayIndex] = BlockStatsRecord.generateBlockStatsRecord(
                        plotBlockCyclesRecord.blockID(), plotBlockCyclesRecord.blockIncluded(), userFunction.isTreatAsIsotopicRatio(),
                        userFunction.isInverted(), plotBlockCyclesRecord.cycleMeansData(), plotBlockCyclesRecord.cyclesIncluded());
            }
            arrayIndex++;
        }
        return blockStatsRecords;
    }

    public static AnalysisStatsRecord generateAnalysisStatsRecord(BlockStatsRecord[] blockStatsRecords) {
        int countOfIncludedBlocks = 0;
        double wmNumerator = 0.0;
        double wmDenominator = 0.0;
        double weightedMeanC;
        double weightedMeanOneSigmaSquaredC;
        double weightedMeanOneSigmaC;
        double chiSquaredTerm = 0.0;
        double chiSquaredC;

        DescriptiveStatistics cycleModeDescriptiveStats = new DescriptiveStatistics();
        List<double[]> cycleModeDataByBlocks = new ArrayList<>();
        List<boolean[]> cycleModeIncludedByBlocks = new ArrayList<>();
        int countOfTotalCycles = 0;

        for (BlockStatsRecord blockStatsRecord : blockStatsRecords) {
            cycleModeDataByBlocks.add(blockStatsRecord.cycleMeansData());
            cycleModeIncludedByBlocks.add(blockStatsRecord.cyclesIncluded());
            countOfTotalCycles += blockStatsRecord.cyclesIncluded().length;

            //todo fix or remove blockincludedflag
            if (blockStatsRecord.blockIncluded()) {
                wmNumerator += blockStatsRecord.mean() / StrictMath.pow(blockStatsRecord.standardDeviation(), 2);
                wmDenominator += 1.0 / StrictMath.pow(blockStatsRecord.standardDeviation(), 2);
                for (int cycleIndex = 0; cycleIndex < blockStatsRecord.cycleMeansData().length; cycleIndex++) {
                    if (blockStatsRecord.cyclesIncluded()[cycleIndex]) {
                        if (blockStatsRecords[0].isRatio()) {
                            if (blockStatsRecords[0].isInverted()) {
                                cycleModeDescriptiveStats.addValue(-StrictMath.log(blockStatsRecord.cycleMeansData()[cycleIndex]));
                            } else {
                                cycleModeDescriptiveStats.addValue(StrictMath.log(blockStatsRecord.cycleMeansData()[cycleIndex]));
                            }
                        } else {
                            cycleModeDescriptiveStats.addValue(blockStatsRecord.cycleMeansData()[cycleIndex]);
                        }
                    }
                }
            }
        }
        weightedMeanC = wmNumerator / wmDenominator;
        weightedMeanOneSigmaSquaredC = 1.0 / wmDenominator;
        weightedMeanOneSigmaC = StrictMath.sqrt(weightedMeanOneSigmaSquaredC);

        for (BlockStatsRecord blockStatsRecord : blockStatsRecords) {
            if (blockStatsRecord.blockIncluded()) {
                chiSquaredTerm += StrictMath.pow(blockStatsRecord.mean() - weightedMeanC, 2) / weightedMeanOneSigmaSquaredC;
                countOfIncludedBlocks++;
            }
        }
        chiSquaredC = chiSquaredTerm / (countOfIncludedBlocks - 1);


        double cycleModeMean = cycleModeDescriptiveStats.getMean();
        double cycleModeVariance = cycleModeDescriptiveStats.getVariance();
        double cycleModeStandardDeviation = cycleModeDescriptiveStats.getStandardDeviation();
        double cycleModeStandardError = StrictMath.sqrt(cycleModeVariance / cycleModeDescriptiveStats.getN());

        boolean[] cycleModeIncluded = new boolean[countOfTotalCycles];
        double[] cycleModeData = new double[countOfTotalCycles];
        int index = 0;
        for (boolean[] cyclesIncluded : cycleModeIncludedByBlocks) {
            for (boolean b : cyclesIncluded) {
                cycleModeIncluded[index] = b;
                index++;
            }
        }

        index = 0;
        for (double[] cycleData : cycleModeDataByBlocks) {
            for (double cycleDatum : cycleData) {
                cycleModeData[index] = cycleDatum;
                index++;
            }
        }

        return new AnalysisStatsRecord(
                blockStatsRecords.length > 0 && blockStatsRecords[0].isRatio(),
                blockStatsRecords,
                weightedMeanC,
                weightedMeanOneSigmaC,
                chiSquaredC,
                countOfIncludedBlocks,
                cycleModeMean,
                cycleModeVariance,
                cycleModeStandardDeviation,
                cycleModeStandardError,
                cycleModeIncluded,
                cycleModeData,
                countOfTotalCycles,
                (int) cycleModeDescriptiveStats.getN());
    }

    public static String prettyPrintRatioCycleMean(UserFunction userFunction) {
        /*
                Round the (1-sigma percent) standard error and (1-sigma percent) standard deviation to two significant decimal places.
                 If there is a (+ and -) display on either because they are different, round both to the number of decimal places
                 belonging to the smallest increment.  So, below, 0.85 gets rounded to the hundredths to get two significant figures.,
                 For the Percent standard deviation, +10.0 and -9.1 get rounded to the tenths decimal place, matching the smallest
                 increment (tenths vs. the ones places for the +10).
                 Use two significant figures of the 1-sigma absolute standard error to determine where to round the mean.
                 */
        // todo: refactor this code which duplicates cyclesplot code
        String cycleMean = "";
        if (userFunction.getAnalysisStatsRecord() != null) {
            AnalysisStatsRecord analysisStatsRecord = userFunction.getAnalysisStatsRecord();
            GeometricMeanStatsRecord geometricMeanStatsRecord =
                    generateGeometricMeanStats(analysisStatsRecord.cycleModeMean(), analysisStatsRecord.cycleModeStandardDeviation(), analysisStatsRecord.cycleModeStandardError());
            double geoMean = geometricMeanStatsRecord.geoMean();
            if (!Double.isNaN(geoMean)) {
                double geoMeanPlusOneStandardDeviation = geometricMeanStatsRecord.geoMeanPlusOneStdDev();

                FormatterForSigFigN.FormattedStats formattedStats;
                if ((abs(geoMean) >= 1e7) || (abs(geoMean) <= 1e-5)) {
                    formattedStats = FormatterForSigFigN.formatToScientific(geoMean, geoMeanPlusOneStandardDeviation - geoMean, 0, 2).padLeft();
                } else {
                    formattedStats = FormatterForSigFigN.formatToSigFig(geoMean, geoMeanPlusOneStandardDeviation - geoMean, 0, 2).padLeft();
                }
                cycleMean = formattedStats.meanAsString();
            }
        }
        return "x̄=" + cycleMean;
    }
}