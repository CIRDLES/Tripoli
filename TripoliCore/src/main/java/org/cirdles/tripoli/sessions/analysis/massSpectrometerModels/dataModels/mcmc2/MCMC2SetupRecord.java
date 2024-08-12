package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2;

import java.util.Arrays;

public record MCMC2SetupRecord(
        int nBLIntegrations,
        int nOPIntegrations,
        String type,
        double resistance,
        double gain,
        double[] blIntegrationTimes,
        double[] opIntegrationTimes

        ) {
    public MCMC2SetupRecord initializeIntegrationTimes(double blTime, double opTime){
        double[] blIntegrationTimesInit = new double[blIntegrationTimes.length];
        Arrays.fill(blIntegrationTimesInit, blTime);

        double[] opIntegrationTimesInit = new double[opIntegrationTimes.length];
        Arrays.fill(opIntegrationTimesInit, opTime);

        return new MCMC2SetupRecord(nBLIntegrations, nOPIntegrations, type, resistance, gain, blIntegrationTimesInit, opIntegrationTimesInit);
    }
}
