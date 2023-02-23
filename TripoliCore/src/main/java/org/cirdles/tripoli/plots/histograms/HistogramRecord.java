package org.cirdles.tripoli.plots.histograms;

import java.io.Serializable;

public record HistogramRecord(
        double[] data,
        double mean,
        double standardDeviation,
        int binCount,
        double[] binCounts,
        double binWidth,
        double[] binCenters,
        String[] title,
        String xAxisLabel,
        String yAxisLabel
) implements Serializable {
}