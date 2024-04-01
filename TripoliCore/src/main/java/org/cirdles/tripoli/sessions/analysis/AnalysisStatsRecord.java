package org.cirdles.tripoli.sessions.analysis;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

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
        int countOfTotalCycles = 0;
        for (int i = 0; i < blockStatsRecords.length; i++) {
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
            countOfTotalCycles += blockStatsRecords[i].cyclesIncluded().length;
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
                countOfTotalCycles,
                (int) cycleModeDescriptiveStats.getN());
    }
}