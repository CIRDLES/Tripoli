package org.cirdles.tripoli.plots.sessionPlots;

import org.cirdles.tripoli.plots.linePlots.PeakShapesOverlayRecord;

import java.io.Serializable;
import java.util.Map;

public record PeakCentreSessionRecord(
        double blockCount,
        Map<Integer, PeakShapesOverlayRecord> mapBlockIdToPeakShapeRecord,
        double[] blockIds,
        double[] blockPeakWidths,
        String[] title,
        String xAxisLabel,
        String yAxisLabel
) implements Serializable {
}
