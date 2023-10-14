package org.cirdles.tripoli.plots.analysisPlotBuilders;

import org.cirdles.tripoli.plots.linePlots.PeakShapesOverlayRecord;

import java.io.Serializable;
import java.util.Map;

public record PeakCentreAnalysisRecord(
        double blockCount,
        Map<Integer, PeakShapesOverlayRecord> mapBlockIdToPeakShapeRecord,
        double[] blockIds,
        double[] blockPeakWidths,
        String[] title,
        String xAxisLabel,
        String yAxisLabel
) implements Serializable {
}