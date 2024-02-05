package org.cirdles.tripoli.sessions.analysis;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public record BlockStatsRecord(
        int blockID,
        boolean blockIncluded,
        boolean isRatio,
        double[] cycleMeansData,
        boolean[] cyclesIncluded,
        double mean,
        double variance,
        double standardDeviation,
        double standardError
) {
    /**
     * // BLOCK MODE Functions calculate stats on logRatios (of Ratios) and user functions:
     * see https://docs.google.com/document/d/14PPEDEJPylNMavpJDpYSuemNb0gF5dz_To3Ek1Y_Agw/edit#bookmark=id.3tts8ahgz00i
     *
     * @param blockID
     * @param blockIncluded
     * @param isRatio
     * @param data
     * @return
     */
    public static BlockStatsRecord generateBlockStatsRecord(int blockID, boolean blockIncluded, boolean isRatio, double[] cycleMeansData, boolean[] cyclesIncluded) {
        DescriptiveStatistics descriptiveStatisticsBlockStats = new DescriptiveStatistics();
        for (int i = 0; i < cycleMeansData.length; i++) {
            if (isRatio) {
                descriptiveStatisticsBlockStats.addValue(StrictMath.log(cycleMeansData[i]));
            } else {
                descriptiveStatisticsBlockStats.addValue(cycleMeansData[i]);
            }
        }
        double mean = descriptiveStatisticsBlockStats.getMean();
        double variance = descriptiveStatisticsBlockStats.getVariance();
        double standardDeviation = descriptiveStatisticsBlockStats.getStandardDeviation();
        double standardError = StrictMath.sqrt(variance / cycleMeansData.length);

        return new BlockStatsRecord(
                blockID,
                blockIncluded,
                isRatio,
                cycleMeansData,
                cyclesIncluded,
                mean,
                variance,
                standardDeviation,
                standardError
        );
    }
}