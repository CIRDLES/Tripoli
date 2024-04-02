package org.cirdles.tripoli.plots.compoundPlotBuilders;

import com.google.common.primitives.Booleans;

import java.io.Serializable;

import static com.google.common.primitives.Booleans.countTrue;
import static org.cirdles.tripoli.utilities.mathUtilities.MathUtilities.applyChauvenetsCriterion;

public record PlotBlockCyclesRecord(
        int blockID,
        boolean isRatio,
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
            invertedRatios[i] = StrictMath.exp(-StrictMath.log(cycleMeansData[i]));
        }
        return invertedRatios;
    }

    public PlotBlockCyclesRecord resetAllDataIncluded() {
        for (int i = 0; i < cyclesIncluded.length; i++) {
            cyclesIncluded[i] = true;
        }

        return new PlotBlockCyclesRecord(
                blockID,
                isRatio,
                processed,
                true,
                cyclesIncluded,
                cycleMeansData,
                cycleOneSigmaData,
                title
        );
    }

    public PlotBlockCyclesRecord performChauvenets() {
        boolean [] cyclesIncludedChauvenet = applyChauvenetsCriterion(cycleMeansData, cyclesIncluded);

        return new PlotBlockCyclesRecord(
                blockID,
                isRatio,
                processed,
                true,
                cyclesIncludedChauvenet,
                cycleMeansData,
                cycleOneSigmaData,
                title
        );
    }

    public boolean detectAllIncludedStatus(){
        // catch short last block
        boolean [] cyclesIncludedCheck = cyclesIncluded.clone();
        for (int i = 0; i < cycleMeansData.length; i++){
            if (cycleMeansData[i] == 0.0) cyclesIncludedCheck[i] = true;
        }
        return cyclesIncluded.length == countTrue(cyclesIncludedCheck);
    }

    public PlotBlockCyclesRecord toggleBlockIncluded() {
        for (int i = 0; i < cyclesIncluded.length; i++) {
            cyclesIncluded[i] = !blockIncluded;
        }
        return new PlotBlockCyclesRecord(
                blockID,
                isRatio,
                processed,
                !blockIncluded,
                cyclesIncluded,
                cycleMeansData,
                cycleOneSigmaData,
                title
        );
    }

    public PlotBlockCyclesRecord setBlockIncluded() {
        for (int i = 0; i < cyclesIncluded.length; i++) {
            cyclesIncluded[i] = true;
        }
        return new PlotBlockCyclesRecord(
                blockID,
                isRatio,
                processed,
                true,
                cyclesIncluded,
                cycleMeansData,
                cycleOneSigmaData,
                title
        );
    }

    public PlotBlockCyclesRecord changeBlockIncluded(boolean blockIncluded) {
        for (int i = 0; i < cyclesIncluded.length; i++) {
            cyclesIncluded[i] = blockIncluded;
        }

        return new PlotBlockCyclesRecord(
                blockID,
                isRatio,
                processed,
                blockIncluded,
                cyclesIncluded,
                cycleMeansData,
                cycleOneSigmaData,
                title
        );
    }

    public PlotBlockCyclesRecord updateCyclesIncluded(boolean[] cyclesIncluded) {
        int countIncluded = countTrue(cyclesIncluded);

        return new PlotBlockCyclesRecord(
                blockID,
                isRatio,
                processed,
                (countIncluded != 0),
                cyclesIncluded,
                cycleMeansData,
                cycleOneSigmaData,
                title
        );
    }
}