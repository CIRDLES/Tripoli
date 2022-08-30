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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.rjmcmc;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.DataSourceProcessor_OPPhoenix;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethodBuiltinFactory;
import org.cirdles.tripoli.utilities.callbacks.LoggingCallbackInterface;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer;
import org.cirdles.tripoli.visualizationUtilities.AbstractPlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.histograms.HistogramBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.ComboPlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.LinePlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.MultiLinePlotBuilder;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Normal;
import org.ojalgo.structure.Access1D;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.StrictMath.exp;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.rjmcmc.DataModelUpdater.updateMSv2;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.rjmcmc.DataModelUpdater.updateMeanCovMS;

/**
 * @author James F. Bowring
 */
public class DataModelDriverExperiment {

    private static final boolean doFullProcessing = true;

    public static AbstractPlotBuilder[] driveModelTest(Path dataFilePath, LoggingCallbackInterface loggingCallback) throws IOException {

        DataSourceProcessor_OPPhoenix dataSourceProcessorOPPhoenix
                = DataSourceProcessor_OPPhoenix.initializeWithAnalysisMethod(AnalysisMethodBuiltinFactory.analysisMethodsBuiltinMap.get("BurdickBlSyntheticData"));
        MassSpecOutputDataRecord massSpecOutputDataRecord = dataSourceProcessorOPPhoenix.prepareInputDataModelFromFile(dataFilePath);

        AbstractPlotBuilder[] plotBuilders;
        DataModellerOutputRecord dataModelInit;
        try {
            dataModelInit = DataModelInitializer.modellingTest(massSpecOutputDataRecord);

            List<EnsembleRecord> ensembleRecordsList = new ArrayList<>();
            DataModellerOutputRecord lastDataModelInit = null;
            if (doFullProcessing) {
                plotBuilders = applyInversionWithRJMCMC(massSpecOutputDataRecord, dataModelInit, loggingCallback);
            } else {
                try {
                    EnsemblesStore ensemblesStore = (EnsemblesStore) TripoliSerializer.getSerializedObjectFromFile("EnsemblesStore.ser", true);
                    ensembleRecordsList = ensemblesStore.getEnsembles();
                    lastDataModelInit = ensemblesStore.getLastDataModelInit();
                } catch (TripoliException e) {
                    e.printStackTrace();
                }
                plotBuilders = analysisAndPlotting(massSpecOutputDataRecord, ensembleRecordsList, lastDataModelInit);
            }
        } catch (RecoverableCondition e) {
            plotBuilders = new AbstractPlotBuilder[0];
        }

        return plotBuilders;
    }

