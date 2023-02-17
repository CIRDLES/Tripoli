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
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.utilities.callbacks.LoggingCallbackInterface;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.structure.Access2D;

import java.text.DecimalFormat;
import java.util.*;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.StrictMath.exp;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.ProposedModelParameters.buildProposalRangesRecord;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.ProposedModelParameters.buildProposalSigmasRecord;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockModelUpdater.*;

/**
 * @author James F. Bowring
 */
public class MCMCProcess {

    public static boolean ALLOW_EXECUTION = true;
    private final SingleBlockModelRecord singleBlockInitialModelRecord_X0;
    private final AnalysisMethod analysisMethod;
    private final SingleBlockDataSetRecord singleBlockDataSetRecord;
    List<EnsemblesStore.EnsembleRecord> ensembleRecordsList;
    private int faradayCount;
    private int ratioCount;
    private int maxIterationCount;
    private boolean hierarchical;
    private double tempering;
    private int stepCountForcedSave;
    private int burnInThreshold;
    private double[] baselineMultiplier;
    private ProposedModelParameters.ProposalRangesRecord proposalRangesRecord;
    private ProposedModelParameters.ProposalSigmasRecord proposalSigmasRecord;
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
    private double[][] delx_adapt;


    private MCMCProcess(AnalysisMethod analysisMethod, SingleBlockDataSetRecord singleBlockDataSetRecord, SingleBlockModelRecord singleBlockInitialModelRecord) {
        this.analysisMethod = analysisMethod;
        this.singleBlockDataSetRecord = singleBlockDataSetRecord;
        singleBlockInitialModelRecord_X0 = singleBlockInitialModelRecord;
    }

    public static MCMCProcess createMCMCProcess(AnalysisMethod analysisMethod, SingleBlockDataSetRecord singleBlockDataSetRecord, SingleBlockModelRecord singleBlockInitialModelRecord) {
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
        MCMCProcess mcmcProcess = new MCMCProcess(analysisMethod, singleBlockDataSetRecord, singleBlockInitialModelRecord);
        return mcmcProcess;
    }

    public void initializeMCMCProcess() {
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
        maxIterationCount = 2000;
        hierarchical = true;
        tempering = 1.0;
        stepCountForcedSave = 100;
        burnInThreshold = 10;
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

        xDataMean = new double[sizeOfModel];
        xDataCovariance = new double[sizeOfModel][sizeOfModel];
        delx_adapt = new double[sizeOfModel][stepCountForcedSave];

        proposalRangesRecord =
                buildProposalRangesRecord(singleBlockInitialModelRecord_X0.intensities());
        proposalSigmasRecord =
                buildProposalSigmasRecord(singleBlockInitialModelRecord_X0.baselineStandardDeviationsArray(), proposalRangesRecord);

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
            % Calculate block blockIntensities from blockIntensities variables
            Intensity{n} = InterpMat{n}*x0.I{n};
            Intensity2{n} = Intensity{n};

            %Iterate over Isotopes
            for mm=1:d0.Niso;
                % Calculate Daly data
                itmp = d0.iso_ind(:,mm) & d0.axflag & d0.block(:,n); % If isotope and axial and block number
                d(itmp) = exp(x0.lograt(mm))*Intensity{n}(d0.time_ind(itmp));
                dnobl(itmp) = d(itmp);

                % Calculate Faraday datas
                itmp = d0.iso_ind(:,mm) & ~d0.axflag & d0.block(:,n);
                dnobl(itmp) = exp(x0.lograt(mm))*x0.DFgain^-1 *Intensity{n}(d0.time_ind(itmp)); % Data w/o baseline
                d(itmp) = dnobl(itmp) + x0.BL(d0.det_vec(itmp)); % Add baseline
            end
        end
        */

        // NOTE: these already populated in the initial model singleBlockInitialModelRecord_X0
        dataArray = singleBlockInitialModelRecord_X0.dataArray().clone();
        dataWithNoBaselineArray = singleBlockInitialModelRecord_X0.dataWithNoBaselineArray().clone();
        dataSignalNoiseArray = singleBlockInitialModelRecord_X0.dataSignalNoiseArray();

        /*
            % New data covariance vector
            Dsig = sqrt(x0.sig(d0.det_vec).^2 + x0.sig(d0.iso_vec+d0.Ndet).*dnobl);

            % Initialize data residual vectors
            restmp=zeros(size(Dsig));
            restmp2=zeros(size(Dsig));

            % Calculate data residuals from starting model
            restmp = (d0.data-d).^2;

            % Calculate error function
            E=sum(restmp.*blmult./Dsig);  % Weighted by noise variance (for acceptance)
            E0=sum(restmp);  % Unweighted (for tracking convergence)
        */

        initialModelErrorWeighted_E = 0.0;
        initialModelErrorUnWeighted_E0 = 0.0;

        for (int row = 0; row < dataSignalNoiseArray.length; row++) {
            double calculatedValue = StrictMath.pow(singleBlockDataSetRecord.blockIntensityArray()[row] - dataArray[row], 2);
            initialModelErrorWeighted_E = initialModelErrorWeighted_E + (calculatedValue * baselineMultiplier[row] / dataSignalNoiseArray[row]);
            initialModelErrorUnWeighted_E0 = initialModelErrorUnWeighted_E0 + calculatedValue;
        }
    }

