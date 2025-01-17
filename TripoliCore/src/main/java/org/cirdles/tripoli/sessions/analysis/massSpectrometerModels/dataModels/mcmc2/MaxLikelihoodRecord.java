package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2;

public record MaxLikelihoodRecord(
        MCMC2ModelRecord model,
        double[] dVar,
        double[][] covarianceMatrix
) {

}
