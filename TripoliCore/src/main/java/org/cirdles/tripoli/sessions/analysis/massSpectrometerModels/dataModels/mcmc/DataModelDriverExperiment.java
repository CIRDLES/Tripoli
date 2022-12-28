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
import org.cirdles.tripoli.plots.AbstractPlotBuilder;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.DataSourceProcessor_PhoenixSyntheticTextFile;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputDataRecord;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.utilities.callbacks.LoggingCallbackInterface;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.StrictMath.exp;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.DataModelUpdater.updateMSv2;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.DataModelUpdater.updateMeanCovMS;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.DataModelUpdaterHelper.buildPriorRecord;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.DataModelUpdaterHelper.buildPsigRecord;

/**
 * @author James F. Bowring
 */
public enum DataModelDriverExperiment {
    ;

    private static final boolean doFullProcessing = true;
    public static boolean ALLOW_EXECUTION = true;
    // todo flag for linear or spline
    // private static final boolean splineVsLinear = true;

    public static AbstractPlotBuilder[][] driveModelTest(Path dataFilePath, AnalysisMethod analysisMethod, LoggingCallbackInterface loggingCallback) throws IOException {

        DataSourceProcessor_PhoenixSyntheticTextFile dataSourceProcessorOPPhoenix
                = DataSourceProcessor_PhoenixSyntheticTextFile.initializeWithAnalysisMethod(analysisMethod);
        MassSpecOutputDataRecord massSpecOutputDataRecord = dataSourceProcessorOPPhoenix.prepareInputDataModelFromFile(dataFilePath);

        AbstractPlotBuilder[][] plotBuilders;
        DataModellerOutputRecord dataModelInit;
        try {
            dataModelInit = DataModelInitializer.modellingTest(massSpecOutputDataRecord);

            List<EnsemblesStore.EnsembleRecord> ensembleRecordsList = new ArrayList<>();
            DataModellerOutputRecord lastDataModelInit = null;
            if (doFullProcessing) {
                plotBuilders = applyInversionWithAdaptiveMCMC(massSpecOutputDataRecord, dataModelInit, analysisMethod, loggingCallback);
            } else {
                try {
                    EnsemblesStore ensemblesStore = (EnsemblesStore) TripoliSerializer.getSerializedObjectFromFile("EnsemblesStore.ser", true);
                    ensembleRecordsList = ensemblesStore.getEnsembles();
                    lastDataModelInit = ensemblesStore.getLastDataModelInit();
                } catch (TripoliException e) {
                    e.printStackTrace();
                }

                plotBuilders = DataModelPlot.analysisAndPlotting(massSpecOutputDataRecord, ensembleRecordsList, lastDataModelInit, analysisMethod);
            }
        } catch (RecoverableCondition e) {
            plotBuilders = new AbstractPlotBuilder[0][0];
        }

        return plotBuilders;
    }