    public PlotBuilder[][] applyInversionWithAdaptiveMCMC(LoggingCallbackInterface loggingCallback) {

        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        SingleBlockModelRecord singleBlockInitialModelRecord_initial = singleBlockInitialModelRecord_X0.clone();
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
        int[] operationOrder = preOrderOpsMS(singleBlockInitialModelRecord_initial, maxIterationCount * stepCountForcedSave);
        buildPriorLimits(proposalSigmasRecord, proposalRangesRecord);

        int countOfData = singleBlockInitialModelRecord_initial.dataArray().length;
        int[] detectorOrdinalIndices = singleBlockDataSetRecord.blockDetectorOrdinalIndicesArray();
        Map<Integer, Integer> mapDetectorOrdinalToFaradayIndex = singleBlockInitialModelRecord_initial.mapDetectorOrdinalToFaradayIndex();
        int[] isotopeOrdinalIndicesArray = singleBlockDataSetRecord.blockIsotopeOrdinalIndicesArray();

        String loggingSnippet = "";
        for (int modelIndex = 1; modelIndex <= maxIterationCount * stepCountForcedSave; modelIndex++) {//*****************
            if (ALLOW_EXECUTION) {
                long prev = System.nanoTime();

                // todo: handle adaptiveFlag case
                boolean adaptiveFlag = (500000 <= counter); // abandon for now
                boolean allFlag = adaptiveFlag;
                int columnChoice = modelIndex % stepCountForcedSave;
                double[] delx_adapt_slice = storeFactory.copy(Access2D.wrap(delx_adapt)).sliceColumn(columnChoice).toRawCopy1D();

                // original way
//            String operation = SingleBlockModelUpdater.randomOperation(hierarchical);
//            SingleBlockModelRecord dataModelUpdaterOutputRecord_x2 = updateMSv2(
//                    operation,
//                    singleBlockInitialModelRecord_initial,
//                    proposalSigmasRecord,
//                    proposalRangesRecord,
//                    xDataCovariance,
//                    delx_adapt_slice,
//                    adaptiveFlag,
//                    allFlag
//            );

                // Scott's new way Feb 2023
                String operation = randomOperMS_Preorder(operationOrder[modelIndex - 1]);
                SingleBlockModelRecord dataModelUpdaterOutputRecord_x2 = updateMSv2Preorder(
                        operationOrder[modelIndex - 1],
                        singleBlockInitialModelRecord_initial,
                        proposalSigmasRecord,
                        proposalRangesRecord,
                        xDataCovariance,
                        delx_adapt_slice,
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
                tmpLR = exp(x2.lograt(d0.iso_vec));
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
                    int detectorIndex = mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndices[row]);
                    updatedBaseLineArray[row] = updatedBaseLineMeansArray[detectorIndex];
                    if ((row >= startingIndexOfFaradayData) && (row < startingIndexOfPhotoMultiplierData)) {
                        updatedDetectorFaradayArray[row] = 1.0 / detectorFaradayGain;
                    }
                    // Oct 2022 per email from Noah, eliminate the iden/iden ratio to guarantee positive definite  covariance matrix >> isotope count - 1
                    if (isotopeOrdinalIndicesArray[row] - 1 < logRatios.length) {
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

                double[] dataSignalNoise2Array = new double[countOfData];
                double E02 = 0.0;
                double E = 0.0;
                double E2 = 0.0;
                double dE;
                double sumLogDSignalNoise = 0.0;
                double sumLogDSignalNoise2 = 0.0;
                double keep;

                long interval3 = System.nanoTime() - prev;
                prev = interval3 + prev;

                int[] isotopeOrdinalIndices = singleBlockDataSetRecord.blockIsotopeOrdinalIndicesArray();
                double[] intensitiesArray = singleBlockDataSetRecord.blockIntensityArray();
                double[] signalNoiseSigma = dataModelUpdaterOutputRecord_x2.signalNoiseSigma();
                for (int row = 0; row < countOfData; row++) {
                    int detectorIndex = mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndices[row]);
                    double term1 = StrictMath.pow(signalNoiseSigma[detectorIndex], 2);
                    double term2 = signalNoiseSigma[isotopeOrdinalIndices[row] - 1 + faradayCount + 1];
                    dataSignalNoise2Array[row] = term1 + term2 * dataWithNoBaselineArray2[row];
                    double residualValue = pow(intensitiesArray[row] - dataArray[row], 2);
                    double residualValue2 = pow(intensitiesArray[row] - dataArray2[row], 2);
                    E02 += residualValue2;

                /*
                    % Decide whether to accept or reject model
                    keep = AcceptItMS(oper,dE,psig,delx,prior,Dsig,Dsig2,d0);
                 */
                    if (noiseOperation) {
                        E += residualValue / dataSignalNoiseArray[row];
                        E2 += residualValue2 / dataSignalNoise2Array[row];
                        sumLogDSignalNoise += -1.0 * Math.log(dataSignalNoiseArray[row]);
                        sumLogDSignalNoise2 += -1.0 * Math.log(dataSignalNoise2Array[row]);
                    } else {
                        E += residualValue * baselineMultiplier[row] / dataSignalNoiseArray[row];
                        E2 += residualValue2 * baselineMultiplier[row] / dataSignalNoise2Array[row];
                    }
                } //rows loop

                long interval4 = System.nanoTime() - prev;
                prev = interval4 + prev;

                if (noiseOperation) {
                    dE = E2 - E;
                    double deltaLogNoise = sumLogDSignalNoise2 - sumLogDSignalNoise;
                    keep = min(1.0, exp(deltaLogNoise / 2.0 - (dE) / 2.0));//keep = min(1,exp(X/2-(dE)/2));
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

                    singleBlockInitialModelRecord_initial = new SingleBlockModelRecord(
                            dataModelUpdaterOutputRecord_x2.baselineMeansArray(),
                            dataModelUpdaterOutputRecord_x2.baselineStandardDeviationsArray(),
                            dataModelUpdaterOutputRecord_x2.detectorFaradayGain(),
                            dataModelUpdaterOutputRecord_x2.mapDetectorOrdinalToFaradayIndex(),
                            dataModelUpdaterOutputRecord_x2.logRatios(),
                            dataModelUpdaterOutputRecord_x2.signalNoiseSigma(),
                            dataArray2.clone(),
                            dataWithNoBaselineArray2.clone(),
                            dataSignalNoise2Array.clone(),
                            dataModelUpdaterOutputRecord_x2.I0(),
                            intensity2.toRawCopy1D(),
                            dataModelUpdaterOutputRecord_x2.faradayCount(),
                            dataModelUpdaterOutputRecord_x2.isotopeCount()
                    );
                    dataSignalNoiseArray = dataSignalNoise2Array.clone();

                    keptUpdates[operationIndex][0] = keptUpdates[operationIndex][0] + 1;
                    keptUpdates[operationIndex][2] = keptUpdates[operationIndex][2] + 1;
                }

                long interval5 = System.nanoTime() - prev;

                if (0 == modelIndex % (stepCountForcedSave)) {
                /*
                    cnt=cnt+1; % Increment counter
                    ensemble(cnt).lograt=x.lograt; % Log ratios
                    for mm=1:d0.Nblock
                        ensemble(cnt).I{mm}=x.I{mm}; % Intensity by block
                    end
                    ensemble(cnt).BL=x.BL;  % Baselines
                    ensemble(cnt).DFgain=x.DFgain;  %Daly-Faraday gain
                    ensemble(cnt).sig=x.sig;  % Noise hyperparameter
                    ensemble(cnt).E=E;  % Misfit
                    ensemble(cnt).E0=E0; % Unweighted misfit

                    covstart = 50;  % After this many iterations, begin calculating covariance iteratively
                    if cnt>=covstart+1
                        % Iterative covariance
                        [xmean,xcov] = UpdateMeanCovMS(x,xcov,xmean,ensemble,cnt-covstart,0);

                        % Draw random numbers based on covariance for next update
                        delx_adapt = mvnrnd(zeros(Nmod,1),2.38^2*xcov/Nmod,datsav)';
                    end
                 */
                    counter++;
                    ensembleRecordsList.add(new EnsemblesStore.EnsembleRecord(
                            singleBlockInitialModelRecord_initial.logRatios(),
                            singleBlockInitialModelRecord_initial.intensities(),
                            singleBlockInitialModelRecord_initial.baselineMeansArray(),
                            singleBlockInitialModelRecord_initial.detectorFaradayGain(),
                            singleBlockInitialModelRecord_initial.signalNoiseSigma(),
                            E,
                            initialModelErrorUnWeighted_E0));
                }

                int covStart = 50;
                if (counter >= covStart + 1) {
            /*
            % Iterative covariance
            [xmean,xcov] = UpdateMeanCovMS(x,xcov,xmean,ensemble,cnt-covstart,0);

            % Draw random numbers based on covariance for next update
            delx_adapt = mvnrnd(
            mean vector - zeros(Nmod,1),
                zeros(sizeOfModel,1)
            covariance matrix - 2.38^2*xcov/Nmod,

            n dimension of output matrix - datsav)';
                stepCountForcedSave
            */
                    SingleBlockModelUpdater.UpdatedCovariancesRecord updatedCovariancesRecord =
                            updateMeanCovMS(singleBlockInitialModelRecord_initial, xDataCovariance, xDataMean, ensembleRecordsList, counter - covStart, false);
                    xDataCovariance = updatedCovariancesRecord.dataCov();
                    xDataMean = updatedCovariancesRecord.dataMean();

                    if (adaptiveFlag) {
                    /*
                        delx_adapt =  mvnrnd(zeros(sizeOfModel,1), 2.38^2*xDataCovariance/sizeOfModel, stepCountForcedSave)'
                        stepCountForcedSave = 100
                        sizeOfModel = 20
                        */

                        double[] zeroMean = new double[sizeOfModel];

                        Matrix a = new Matrix(xDataCovariance);

                        if (0.0 != a.det()) {

                            MultivariateNormalDistribution mnd =
                                    new MultivariateNormalDistribution(zeroMean, storeFactory.copy(Access2D.wrap(xDataCovariance)).multiply(pow(2.38, 2) / (sizeOfModel)).toRawCopy2D());
                            double[][] samples = new double[stepCountForcedSave][];
                            for (int i = 0; i < stepCountForcedSave; i++) {
                                samples[i] = mnd.sample();
                            }
                            delx_adapt = storeFactory.copy(Access2D.wrap(samples)).transpose().toRawCopy2D();
                        } else {
                            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Bad matrix at model " + counter);
                        }
                    }
                }

                if (0 == modelIndex % (10 * stepCountForcedSave)) {
                    loggingSnippet =
                            "%%%%%%%%%%%%%%%%%%%%%%% Tripoli in Java test %%%%%%%%%%%%%%%%%%%%%%%"
                                    + " ADAPTIVE = " + adaptiveFlag
                                    + "\nElapsed time = " + statsFormat.format(watch.getTime() / 1000.0) + " seconds for " + 10 * stepCountForcedSave + " realizations of total = " + modelIndex
                                    + "\nError function = "
                                    + statsFormat.format(StrictMath.sqrt(initialModelErrorUnWeighted_E0 / countOfData))

                                    + "\nChange Log Ratio: "
                                    + keptUpdates[0][0]
                                    + " of "
                                    + keptUpdates[0][1]
                                    + " accepted (" + statsFormat.format(100.0 * keptUpdates[0][2] / keptUpdates[0][3]) + "% total)"

                                    + "\nChange Intensity: "
                                    + keptUpdates[1][0]
                                    + " of "
                                    + keptUpdates[1][1]
                                    + " accepted (" + statsFormat.format(100.0 * keptUpdates[1][2] / keptUpdates[1][3]) + "% total)"

                                    + "\nChange DF Gain: "
                                    + keptUpdates[2][0]
                                    + " of "
                                    + keptUpdates[2][1]
                                    + " accepted (" + statsFormat.format(100.0 * keptUpdates[2][2] / keptUpdates[2][3]) + "% total)"

                                    + "\nChange Baseline: "
                                    + keptUpdates[3][0]
                                    + " of "
                                    + keptUpdates[3][1]
                                    + " accepted (" + statsFormat.format(100.0 * keptUpdates[3][2] / keptUpdates[3][3]) + "% total)"

                                    + (hierarchical ?
                                    ("\nNoise: "
                                            + keptUpdates[4][0]
                                            + " of "
                                            + keptUpdates[4][1]
                                            + " accepted (" + statsFormat.format(100.0 * keptUpdates[4][2] / keptUpdates[4][3]) + "% total)")
                                            + ("\nIntervals: in microseconds, each from prev or zero time till new interval"
                                            + " Interval1 " + (interval1 / 1000)
                                            + " Interval2 " + (interval2 / 1000)
                                            + " Interval3 " + (interval3 / 1000)
                                            + " Interval4 " + (interval4 / 1000)
                                            + " Interval5 " + (interval5 / 1000)
                                    )
                                    : "")
                                    + "\n";

                    System.err.println("\n" + loggingSnippet);
                    loggingCallback.receiveLoggingSnippet(loggingSnippet);

                    for (int i = 0; 5 > i; i++) {
                        keptUpdates[i][0] = 0;
                        keptUpdates[i][1] = 0;
                    }

                    watch.reset();
                    watch.start();
                }
            } else {
                //loggingCallback.receiveLoggingSnippet("\n\nCancelled by user.");
                break;
            }
        }// end model loop
        if (ALLOW_EXECUTION) {
            // experiment with serializing results during development
            EnsemblesStore ensemblesStore = new EnsemblesStore(ensembleRecordsList, singleBlockInitialModelRecord_initial);
            try {
                TripoliSerializer.serializeObjectToFile(ensemblesStore, "EnsemblesStore.ser");
            } catch (TripoliException e) {
                e.printStackTrace();
            }
        }
        return SingleBlockDataModelPlot.analysisAndPlotting(singleBlockDataSetRecord, ensembleRecordsList, singleBlockInitialModelRecord_initial, analysisMethod);
    }
}