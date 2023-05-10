package org.cirdles.tripoli.plots.linePlots;

public record GBeamRecord(
        double[] xData,
        double[] yData,
        double[] intensityData,
        String[] title,
        String xAxisLabel,
        String yAxisLabel
) {
}
