package org.cirdles.tripoli.visualizationUtilities.histograms;

import java.io.Serializable;

public record HistogramRecord(
        double[] data,
        int binCount,
        double[] binCounts,
        double binWidth,
        double[] binCenters
) implements Serializable {
}