package org.cirdles.tripoli.plots.compoundPlotBuilders;

import java.io.Serializable;

public record BlockRatioCyclesRecord(
        int blockID,
        boolean processed,
        double dalyFaradayGain,
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
                dalyFaradayGain,
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