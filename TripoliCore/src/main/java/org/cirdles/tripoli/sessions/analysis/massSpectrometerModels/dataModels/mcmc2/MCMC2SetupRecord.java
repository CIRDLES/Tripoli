/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2;

import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;

import java.util.Arrays;

public record MCMC2SetupRecord(
        int nBLIntegrations,
        int nOPIntegrations,
        Detector detector,
        double[] blIntegrationTimes,
        double[] opIntegrationTimes,
        double[][] proposalCovariance,
        int MCMCTrialsCount,
        int seive,
        int modelParameterCount,
        int chainsCount,
        int pertubation,
        int simulationsCount,
        int burnIn,
        int postBurnInCount
) {
    public MCMC2SetupRecord initializeIntegrationTimes(double blTime, double opTime) {
        double[] blIntegrationTimesInit = new double[blIntegrationTimes.length];
        Arrays.fill(blIntegrationTimesInit, blTime);

        double[] opIntegrationTimesInit = new double[opIntegrationTimes.length];
        Arrays.fill(opIntegrationTimesInit, opTime);

        return new MCMC2SetupRecord(nBLIntegrations, nOPIntegrations, detector, blIntegrationTimesInit, opIntegrationTimesInit,
                null, MCMCTrialsCount, seive, modelParameterCount, chainsCount, pertubation, simulationsCount, burnIn, postBurnInCount);
    }

    public MCMC2SetupRecord updateRecordWithCovariance(double[][] proposalCovariance) {
        return new MCMC2SetupRecord(nBLIntegrations, nOPIntegrations, detector, blIntegrationTimes, opIntegrationTimes,
                proposalCovariance, MCMCTrialsCount, seive, modelParameterCount, chainsCount, pertubation, simulationsCount, burnIn, postBurnInCount);
    }

    public MCMC2SetupRecord updateRecordWithPostBurnIn(int postBurnInCount) {
        return new MCMC2SetupRecord(nBLIntegrations, nOPIntegrations, detector, blIntegrationTimes, opIntegrationTimes,
                proposalCovariance, MCMCTrialsCount, seive, modelParameterCount, chainsCount, pertubation, simulationsCount, burnIn, postBurnInCount);
    }
}
