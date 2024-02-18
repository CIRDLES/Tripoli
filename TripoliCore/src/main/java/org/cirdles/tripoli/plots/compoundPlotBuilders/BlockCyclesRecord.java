package org.cirdles.tripoli.plots.compoundPlotBuilders;

import java.io.Serializable;

public record BlockCyclesRecord(
        int blockID,
        boolean processed,
        double dalyFaradayGain,
        boolean blockIncluded,
        boolean[] cyclesIncluded,
        double[] cycleRatioMeansData,
        double[] cycleRatioOneSigmaData
) implements Serializable {
    public BlockCyclesRecord toggleBlockIncluded() {
        return new BlockCyclesRecord(
                blockID,
                processed,
                dalyFaradayGain,
                !blockIncluded,
                cyclesIncluded,
                cycleRatioMeansData,
                cycleRatioOneSigmaData
        );
    }
}