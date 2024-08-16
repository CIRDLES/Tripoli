package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2;

import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;

import static java.lang.StrictMath.exp;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2.TestDriver.filterDataByValue;

public class MathUtilities {


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

    /**
     * function dhat = evaluateModel(m, setup)
     * <p>
     * % y = slope * x       + y-intercept
     * % dhat = m(1) * setup.x + m(2);
     * <p>
     * lograb = m(1);
     * logCb  = m(2) * ones(setup.nOPIntegrations, 1);
     * ref1   = m(3);
     * ref2   = m(4);
     * <p>
     * BL_det1 = ref1*ones(setup.nBLIntegrations,1);
     * BL_det2 = ref2*ones(setup.nBLIntegrations,1);
     * OP_det1 = exp(lograb + logCb) + ref1;
     * OP_det2 = exp(logCb) + ref2;
     * <p>
     * dhat = [BL_det1; BL_det2; OP_det1; OP_det2];
     * <p>
     * end % function evaluateModel
     */
    private static double[] evaluateModel() {
        double[] predictedData = new double[1];

        return predictedData;
    }






    /*
    %% evaluate this particular model
function dhat = evaluateModel(m, setup)
    lograb = m(1);
    logCb  = m(2) * ones(setup.nOPIntegrations, 1);
    ref1   = m(3);
    ref2   = m(4);

    BL_det1 = ref1*ones(setup.nBLIntegrations,1);
    BL_det2 = ref2*ones(setup.nBLIntegrations,1);
    OP_det1 = exp(lograb + logCb) + ref1;
    OP_det2 = exp(logCb) + ref2;

    dhat = [BL_det1; BL_det2; OP_det1; OP_det2];

end % function evaluateModel

     */


    /*
        %% update data covariance matrix

        function dvar = updateDataVariance(m, setup)

        % estimate ion beam variances from model parameters
        lograb = m(1);
        logCb = m(2);

        BL_det1_var = estimateIonBeamVariance(...
            zeros(setup.nBLIntegrations,1), ...
            setup.BLIntegrationTimes, ...
            setup.detector);
        BL_det2_var = estimateIonBeamVariance(...
            zeros(setup.nBLIntegrations,1), ...
            setup.BLIntegrationTimes, ...
            setup.detector);
        OP_det1_var = estimateIonBeamVariance(...
            exp(lograb + logCb), ...
            setup.BLIntegrationTimes, ...
            setup.detector);
        OP_det2_var = estimateIonBeamVariance(...
            exp(logCb), ...
            setup.BLIntegrationTimes, ...
            setup.detector);

        dvar = [BL_det1_var; BL_det2_var; OP_det1_var; OP_det2_var];

end % updateDataCovariance
     */

    static double[] updateDataVariance(MCMC2ModelRecord m, MCMC2SetupRecord setup) {
        double lograb = m.lograb();
        double logCb = m.logCb();

        double[] BL_det1_var = estimateIonBeamVariance(
                new double[setup.nBLIntegrations()],
                setup.blIntegrationTimes(),
                setup.detector());

        double[] BL_det2_var = estimateIonBeamVariance(
                new double[setup.nBLIntegrations()],
                setup.blIntegrationTimes(),
                setup.detector());

        double[] OP_det1_var = estimateIonBeamVariance(
                new double[]{exp(lograb + logCb)},
                setup.opIntegrationTimes(),
                setup.detector());

        double[] OP_det2_var = estimateIonBeamVariance(
                new double[]{exp(logCb)},
                setup.opIntegrationTimes(),
                setup.detector());

        double[] updatedDataVariance = new double[2 * setup.nBLIntegrations() + 2 * setup.nOPIntegrations()];
        System.arraycopy(BL_det1_var, 0, updatedDataVariance, 0, setup.nBLIntegrations());
        System.arraycopy(BL_det2_var, 0, updatedDataVariance, setup.nBLIntegrations(), setup.nBLIntegrations());
        System.arraycopy(OP_det1_var, 0, updatedDataVariance, 2 * setup.nBLIntegrations(), setup.nOPIntegrations());
        System.arraycopy(OP_det2_var, 0, updatedDataVariance, 2 * setup.nBLIntegrations() + setup.nOPIntegrations(), setup.nOPIntegrations());

        return updatedDataVariance;
    }

    /*
    %% calculate log-likelihood (including -1/2 term)
function ll = loglik(dhat, data, dvar)
    residuals = (data.int - dhat);
    chiSqTerms = residuals.^2 ./ dvar;
    ll = -1/2 * sum(chiSqTerms) - 1/2 * sum(log(dvar));
end % function loglik
     */

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
    private static double[] estimateIonBeamVariance(double[] trueCountRates, double[] integrationTimes, Detector detector) {
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
        if (detector.getDetectorType() == Detector.DetectorTypeEnum.FARADAY) {
            //noise is shot noise +Johnson noise
            // output is in volts
            ionBeamVariance = addTwoVectors(JNvarianceInCPS, PoissonVarianceInCPS);
        } else if (detector.getDetectorType() == Detector.DetectorTypeEnum.DALYDETECTOR) {
            // noise is shot noise only
            // output is in cps
            ionBeamVariance = PoissonVarianceInCPS;
        }
        return  ionBeamVariance;
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
            if (a.length == 1){
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
            divided[i] = vectorA[i] / ((vectorB[i] != 0) ? vectorB[i] : 1.0);
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

    static double[][] makeG(MCMC2ModelRecord m, MCMC2DataRecord data){
        boolean[] isIsotopeA = filterDataByValue(data.iso(), 1);
        boolean[] isIsotopeB = filterDataByValue(data.iso(), 2);

        double[][] matrixG = new double[data.intensities().length][4];

        for (int i = 0; i < data.intensities().length; i ++){
            if (isIsotopeA[i]){
                matrixG[i][0] = exp(m.lograb() + m.logCb());
                matrixG[i][1] = exp(m.lograb() + m.logCb());
            }
            if (isIsotopeB[i]){
                matrixG[i][1] = exp(m.logCb());
            }
            if (data.det()[i] == 1){ matrixG[i][2] = 1;}
            if (data.det()[i] == 2){ matrixG[i][3] = 1;}
        }
        return matrixG;
    }
}
