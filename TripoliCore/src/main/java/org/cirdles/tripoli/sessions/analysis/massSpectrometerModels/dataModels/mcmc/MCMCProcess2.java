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

import jama.Matrix;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.utilities.callbacks.LoggingCallbackInterface;
import org.cirdles.tripoli.utilities.mathUtilities.MatLabCholesky;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.text.DecimalFormat;
import java.util.*;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.StrictMath.exp;
import static org.apache.commons.math3.special.Gamma.gamma;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.ProposedModelParameters.buildProposalRangesRecord;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockModelUpdater.operations;
import static org.cirdles.tripoli.utilities.mathUtilities.MatLab.linspace;

/**
 * @author James F. Bowring
 */
public class MCMCProcess2 {

    private static final int maxIterationCount = 10000;
    private static final int stepCountForcedSave = 10;
    private static final int modelCount = maxIterationCount * stepCountForcedSave;
    private final SingleBlockModelRecord singleBlockInitialModelRecord_X0;
    private final Matrix covarianceMatrix_C0;
    private final AnalysisMethod analysisMethod;
    private final SingleBlockDataSetRecord singleBlockDataSetRecord;
    List<EnsemblesStore.EnsembleRecord> ensembleRecordsList;
    private int faradayCount;
    private int ratioCount;
    private boolean hierarchical;
    private double tempering;
    private double[] baselineMultiplier;
    private ProposedModelParameters.ProposalRangesRecord proposalRangesRecord;
    private double[] dataArray;
    private double[] dataWithNoBaselineArray;
    private double[] dataSignalNoiseArray;
    private double initialModelErrorWeighted_E;
    private double initialModelErrorUnWeighted_E0;
    private int[][] keptUpdates;
    private int sizeOfModel;
    private int startingIndexOfFaradayData;
    private int startingIndexOfPhotoMultiplierData;
    private double[] xDataMean;
    private double[][] xDataCovariance;
    private Matrix TT;
    private double effectSamp;
    private double ExitCrit;

    private MCMCProcess2(
            AnalysisMethod analysisMethod,
            SingleBlockDataSetRecord singleBlockDataSetRecord,
            SingleBlockModelInitForMCMC2.SingleBlockModelRecordWithCov singleBlockInitialModelRecordWithCov) {
        this.analysisMethod = analysisMethod;
        this.singleBlockDataSetRecord = singleBlockDataSetRecord;
        singleBlockInitialModelRecord_X0 = singleBlockInitialModelRecordWithCov.singleBlockModelRecord();
        covarianceMatrix_C0 = singleBlockInitialModelRecordWithCov.covarianceMatrix_C0();
    }

    public static int getModelCount() {
        return modelCount;
    }

    public static synchronized MCMCProcess2 createMCMCProcess2(
            AnalysisMethod analysisMethod,
            SingleBlockDataSetRecord singleBlockDataSetRecord,
            SingleBlockModelInitForMCMC2.SingleBlockModelRecordWithCov singleBlockInitialModelRecordWithCov) {
        /*
            % MCMC Parameters
            maxcnt = 2000;  % Maximum number of models to save
            hier = 1;  % Hierachical?
            datsav=100;  % Save model every this many steps

            burn = 10;  % Burn-in, start doing stats after this many saved models

            temp=1; % Unused parameter for parallel tempering algorithm

            % Baseline multiplier - weight Daly more strongly (I think)
            blmult = ones(size(d0.data));
            blmult(d0.axflag)=0.1;


            Ndata=d0.Ndata; % Number of picks
            Nsig = d0.Nsig; % Number of noise variables
         */
        MCMCProcess2 mcmcProcess2 = new MCMCProcess2(analysisMethod, singleBlockDataSetRecord, singleBlockInitialModelRecordWithCov);
        return mcmcProcess2;
    }

