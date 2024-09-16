package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2;

import jama.Matrix;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.StrictMath.*;
import static org.apache.commons.math3.stat.StatUtils.mean;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2.MathUtilities.*;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc2.TestDriver.*;
import static org.cirdles.tripoli.utilities.mathUtilities.MatLabCholesky.mvnrndTripoli;

public enum DataUtilities {
    ;

    static MCMC2DataRecord syntheticData(int iSim) {
        ///Users/bowring/Development/Tripoli_ET/TripoliCore/src/main/resources/org/cirdles/tripoli/dataSourceProcessors/dataSources/syntheticData/SyntheticOutToTripoli/10_Sim/
        //
        // for now use synthetic data produced by matlab code
        double[] dataInt = extractDoubleData(iSim, "data-int.txt");
        double[] dataDet = extractDoubleData(iSim, "data-det.txt");
        double[] dataIso = extractDoubleData(iSim, "data-iso.txt");
        boolean[] dataIsOP = extractBooleanData(iSim,"data-isOP.txt");

        return new MCMC2DataRecord(
                null, null, null, null, dataInt, dataIsOP, dataDet, dataIso, null, null);
    }

    static MaxLikelihoodRecord maxLikelihood(MCMC2DataRecord data, MCMC2SetupRecord setup) {
            /*
            function maxlik = maxLikelihood(data, setup)
            % maxlik is a struct with three fields:
            %   model is the maximum likelihood model
            %   dvar is the expected variance in the data given the model parameters
            %   CM is the covariance matrix of the max likelihood model parameters

            functionToMinimize = @(m) -loglikLeastSquares(m, data, setup);
            opts = optimoptions('fminunc', 'Display', 'off');
            maxlik.model = fminunc(@(m) functionToMinimize(m), mRough, opts);
            %llInitial = -negLogLik;
            maxlik.dvar = updateDataVariance(maxlik.model, setup);
            %dhatCurrent = evaluateModel(modelInitial, setup);

            % build Jacobian matrix
            G = makeG(maxlik.model, data);

            % least squares model parameter covariance matrix
            maxlik.CM = inv(G'*diag(1./maxlik.dvar)*G);

            end % function maxLikelihood

         */

        boolean[] isIsotopeA = filterDataByValue(data.iso(), 1);
        boolean[] isIsotopeB = filterDataByValue(data.iso(), 2);

        double rough_lograb = mean(logVector(leftDivideVectors(filterDataByFlags(data.intensities(), isIsotopeA), filterDataByFlags(data.intensities(), isIsotopeB))));
        double rough_logCb = max(1.0, mean((logVector(filterDataByFlags(data.intensities(), isIsotopeB)))));

        boolean[] inBL_det1 = logicalAnd(invertSelector(data.isOP()), filterDataByValue(data.det(), 1));
        boolean[] inBL_det2 = logicalAnd(invertSelector(data.isOP()), filterDataByValue(data.det(), 2));

        double rough_ref1 = mean(filterDataByFlags(data.intensities(), inBL_det1));
        double rough_ref2 = mean(filterDataByFlags(data.intensities(), inBL_det2));

        MCMC2ModelRecord mRough = new MCMC2ModelRecord(new double[]{rough_lograb, rough_logCb, rough_ref1, rough_ref2});

        // https://commons.apache.org/proper/commons-math/javadocs/api-3.6.1/org/apache/commons/math3/optimization/direct/PowellOptimizer.html
        // https://github.com/imagej/imagej-legacy/blob/master/src/main/resources/script_templates/ImageJ_1.x/Examples/Optimization_Example.java
// TODO: implement fminunc
        // for now:
        MCMC2ModelRecord maxlikModel = mRough;
        double[] maxlikDVar = updateDataVariance(maxlikModel.parameters(), setup);
        double[][] matrixG = makeG(maxlikModel, data);

        double[] maxlikDVarInverted = rightScalarVectorDivision(1, maxlikDVar);
        double[][] maxlikDVarInvertedDiagonal = new double[maxlikDVarInverted.length][maxlikDVarInverted.length];
        for (int i = 0; i < maxlikDVarInverted.length; i++) {
            maxlikDVarInvertedDiagonal[i][i] = 1.0 / maxlikDVarInverted[i];
        }
        Matrix maxlikDVarInvertedDiagonalM = new Matrix(maxlikDVarInvertedDiagonal);
        Matrix matrixGM = new Matrix(matrixG);
        Matrix maxlikCMM = matrixGM.transpose().times(maxlikDVarInvertedDiagonalM).times(matrixGM).inverse();
        double[][] maxLikeCM = maxlikCMM.getArray();

        return new MaxLikelihoodRecord(maxlikModel, maxlikDVar, maxLikeCM);
    }

