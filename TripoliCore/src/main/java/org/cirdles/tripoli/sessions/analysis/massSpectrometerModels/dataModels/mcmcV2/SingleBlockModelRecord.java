package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmcV2;

import org.ojalgo.matrix.store.MatrixStore;

import java.io.Serializable;
import java.util.Map;


public record SingleBlockModelRecord(
        double[] baselineMeansArray,
        double[] baselineStandardDeviationsArray,
        double detectorFaradayGain,
        Map<Integer, Integer> mapDetectorOrdinalToFaradayIndex,
        double[] logRatios,
        double[] signalNoiseSigma,
        double[] dataArray,
        double[] dataWithNoBaselineArray,
        double[] dataSignalNoiseArray,
        double[] I0,
        MatrixStore<Double> intensities,
        int faradayCount,
        int isotopeCount
) implements Serializable {
    public SingleBlockModelRecord clone(){
        return new SingleBlockModelRecord(
                baselineMeansArray.clone(),
                baselineStandardDeviationsArray.clone(),
                detectorFaradayGain,
                mapDetectorOrdinalToFaradayIndex,
                logRatios.clone(),
                signalNoiseSigma.clone(),
                dataArray.clone(),
                dataWithNoBaselineArray.clone(),
                dataSignalNoiseArray.clone(),
                I0.clone(),
                intensities.copy(),
                faradayCount,
                isotopeCount
        );
    }
}