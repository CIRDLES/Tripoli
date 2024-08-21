package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2;

public record MCMC2ResultsRecord(
        int simulationIndex,
        double[] modelMeans,
        double[][] modelCov,
        double r,
        double ChiSq

) {
}
