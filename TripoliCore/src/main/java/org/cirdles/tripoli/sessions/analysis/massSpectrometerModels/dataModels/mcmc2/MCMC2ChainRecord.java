package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2;

public record MCMC2ChainRecord(
        double[][] initModels,
        double[] initLogLiks
) {
}
