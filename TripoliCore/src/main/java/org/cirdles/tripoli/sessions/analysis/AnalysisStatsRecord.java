package org.cirdles.tripoli.sessions.analysis;

public record AnalysisStatsRecord(
        BlockStatsRecord[] blockStatsRecords,
        double weightedMean // see package org.cirdles.tripoli.utilities.mathUtilities.weightedMeans;
) {
}