    static AbstractPlotBuilder[] applyInversionWithRJMCMC(MassSpecOutputDataRecord massSpecOutputDataRecord, DataModellerOutputRecord dataModelInit_X0, LoggingCallbackInterface loggingCallback) {
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


            % Range for ratios and intensity parameters
            prior.BL = [-1 1]*1e6;  % Faraday baseline
            prior.BLdaly = [0 0];   % Daly baseline (no baseline uncertainty)
            prior.lograt = [-20 20]; % Log ratio
            prior.I = [0 1.5*max([x0.I{:}])];  % Intensity
            prior.DFgain = [0.8 1.0];  % Daly-Faraday gain

            prior.sig = [0 1e6];  % Noise hyperparameter for Faraday
            prior.sigdaly = [0 0]; % Gaussian noise on Daly
            prior.sigpois = [0 10]; % Poisson noise on Daly
         */
        int maxCount = 1000;//2000;
        boolean hierarchical = true;
        int stepCountForcedSave = 100;

        double[] baselineMultiplier = new double[massSpecOutputDataRecord.rawDataColumn().length];
        Arrays.fill(baselineMultiplier, 1.0);
        for (int row = 0; row < massSpecOutputDataRecord.axialFlagsForRawDataColumn().length; row++) {
            if (massSpecOutputDataRecord.axialFlagsForRawDataColumn()[row] == 1) {
                baselineMultiplier[row] = 0.1;
            }
        }
        double[][] priorBaselineFaraday = new double[][]{{-1.0e6, 1.0e6}};
        double[][] priorBaselineDaly = new double[][]{{0.0, 0.0}};
        double[][] priorLogRatio = new double[][]{{-20.0, 20.0}};
        double maxIntensity = Double.MIN_VALUE;
        double minIntensity = Double.MAX_VALUE;

        for (int row = 0; row < dataModelInit_X0.blockIntensities().length; row++) {
            maxIntensity = Math.max(dataModelInit_X0.blockIntensities()[row], maxIntensity);
            minIntensity = min(dataModelInit_X0.blockIntensities()[row], minIntensity);

        }

        double[][] priorIntensity = new double[][]{{0.0, 1.5 * maxIntensity}};
        double[][] priorDFgain = new double[][]{{0.8, 1.0}};
        double[][] priorSignalNoiseFaraday = new double[][]{{0.0, 1.0e6}};
        double[][] priorSignalNoiseDaly = new double[][]{{0.0, 0.0}};
        double[][] priorPoissonNoiseDaly = new double[][]{{0.0, 10.0}};

        PriorRecord priorRecord = new PriorRecord(
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

        for (int row = 0; row < dataModelInit_X0.baselineStandardDeviations().length; row++) {
            maxValue = Math.max(dataModelInit_X0.baselineStandardDeviations()[row], maxValue);
        }
        double psigBaselineFaraday = maxValue / 10.0;
        double psigBaselineDaly = 1.0e-1;
        double psigLogRatio = 0.0005 * 0.2;
        double psigIntensityPercent = (maxIntensity - minIntensity) / 100.0;
        double psigDFgain = 0.001;
        double psigSignalNoiseFaraday = maxValue;
        double psigSignalNoisePoisson = 0.5;
        double psigSignalNoiseDaly = 0;

        PsigRecord psigRecord = new PsigRecord(
                psigBaselineFaraday,
                psigBaselineDaly,
                psigLogRatio,
                psigIntensityPercent,
                psigDFgain,
                psigSignalNoiseFaraday,
                psigSignalNoisePoisson,
                psigSignalNoiseDaly
        );

        /*
            % Assign initial values for model x
            x=x0;

            %% Forward model data from initial model
            % Forward model baseline measurements
            for mm=1:d0.Nfar%+1  % Iterate over Faradays
                d(d0.blflag & d0.det_ind(:,mm),1) = x0.BL(mm); % Faraday Baseline
                dnobl(d0.blflag & d0.det_ind(:,mm),1) = 0; % Data with No Baseline
            end
         */

        double[] data = dataModelInit_X0.dataArray().clone();
        double[] dataWithNoBaseline = new double[dataModelInit_X0.dataArray().length];

        for (int faradayIndex = 0; faradayIndex < massSpecOutputDataRecord.faradayCount(); faradayIndex++) {
            for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().length; row++) {
                if ((massSpecOutputDataRecord.baseLineFlagsForRawDataColumn()[row] == 1)
                        &&
                        (massSpecOutputDataRecord.detectorFlagsForRawDataColumn()[row][faradayIndex] == 1)) {
                    data[row] = dataModelInit_X0.baselineMeans()[faradayIndex];
                    dataWithNoBaseline[row] = 0.0;
                }
            }
        }

        /*
        % Forward model isotope measurements
        for n = 1:d0.Nblock  % Iterate over blocks
            % Calculate block intensity from intensity variables
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

        // only using first block
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        for (int blockIndex = 0; blockIndex < 1; blockIndex++) {
            for (int isotopeIndex = 0; isotopeIndex < massSpecOutputDataRecord.isotopeCount(); isotopeIndex++) {
                for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().length; row++) {
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn()[row][isotopeIndex] == 1)
                            && (massSpecOutputDataRecord.axialFlagsForRawDataColumn()[row] == 1)
                            && massSpecOutputDataRecord.blockIndicesForRawDataColumn()[row] == (blockIndex + 1)) {
                        double calcValue =
                                // todo check here
                                exp(dataModelInit_X0.logratios()[isotopeIndex])
                                        * dataModelInit_X0.intensityPerBlock().get(blockIndex)[(int) massSpecOutputDataRecord.timeIndColumn()[row] - 1];
                        data[row] = calcValue;
                        dataWithNoBaseline[row] = calcValue;
                    }
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn()[row][isotopeIndex] == 1)
                            && (massSpecOutputDataRecord.axialFlagsForRawDataColumn()[row] == 0)
                            && massSpecOutputDataRecord.blockIndicesForRawDataColumn()[row] == (blockIndex + 1)) {
                        double calcValue =
                                exp(dataModelInit_X0.logratios()[isotopeIndex]) / dataModelInit_X0.dfGain()
                                        * dataModelInit_X0.intensityPerBlock().get(blockIndex)[(int) massSpecOutputDataRecord.timeIndColumn()[row] - 1];
                        dataWithNoBaseline[row] = calcValue;
                        data[row] =
                                calcValue + dataModelInit_X0.baselineMeans()[(int) massSpecOutputDataRecord.detectorIndicesForRawDataColumn()[row] - 1];
                    }
                }
            }
        }

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
        double[] dSignalNoiseArray = new double[massSpecOutputDataRecord.detectorIndicesForRawDataColumn().length];
        // for (int row = 0; row < massSpecOutputDataRecord.detectorIndicesForRawDataColumn().getRowDimension(); row++) {
        for (int row = 0; row < massSpecOutputDataRecord.detectorIndicesForRawDataColumn().length; row++) {
            double calculatedValue =
                    StrictMath.sqrt(pow(dataModelInit_X0.signalNoise()[(int) massSpecOutputDataRecord.detectorIndicesForRawDataColumn()[row] - 1], 2)
                            // faradaycount plus 1 = number of detectors, and we subtract 1 for the 1-based matlab indices
                            + dataModelInit_X0.signalNoise()[(int) massSpecOutputDataRecord.isotopeIndicesForRawDataColumn()[row] + massSpecOutputDataRecord.faradayCount()]
                            * dataWithNoBaseline[row]);
            dSignalNoiseArray[row] = calculatedValue;
        }

        double[] residualTmpArray = new double[dSignalNoiseArray.length];
        // not used?? Matrix residualTmp2 = new Matrix(dSignalNoise.getRowDimension(), 1);
        double initialModelErrorWeighted_E = 0.0;
        double initialModelErrorUnWeighted_E0 = 0.0;

        for (int row = 0; row < residualTmpArray.length; row++) {
            double calculatedValue = StrictMath.pow(massSpecOutputDataRecord.rawDataColumn()[row] - data[row], 2);
            residualTmpArray[row] = calculatedValue;
            initialModelErrorWeighted_E = initialModelErrorWeighted_E + (calculatedValue * baselineMultiplier[row] / dSignalNoiseArray[row]);
            initialModelErrorUnWeighted_E0 = initialModelErrorUnWeighted_E0 + calculatedValue;
        }

        /*
            %% Initialize MCMC loop variables
            cnt=0; % Counter
            kept=zeros(5,4); % For displaying how many updates are accepted

            clear ens*
            ensemble=[]; % Make sure to start with new ensemble

            Ndf = 1; % Number of DF gains = 1

            % Size of model: # isotopes + # intensity knots + # baselines + # df gain
            Nmod = d0.Niso + sum(d0.Ncycle) + d0.Nfar + Ndf ;
            % Data and data covariance vectors
            xmean = zeros(Nmod,1);
            xcov = zeros(Nmod,Nmod);

            % Adaptive MCMC proposal term
            delx_adapt=zeros(Nmod,datsav);

            %%
            d0.iso_vec(d0.iso_vec==0)=d0.Niso; %Set BL to denominator iso
         */
        int counter = 0;
        double[][] keptUpdates = new double[5][4];
        List<EnsembleRecord> ensembleRecordsList = new ArrayList<>();
        int countOfDFGains = 1;
        int sumNCycle = 0;
        for (int i = 0; i < massSpecOutputDataRecord.nCycleArray().length; i++) {
            sumNCycle = sumNCycle + massSpecOutputDataRecord.nCycleArray()[i];
        }
        int sizeOfModel = massSpecOutputDataRecord.isotopeCount() + sumNCycle + massSpecOutputDataRecord.faradayCount() + countOfDFGains;
        double[] xDataMean = new double[sizeOfModel];
        double[][] xDataCovariance = new double[sizeOfModel][sizeOfModel];
        PhysicalStore<Double> delx_adapt = storeFactory.make(sizeOfModel, stepCountForcedSave);

        for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().length; row++) {
            if (massSpecOutputDataRecord.isotopeIndicesForRawDataColumn()[row] == 0) {
                // TODO: see matlab comment above this seems odd in case of five isotopes
                massSpecOutputDataRecord.isotopeIndicesForRawDataColumn()[row] = massSpecOutputDataRecord.isotopeCount();
            }
        }