    public void initializeMCMCProcess2() {
        faradayCount = singleBlockInitialModelRecord_X0.faradayCount();
        // do not use identity ratio per Noah as it introduces zeroes into covariance matrix
        ratioCount = singleBlockInitialModelRecord_X0.isotopeCount() - 1;

        /*
            % MCMC Parameters
            maxcnt = 2000;  % Maximum number of models to save
            hier = 1;  % Hierachical?
            datsav=100;  % Save model every this many steps

            burn = 10;  % Burn-in, start doing stats after this many saved models

            temp=1; % Unused parameter for parallel tempering algorithm

            % Baseline multiplier - weight Daly more strongly (I think)
            blmult = ones(size(d0.data));
            blmult(d0.axflag)=0.1;


            Ndata=d0.Ndata; % Number of picks
            Nsig = d0.Nsig; % Number of noise variables
         */
        //TODO: remove his variable
        hierarchical = false;
        tempering = 1.0;
        /*
            Ntemp = 10000; % Cool search over this number of steps
            % Create tempering vector - start high, cool down to 1 then stay there
            TT = ones(maxcnt*datsav,1);TT(1:Ntemp) = linspace(5,1,Ntemp)';
         */
        double nTemp = 10000;

        double[] hot = linspace(5, 1, nTemp).toRawCopy1D();
        double[] TTarray = new double[modelCount + 1];
        Arrays.fill(TTarray, 1.0);
        System.arraycopy(hot, 0, TTarray, 0, hot.length);
        TT = new Matrix(TTarray, modelCount + 1);//modelCount);

        startingIndexOfFaradayData = singleBlockDataSetRecord.getCountOfBaselineIntensities();
        startingIndexOfPhotoMultiplierData = startingIndexOfFaradayData + singleBlockDataSetRecord.getCountOfOnPeakFaradayIntensities();

        baselineMultiplier = new double[singleBlockInitialModelRecord_X0.dataArray().length];
        Arrays.fill(baselineMultiplier, 1.0);
        for (int row = startingIndexOfPhotoMultiplierData; row < singleBlockInitialModelRecord_X0.dataArray().length; row++) {
            baselineMultiplier[row] = 0.1;
        }

        keptUpdates = new int[5][4];
        ensembleRecordsList = new ArrayList<>();
        int countOfDFGains = 1;
        int countOfCycles = singleBlockDataSetRecord.blockKnotInterpolationStore().getColDim();

        sizeOfModel = ratioCount + countOfCycles + faradayCount + countOfDFGains;


        proposalRangesRecord =
                buildProposalRangesRecord(singleBlockInitialModelRecord_X0.intensities());
//        proposalSigmasRecord =
//                buildProposalSigmasRecord(singleBlockInitialModelRecord_X0.baselineStandardDeviationsArray(), proposalRangesRecord);

        /*
            % Modified Gelman-Rubin Convergence
            alpha=0.025; % Confidence interval we want to be accurate (0.05 = 95% CI)
            epsilon=0.025; % Relative confidence in mean compared to std dev estimator(?)
            EffectSamp = 2^(2/Nmod)*pi/(Nmod*gamma(Nmod/2))^(2/Nmod)*chi2inv(1-alpha,Nmod)/epsilon^2;
            Mchain = 1; % Number of Chains
            ExitCrit = sqrt(1+Mchain/EffectSamp); % Exit when G-R criterium less than this
         */
        double alpha = 0.025; //Confidence interval we want to be accurate (0.05 = 95% CI)
        double epsilon = 0.025; // Relative confidence in mean compared to std dev estimator(?)
        ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(sizeOfModel, 1.0 - alpha);
        effectSamp =
                StrictMath.pow(2.0, (2.0 / sizeOfModel))
                        * Math.PI
                        / StrictMath.pow((sizeOfModel * gamma(sizeOfModel / 2.0)), (2.0 / sizeOfModel))
                        * chiSquaredDistribution.inverseCumulativeProbability(1.0 - alpha) / StrictMath.pow(epsilon, 2.0);
        double mchain = 1.0; // Number of Chains
        ExitCrit = StrictMath.sqrt(1.0 + mchain / effectSamp); //Exit when G-R criterium less than this

        xDataMean = new double[sizeOfModel];
        xDataCovariance = new double[sizeOfModel][sizeOfModel];

        buildForwardModel();

    }

