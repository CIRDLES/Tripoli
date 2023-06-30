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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc;

import static java.lang.Math.min;

/**
 * @author James F. Bowring
 */
public enum ProposedModelParameters {
    ;

    /*
    % Range for ratios and blockIntensities parameters
            prior.BL = [-1 1]*1e6;  % Faraday baseline
            prior.BLdaly = [0 0];   % Daly baseline (no baseline uncertainty)
            prior.lograt = [-20 20]; % Log ratio
            prior.I = [0 1.5*max([x0.I{:}])];  % Intensity
            prior.DFgain = [0.8 1.0];  % Daly-Faraday gain

            prior.sig = [0 1e6];  % Noise hyperparameter for Faraday
            prior.sigdaly = [0 0]; % Gaussian noise on Daly
            prior.sigpois = [0 10]; % Poisson noise on Daly
     */
    public static ProposalRangesRecord buildProposalRangesRecord(double[] blockIntensities) {
        double[][] priorBaselineFaraday = {{-4.0e6, 4.0e6}};
        double[][] priorBaselineDaly = {{0.0, 1.0}};
        double[][] priorLogRatio = {{-20.0, 20.0}};
        double maxIntensity = Double.MIN_VALUE;
        double minIntensity = Double.MAX_VALUE;

        for (int row = 0; row < blockIntensities.length; row++) {
            maxIntensity = Math.max(blockIntensities[row], maxIntensity);
            minIntensity = min(blockIntensities[row], minIntensity);
        }

        double[][] priorIntensity = {{0.0, 1.5 * maxIntensity}};
        double[][] priorDFgain = {{0.0, Double.POSITIVE_INFINITY}};
        double[][] priorSignalNoiseFaraday = {{0.0, 1.0e6}};
        double[][] priorSignalNoiseDaly = {{0.0, 0.0}};
        double[][] priorPoissonNoiseDaly = {{0.0, 10.0}};

        return new ProposalRangesRecord(
                priorBaselineFaraday,
                priorBaselineDaly,
                priorLogRatio,
                maxIntensity,
                minIntensity,
                priorIntensity,
                priorDFgain,
                priorSignalNoiseFaraday,
                priorSignalNoiseDaly,
                priorPoissonNoiseDaly
        );
    }

    record ProposalRangesRecord(
            double[][] priorBaselineFaraday,
            double[][] priorBaselineDaly,
            double[][] priorLogRatio,
            double maxIntensity,
            double minIntensity,
            double[][] priorIntensity,
            double[][] priorDFgain,
            double[][] priorSignalNoiseFaraday,
            double[][] priorSignalNoiseDaly,
            double[][] priorPoissonNoiseDaly
    ) {
    }
}