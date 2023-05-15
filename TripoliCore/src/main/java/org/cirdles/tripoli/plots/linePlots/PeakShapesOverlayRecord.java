package org.cirdles.tripoli.plots.linePlots;

public record PeakShapesOverlayRecord(
        int blockID,
        double peakWidth,
        double[] beamXData,
        double[] gBeamXData,
        double[] beamYData,
        double[] gBeamYData,
        double[] intensityData,
        int leftBoundary,
        int rightBoundary,
        String[] title,
        String xAxisLabel,
        String yAxisLabel
) {
}
