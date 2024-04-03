package org.cirdles.tripoli.sessions.analysis;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.List;

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
        int countOfIncludedCycles) {

    public static AnalysisStatsRecord generateAnalysisStatsRecord(BlockStatsRecord[] blockStatsRecords) {
        int countOfIncludedBlocks = 0;
        double wmNumerator = 0.0;
        double wmDenominator = 0.0;
        double weightedMeanC = 0.0;
        double weightedMeanOneSigmaSquaredC;
        double weightedMeanOneSigmaC;
        double chiSquaredTerm = 0.0;
        double chiSquaredC;

        DescriptiveStatistics cycleModeDescriptiveStats = new DescriptiveStatistics();
        List<double[]> cycleModeDataByBlocks = new ArrayList<>();
        List<boolean[]> cycleModeIncludedByBlocks = new ArrayList<>();
        int countOfTotalCycles = 0;

        for (int i = 0; i < blockStatsRecords.length; i++) {
            cycleModeDataByBlocks.add(blockStatsRecords[i].cycleMeansData());
            cycleModeIncludedByBlocks.add(blockStatsRecords[i].cyclesIncluded());
            countOfTotalCycles += blockStatsRecords[i].cyclesIncluded().length;

            //todo fix or remove blockincludedflag
            if (blockStatsRecords[i].blockIncluded()) {
                wmNumerator += blockStatsRecords[i].mean() / StrictMath.pow(blockStatsRecords[i].standardDeviation(), 2);
                wmDenominator += 1.0 / StrictMath.pow(blockStatsRecords[i].standardDeviation(), 2);
                for (int cycleIndex = 0; cycleIndex < blockStatsRecords[i].cycleMeansData().length; cycleIndex++) {
                    if (blockStatsRecords[i].cyclesIncluded()[cycleIndex]) {
                        if (blockStatsRecords[0].isRatio()) {
                            if (blockStatsRecords[0].isRatio() && blockStatsRecords[0].isInverted()) {
                                cycleModeDescriptiveStats.addValue(-StrictMath.log(blockStatsRecords[i].cycleMeansData()[cycleIndex]));
                            } else {
                                cycleModeDescriptiveStats.addValue(StrictMath.log(blockStatsRecords[i].cycleMeansData()[cycleIndex]));
                            }
                        } else {
                            cycleModeDescriptiveStats.addValue(blockStatsRecords[i].cycleMeansData()[cycleIndex]);
                        }
                    }
                }
            }
        }
        weightedMeanC = wmNumerator / wmDenominator;
        weightedMeanOneSigmaSquaredC = 1.0 / wmDenominator;
        weightedMeanOneSigmaC = StrictMath.sqrt(weightedMeanOneSigmaSquaredC);

        for (int i = 0; i < blockStatsRecords.length; i++) {
            if (blockStatsRecords[i].blockIncluded()) {
                chiSquaredTerm += StrictMath.pow(blockStatsRecords[i].mean() - weightedMeanC, 2) / weightedMeanOneSigmaSquaredC;
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
        int index =0;
        for (boolean[] cyclesIncluded : cycleModeIncludedByBlocks){
            for (int i = 0; i < cyclesIncluded.length; i ++){
                cycleModeIncluded[index] = cyclesIncluded[i];
                index++;
            }
        }

        index =0;
        for (double[] cycleData : cycleModeDataByBlocks){
            for (int i = 0; i < cycleData.length; i ++){
                cycleModeData[index] = cycleData[i];
                index++;
            }
        }

        return new AnalysisStatsRecord(
                blockStatsRecords[0].isRatio(),
                blockStatsRecords,
                weightedMeanC,
                weightedMeanOneSigmaC,
                chiSquaredC,
                countOfIncludedBlocks,
                cycleModeMean,
                cycleModeVariance,
                cycleModeStandardDeviation,
                cycleModeStandardError,
                cycleModeIncluded, cycleModeData, countOfTotalCycles,
                (int) cycleModeDescriptiveStats.getN());
    }
}