    static MCMC2ChainRecord initializeChains(MCMC2SetupRecord setup, MCMC2DataRecord data, MaxLikelihoodRecord maxLik) {
        /*
            function [initModels, initLogLiks] = initializeChains(setup, data, maxlik)

            % perturb modelCurrent by setup.perturbation standard deviations
            initModels = zeros(setup.nmodel, setup.nChains);
            initLogLiks = zeros(1, setup.nChains);
            for iChain = 1:setup.nChains
                initModels(:, iChain) = maxlik.model + ...
                               setup.perturbation*randn(setup.nmodel,1) .* ...
                               sqrt(diag(maxlik.CM));
                dhatCurrent = evaluateModel(initModels(:,iChain), setup);
                dvarCurrent = updateDataVariance(initModels(:,iChain), setup);
                initLogLiks(iChain) = loglik(dhatCurrent, data, dvarCurrent);
            end

            end % initializeChains()
         */
        double[][] initModels = new double[setup.modelParameterCount()][setup.chainsCount()];
        double[] initLogLiks = new double[setup.chainsCount()];
//        UniformRandomProvider rng = RandomSource.XO_RO_SHI_RO_128_PP.create();

        Random ran = new Random();

        for (int iChain = 0; iChain < setup.chainsCount(); iChain++) {
            for (int iParameter = 0; iParameter < setup.modelParameterCount(); iParameter++) {
//                double random = rng.nextDouble(-setup.modelParameterCount(), setup.modelParameterCount());
                double nextGaussian = ran.nextGaussian();
                initModels[iParameter][iChain]
                        = maxLik.model().parameters()[iParameter]
                        + setup.pertubation() * nextGaussian
                        * sqrt(maxLik.covarianceMatrix()[iParameter][iParameter]);
            }
            MCMC2ModelRecord mcmc2ModelRecord = new MCMC2ModelRecord(makeVectorFromColumn(iChain, initModels));
            double[] dhatCurrent = evaluateModel(mcmc2ModelRecord.parameters(), setup);
            double[] dvarCurrent = updateDataVariance(mcmc2ModelRecord.parameters(), setup);
            initLogLiks[iChain] = logLik(dhatCurrent, data, dvarCurrent);
        }

        return new MCMC2ChainRecord(initModels, initLogLiks);
    }

    static double[] makeVectorFromColumn(int col, double[][] matrix) {
        double[] vector = new double[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            vector[i] = matrix[i][col];
        }
        return vector;
    }

