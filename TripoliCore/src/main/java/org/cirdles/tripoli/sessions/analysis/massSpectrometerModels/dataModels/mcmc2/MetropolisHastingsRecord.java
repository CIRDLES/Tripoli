package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2;

public record MetropolisHastingsRecord(
        double[][] outputModels,
        double[] outputLogLiks
) {
}
