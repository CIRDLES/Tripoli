package org.cirdles.tripoli.plots.compoundPlots;

import java.io.Serializable;

public record BlockRatioCyclesRecord(
        int blockID,
        boolean processed,
        boolean blockIncluded,
        boolean[] cyclesIncluded,
        double[] cycleRatioMeansData,
        double[] cycleRatioOneSigmaData,
        double mean,
        double standardDeviation,
        String[] title,
        String xAxisLabel,
        String yAxisLabel
) implements Serializable {
    public BlockRatioCyclesRecord toggleBlockIncluded() {
        return new BlockRatioCyclesRecord(
                blockID,
                processed,
                !blockIncluded,
                cyclesIncluded,
                cycleRatioMeansData,
                cycleRatioOneSigmaData,
                mean,
                standardDeviation,
                title,
                xAxisLabel,
                yAxisLabel
        );
    }
}