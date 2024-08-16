package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2;

public record MCMC2DataRecord(
        double[] blDet1,
        double[] blDet2,
        double[] opDet1,
        double[] opDet2,
        double[] intensities,
        boolean[] isOP,
        double[] det,
        double[] iso,
        double[] blTimes,
        double[] opTimes

        ) {
}
