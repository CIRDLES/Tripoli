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

    public double[] invertedCycleMeansData() {
        double[] invertedRatios = new double[cycleMeansData.length];
        for (int i = 0; i < cycleMeansData.length; i++) {
            invertedRatios[i] = Math.exp(-Math.log(cycleMeansData[i]));
        }
        return invertedRatios;
    }

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

    public BlockCyclesRecord setBlockIncluded() {
        return new BlockCyclesRecord(
                blockID,
                processed,
                true,
                cyclesIncluded,
                cycleMeansData,
                cycleOneSigmaData,
                title
        );
    }

    public BlockCyclesRecord changeBlockIncluded(boolean blockIncluded) {
        return new BlockCyclesRecord(
                blockID,
                processed,
                blockIncluded,
                cyclesIncluded,
                cycleMeansData,
                cycleOneSigmaData,
                title
        );
    }
}