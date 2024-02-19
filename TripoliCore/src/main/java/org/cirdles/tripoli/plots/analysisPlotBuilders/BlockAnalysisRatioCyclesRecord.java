package org.cirdles.tripoli.plots.analysisPlotBuilders;

import org.cirdles.tripoli.plots.compoundPlotBuilders.BlockCyclesRecord;

import java.io.Serializable;
import java.util.Map;

public record BlockAnalysisRatioCyclesRecord(
        org.cirdles.tripoli.species.IsotopicRatio isotopicRatio,
        Map<Integer, BlockCyclesRecord> mapBlockIdToBlockRatioCyclesRecord,
        int cyclesPerBlock,
        double analysisDalyFaradayGainMean,
        double analysisDalyFaradayGainOneSigmaAbs,
        double analysisMean,
        double analysisOneSigma,
        String[] title,
        String xAxisLabel,
        String yAxisLabel
) implements Serializable {
}