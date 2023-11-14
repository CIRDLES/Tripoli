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
            prior.DFgain = [0.8 1.0];  % Daly-Faraday gainCorr

            prior.sig = [0 1e6];  % Noise hyperparameter for Faraday
            prior.sigdaly = [0 0]; % Gaussian noise on Daly
            prior.sigpois = [0 10]; % Poisson noise on Daly
     */
    public static ProposalRangesRecord buildProposalRangesRecord(double[] blockIntensities) {
        /*
            % Range for ratios and intensity parameters
            %sb629  Changed priors to infinite where appropropriate
            prior.BL = [-inf inf];  % Faraday baseline
            prior.BLdaly = [0 0];   % Daly baseline (no baseline uncertainty)
            prior.lograt = [-20 20]; % Log ratio
            prior.I = [0 inf];  % Intensity
            prior.DFgain = [0 inf];  % Daly-Faraday gainCorr

            prior.sig = [0 1e6];  % Noise hyperparameter for Faraday
            prior.sigdaly = [0 0]; % Gaussian noise on Daly
            prior.sigpois = [0 10]; % Poisson noise on Daly
         */
        double[][] priorBaselineFaraday = {{Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}};
        double[][] priorBaselineDaly = {{0.0, 0.0}};
        double[][] priorLogRatio = {{-20.0, 20.0}};
        double[][] priorIntensity = {{0.0, Double.POSITIVE_INFINITY}};
        double[][] priorDFgain = {{0.0, Double.POSITIVE_INFINITY}};
        double[][] priorSignalNoiseFaraday = {{0.0, 1.0e6}};
        double[][] priorSignalNoiseDaly = {{0.0, 0.0}};
        double[][] priorPoissonNoiseDaly = {{0.0, 10.0}};

        return new ProposalRangesRecord(
                priorBaselineFaraday,
                priorBaselineDaly,
                priorLogRatio,
                priorIntensity,
                priorDFgain,
                priorSignalNoiseFaraday,
                priorSignalNoiseDaly,
                priorPoissonNoiseDaly
        );
    }

    public record ProposalRangesRecord(
            double[][] priorBaselineFaraday,
            double[][] priorBaselineDaly,
            double[][] priorLogRatio,
            double[][] priorIntensity,
            double[][] priorDFgain,
            double[][] priorSignalNoiseFaraday,
            double[][] priorSignalNoiseDaly,
            double[][] priorPoissonNoiseDaly
    ) {
    }
}