    private void buildForwardModel() {
        /*
% Assign initial values for model x
x=x0;


    %% Forward model data from initial model

        % Forward model baseline measurements
        for mm=1:d0.Nfar%+1  % Iterate over Faradays
            d(d0.blflag & d0.det_ind(:,mm),1) = x0.BL(mm); % Faraday Baseline
            dnobl(d0.blflag & d0.det_ind(:,mm),1) = 0; % Data with No Baseline
        end

        % Forward model isotope measurements
        for n = 1:d0.Nblock  % Iterate over blocks

            % Calculate block intensity from intensity variables
            Intensity{n} = InterpMat{n}*x0.I{n};
            Intensity2{n} = Intensity{n};

            %Iterate over Isotopes
            for mm=1:d0.Niso;
                % Calculate Daly data
                itmp = d0.iso_ind(:,mm) & d0.axflag & d0.block(:,n); % If isotope and axial and block number
                %d(itmp) = exp(x0.lograt(mm))*Intensity{n}(d0.time_ind(itmp));
                d(itmp) = (x0.lograt(mm))*Intensity{n}(d0.time_ind(itmp));    %debug
                dnobl(itmp) = d(itmp);

                % Calculate Faraday datas
                itmp = d0.iso_ind(:,mm) & ~d0.axflag & d0.block(:,n);
                %dnobl(itmp) = exp(x0.lograt(mm))*x0.DFgain^-1 *Intensity{n}(d0.time_ind(itmp)); % Data w/o baseline
                dnobl(itmp) = (x0.lograt(mm))*x0.DFgain^-1 *Intensity{n}(d0.time_ind(itmp)); % Data w/o baseline % debug
                d(itmp) = dnobl(itmp) + x0.BL(d0.det_vec(itmp)); % Add baseline

            end
        end

        */

        // NOTE: these already populated in the initial model singleBlockInitialModelRecord_X0
        dataArray = singleBlockInitialModelRecord_X0.dataWithNoBaselineArray().clone();
        dataWithNoBaselineArray = singleBlockInitialModelRecord_X0.dataWithNoBaselineArray().clone();
        dataSignalNoiseArray = singleBlockInitialModelRecord_X0.dataSignalNoiseArray().clone();

        /*
            % New data covariance vector
            Dsig = sqrt(x0.sig(d0.det_vec).^2 + x0.sig(end).*dnobl);

            % Initialize data residual vectors
            restmp=zeros(size(Dsig));
            restmp2=zeros(size(Dsig));

            % Calculate data residuals from starting model=
            restmp = (d0.data-d).^2;


            % Calculate error function
            E=sum(restmp.*blmult./Dsig/TT(1));  % Weighted by noise variance (for acceptance)
            E0=sum(restmp);  % Unweighted (for tracking convergence)
        */

        initialModelErrorWeighted_E = 0.0;
        initialModelErrorUnWeighted_E0 = 0.0;

        for (int row = 0; row < dataSignalNoiseArray.length; row++) {
            double calculatedValue = StrictMath.pow(singleBlockDataSetRecord.blockIntensityArray()[row] - dataArray[row], 2);
            initialModelErrorWeighted_E = initialModelErrorWeighted_E + (calculatedValue * baselineMultiplier[row] / dataSignalNoiseArray[row] / TT.get(1, 0));
            initialModelErrorUnWeighted_E0 = initialModelErrorUnWeighted_E0 + calculatedValue;
        }
    }

