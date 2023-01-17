package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmcV2;

import org.ojalgo.matrix.store.MatrixStore;

import java.io.Serializable;


public record SingleBlockModelRecord(
        double[] baselineMeansArray,
        double[] baselineStandardDeviationsArray,
        double detectorFaradayGain,
        double[] logRatios,
        double[] signalNoise,
        double[] dataArray,
        double[] I0,
        MatrixStore<Double> intensities
) implements Serializable {
}