    static AbstractPlotBuilder[][] applyInversionWithAdaptiveMCMC
            (MassSpecOutputDataRecord massSpecOutputDataRecord,
             DataModellerOutputRecord dataModelInit_X0,
             AnalysisMethod analysisMethod,
             LoggingCallbackInterface loggingCallback) {
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

        int maxCount = 2000;
//        if (dataModelInit_X0.logratios().length > 2) {
//            maxCount = 1000;
//        }

        boolean hierarchical = true;
        int stepCountForcedSave = 100;

        double[] baselineMultiplier = new double[massSpecOutputDataRecord.rawDataColumn().length];
        Arrays.fill(baselineMultiplier, 1.0);
        for (int row = 0; row < massSpecOutputDataRecord.ionCounterFlagsForRawDataColumn().length; row++) {
            if (1 == massSpecOutputDataRecord.ionCounterFlagsForRawDataColumn()[row]) {
                baselineMultiplier[row] = 0.1;
            }
        }

        DataModelUpdaterHelper.PriorRecord priorRecord = buildPriorRecord(dataModelInit_X0.blockIntensities());
        DataModelUpdaterHelper.PsigRecord psigRecord = buildPsigRecord(dataModelInit_X0.baselineStandardDeviations(), priorRecord);

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
                if ((1 == massSpecOutputDataRecord.baseLineFlagsForRawDataColumn()[row])
                        &&
                        (1 == massSpecOutputDataRecord.detectorFlagsForRawDataColumn()[row][faradayIndex])) {
                    data[row] = dataModelInit_X0.baselineMeans()[faradayIndex];
                    dataWithNoBaseline[row] = 0.0;
                }
            }
        }

        /*
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

        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        for (int blockIndex = 0; blockIndex < massSpecOutputDataRecord.blockCount(); blockIndex++) {
            for (int isotopeIndex = 0; isotopeIndex < massSpecOutputDataRecord.isotopeCount(); isotopeIndex++) {
                for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().length; row++) {
                    if ((1 == massSpecOutputDataRecord.isotopeFlagsForRawDataColumn()[row][isotopeIndex])
                            && (1 == massSpecOutputDataRecord.ionCounterFlagsForRawDataColumn()[row])
                            && massSpecOutputDataRecord.blockIndicesForRawDataColumn()[row] == (blockIndex + 1)) {
                        double calcValue;
                        // Oct 2022 per email from Noah, eliminate the iden/iden ratio to guarantee positive definite  covariance matrix >> isotope count - 1
                        if (isotopeIndex < dataModelInit_X0.logratios().length) {
                            calcValue =
                                    exp(dataModelInit_X0.logratios()[isotopeIndex])
                                            * dataModelInit_X0.intensityPerBlock().get(blockIndex)[(int) massSpecOutputDataRecord.timeIndColumn()[row] - 1];
                        } else {
                            calcValue = dataModelInit_X0.intensityPerBlock().get(blockIndex)[(int) massSpecOutputDataRecord.timeIndColumn()[row] - 1];
                        }
                        data[row] = calcValue;
                        dataWithNoBaseline[row] = calcValue;
                    }
                    if ((1 == massSpecOutputDataRecord.isotopeFlagsForRawDataColumn()[row][isotopeIndex])
                            && (0 == massSpecOutputDataRecord.ionCounterFlagsForRawDataColumn()[row])
                            && massSpecOutputDataRecord.blockIndicesForRawDataColumn()[row] == (blockIndex + 1)) {
                        double calcValue;
                        // Oct 2022 per email from Noah, eliminate the iden/iden ratio to guarantee positive definite  covariance matrix >> isotope count - 1
                        if (isotopeIndex < dataModelInit_X0.logratios().length) {
                            calcValue =
                                    exp(dataModelInit_X0.logratios()[isotopeIndex]) / dataModelInit_X0.dfGain()
                                            * dataModelInit_X0.intensityPerBlock().get(blockIndex)[(int) massSpecOutputDataRecord.timeIndColumn()[row] - 1];
                        } else {
                            calcValue = 1.0 / dataModelInit_X0.dfGain()
                                    * dataModelInit_X0.intensityPerBlock().get(blockIndex)[(int) massSpecOutputDataRecord.timeIndColumn()[row] - 1];
                        }
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
        for (int row = 0; row < massSpecOutputDataRecord.detectorIndicesForRawDataColumn().length; row++) {
            double calculatedValue =
                    StrictMath.sqrt(pow(dataModelInit_X0.signalNoise()[(int) massSpecOutputDataRecord.detectorIndicesForRawDataColumn()[row] - 1], 2)
                            // faradaycount plus 1 = number of detectors, and we subtract 1 for the 1-based matlab indices
                            + dataModelInit_X0.signalNoise()[(int) massSpecOutputDataRecord.isotopeIndicesForRawDataColumn()[row] + massSpecOutputDataRecord.faradayCount()]
                            * dataWithNoBaseline[row]);
            dSignalNoiseArray[row] = calculatedValue;
        }

        // not used double[] residualTmpArray = new double[dSignalNoiseArray.length];
        // not used?? Matrix residualTmp2 = new Matrix(dSignalNoise.getRowDimension(), 1);
        double initialModelErrorWeighted_E = 0.0;
        double initialModelErrorUnWeighted_E0 = 0.0;

        for (int row = 0; row < dSignalNoiseArray.length; row++) {
            double calculatedValue = StrictMath.pow(massSpecOutputDataRecord.rawDataColumn()[row] - data[row], 2);
//            residualTmpArray[row] = calculatedValue;
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

            % Size of model: # isotopes + # blockIntensities knots + # baselines + # df gain
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
        List<EnsemblesStore.EnsembleRecord> ensembleRecordsList = new ArrayList<>();
        int countOfDFGains = 1;
        int sumNCycle = 0;
        for (int i = 0; i < massSpecOutputDataRecord.nCycleArray().length; i++) {
            sumNCycle = sumNCycle + massSpecOutputDataRecord.nCycleArray()[i];
        }

        int sizeOfModel = massSpecOutputDataRecord.isotopeCount() - 1 + sumNCycle + massSpecOutputDataRecord.faradayCount() + countOfDFGains;

        double[] xDataMean = new double[sizeOfModel];
        double[][] xDataCovariance = new double[sizeOfModel][sizeOfModel];
        double[][] delx_adapt = new double[sizeOfModel][stepCountForcedSave];

        for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().length; row++) {
            if (0 == massSpecOutputDataRecord.isotopeIndicesForRawDataColumn()[row]) {
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
                    DataModelUpdaterHelper.findFirstOrLast(true, blockIndex + 1, massSpecOutputDataRecord.blockIndicesForRawDataColumn(), 0, massSpecOutputDataRecord.ionCounterFlagsForRawDataColumn());
            blockEndIndicesFaraday[blockIndex] =
                    DataModelUpdaterHelper.findFirstOrLast(false, blockIndex + 1, massSpecOutputDataRecord.blockIndicesForRawDataColumn(), 0, massSpecOutputDataRecord.ionCounterFlagsForRawDataColumn());
            blockStartIndicesDaly[blockIndex] =
                    DataModelUpdaterHelper.findFirstOrLast(true, blockIndex + 1, massSpecOutputDataRecord.blockIndicesForRawDataColumn(), 1, massSpecOutputDataRecord.ionCounterFlagsForRawDataColumn());
            blockEndIndicesDaly[blockIndex] =
                    DataModelUpdaterHelper.findFirstOrLast(false, blockIndex + 1, massSpecOutputDataRecord.blockIndicesForRawDataColumn(), 1, massSpecOutputDataRecord.ionCounterFlagsForRawDataColumn());
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
            if (ALLOW_EXECUTION) {
                long prev = System.nanoTime();
                String operation = DataModelUpdaterHelper.randomOperMS(hierarchical);
                // todo: handle adaptiveFlag case
                boolean adaptiveFlag = (500000 <= counter); // abandon for now
                boolean allFlag = adaptiveFlag;
                int columnChoice = modelIndex % stepCountForcedSave;
                double[] delx_adapt_slice = storeFactory.copy(Access2D.wrap(delx_adapt)).sliceColumn(columnChoice).toRawCopy1D();

                DataModellerOutputRecord dataModelUpdaterOutputRecord_x2 = updateMSv2(
                        operation,
                        dataModelInit,
                        psigRecord,
                        priorRecord,
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
                    if (0 == massSpecOutputDataRecord.ionCounterFlagsForRawDataColumn()[row]) {
                        tmpDFArray[row] = 1.0 / dataModelUpdaterOutputRecord_x2.dfGain();
                    }
                    // Oct 2022 per email from Noah, eliminate the iden/iden ratio to guarantee positive definite  covariance matrix >> isotope count - 1
                    if (massSpecOutputDataRecord.isotopeIndicesForRawDataColumn()[row] - 1 < dataModelUpdaterOutputRecord_x2.logratios().length) {
                        tmpLRArray[row] = exp(dataModelUpdaterOutputRecord_x2.logratios()[(int) massSpecOutputDataRecord.isotopeIndicesForRawDataColumn()[row] - 1]);
                    } else {
                        tmpLRArray[row] = 1.0;
                    }
                }

                long interval1 = System.nanoTime() - prev;
                prev = interval1 + prev;

                ArrayList<double[]> intensity2 = new ArrayList<>(1);
                for (int blockIndex = 0; blockIndex < massSpecOutputDataRecord.blockCount(); blockIndex++) {
                    PhysicalStore<Double> tempIntensity = storeFactory.make(massSpecOutputDataRecord.allBlockInterpolations().get(blockIndex).countRows(),
                            storeFactory.columns(dataModelUpdaterOutputRecord_x2.blockIntensities()[blockIndex]).getColDim());
                    tempIntensity.fillByMultiplying(massSpecOutputDataRecord.allBlockInterpolations().get(blockIndex), Access1D.wrap(dataModelUpdaterOutputRecord_x2.blockIntensities()[blockIndex]));
                    intensity2.add(tempIntensity.toRawCopy1D());

                    for (int row = (int) blockStartIndicesFaraday[blockIndex]; row <= (int) blockEndIndicesFaraday[blockIndex]; row++) {
                        tmpIArray[row] = intensity2.get(blockIndex)[(int) massSpecOutputDataRecord.timeIndColumn()[row] - 1];
                    }
                    for (int row = (int) blockStartIndicesDaly[blockIndex]; row <= (int) blockEndIndicesDaly[blockIndex]; row++) {
                        tmpIArray[row] = intensity2.get(blockIndex)[(int) massSpecOutputDataRecord.timeIndColumn()[row] - 1];
                    }
                }

                long interval2 = System.nanoTime() - prev;
                prev = interval2 + prev;

//            ArrayList<double[]> intensity2 = new ArrayList<>(1);
//            for (int blockIndex = 0; blockIndex < massSpecOutputDataRecord.blockCount(); blockIndex++) {
//                PhysicalStore<Double> tempIntensity = storeFactory.make(massSpecOutputDataRecord.allBlockInterpolations().get(blockIndex).countRows(),
//                        storeFactory.columns(dataModelUpdaterOutputRecord_x2.blockIntensities()[blockIndex]).getColDim());
//                tempIntensity.fillByMultiplying(massSpecOutputDataRecord.allBlockInterpolations().get(blockIndex), Access1D.wrap(dataModelUpdaterOutputRecord_x2.blockIntensities()[blockIndex]));
//                intensity2.add(tempIntensity.toRawCopy1D());
                double[] dnobl2 = new double[rowDimension];
                double[] d2 = new double[rowDimension];

                for (int row = 0; row < rowDimension; row++) {
                    double value = tmpDFArray[row] * tmpLRArray[row] * tmpIArray[row];
                    dnobl2[row] = value;
                    d2[row] = value + tmpBLArray[row];
                }

                double[] dSignalNoise2Array = new double[massSpecOutputDataRecord.rawDataColumn().length];
                double E02 = 0.0;
//            double E0 = 0.0;
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
//                E0 += residualValue;
                    double residualValue2 = pow(massSpecOutputDataRecord.rawDataColumn()[row] - d2[row], 2);
                    E02 += residualValue2;

                /*
                    % Decide whether to accept or reject model
                    keep = AcceptItMS(oper,dE,psig,delx,prior,Dsig,Dsig2,d0);
                 */
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

                        if (adaptiveFlag) {
                        /*
                        delx_adapt =  mvnrnd(zeros(sizeOfModel,1), 2.38^2*xDataCovariance/sizeOfModel, stepCountForcedSave)'
                        stepCountForcedSave = 100
                        sizeOfModel = 20
                        */

                            double[] zeroMean = new double[sizeOfModel];

                            Matrix a = new Matrix(xDataCovariance);

                            if (0.0 != a.det()) {
                                Matrix ai = a.inverse();
                                Matrix aia = a.times(ai);

                                MultivariateNormalDistribution mnd =
                                        new MultivariateNormalDistribution(zeroMean, storeFactory.copy(Access2D.wrap(xDataCovariance)).multiply(pow(2.38, 2) / (sizeOfModel)).toRawCopy2D());
                                double[][] samples = new double[stepCountForcedSave][];
                                for (int i = 0; i < stepCountForcedSave; i++) {
                                    samples[i] = mnd.sample();
                                }
                                delx_adapt = storeFactory.copy(Access2D.wrap(samples)).transpose().toRawCopy2D();
                            } else {
                                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Bad matrix");
                            }
                        }
                    }

                    if (0 == modelIndex % (10 * stepCountForcedSave)) {
                        String loggingSnippet =
                                "%%%%%%%%%%%%%%%%%%%%%%% Tripoli in Java test %%%%%%%%%%%%%%%%%%%%%%%"
                                        + " ADAPTIVE = " + adaptiveFlag
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

                        for (int i = 0; 5 > i; i++) {
                            keptUpdates[i][0] = 0;
                            keptUpdates[i][1] = 0;
                        }

                        watch.reset();
                        watch.start();
                    }
                }
            } else {
                loggingCallback.receiveLoggingSnippet("Cancelled by user.");
                break;
            }
        } // end model loop

        if (ALLOW_EXECUTION) {
            // experiment with serializing results during development
            EnsemblesStore ensemblesStore = new EnsemblesStore(ensembleRecordsList, dataModelInit);
            try {
                TripoliSerializer.serializeObjectToFile(ensemblesStore, "EnsemblesStore.ser");
            } catch (TripoliException e) {
                e.printStackTrace();
            }
        }

        return DataModelPlot.analysisAndPlotting(massSpecOutputDataRecord, ensembleRecordsList, dataModelInit, analysisMethod);
    }

}