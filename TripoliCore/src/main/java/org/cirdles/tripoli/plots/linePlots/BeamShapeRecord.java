package org.cirdles.tripoli.plots.linePlots;

public record BeamShapeRecord(
        double[] xData,
        double[] yData,
        int leftBoundary,
        int rightBoundary,
        String[] title,
        String xAxisLabel,
        String yAxisLabel
) {
}
