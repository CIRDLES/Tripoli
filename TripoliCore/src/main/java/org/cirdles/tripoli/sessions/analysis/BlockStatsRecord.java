package org.cirdles.tripoli.sessions.analysis;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public record BlockStatsRecord(
        int blockID,
        int cyclesCount,
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
     * @param ratio
     * @param data
     * @return
     */
    public static BlockStatsRecord generateBlockStatsRecord(int blockID, boolean ratio, double[] data, int cyclesCount) {
        DescriptiveStatistics descriptiveStatisticsBlockStats = new DescriptiveStatistics();
        for (int i = 0; i < data.length; i++) {
            if (ratio) {
                descriptiveStatisticsBlockStats.addValue(StrictMath.log(data[i]));
            } else {
                descriptiveStatisticsBlockStats.addValue(data[i]);
            }
        }
        double mean = descriptiveStatisticsBlockStats.getMean();
        double variance = descriptiveStatisticsBlockStats.getVariance();
        double standardDeviation = descriptiveStatisticsBlockStats.getStandardDeviation();
        double standardError = StrictMath.sqrt(variance / data.length);

        return new BlockStatsRecord(
                blockID,
                cyclesCount,
                mean,
                variance,
                standardDeviation,
                standardError
        );
    }
}