        /*
            % Find beginning and end of each block for Faraday and Daly (ax)
            for ii=1:d0.Nblock
                block0(ii,1) = find(d0.block(:,ii)&~d0.axflag,1,'first');
                blockf(ii,1) = find(d0.block(:,ii)&~d0.axflag,1,'last');
                blockax0(ii,1) = find(d0.block(:,ii)&d0.axflag,1,'first');
                blockaxf(ii,1) = find(d0.block(:,ii)&d0.axflag,1,'last');
            end
         */

        double[] blockStartIndicesFaraday = new double[massSpecOutputDataRecord.blockCount()];
        double[] blockEndIndicesFaraday = new double[massSpecOutputDataRecord.blockCount()];
        double[] blockStartIndicesDaly = new double[massSpecOutputDataRecord.blockCount()];
        double[] blockEndIndicesDaly = new double[massSpecOutputDataRecord.blockCount()];
        for (int blockIndex = 0; blockIndex < massSpecOutputDataRecord.blockCount(); blockIndex++) {
            blockStartIndicesFaraday[blockIndex] =
                    findFirstOrLast(true, blockIndex + 1, massSpecOutputDataRecord.blockIndicesForRawDataColumn(), 0, massSpecOutputDataRecord.axialFlagsForRawDataColumn());
            blockEndIndicesFaraday[blockIndex] =
                    findFirstOrLast(false, blockIndex + 1, massSpecOutputDataRecord.blockIndicesForRawDataColumn(), 0, massSpecOutputDataRecord.axialFlagsForRawDataColumn());
            blockStartIndicesDaly[blockIndex] =
                    findFirstOrLast(true, blockIndex + 1, massSpecOutputDataRecord.blockIndicesForRawDataColumn(), 1, massSpecOutputDataRecord.axialFlagsForRawDataColumn());
            blockEndIndicesDaly[blockIndex] =
                    findFirstOrLast(false, blockIndex + 1, massSpecOutputDataRecord.blockIndicesForRawDataColumn(), 1, massSpecOutputDataRecord.axialFlagsForRawDataColumn());
        }

        DataModellerOutputRecord dataModelInit = new DataModellerOutputRecord(
                dataModelInit_X0.baselineMeans().clone(),
                dataModelInit_X0.baselineStandardDeviations().clone(),
                dataModelInit_X0.dfGain(),
                dataModelInit_X0.logratios().clone(),
                dataModelInit_X0.signalNoise().clone(),
                data,
                dataModelInit_X0.blockIntensities(),
                dataModelInit_X0.intensityPerBlock()
        );

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

