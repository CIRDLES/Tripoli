package org.cirdles.tripoli.plots.analysisPlotBuilders;

import org.cirdles.tripoli.plots.histograms.HistogramRecord;

import java.io.Serializable;
import java.util.Map;

public record HistogramAnalysisRecord(
        org.cirdles.tripoli.species.IsotopicRatio ratio, int blockCount,
        Map<Integer, HistogramRecord> mapBlockIdToHistogramRecord,
        double[] blockIds,
        double[] blockMeans,
        double[] blockOneSigmas,
        double analysisMean,
        double analysisOneSigma,
        String[] title,
        String xAxisLabel,
        String yAxisLabel
) implements Serializable {
}