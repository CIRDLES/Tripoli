package org.cirdles.tripoli.plots.compoundPlotBuilders;

import java.io.Serializable;

public record BlockCyclesRecord(
        int blockID,
        boolean processed,
        boolean blockIncluded,
        boolean[] cyclesIncluded,
        double[] cycleMeansData,
        double[] cycleOneSigmaData,
        String[] title
) implements Serializable {
    public BlockCyclesRecord toggleBlockIncluded() {
        return new BlockCyclesRecord(
                blockID,
                processed,
                !blockIncluded,
                cyclesIncluded,
                cycleMeansData,
                cycleOneSigmaData,
                title
        );
    }
}