        DecimalFormat statsFormat = new DecimalFormat("#0.000");
        org.apache.commons.lang3.time.StopWatch watch = new StopWatch();
        watch.start();
        for (int modelIndex = 1; modelIndex <= maxCount * stepCountForcedSave; modelIndex++) {//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
            long prev = System.nanoTime();
            String operation = randomOperMS(hierarchical);
            // todo: handle adaptiveFlag case
            boolean adaptiveFlag = (counter >= 100000);
            boolean allFlag = adaptiveFlag;
            int columnChoice = modelIndex % stepCountForcedSave;
            DataModellerOutputRecord dataModelUpdaterOutputRecord_x2 = updateMSv2(
                    operation,
                    dataModelInit,
                    psigRecord,
                    priorRecord,
                    xDataCovariance,
                    delx_adapt.sliceColumn(columnChoice).select(delx_adapt.getRowDim() - 1).toRawCopy1D(),
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
            int tempering = 1;
            double[] tmpBLindArray = new double[dataModelUpdaterOutputRecord_x2.baselineMeans().length + 1];
            System.arraycopy(dataModelUpdaterOutputRecord_x2.baselineMeans(),
                    0, tmpBLindArray, 0, tmpBLindArray.length - 1);

            int rowDimension = massSpecOutputDataRecord.detectorIndicesForRawDataColumn().length;
            double[] tmpBLArray = new double[rowDimension];
            double[] tmpDFArray = new double[rowDimension];
            Arrays.fill(tmpDFArray, 1.0);
            double[] tmpLRArray = new double[rowDimension];
            double[] tmpIArray = new double[rowDimension];
            for (int row = 0; row < rowDimension; row++) {
                tmpBLArray[row] = tmpBLindArray[(int) massSpecOutputDataRecord.detectorIndicesForRawDataColumn()[row] - 1];
                if (massSpecOutputDataRecord.axialFlagsForRawDataColumn()[row] == 0) {
                    tmpDFArray[row] = 1.0 / dataModelUpdaterOutputRecord_x2.dfGain();
                }
                tmpLRArray[row] = exp(dataModelUpdaterOutputRecord_x2.logratios()[(int) massSpecOutputDataRecord.isotopeIndicesForRawDataColumn()[row] - 1]);
            }

            long interval1 = System.nanoTime() - prev;
            prev = interval1 + prev;

            // todo: reminder only 1 block here
            // todo: remove zeroes from firstblockinterpolations
            ArrayList<double[]> intensity2 = new ArrayList<>(1);
            PhysicalStore<Double> tempIntensity = storeFactory.make(massSpecOutputDataRecord.allBlockInterpolations()[0].countRows(),
                    storeFactory.columns(dataModelUpdaterOutputRecord_x2.blockIntensities()).getColDim());
            tempIntensity.fillByMultiplying(massSpecOutputDataRecord.allBlockInterpolations()[0], Access1D.wrap(dataModelUpdaterOutputRecord_x2.blockIntensities()));
            intensity2.add(0, tempIntensity.toRawCopy1D());

            for (int row = (int) blockStartIndicesFaraday[0]; row <= (int) blockEndIndicesFaraday[0]; row++) {
                tmpIArray[row] = intensity2.get(0)[(int) massSpecOutputDataRecord.timeIndColumn()[row] - 1];
            }
            for (int row = (int) blockStartIndicesDaly[0]; row <= (int) blockEndIndicesDaly[0]; row++) {
                tmpIArray[row] = intensity2.get(0)[(int) massSpecOutputDataRecord.timeIndColumn()[row] - 1];
            }

            long interval2 = System.nanoTime() - prev;
            prev = interval2 + prev;

            // todo: reminder only 1 block here
            double[] dnobl2 = new double[rowDimension];
            double[] d2 = new double[rowDimension];

            for (int row = 0; row < rowDimension; row++) {
                double value = tmpDFArray[row] * tmpLRArray[row] * tmpIArray[row];
                dnobl2[row] = value;
                d2[row] = value + tmpBLArray[row];
            }

            double[] dSignalNoise2Array = new double[massSpecOutputDataRecord.rawDataColumn().length];
            double E02 = 0.0;
            double E0 = 0.0;
            double E = 0.0;
            double E2 = 0.0;
            double dE;
            double sumLogDSignalNoise = 0.0;
            double sumLogDSignalNoise2 = 0.0;
            double keep;

            long interval3 = System.nanoTime() - prev;
            prev = interval3 + prev;

            for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().length; row++) {
                double term1 = pow(dataModelUpdaterOutputRecord_x2.signalNoise()[(int) massSpecOutputDataRecord.detectorIndicesForRawDataColumn()[row] - 1], 2);
                double term2 = dataModelUpdaterOutputRecord_x2.signalNoise()[(int) massSpecOutputDataRecord.isotopeIndicesForRawDataColumn()[row] - 1 + massSpecOutputDataRecord.faradayCount() + 1];
                dSignalNoise2Array[row] = term1 + term2 * dnobl2[row];
                double residualValue = pow(massSpecOutputDataRecord.rawDataColumn()[row] - dataModelInit.dataArray()[row], 2);
                E0 += residualValue;
                double residualValue2 = pow(massSpecOutputDataRecord.rawDataColumn()[row] - d2[row], 2);
                E02 += residualValue2;

                /*
                    % Decide whether to accept or reject model
                    keep = AcceptItMS(oper,dE,psig,delx,prior,Dsig,Dsig2,d0);
                 */
                // todo: make variable for bool calculate before for loop, right after its declared
                if (noiseOperation) {
                    E += residualValue / dSignalNoiseArray[row];
                    E2 += residualValue2 / dSignalNoise2Array[row];
                    sumLogDSignalNoise += -1.0 * Math.log(dSignalNoiseArray[row]);
                    sumLogDSignalNoise2 += -1.0 * Math.log(dSignalNoise2Array[row]);
                } else {
                    E += residualValue * baselineMultiplier[row] / dSignalNoiseArray[row];
                    E2 += residualValue2 * baselineMultiplier[row] / dSignalNoise2Array[row];
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
                keep = min(1, exp(-(dE) / 2.0));
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

            int operationIndex = DataModelUpdater.operations.indexOf(operation);
            keptUpdates[operationIndex][1] = keptUpdates[operationIndex][1] + 1;
            keptUpdates[operationIndex][3] = keptUpdates[operationIndex][3] + 1;

            RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
            randomDataGenerator.reSeedSecure();

            if (keep >= randomDataGenerator.nextUniform(0, 1)) {
                E = E2;
                initialModelErrorUnWeighted_E0 = E02;

                dataModelInit = new DataModellerOutputRecord(
                        dataModelUpdaterOutputRecord_x2.baselineMeans(),
                        dataModelUpdaterOutputRecord_x2.baselineStandardDeviations(),
                        dataModelUpdaterOutputRecord_x2.dfGain(),
                        dataModelUpdaterOutputRecord_x2.logratios(),
                        dataModelUpdaterOutputRecord_x2.signalNoise(),
                        d2.clone(),
                        dataModelUpdaterOutputRecord_x2.blockIntensities(),
                        intensity2
                );
                dSignalNoiseArray = dSignalNoise2Array.clone();

                keptUpdates[operationIndex][0] = keptUpdates[operationIndex][0] + 1;
                keptUpdates[operationIndex][2] = keptUpdates[operationIndex][2] + 1;
            }

            long interval5 = System.nanoTime() - prev;

            if (modelIndex % (stepCountForcedSave) == 0) {
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
                ensembleRecordsList.add(new EnsembleRecord(
                        dataModelInit.logratios(),
                        dataModelInit.blockIntensities(),
                        dataModelInit.baselineMeans(),
                        dataModelInit.dfGain(),
                        dataModelInit.signalNoise(),
                        E,
                        initialModelErrorUnWeighted_E0
                ));

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
                    DataModelUpdater.UpdatedCovariancesRecord updatedCovariancesRecord =
                            updateMeanCovMS(dataModelInit, xDataCovariance, xDataMean, ensembleRecordsList, counter - covStart, false);
                    xDataCovariance = updatedCovariancesRecord.dataCov();
                    xDataMean = updatedCovariancesRecord.dataMean();

                    //todo: delx_adapt
                    if (adaptiveFlag) {
                    /*
                    mvnrnd(
                    zeros(sizeOfModel,1)
                    ,2.38^2*xDataCovariance/sizeOfModel
                    ,stepCountForcedSave)'
                    stepCountForcedSave = 100
                    sizeOfModel = 21

                    function [r,T] = mvnrnd(mu,sigma,cases,T)
                        mu = mu';
                        n = cases;
                        mu = repmat(mu,n,1);
                        [T,err] = cholcov(sigma);
                        r = randn(n,size(T,1)) * T + mu;
                        t = diag(sigma);
                        r(:,t==0) = mu(:,t==0); % force exact mean when variance is 0
                     */
                        Normal tmpNormDistribution = new Normal();
                        Primitive64Store distribution = Primitive64Store.FACTORY.makeFilled(stepCountForcedSave, sizeOfModel, tmpNormDistribution);

                        Cholesky<Double> cholesky = Cholesky.PRIMITIVE.make();
                        cholesky.decompose(storeFactory.columns(xDataCovariance).multiply(pow(2.38, 2) / sizeOfModel));

                        delx_adapt = distribution.multiply(cholesky.getR()).transpose().copy();
                    }
                }

                if (modelIndex % (10 * stepCountForcedSave) == 0) {
                    String loggingSnippet =
                            "%%%%%%%%%%%%%%%%%%%%%%% Tripoli in Java test %%%%%%%%%%%%%%%%%%%%%%%"
                                    + "\nElapsed time = " + statsFormat.format(watch.getTime() / 1000.0) + " seconds for " + 10 * stepCountForcedSave + " realizations of total = " + modelIndex
                                    + "\nError function = "
                                    + statsFormat.format(StrictMath.sqrt(initialModelErrorUnWeighted_E0 / massSpecOutputDataRecord.detectorIndicesForRawDataColumn().length))

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
                                    : "");

                    System.err.println("\n" + loggingSnippet);
                    loggingCallback.receiveLoggingSnippet(loggingSnippet);

                    for (int i = 0; i < 5; i++) {
                        keptUpdates[i][0] = 0;
                        keptUpdates[i][1] = 0;
                    }

                    watch.reset();
                    watch.start();
                }
            }
        } // end model loop

