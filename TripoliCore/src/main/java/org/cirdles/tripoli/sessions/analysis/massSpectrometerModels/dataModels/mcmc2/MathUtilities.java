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

public enum MathUtilities {
    ;


    /*
         %% caluclate log-likelihood as a function of model parameters
         % minimize ll for least squares

         function ll = loglikLeastSquares(m, data, setup)

         dhat = evaluateModel(m, setup);
         dvar = updateDataVariance(m, setup);
         ll = loglik(dhat, data, dvar);

         end % function loglikLeastSquares
 */
    private static double loglikLeastSquares(MCMC2ModelRecord m, double[] data, MCMC2SetupRecord setup) {

        return 0.0;
    }

    /*
        function ionBeamVariance = ...
            estimateIonBeamVariance(trueCountRates, integrationTimes, detector)
                %ESTIMATEIONBEAMVARIANCE Estimate ion beam variance
                %   Estimate the ion beam variance based on mass spec properties.
                %   Noise is the sum shot noise and, if Farday output is selected,
                %   Johnson-Nyquist noise.
                %
                %   Inputs:
                %   - countRates: vector of count rates in cps
                %   - integrationTimes: vector of integration times in cps
                %   - detector: struct containing fields
                %       - type = "F" or "IC" for "Faraday" or "Ion Counter"
                %       - resistance = eg 1e11 or 1e12
                %       - gain = eg 1 or 0.9
                %
                %   Output:
                %   - ionBeamVariance: vector of estimated variances in cps^2

                %% Constants
                kB = 1.380649e-23; % exact, Joules per Kelvin
                T = 290; % temperature, Kelvin
                R = detector.resistance; % 1e11; % resistance, ohms

                ionsPerCoulomb = 6241509074460762607.776;
                CPSperVolt = ionsPerCoulomb/R;

                %% Johnson noise
                deltaf = 1./integrationTimes; % bandwidth in Hertz = 1/integration time
                JNvarianceInVolts = 4*kB*T*R*deltaf; % volts^2
                JNvarianceInCPS = JNvarianceInVolts * (CPSperVolt)^2;

                %% Shot noise
                % Poisson variance = total ions = counts/second * seconds
                PoissonVarianceInCPS =(trueCountRates*detector.gain) ./ integrationTimes; % counts^2

                %% Create output
                if detector.type == "F"
                    % noise is shot noise + Johnson noise
                    % output is in volts
                    ionBeamVariance = JNvarianceInCPS + PoissonVarianceInCPS;

                elseif detector.type == "IC"
                    % noise is shot noise only
                    % output is in cps

                    ionBeamVariance = PoissonVarianceInCPS;

                else %
                    error("unrecognized detector type, use F or IC")

                end % if detector.type, for output


                end % function
          */
    static double[] estimateIonBeamVariance(double[] trueCountRates, double[] integrationTimes, Detector detector) {
        double[] ionBeamVariance = null;
        //Constants
        //todo: refactor into tripoli constants
        double kB = 1.380649e-23; // exact, Joules per Kelvin
        double T = 290; // temperature, Kelvin
        double R = detector.getAmplifierResistanceInOhms(); // 1e11; % resistance, ohms
        double ionsPerCoulomb = 6241509074460762607.776;
        double CPSperVolt = ionsPerCoulomb / R;

        // Johnson noise
        double[] deltaf = rightScalarVectorDivision(1.0, integrationTimes); // bandwidth in Hertz = 1/integration time
        double[] JNvarianceInVolts = rightScalarVectorMultiplication(4 * kB * T * R, deltaf); // volts^2
        double[] JNvarianceInCPS = rightScalarVectorMultiplication((CPSperVolt) * (CPSperVolt), JNvarianceInVolts);

        // Shot noise
        //Poisson variance = total ions = counts/second * seconds
        double[] PoissonVarianceInCPS = rightVectorVectorDivision(rightScalarVectorMultiplication(detector.getAmplifierGain(), trueCountRates), integrationTimes); //% counts^2

        // Create output
        if (Detector.DetectorTypeEnum.FARADAY == detector.getDetectorType()) {
            //noise is shot noise +Johnson noise
            // output is in volts
            ionBeamVariance = addTwoVectors(JNvarianceInCPS, PoissonVarianceInCPS);
        } else if (Detector.DetectorTypeEnum.DALYDETECTOR == detector.getDetectorType()) {
            // noise is shot noise only
            // output is in cps
            ionBeamVariance = PoissonVarianceInCPS;
        }
        return ionBeamVariance;
    }

    static double[] rightScalarVectorMultiplication(double a, double[] b) {
        double[] multiplied = new double[b.length];
        for (int i = 0; i < b.length; i++) {
            multiplied[i] = a * b[i];
        }
        return multiplied;
    }

    static double[] rightScalarVectorDivision(double a, double[] b) {
        double[] divided = new double[b.length];
        for (int i = 0; i < b.length; i++) {
            divided[i] = a * b[i];
        }
        return divided;
    }

    static double[] rightVectorVectorDivision(double[] a, double[] b) {
        double[] divided = new double[b.length];
        for (int i = 0; i < b.length; i++) {
            if (1 == a.length) {
                divided[i] = a[0] / b[i];
            } else {
                divided[i] = a[i] / b[i];
            }
        }
        return divided;
    }

    static double[] addTwoVectors(double[] a, double[] b) {
        double[] added = new double[b.length];
        for (int i = 0; i < b.length; i++) {
            added[i] = a[i] + b[i];
        }
        return added;
    }

    static double[] leftDivideVectors(double[] vectorA, double[] vectorB) {
        // precondition a and b same length
        double[] divided = new double[vectorA.length];
        for (int i = 0; i < vectorA.length; i++) {
            divided[i] = vectorA[i] / ((0 != vectorB[i]) ? vectorB[i] : 1.0);
        }
        return divided;
    }

    static double[] logVector(double[] source) {
        double[] logVector = new double[source.length];
        for (int i = 0; i < source.length; i++) {
            logVector[i] = StrictMath.log(source[i]);
        }
        return logVector;
    }

    /*
        %% assemble design matrix G from model and data
            % G is the linearized design matrix in d = Gm

            function G = makeG(m, data)

            isIsotopeA = data.iso == 1;
            isIsotopeB = data.iso == 2;

            G = zeros(length(data.int), 4);

            % derivative of ca wrt log(a/b): dca__dlogra_b
            G(isIsotopeA, 1) = exp(m(1)+m(2));

            % derivative of ca wrt log(Cb)
            G(isIsotopeA, 2) = exp(m(1)+m(2));

            % derivative of cb wrt log(Cb)
            G(isIsotopeB, 2) = exp(m(2));

            G(data.det == 1, 3) = 1; % derivative wrt ref1
            G(data.det == 2, 4) = 1; % derivative wrt ref2

            end % function G = makeG(m, data)
     */


    static double[] extractColumn(double[][] source, int col) {
        double[] column = new double[source.length];
        for (int i = 0; i < source.length; i++) {
            column[i] = source[i][col];
        }
        return column;
    }

    static double[][] writeColumn(double[] row, double[][] target, int col) {
        double[][] updated = target.clone();
        for (int i = 0; i < row.length; i++) {
            updated[i][col] = row[i];
        }
        return updated;
    }

}
