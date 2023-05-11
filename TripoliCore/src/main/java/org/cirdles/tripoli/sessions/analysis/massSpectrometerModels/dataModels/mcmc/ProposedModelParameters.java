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
        double[][] priorBaselineFaraday = {{-1.0e6, 1.0e6}};
        double[][] priorBaselineDaly = {{0.0, 0.0}};
        double[][] priorLogRatio = {{-20.0, 20.0}};
        double maxIntensity = Double.MIN_VALUE;
        double minIntensity = Double.MAX_VALUE;

        for (int row = 0; row < blockIntensities.length; row++) {
            maxIntensity = Math.max(blockIntensities[row], maxIntensity);
            minIntensity = min(blockIntensities[row], minIntensity);
        }

        double[][] priorIntensity = {{0.0, 1.5 * maxIntensity}};
        double[][] priorDFgain = {{0.8, 1.0}};
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

    public static ProposalSigmasRecord buildProposalSigmasRecord(double[] baselineStandardDeviations, ProposalRangesRecord proposalRangesRecord) {
        /*
            % "Proposal Sigmas"
            % Standard deviations for proposing changes to model
            psig.BL = max(x0.BLstd)/10*1;  % Faraday Baseline
            psig.BLdaly = 1e-1*1;  % Daly Baseline
            psig.lograt = 0.0005*.2;  % Log Ratio
            psig.I = max(max([x0.I{:}])-min([x0.I{:}]))/100*1 ; % Intensity
            psig.DFgain = 0.001; % Daly-Faraday gain

            psig.sig = max(x0.BLstd); % Noise hyperparameter for Faraday
            psig.sigpois = 0.5; % Poisson noise on Daly
            psig.sigdaly = 0;  % Gaussian noise on Daly
         */

        double maxValue = Double.MIN_VALUE;

        for (int row = 0; row < baselineStandardDeviations.length; row++) {
            maxValue = Math.max(baselineStandardDeviations[row], maxValue);
        }
        double psigBaselineFaraday = maxValue;// / 10.0; removed per team meeting 5/11/2023
        double psigBaselineDaly = 1.0e-1;
        double psigLogRatio = 0.02;//0.0005 * 0.2;
        double psigIntensity = (proposalRangesRecord.maxIntensity() - proposalRangesRecord.minIntensity()) / 100.0;
        double psigDFgain = 0.001;
        double psigSignalNoiseFaraday = maxValue;
        double psigSignalNoisePoisson = 0.5;
        double psigSignalNoiseDaly = 0.0;

        return new ProposalSigmasRecord(
                psigBaselineFaraday,
                psigBaselineDaly,
                psigLogRatio,
                psigIntensity,
                psigDFgain,
                psigSignalNoiseFaraday,
                psigSignalNoisePoisson,
                psigSignalNoiseDaly
        );

    }

    record ProposalSigmasRecord(
            double psigBaselineFaraday,
            double psigBaselineDaly,
            double psigLogRatio,
            double psigIntensity,
            double psigDFgain,
            double psigSignalNoiseFaraday,
            double psigSignalNoisePoisson,
            double psigSignalNoiseDaly
    ) {
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