        // experiment with serializing results during development
        EnsemblesStore ensemblesStore = new EnsemblesStore(ensembleRecordsList, dataModelInit);
        try {
            TripoliSerializer.serializeObjectToFile(ensemblesStore, "EnsemblesStore.ser");
        } catch (TripoliException e) {
            e.printStackTrace();
        }

        return analysisAndPlotting(massSpecOutputDataRecord, ensembleRecordsList, dataModelInit);
    }

    private static AbstractPlotBuilder[] analysisAndPlotting(
            MassSpecOutputDataRecord massSpecOutputDataRecord, List<EnsembleRecord> ensembleRecordsList, DataModellerOutputRecord lastDataModelInit) {
        /*
            %% Analysis and Plotting

            burn = 1000; % Number of models to discard
            ens_rat =[ensemble.lograt];

            % Calculate mean and st dev of ratios after burn in time
            ratmean = mean(ens_rat(:,burn:cnt),2);  % Log ratios
            ratstd = std(ens_rat(:,burn:cnt),[],2);

            BLmean = mean(ens_BL(:,burn:cnt),2);  % Baselines
            BLstd = std(ens_BL(:,burn:cnt),[],2);

            sigmean = mean(ens_sig(:,burn:cnt),2);   % Noise hyperparams
            sigstd = std(ens_sig(:,burn:cnt),[],2);

            DFmean = mean(ens_DF(:,burn:cnt),2);   % Daly-Far gain
            DFstd = std(ens_DF(:,burn:cnt),[],2);

         */
        int burn = 500;//1000;
        int countOfEnsemblesUsed = ensembleRecordsList.size() - burn;

        // log ratios - only the first row
        double[] ensembleLogRatios = new double[countOfEnsemblesUsed];
        double[] ensembleRatios = new double[countOfEnsemblesUsed];
        DescriptiveStatistics descriptiveStatisticsLogRatios = new DescriptiveStatistics();
        for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
            ensembleLogRatios[index - burn] = ensembleRecordsList.get(index).logRatios()[0];
            descriptiveStatisticsLogRatios.addValue(ensembleLogRatios[index - burn]);
            ensembleRatios[index - burn] = exp(ensembleLogRatios[index - burn]);
        }
        double logRatioMean = descriptiveStatisticsLogRatios.getMean();
        double logRatioStdDev = descriptiveStatisticsLogRatios.getStandardDeviation();

        // baseLines - first 2 rows
        double[][] ensembleBaselines = new double[ensembleRecordsList.get(0).baseLine().length][countOfEnsemblesUsed];
        double[] baselinesMeans = new double[massSpecOutputDataRecord.isotopeCount()];
        double[] baselinesStdDev = new double[massSpecOutputDataRecord.isotopeCount()];

        for (int row = 0; row < ensembleRecordsList.get(0).baseLine().length; row++) {
            DescriptiveStatistics descriptiveStatisticsBaselines = new DescriptiveStatistics();
            for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
                ensembleBaselines[row][index - burn] = ensembleRecordsList.get(index).baseLine()[row] / 6.24e7 * 1e6;
                descriptiveStatisticsBaselines.addValue(ensembleBaselines[row][index - burn]);
            }
            baselinesMeans[row] = descriptiveStatisticsBaselines.getMean();
            baselinesStdDev[row] = descriptiveStatisticsBaselines.getStandardDeviation();
        }

        // dalyFaraday gains
        double[] ensembleDalyFaradayGain = new double[countOfEnsemblesUsed];
        DescriptiveStatistics descriptiveStatisticsDalyFaradayGain = new DescriptiveStatistics();
        for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
            ensembleDalyFaradayGain[index - burn] = ensembleRecordsList.get(index).dfGain();
            descriptiveStatisticsDalyFaradayGain.addValue(ensembleDalyFaradayGain[index - burn]);
        }
        double dalyFaradayGainMean = descriptiveStatisticsDalyFaradayGain.getMean();
        double dalyFaradayGainStdDev = descriptiveStatisticsDalyFaradayGain.getStandardDeviation();

        // signal noise
        double[][] ensembleSignalnoise = new double[2][countOfEnsemblesUsed];
        double[] signalNoiseMeans = new double[2];
        double[] signalNoiseStdDev = new double[2];

        for (int row = 0; row < massSpecOutputDataRecord.faradayCount(); row++) {
            DescriptiveStatistics descriptiveStatisticsSignalNoise = new DescriptiveStatistics();
            for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
                ensembleSignalnoise[row][index - burn] = ensembleRecordsList.get(index).signalNoise()[row];
                descriptiveStatisticsSignalNoise.addValue(ensembleSignalnoise[row][index - burn]);
            }
            signalNoiseMeans[row] = descriptiveStatisticsSignalNoise.getMean();
            signalNoiseStdDev[row] = descriptiveStatisticsSignalNoise.getStandardDeviation();
        }

        /*
            for m=1:d0.Nblock
                for n = 1:cnt;
                    ens_I{m}(:,n) =[ensemble(n).I{m}];
                end
                Imean{m} = mean(ens_I{m}(:,burn:cnt),2);
                Istd{m} = std(ens_I{m}(:,burn:cnt),[],2);
            end
         */

        // Intensity
        // meanof 16 items across 400
        int knotsCount = ensembleRecordsList.get(0).intensity().length;
        double[][] ensembleIntensity = new double[knotsCount][countOfEnsemblesUsed];
        double[] intensityMeans = new double[knotsCount];
        double[] intensityStdDevs = new double[knotsCount];

        for (int knotIndex = 0; knotIndex < knotsCount; knotIndex++) {
            DescriptiveStatistics descriptiveStatisticsIntensity = new DescriptiveStatistics();
            for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
                ensembleIntensity[knotIndex][index - burn] = ensembleRecordsList.get(index).intensity()[knotIndex];
                descriptiveStatisticsIntensity.addValue(ensembleIntensity[knotIndex][index - burn]);
            }
            intensityMeans[knotIndex] = descriptiveStatisticsIntensity.getMean();
            intensityStdDevs[knotIndex] = descriptiveStatisticsIntensity.getStandardDeviation();
        }

        // calculate intensity means for plotting
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        MatrixStore<Double> intensityMeansMatrix = storeFactory.columns(intensityMeans);
        MatrixStore<Double> yDataMatrix = massSpecOutputDataRecord.allBlockInterpolations()[0].multiply(intensityMeansMatrix).multiply((1.0 / (dalyFaradayGainMean * 6.24e7)) * 1e6);
        double[] yDataIntensityMeans = yDataMatrix.toRawCopy1D();
        double[] xDataIntensityMeans = new double[massSpecOutputDataRecord.allBlockInterpolations()[0].getRowDim()];
        for (int i = 0; i < xDataIntensityMeans.length; i++) {
            xDataIntensityMeans[i] = i;
        }


        // visualization - Ensembles tab
        AbstractPlotBuilder[] plotBuilders = new AbstractPlotBuilder[15];
        plotBuilders[0] = HistogramBuilder.initializeHistogram(ensembleRatios, 50, "Histogram of ratios");
        plotBuilders[1] = HistogramBuilder.initializeHistogram(true, ensembleBaselines, 50, "Histogram of baseline");
        plotBuilders[2] = HistogramBuilder.initializeHistogram(ensembleDalyFaradayGain, 50, "Histogram of Daly/Faraday Gain");
        plotBuilders[3] = HistogramBuilder.initializeHistogram(true, ensembleSignalnoise, 50, "Histogram of Signal Noise");
        plotBuilders[4] = LinePlotBuilder.initializeLinePlot(xDataIntensityMeans, yDataIntensityMeans, "Mean Intensity");

        // visualization converge ratio and others tabs
        double[] convergeLogRatios = new double[ensembleRecordsList.size()];
        double[] convergeRatios = new double[ensembleRecordsList.size()];
        // todo: hardwired for 2 isotopes
        double[] convergeBaselineFaradayL1 = new double[ensembleRecordsList.size()];
        double[] convergeBaselineFaradayH1 = new double[ensembleRecordsList.size()];
        double[] convergeErrWeightedMisfit = new double[ensembleRecordsList.size()];
        double[] convergeErrRawMisfit = new double[ensembleRecordsList.size()];
        double[] xDataconvergeSavedIterations = new double[ensembleRecordsList.size()];
        double[][] convergeIntensities = new double[ensembleRecordsList.get(0).intensity().length][ensembleRecordsList.size()];
        double[] convergeNoiseFaradayL1 = new double[ensembleRecordsList.size()];
        double[] convergeNoiseFaradayH1 = new double[ensembleRecordsList.size()];
        for (int index = 0; index < ensembleRecordsList.size(); index++) {
            convergeLogRatios[index] = ensembleRecordsList.get(index).logRatios()[0];
            convergeRatios[index] = exp(convergeLogRatios[index]);
            convergeBaselineFaradayL1[index] = ensembleRecordsList.get(index).baseLine()[0];
            convergeBaselineFaradayH1[index] = ensembleRecordsList.get(index).baseLine()[1];
            convergeErrWeightedMisfit[index] = StrictMath.sqrt(ensembleRecordsList.get(index).errorWeighted());
            convergeErrRawMisfit[index] = StrictMath.sqrt(ensembleRecordsList.get(index).errorUnWeighted());
            for (int intensityIndex = 0; intensityIndex < convergeIntensities.length; intensityIndex++) {
                convergeIntensities[intensityIndex][index] = ensembleRecordsList.get(index).intensity()[intensityIndex];
            }
            convergeNoiseFaradayL1[index] = ensembleRecordsList.get(index).signalNoise()[0];
            convergeNoiseFaradayH1[index] = ensembleRecordsList.get(index).signalNoise()[1];
            xDataconvergeSavedIterations[index] = index + 1;
        }
        plotBuilders[5] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeRatios, "Converge Ratio");
        plotBuilders[6] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeBaselineFaradayL1, "Converge Baseline Faraday L1");
        plotBuilders[7] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeBaselineFaradayH1, "Converge Baseline Faraday H1");
        plotBuilders[8] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeErrWeightedMisfit, "Converge Weighted Misfit");
        plotBuilders[9] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeErrRawMisfit, "Converge Raw Misfit");
        plotBuilders[10] = MultiLinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeIntensities, "Converge Intensity");
        plotBuilders[11] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeNoiseFaradayL1, "Converge Noise Faraday L1");
        plotBuilders[12] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeNoiseFaradayH1, "Converge Noise Faraday H1");


        // visualization data fit
        // only first block for now
        // todo: this is duplicated code from above in part
        double[] data = lastDataModelInit.dataArray();
        double[] dataWithNoBaseline = new double[lastDataModelInit.dataArray().length];
        EnsembleRecord lastModelRecord = ensembleRecordsList.get(ensembleRecordsList.size() - 1);

        for (int blockIndex = 0; blockIndex < 1; blockIndex++) {
            ArrayList<double[]> intensity = new ArrayList<>(1);
            intensity.add(0, lastDataModelInit.intensityPerBlock().get(0));
            for (int isotopeIndex = 0; isotopeIndex < massSpecOutputDataRecord.isotopeCount(); isotopeIndex++) {
                for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().length; row++) {
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn()[row][isotopeIndex] == 1)
                            && (massSpecOutputDataRecord.axialFlagsForRawDataColumn()[row] == 1)
                            && massSpecOutputDataRecord.blockIndicesForRawDataColumn()[row] == (blockIndex + 1)) {
                        double calcValue =
                                exp(lastModelRecord.logRatios()[isotopeIndex])
                                        * intensity.get(0)[(int) massSpecOutputDataRecord.timeIndColumn()[row] - 1];
                        data[row] = calcValue;
                        dataWithNoBaseline[row] = calcValue;
                    }
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn()[row][isotopeIndex] == 1)
                            && (massSpecOutputDataRecord.axialFlagsForRawDataColumn()[row] == 0)
                            && massSpecOutputDataRecord.blockIndicesForRawDataColumn()[row] == (blockIndex + 1)) {
                        double calcValue =
                                exp(lastModelRecord.logRatios()[isotopeIndex]) / lastModelRecord.dfGain()
                                        * intensity.get(0)[(int) massSpecOutputDataRecord.timeIndColumn()[row] - 1];
                        dataWithNoBaseline[row] = calcValue;
                        data[row] =
                                calcValue + lastModelRecord.baseLine()[(int) massSpecOutputDataRecord.detectorIndicesForRawDataColumn()[row] - 1];
                    }
                }
            }
        }

        double[] xSig = lastModelRecord.signalNoise;
        double[] detectorIndicesForRawDataColumn = massSpecOutputDataRecord.detectorIndicesForRawDataColumn();
        double[] dataCountsModelOneSigma = new double[detectorIndicesForRawDataColumn.length];
        for (int row = 0; row < detectorIndicesForRawDataColumn.length; row++) {
            dataCountsModelOneSigma[row]
                    = StrictMath.sqrt(StrictMath.pow(xSig[(int) detectorIndicesForRawDataColumn[row] - 1], 2)
                    + xSig[xSig.length - 1] * dataWithNoBaseline[row]);
        }

        int plottingStep = 10;
        double[] dataOriginalCounts = massSpecOutputDataRecord.rawDataColumn();
        double[] xDataIndex = new double[dataOriginalCounts.length / plottingStep];
        double[] yDataCounts = new double[dataOriginalCounts.length / plottingStep];
        double[] yDataModelCounts = new double[dataOriginalCounts.length / plottingStep];

        double[] yDataResiduals = new double[dataOriginalCounts.length / plottingStep];
        double[] yDataSigmas = new double[dataOriginalCounts.length / plottingStep];

        for (int i = 0; i < dataOriginalCounts.length / plottingStep; i++) {
            xDataIndex[i] = i * plottingStep;
            yDataCounts[i] = dataOriginalCounts[i * plottingStep];
            yDataModelCounts[i] = data[i * plottingStep];
            yDataResiduals[i] = dataOriginalCounts[i * plottingStep] - data[i * plottingStep];
            yDataSigmas[i] = dataCountsModelOneSigma[i * plottingStep];
        }
        plotBuilders[13] = ComboPlotBuilder.initializeLinePlot(xDataIndex, yDataCounts, yDataModelCounts, "Observed Data");

        plotBuilders[14] = ComboPlotBuilder.initializeLinePlotWithOneSigma(xDataIndex, yDataResiduals, yDataSigmas, "Residual Data");


        // todo: missing additional elements of signalNoise (i.e., 0,11,11)
        System.err.println(logRatioMean + "         " + logRatioStdDev);
        System.err.println(baselinesMeans[0] + "         " + baselinesMeans[1] + "    " + baselinesStdDev[0] + "     " + baselinesStdDev[1]);
        System.err.println(dalyFaradayGainMean + "    " + dalyFaradayGainStdDev);
        System.err.println(signalNoiseMeans[0] + "         " + signalNoiseMeans[1] + "    " + signalNoiseStdDev[0] + "     " + signalNoiseStdDev[1]);


        return plotBuilders;
    }

    private static int findFirstOrLast(boolean first, int index, double[] target, int flag, double[] flags) {
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
    private static String randomOperMS(boolean hierFlag) {
        Object[][] notHier = new Object[][]{{40, 60, 80, 100}, {"changeI", "changer", "changebl", "changedfg"}};
        Object[][] hier = new Object[][]{{60, 80, 90, 100, 120}, {"changeI", "changer", "changebl", "changedfg", "noise"}};

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

    record EnsembleRecord(
            double[] logRatios,
            double[] intensity,
            double[] baseLine,
            double dfGain,
            double[] signalNoise,
            double errorWeighted,
            double errorUnWeighted
    ) implements Serializable {
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