    static double[][] makeG(MCMC2ModelRecord m, MCMC2DataRecord data) {
        boolean[] isIsotopeA = filterDataByValue(data.iso(), 1);
        boolean[] isIsotopeB = filterDataByValue(data.iso(), 2);

        double[][] matrixG = new double[data.intensities().length][4];

        for (int i = 0; i < data.intensities().length; i++) {
            if (isIsotopeA[i]) {
                matrixG[i][0] = exp(m.parameters()[0] + m.parameters()[1]);
                matrixG[i][1] = exp(m.parameters()[0] + m.parameters()[1]);
            }
            if (isIsotopeB[i]) {
                matrixG[i][1] = exp(m.parameters()[1]);
            }
            if (1 == data.det()[i]) {
                matrixG[i][2] = 1;
            }
            if (2 == data.det()[i]) {
                matrixG[i][3] = 1;
            }
        }
        return matrixG;
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
    private static double[] evaluateModel(double[] modelParameters, MCMC2SetupRecord setup) {
        double lograb = modelParameters[0];
        double[] logCb = new double[setup.nOPIntegrations()];
        Arrays.fill(logCb, modelParameters[1]);
        double ref1 = modelParameters[2];
        double ref2 = modelParameters[3];

        double[] BL_det1 = new double[setup.nBLIntegrations()];
        Arrays.fill(BL_det1, ref1);
        double[] BL_det2 = new double[setup.nBLIntegrations()];
        Arrays.fill(BL_det2, ref2);
        double[] OP_det1 = new double[setup.nOPIntegrations()];
        double[] OP_det2 = new double[setup.nOPIntegrations()];
        for (int i = 0; i < setup.nOPIntegrations(); i++) {
            OP_det1[i] = exp(lograb + logCb[i]) + ref1;
            OP_det2[i] = exp(logCb[i]) + ref1;
        }

        double[] predictedData = new double[2 * setup.nBLIntegrations() + 2 * setup.nOPIntegrations()];
        System.arraycopy(BL_det1, 0, predictedData, 0, setup.nBLIntegrations());
        System.arraycopy(BL_det2, 0, predictedData, setup.nBLIntegrations(), setup.nBLIntegrations());
        System.arraycopy(OP_det1, 0, predictedData, 2 * setup.nBLIntegrations(), setup.nOPIntegrations());
        System.arraycopy(OP_det2, 0, predictedData, 2 * setup.nBLIntegrations() + setup.nOPIntegrations(), setup.nOPIntegrations());

        return predictedData;
    }

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

    static double[] updateDataVariance(double[] modelParameters, MCMC2SetupRecord setup) {
        double lograb = modelParameters[0];
        double logCb = modelParameters[1];

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
    static double logLik(double[] dhat, MCMC2DataRecord data, double[] dvar) {
        double ll;
        double sumChiSqTerms = 0.0;
        double sumLogDvar = 0.0;
        double[] residuals = new double[dhat.length];
        double[] chiSqTerms = new double[dhat.length];
        for (int i = 0; i < dhat.length; i++) {
            residuals[i] = data.intensities()[i] - dhat[i];
            chiSqTerms[i] = pow(residuals[i], 2) / dvar[i];
            sumChiSqTerms += chiSqTerms[i];
            sumLogDvar += log(dvar[i]);
        }
        ll = -0.5 * sumChiSqTerms - 0.5 * sumLogDvar;

        return ll;
    }

    /*
    function [outputModels, outputLogLiks] = ...
    MetropolisHastings(modelInitial, llInitial, data, setup)

        modelCurrent = modelInitial;
        llCurrent = llInitial;

        outputModels = nan([setup.nmodel, setup.nMC/setup.seive], "double");
        outputLogLiks = nan([1, setup.nMC/setup.seive], "double");

        for iMC = 1:setup.nMC

            % save off current model
            if ~mod(iMC,setup.seive)
                outputIndex = iMC/setup.seive;
                outputModels(:, outputIndex) = modelCurrent;
                outputLogLiks(outputIndex) = llCurrent;
            end

            % propose a new model
            modelProposed = modelCurrent + ...
                mvnrnd( zeros(setup.nmodel,1), ...
                setup.proposalCov )';

            % calculate residuals for old and new models
            dhatProposed = evaluateModel(modelProposed, setup);

            % create data covariance with current and proposed noise terms
            dvarProposed = updateDataVariance(modelProposed, setup);

            % calculate log-likelihoods of current and proposed samples
            llProposed = loglik(dhatProposed, data, dvarProposed);

            % difference in log-likelihood = log(likelihood ratio)
            delta_ll = llProposed - llCurrent;

            % probability of keeping the proposed model
            keep = min(1, exp(delta_ll));

            % keep the proposed model with probability = keep
            if keep > rand(1) % if we're keeping the proposed model

                % the proposed model becomes the current one
                modelCurrent = modelProposed;
                llCurrent  = llProposed;

            end % if keep


        end % for iMC = 1:nMC

        end % function MetropolisHastings()

     */

    static MetropolisHastingsRecord metropolisHastings(int iChain, double[] modelInitial, double llInitial, MCMC2DataRecord data, MCMC2SetupRecord setup) {
        double[] modelCurrent = modelInitial.clone();
        double llCurrent = llInitial;

        double[][] outputModels = new double[setup.modelParameterCount()][setup.MCMCTrialsCount() / setup.seive()];
        for (int i = 0; i < setup.modelParameterCount(); i++) {
            Arrays.fill(outputModels[i], Double.NaN);
        }
        double[] outputLogLiks = new double[setup.MCMCTrialsCount() / setup.seive()];
        Arrays.fill(outputLogLiks, Double.NaN);

        RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
        randomDataGenerator.reSeedSecure();

        for (int iMC = 1; iMC < setup.MCMCTrialsCount() + 1; iMC++) {
            if (0 == iMC % setup.seive()) {
                int outputIndex = iMC / setup.seive();
                outputModels = writeColumn(modelCurrent, outputModels, outputIndex - 1);
                outputLogLiks[outputIndex - 1] = llCurrent;
            }

            double[] modelRandom = mvnrndTripoli(new double[setup.modelParameterCount()], setup.proposalCovariance(), 1).getRowPackedCopy();
            double[] modelProposed = new double[setup.modelParameterCount()];
            for (int i = 0; i < setup.modelParameterCount(); i++) {
                modelProposed[i] = modelCurrent[i] + modelRandom[i];
            }

            double[] dhatProposed = evaluateModel(modelProposed, setup);
            double[] dvarProposed = updateDataVariance(modelProposed, setup);
            double llProposed = logLik(dhatProposed, data, dvarProposed);
            double delta_ll = llProposed - llCurrent;
            double keep = min(1, exp(delta_ll));

            if (keep >= randomDataGenerator.nextUniform(0, 1)) {
                modelCurrent = modelProposed;
                llCurrent = llProposed;
            }
        }

        return new MetropolisHastingsRecord(outputModels, outputLogLiks);
        /*
        for iMC = 1:setup.nMC
            % save off current model
            if ~mod(iMC,setup.seive)
                outputIndex = iMC/setup.seive;
                outputModels(:, outputIndex) = modelCurrent;
                outputLogLiks(outputIndex) = llCurrent;
            end

            % propose a new model
            modelProposed = modelCurrent + ...
                mvnrnd( zeros(setup.nmodel,1), ...
                setup.proposalCov )';

            % calculate residuals for old and new models
            dhatProposed = evaluateModel(modelProposed, setup);

            % create data covariance with current and proposed noise terms
            dvarProposed = updateDataVariance(modelProposed, setup);

            % calculate log-likelihoods of current and proposed samples
            llProposed = loglik(dhatProposed, data, dvarProposed);

            % difference in log-likelihood = log(likelihood ratio)
            delta_ll = llProposed - llCurrent;

            % probability of keeping the proposed model
            keep = min(1, exp(delta_ll));

            % keep the proposed model with probability = keep
            if keep > rand(1) % if we're keeping the proposed model

                % the proposed model becomes the current one
                modelCurrent = modelProposed;
                llCurrent  = llProposed;

            end % if keep
    end % for iMC = 1:nMC
         */
    }
}
