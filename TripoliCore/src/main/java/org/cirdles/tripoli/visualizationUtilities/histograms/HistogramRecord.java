package org.cirdles.tripoli.visualizationUtilities.histograms;

import java.io.Serializable;
import javafx.scene.paint.Color;

public record HistogramRecord(
        double[] data,
        double mean,
        double standardDeviation,
        int binCount,
        double[] binCounts,
        double binWidth,
        double[] binCenters,
        Color dataColor,
        String title,
        String xAxisLabel,
        String yAxisLabel
) implements Serializable {
}