package org.cirdles.tripoli.plots.sessionPlots;

import org.cirdles.tripoli.plots.histograms.HistogramRecord;

import java.io.Serializable;
import java.util.Map;

public record HistogramSessionRecord(
        int blockCount,
        Map<Integer, HistogramRecord> mapBlockIdToHistogramRecord,
        double[] blockIds,
        double[] blockMeans,
        double[] blockOneSigmas,
        double sessionMean,
        double sessionOneSigma,
        String[] title,
        String xAxisLabel,
        String yAxisLabel
) implements Serializable {
}