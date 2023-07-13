package org.cirdles.tripoli.plots.compoundPlots;

import java.io.Serializable;

public record BlockRatioCyclesRecord(
        int blockID,
        boolean blockIncluded,
        boolean[] cyclesIncluded,
        double[] cycleLogRatioMeansData,
        double[] cycleLogRatioOneSigmaData,
        double mean,
        double standardDeviation,
        String[] title,
        String xAxisLabel,
        String yAxisLabel
) implements Serializable {
}