    public synchronized PlotBuilder[][] applyInversionWithAdaptiveMCMC2(LoggingCallbackInterface loggingCallback) {

        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        SingleBlockModelRecord singleBlockInitialModelRecord_X = singleBlockInitialModelRecord_X0.clone();
        /*
            for m = 1:maxcnt*datsav
                % Choose an operation for updating model
                oper = RandomOperMS(hier);

                clear delx

                if cnt<100000
                    adaptflag = 0;  % For first NN iterations, do regular MCMC
                else
                    adaptflag = 1;  % After, switch to Adaptive
                    temp = 1;
                end

                allflag = adaptflag;

                % Update model and save proposed update values (delx)
                [x2,delx] = UpdateMSv2(oper,x,psig,prior,ensemble,xcov,delx_adapt(:,mod(m,datsav)+1),adaptflag,allflag);
         */

        RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
        randomDataGenerator.reSeedSecure();

        DecimalFormat statsFormat = new DecimalFormat("#0.000000");
        StopWatch watch = new StopWatch();
        watch.start();
        int counter = 0;
        int counter2 = 0;
        SingleBlockModelUpdater2 singleBlockModelUpdater2 = new SingleBlockModelUpdater2();
        singleBlockModelUpdater2.buildPriorLimits(singleBlockInitialModelRecord_X, proposalRangesRecord);

        int countOfData = singleBlockInitialModelRecord_X.dataArray().length;
        int[] detectorOrdinalIndices = singleBlockDataSetRecord.blockDetectorOrdinalIndicesArray();
        Map<Integer, Integer> mapDetectorOrdinalToFaradayIndex = singleBlockInitialModelRecord_X.mapDetectorOrdinalToFaradayIndex();
        int[] isotopeOrdinalIndicesArray = singleBlockDataSetRecord.blockIsotopeOrdinalIndicesArray();

        double beta = 0.05;

        boolean notConverged = true;
        String loggingSnippet;
        for (long modelIndex = 1; modelCount >= modelIndex; modelIndex++) {//********************************************
            if (notConverged) {
                long prev = System.nanoTime();

                boolean adaptiveFlag = true;
                boolean allFlag = true;
                tempering = 1.0;

                // Scott's new way April 2023
                String operation = singleBlockModelUpdater2.randomOperMS(hierarchical);
            /*
                   if m<=2*Nmod   % Use initial covariance until 2*N
                    C = C0;
                      else  After that begin updating based on model covariance
                    Next proposal based initial variance and iterative covariance
                    C = beta*C0 + (1-beta)*2.38^2*Nmod^-1*xcov;
                    C=(C'+C)/2; % Make sure it's symmetrical
                   end
            */
                Matrix xDataCovarianceMatrix = new Matrix(xDataCovariance);
                Matrix c0_Matrix = covarianceMatrix_C0;
                Matrix c_Matrix;

                if (modelIndex <= 2L * sizeOfModel) {
                    c_Matrix = (Matrix) c0_Matrix.clone();
                } else {
                    c_Matrix = c0_Matrix.times(beta).plus(xDataCovarianceMatrix.times((1.0 - beta) * 2.38 * 2.38 / sizeOfModel));
                    c_Matrix = c_Matrix.transpose().plus(c_Matrix).times(0.5);
                }

                /*
                    % Draw random numbers based on covariance for next proposal
                    delx_adapt = mvnrnd(zeros(Nmod,1),C)';

                    % Update model and save proposed update values (delx)
                    [x2,delx] = UpdateMSv2(oper,x,psig,prior,ensemble,xcov,delx_adapt,adaptflag,allflag);
                */
                Matrix delx_adapt_Matrix = MatLabCholesky.mvnrndTripoli(new double[sizeOfModel], c_Matrix.getArray(), 1).transpose();

                SingleBlockModelRecord dataModelUpdaterOutputRecord_x2 =
                        singleBlockModelUpdater2.updateMSv2(
                                operation,
                                singleBlockInitialModelRecord_X,
                                null,
                                proposalRangesRecord,
                                xDataCovariance,
                                delx_adapt_Matrix.getRowPackedCopy(),
                                adaptiveFlag,
                                allFlag
                        );
                boolean noiseOperation = operation.toLowerCase(Locale.ROOT).startsWith("n");

                /*
          %% Create updated data based on new model
                % I was working on making this more compact and some of the details
                % elude me.
                tmpBLind = [x2.BL; 0];
                tmpBL = tmpBLind(d0.det_vec);
                tmpDF = ones(d0.Ndata,1);
                tmpDF(~d0.axflag) = x2.DFgain^-1;
                %tmpLR = exp(x2.lograt(d0.iso_vec)); % debug
                tmpLR = (x2.lograt(d0.iso_vec));
                tmpI = zeros(d0.Ndata,1);
                for n=1:d0.Nblock
                    Intensity2{n} = InterpMat{n}*x2.I{n};
                    tmpI(block0(n):blockf(n)) = Intensity2{n}(d0.time_ind(block0(n):blockf(n)));
                    tmpI(blockax0(n):blockaxf(n)) = Intensity2{n}(d0.time_ind(blockax0(n):blockaxf(n)));
                end
             */

                double[] baseLineMeansArray = dataModelUpdaterOutputRecord_x2.baselineMeansArray();
                double[] updatedBaseLineMeansArray = new double[baseLineMeansArray.length + 1];
                System.arraycopy(baseLineMeansArray, 0, updatedBaseLineMeansArray, 0, updatedBaseLineMeansArray.length - 1);

                double[] updatedBaseLineArray = new double[countOfData];

                double[] updatedDetectorFaradayArray = new double[countOfData];
                Arrays.fill(updatedDetectorFaradayArray, 1.0);

                double[] updatedLogRatioArray = new double[countOfData];
                double[] updatedIntensitiesArray = new double[countOfData];

                double[] logRatios = dataModelUpdaterOutputRecord_x2.logRatios();
                double detectorFaradayGain = dataModelUpdaterOutputRecord_x2.detectorFaradayGain();

                for (int row = 0; row < countOfData; row++) {
                    if (row < startingIndexOfPhotoMultiplierData) {
                        int detectorIndex = mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndices[row]);
                        updatedBaseLineArray[row] = updatedBaseLineMeansArray[detectorIndex];
                        updatedDetectorFaradayArray[row] = 1.0 / detectorFaradayGain;
                    }
                    // Oct 2022 per email from Noah, eliminate the iden/iden ratio to guarantee positive definite  covariance matrix >> isotope count - 1
                    if (isotopeOrdinalIndicesArray[row] == 0) {
                        updatedLogRatioArray[row] = 1.0;
                    } else if (isotopeOrdinalIndicesArray[row] - 1 < logRatios.length) {
                        updatedLogRatioArray[row] = exp(logRatios[isotopeOrdinalIndicesArray[row] - 1]);
                    } else {
                        updatedLogRatioArray[row] = 1.0;
                    }
                }

                long interval1 = System.nanoTime() - prev;
                prev = interval1 + prev;

                MatrixStore<Double> intensity2 = singleBlockDataSetRecord.blockKnotInterpolationStore().multiply(storeFactory.columns(dataModelUpdaterOutputRecord_x2.I0()));

                double[] intensity2Array = intensity2.toRawCopy1D();
                int[] timeIndicesArray = singleBlockDataSetRecord.blockTimeIndicesArray();
                for (int row = startingIndexOfFaradayData; row < countOfData; row++) {
                    updatedIntensitiesArray[row] = intensity2Array[timeIndicesArray[row]];
                }

                long interval2 = System.nanoTime() - prev;
                prev = interval2 + prev;

                /*
                dnobl2 = tmpDF.*tmpLR.*tmpI;
                % New data vector
                d2 = dnobl2 + tmpBL;
                % New data covariance vector
                Dsig2 = x2.sig(d0.det_vec).^2 + x2.sig(d0.iso_vec+d0.Ndet).*dnobl2;
                % Calculate residuals for current and new model
                restmp = (d0.data-d).^2;
                restmp2 = (d0.data-d2).^2;
                E02=sum(restmp2);  % Unweighted error func (for visualization)
                if strcmp(oper,'noise')
                    % If noise operation
                    E=sum(restmp./Dsig);
                    E2=sum(restmp2./Dsig2);
                    dE=E2-E; % Change in misfit
                else
                    % If any other model update
                    E=sum(restmp.*blmult./Dsig);
                    E2=sum(restmp2.*blmult./Dsig2);
                    dE=temp^-1*(E2-E); % Change in misfit
                end
             */
                double[] dataWithNoBaselineArray2 = new double[countOfData];
                double[] dataArray2 = new double[countOfData];

                for (int row = 0; row < countOfData; row++) {
                    double value = updatedDetectorFaradayArray[row] * updatedLogRatioArray[row] * updatedIntensitiesArray[row];
                    dataWithNoBaselineArray2[row] = value;
                    dataArray2[row] = value + updatedBaseLineArray[row];
                }

                double[] dataSignalNoiseArray2 = new double[countOfData];
                double E02 = 0.0;
                double E = 0.0;
                double E2 = 0.0;
                double dE;
                double sumLogDSignalNoise = 0.0;
                double sumLogDSignalNoise2 = 0.0;
                double keep;

                long interval3 = System.nanoTime() - prev;
                prev = interval3 + prev;

            /*
            Dsig2 = x2.sig(d0.det_vec).^2 + x2.sig(d0.iso_vec+d0.Ndet).*dnobl2;
             */
                dataSignalNoiseArray2 = dataSignalNoiseArray.clone();
//                int[] isotopeOrdinalIndices = singleBlockDataSetRecord.blockIsotopeOrdinalIndicesArray();
                double[] intensitiesArray = singleBlockDataSetRecord.blockIntensityArray();
//                double[] signalNoiseSigma = dataModelUpdaterOutputRecord_x2.signalNoiseSigma();
                for (int row = 0; row < countOfData; row++) {
//                    int detectorIndex = mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndices[row]);
//                    double term1 = StrictMath.pow(signalNoiseSigma[detectorIndex], 2);
//                    double term2 = signalNoiseSigma[isotopeOrdinalIndices[row] - 1 + faradayCount + 1];
//                    dataSignalNoiseArray2[row] = term1 + term2 * dataWithNoBaselineArray2[row];
                    double residualValue = pow(intensitiesArray[row] - dataArray[row], 2);
                    double residualValue2 = pow(intensitiesArray[row] - dataArray2[row], 2);
                    E02 += residualValue2;


                /*
                if strcmp(oper,'noise')
                    % If noise operation
                    E=sum(restmp./Dsig);
                    E2=sum(restmp2./Dsig2);
                    dE=E2-E; % Change in misfit
                else
                    % If any other model update
                    E=sum(restmp.*blmult./Dsig/TT(m));
                    E2=sum(restmp2.*blmult./Dsig2/TT(m));
                    dE=temp^-1*(E2-E); % Change in misfit
                end
                 */
                    if (noiseOperation) {
                        E += residualValue / dataSignalNoiseArray[row];
                        E2 += residualValue2 / dataSignalNoiseArray2[row];
                        sumLogDSignalNoise += -1.0 * Math.log(dataSignalNoiseArray[row]);
                        sumLogDSignalNoise2 += -1.0 * Math.log(dataSignalNoiseArray2[row]);
                    } else {
                        E += residualValue * baselineMultiplier[row] / dataSignalNoiseArray[row] / TT.get((int) modelIndex, 0);
                        E2 += residualValue2 * baselineMultiplier[row] / dataSignalNoiseArray2[row] / TT.get((int) modelIndex, 0);
                    }
                } //rows loop

                long interval4 = System.nanoTime() - prev;
                prev = interval4 + prev;

               /*
                    % Decide whether to accept or reject model
                    keep = AcceptItMS(oper,dE,psig,delx,prior,Dsig,Dsig2,d0);
                    //keep = min(1,exp(X/2-(dE)/2));
                 */
                if (noiseOperation) {
                    dE = E2 - E;
                    double deltaLogNoise = sumLogDSignalNoise2 - sumLogDSignalNoise;
                    keep = min(1.0, exp(deltaLogNoise / 2.0 - (dE) / 2.0));
                } else {
                    dE = 1.0 / tempering * (E2 - E);
                    keep = min(1.0, exp(-(dE) / 2.0));
                }

            /*
                    % Update kept variables for display
                    kept(OpNumMS(oper),2) = kept(OpNumMS(oper),2)+1;
                    kept(OpNumMS(oper),4) = kept(OpNumMS(oper),4)+1;

                    % If we accept the new model update values
                    if keep>=rand(1)
                        E=E2; % Misfit
                        E0=E02; % Unweighted misfit
                        d=d2; % Data
                        x=x2; % Model
                        Dsig=Dsig2;  % Model variance
                        dnobl=dnobl2;  % Data without baseline
                        Intensity=Intensity2;  % Intensity

                        % Display info
                        kept(OpNumMS(oper),1) = kept(OpNumMS(oper),1)+1;
                        kept(OpNumMS(oper),3) = kept(OpNumMS(oper),3)+1;
                    end
                */

                int operationIndex = operations.indexOf(operation);
                keptUpdates[operationIndex][1] = keptUpdates[operationIndex][1] + 1;
                keptUpdates[operationIndex][3] = keptUpdates[operationIndex][3] + 1;

                if (keep >= randomDataGenerator.nextUniform(0, 1)) {
                    E = E2;
                    initialModelErrorUnWeighted_E0 = E02;

                    singleBlockInitialModelRecord_X = new SingleBlockModelRecord(
                            dataModelUpdaterOutputRecord_x2.blockNumber(),
                            dataModelUpdaterOutputRecord_x2.baselineMeansArray(),
                            dataModelUpdaterOutputRecord_x2.baselineStandardDeviationsArray(),
                            dataModelUpdaterOutputRecord_x2.detectorFaradayGain(),
                            dataModelUpdaterOutputRecord_x2.mapDetectorOrdinalToFaradayIndex(),
                            dataModelUpdaterOutputRecord_x2.logRatios(),
                            dataModelUpdaterOutputRecord_x2.signalNoiseSigma(),
                            dataArray2.clone(),
                            dataWithNoBaselineArray2.clone(),
                            dataSignalNoiseArray2.clone(),
                            dataModelUpdaterOutputRecord_x2.I0(),
                            intensity2.toRawCopy1D(),
                            dataModelUpdaterOutputRecord_x2.faradayCount(),
                            dataModelUpdaterOutputRecord_x2.isotopeCount()
                    );
//                    dataSignalNoiseArray = dataSignalNoiseArray2.clone();

                    keptUpdates[operationIndex][0] = keptUpdates[operationIndex][0] + 1;
                    keptUpdates[operationIndex][2] = keptUpdates[operationIndex][2] + 1;
                }

            /*
                [xmean,xcov] = UpdateMeanCovMS(x,xmean,xcov,m);
                 */
                SingleBlockModelUpdater2.UpdatedCovariancesRecord updatedCovariancesRecord =
                        singleBlockModelUpdater2.updateMeanCovMS2(
                                singleBlockInitialModelRecord_X,
                                xDataCovariance,
                                xDataMean,
                                modelIndex
                        );

                xDataCovariance = updatedCovariancesRecord.dataCov();
                xDataMean = updatedCovariancesRecord.dataMean();

                long interval5 = System.nanoTime() - prev;

                if (0 == modelIndex % (stepCountForcedSave)) {
                /*
                    cnt=cnt+1; % Increment counter

                    ensemble(cnt).lograt=log(x.lograt); % Log ratios
                    for mm=1:d0.Nblock
                        ensemble(cnt).I{mm}=x.I{mm}; % Intensity by block
                    end
                    ensemble(cnt).BL=x.BL;  % Baselines
                    ensemble(cnt).DFgain=x.DFgain;  %Daly-Faraday gain
                    ensemble(cnt).sig=x.sig;  % Noise hyperparameter
                    ensemble(cnt).E=E;  % Misfit
                    ensemble(cnt).E0=E0; % Unweighted misfit
                 */
                    counter++;

                    ensembleRecordsList.add(new EnsemblesStore.EnsembleRecord(
                            singleBlockInitialModelRecord_X.logRatios(),
                            singleBlockInitialModelRecord_X.I0(),
                            singleBlockInitialModelRecord_X.baselineMeansArray(),
                            singleBlockInitialModelRecord_X.detectorFaradayGain(),
                            singleBlockInitialModelRecord_X.signalNoiseSigma(),
                            E,
                            initialModelErrorUnWeighted_E0));

                /*
                    display(sprintf('%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%'));
                    %display(sprintf('Processor %d, %d models accepted out of %d',iproc,countr,m));
                    display(sprintf('Elapsed time = %0.2f Seconds for %d realizations (%d total)',toc,10*datsav,m));
                    display(sprintf('Error function = %.0f',sqrt(E0/Ndata)));
                    display(sprintf('Change all variables:   %d of %d accepted (%.1f%% total)',sum(kept(1:4,1:2)),100*sum(kept(1:4,3))/sum(kept(1:4,4))));

                    if hier==1;
                        display(sprintf('Noise:              %d of %d accepted (%.1f%% total)',kept(5,1:2),100*kept(5,3)/kept(5,4)));
                    end
                    display(sprintf(' '));
                 */
                    if (0 == modelIndex % (10 * stepCountForcedSave)) {
                        // calculate summaries
                        int modelsKeptLocal = 0;
                        int modelsTotalLocal = 0;
                        int modelsKept = 0;
                        int modelsTotal = 0;
                        for (int row = 0; 4 > row; row++) {
                            modelsKeptLocal += keptUpdates[row][0];
                            modelsTotalLocal += keptUpdates[row][1];
                            modelsKept += keptUpdates[row][2];
                            modelsTotal += keptUpdates[row][3];
                        }


                        loggingSnippet =
                                modelIndex + " >%%%%%%%%%%%%%%%%%%%%%%% Tripoli in Java test %%%%%%%%%%%%%%%%%%%%%%%"
                                        + "  BLOCK # " + singleBlockInitialModelRecord_X.blockNumber()
                                        + "\nElapsed time = " + statsFormat.format(watch.getTime() / 1000.0) + " seconds for " + 10 * stepCountForcedSave + " realizations of total = " + modelIndex
                                        + "\nError function = " + statsFormat.format(StrictMath.sqrt(initialModelErrorUnWeighted_E0 / countOfData))
                                        + "\nChange All Variables: " + modelsKeptLocal + " of " + modelsTotalLocal + " accepted (" + statsFormat.format(100.0 * modelsKept / modelsTotal) + "% total)"
                                        + ("\nIntervals: in microseconds, each from prev or zero time till new interval"
                                        + " Interval1 " + (interval1 / 1000)
                                        + " Interval2 " + (interval2 / 1000)
                                        + " Interval3 " + (interval3 / 1000)
                                        + " Interval4 " + (interval4 / 1000)
                                        + " Interval5 " + (interval5 / 1000));

                        System.err.println("\n" + loggingSnippet + "\n");
                        loggingCallback.receiveLoggingSnippet(loggingSnippet);

                        for (int i = 0; 5 > i; i++) {
                            keptUpdates[i][0] = 0;
                            keptUpdates[i][1] = 0;
                        }

                    /*
                     % If number of iterations is square number, larger than effective
                        % sample size, test for convergence
                        if mod(sqrt(cnt),1)==0 && cnt >= EffectSamp/datsav

                            cnt2 = cnt2+1;
                            Rexit = GRConverge(x,ensemble);  %Gelman-Rubin multivariate criterium

                            rrr(cnt2) = Rexit; %debug

                            if Rexit<=ExitCrit
                                disp(sprintf('MCMC exiting after %d iters with R of %0.6f',m,Rexit))
                                break
                            end
                        end
                     */

                        if ((0 == Math.sqrt(counter) % 1) && (counter >= effectSamp / stepCountForcedSave)) {
                            counter2++;
                            double rExit = singleBlockModelUpdater2.grConverge(ensembleRecordsList);

                            if (rExit <= ExitCrit) {
                                notConverged = false;
                                String exitMessage = "Alert:  for BLOCK # " + singleBlockInitialModelRecord_X.blockNumber() + ",  MCMC2 has converged after " + modelIndex + " iterations, with R = " + rExit;
                                System.err.println("\n" + exitMessage + "\n");
                                loggingCallback.receiveLoggingSnippet(exitMessage);
                            }
                        }

                        watch.reset();
                        watch.start();
                    }
                }
            }// end model loop
        }// convergence check

        return SingleBlockDataModelPlot.analysisAndPlotting(singleBlockDataSetRecord, ensembleRecordsList, singleBlockInitialModelRecord_X, analysisMethod);
    }
}