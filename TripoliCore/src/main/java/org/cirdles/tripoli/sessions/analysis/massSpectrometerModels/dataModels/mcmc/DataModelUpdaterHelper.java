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

import org.apache.commons.math3.random.RandomDataGenerator;

import static java.lang.Math.min;

/**
 * @author James F. Bowring
 */
public class DataModelUpdaterHelper {

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
    public static PriorRecord buildPriorRecord(double[][] blockIntensities) {
        double[][] priorBaselineFaraday = new double[][]{{-1.0e6, 1.0e6}};
        double[][] priorBaselineDaly = new double[][]{{0.0, 0.0}};
        double[][] priorLogRatio = new double[][]{{-20.0, 20.0}};
        double maxIntensity = Double.MIN_VALUE;
        double minIntensity = Double.MAX_VALUE;

        for (int row = 0; row < blockIntensities.length; row++) {
            for (int col = 0; col < blockIntensities[row].length; col++) {
                maxIntensity = Math.max(blockIntensities[row][col], maxIntensity);
                minIntensity = min(blockIntensities[row][col], minIntensity);
            }
        }

        double[][] priorIntensity = new double[][]{{0.0, 1.5 * maxIntensity}};
        double[][] priorDFgain = new double[][]{{0.8, 1.0}};
        double[][] priorSignalNoiseFaraday = new double[][]{{0.0, 1.0e6}};
        double[][] priorSignalNoiseDaly = new double[][]{{0.0, 0.0}};
        double[][] priorPoissonNoiseDaly = new double[][]{{0.0, 10.0}};

        return new PriorRecord(
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

    public static PsigRecord buildPsigRecord(double[] baselineStandardDeviations, PriorRecord priorRecord) {
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
        double psigBaselineFaraday = maxValue / 10.0;
        double psigBaselineDaly = 1.0e-1;
        double psigLogRatio = 0.0005 * 0.2;
        // TODO: Confirm this should be called percent
        double psigIntensityPercent = (priorRecord.maxIntensity() - priorRecord.minIntensity()) / 100.0;
        double psigDFgain = 0.001;
        double psigSignalNoiseFaraday = maxValue;
        double psigSignalNoisePoisson = 0.5;
        double psigSignalNoiseDaly = 0;

        return new PsigRecord(
                psigBaselineFaraday,
                psigBaselineDaly,
                psigLogRatio,
                psigIntensityPercent,
                psigDFgain,
                psigSignalNoiseFaraday,
                psigSignalNoisePoisson,
                psigSignalNoiseDaly
        );

    }

    static int findFirstOrLast(boolean first, int index, double[] target, int flag, double[] flags) {
        // assume column vectors
        int retVal = -1;
        for (int row = 0; row < target.length; row++) {
            if ((target[row] == index) && (flags[row] == flag)) {
                retVal = row;
                if (first) break;
            }
        }
        return retVal;
    }

    /**
     * Randomly generate next model operation, with or without hierarchical step
     *
     * @param hierFlag Hierarchical = true
     * @return Rnadom operation by name
     */
    static String randomOperMS(boolean hierFlag) {
        Object[][] notHier = new Object[][]{{40, 60, 80, 100}, {"changeI", "changer", "changebl", "changedfg"}};
        Object[][] hier = new Object[][]{{60, 80, 90, 100, 120}, {"changeI", "changer", "changebl", "changedfg", "noise"}};
//        Object[][] hier = new Object[][]{{400, 440, 520, 540, 600}, {"changeI", "changer", "changebl", "changedfg", "noise"}};

        RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
        randomDataGenerator.reSeedSecure();
        int choice = hierFlag ? randomDataGenerator.nextInt(0, 120) : randomDataGenerator.nextInt(0, 100);
        String retVal = "changeI";
        if (hierFlag) {
            for (int i = 0; i < hier[0].length; i++) {
                if (choice < (int) hier[0][i]) {
                    retVal = (String) hier[1][i];
                    break;
                }
            }
        } else {
            for (int i = 0; i < notHier[0].length; i++) {
                if (choice < (int) notHier[0][i]) {
                    retVal = (String) notHier[1][i];
                    break;
                }
            }
        }

        return retVal;
    }

    record PsigRecord(
            double psigBaselineFaraday,
            double psigBaselineDaly,
            double psigLogRatio,
            double psigIntensityPercent,
            double psigDFgain,
            double psigSignalNoiseFaraday,
            double psigSignalNoisePoisson,
            double psigSignalNoiseDaly
    ) {
    }

    record PriorRecord(
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