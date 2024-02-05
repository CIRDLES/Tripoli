package org.cirdles.tripoli.plots.analysisPlotBuilders;

import org.cirdles.tripoli.plots.compoundPlotBuilders.BlockCyclesRecord;

import java.io.Serializable;
import java.util.Map;

public record AnalysisBlockCyclesRecord(
        Map<Integer, BlockCyclesRecord> mapBlockIdToBlockCyclesRecord,
        int cyclesPerBlock,
        String[] title,
        boolean isRatio) implements Serializable {
}