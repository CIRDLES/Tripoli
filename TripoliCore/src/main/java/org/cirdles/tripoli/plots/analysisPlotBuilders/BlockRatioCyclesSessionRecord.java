package org.cirdles.tripoli.plots.analysisPlotBuilders;

import org.cirdles.tripoli.plots.compoundPlotBuilders.BlockRatioCyclesRecord;

import java.io.Serializable;
import java.util.Map;

public record BlockRatioCyclesSessionRecord(
        org.cirdles.tripoli.species.IsotopicRatio isotopicRatio,
        Map<Integer, BlockRatioCyclesRecord> mapBlockIdToBlockRatioCyclesRecord,
        int cyclesPerBlock,
        double sessionDalyFaradayGainMean,
        double sessionDalyFaradayGainOneSigmaAbs,
        double sessionMean,
        double sessionOneSigma,
        String[] title,
        String xAxisLabel,
        String yAxisLabel
) implements Serializable {
}