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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels;

import jama.Matrix;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.sessions.analysis.analysisMethods.AnalysisMethodBuiltinFactory;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.DataSourceProcessor_OPPhoenix;
import org.cirdles.tripoli.utilities.callBacks.LoggingCallbackInterface;
import org.cirdles.tripoli.visualizationUtilities.Histogram;
import org.ojalgo.*;
import org.ojalgo.matrix.Primitive64Matrix;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.min;
import static java.lang.StrictMath.exp;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.DataModelUpdater.updateMSv2;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.DataModelUpdater.updateMeanCovMS;

/**
 * @author James F. Bowring
 */
public class DataModelDriverExperiment {

    public static Histogram driveModelTest(Path dataFilePath, LoggingCallbackInterface loggingCallback) throws IOException {

        DataSourceProcessor_OPPhoenix dataSourceProcessorOPPhoenix
                = DataSourceProcessor_OPPhoenix.initializeWithAnalysisMethod(AnalysisMethodBuiltinFactory.analysisMethodsBuiltinMap.get("BurdickBlSyntheticData"));
        MassSpecOutputDataRecord massSpecOutputDataRecord = dataSourceProcessorOPPhoenix.prepareInputDataModelFromFile(dataFilePath);
        DataModellerOutputRecord dataModelInit = DataModelInitializer.modellingTest(massSpecOutputDataRecord);

        Histogram histogram = applyInversionWithRJ_MCMC(massSpecOutputDataRecord, dataModelInit, loggingCallback);


        return histogram;
    }

    static Histogram applyInversionWithRJ_MCMC(MassSpecOutputDataRecord massSpecOutputDataRecord, DataModellerOutputRecord dataModelInit, LoggingCallbackInterface loggingCallback) {
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
        int maxCount = 500;//2000;
        boolean hierarchical = true;
        int stepCountForcedSave = 100;
        int burn = 10;
        Matrix baselineMultiplier = new Matrix(massSpecOutputDataRecord.rawDataColumn().getRowDimension(), 1, 1.0);
        for (int row = 0; row < massSpecOutputDataRecord.axialFlagsForRawDataColumn().getRowDimension(); row++) {
            if (massSpecOutputDataRecord.axialFlagsForRawDataColumn().get(row, 0) == 1) {
                baselineMultiplier.set(row, 0, 0.1);
            }
        }
        Matrix priorBaselineFaraday = new Matrix(new double[][]{{-1.0e6, 1.0e6}});
        Matrix priorBaselineDaly = new Matrix(new double[][]{{0.0, 0.0}});
        Matrix priorLogRatio = new Matrix(new double[][]{{-20.0, 20.0}});
        double maxIntensity = Double.MIN_VALUE;
        double minIntensity = Double.MAX_VALUE;
        for (int row = 0; row < dataModelInit.blockIntensitiesOJ().getRowDim(); row++) {
            maxIntensity = Math.max(dataModelInit.blockIntensitiesOJ().get(row, 0), maxIntensity);
            minIntensity = min(dataModelInit.blockIntensitiesOJ().get(row, 0), minIntensity);
        }
        Matrix priorIntensity = new Matrix(new double[][]{{0.0, 1.5 * maxIntensity}});
        Matrix priorDFgain = new Matrix(new double[][]{{0.8, 1.0}});
        Matrix priorSignalNoiseFaraday = new Matrix(new double[][]{{0.0, 1.0e6}});
        Matrix priorSignalNoiseDaly = new Matrix(new double[][]{{0.0, 0.0}});
        Matrix priorPoissonNoiseDaly = new Matrix(new double[][]{{0.0, 10.0}});

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
        for (int row = 0; row < dataModelInit.baselineStandardDeviations().getRowDimension(); row++) {
            maxValue = Math.max(dataModelInit.baselineStandardDeviations().get(row, 0), maxValue);
        }
        double psigBaselineFaraday = maxValue / 10.0;
        double psigBaselineDaly = 1.0e-1;
        double psigLogRatio = 0.0005 * 0.2;
        double psigIntensityPercent = (maxIntensity - minIntensity) / 100.0 * 1.0;
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

        Matrix dataWithNoBaseline = new Matrix(dataModelInit.dataArray().getRowDimension(), 1);
        for (int faradayIndex = 0; faradayIndex < massSpecOutputDataRecord.faradayCount(); faradayIndex++) {
            for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().getRowDimension(); row++) {
                if ((massSpecOutputDataRecord.baseLineFlagsForRawDataColumn().get(row, 0) == 1)
                        &&
                        (massSpecOutputDataRecord.detectorFlagsForRawDataColumn().get(row, faradayIndex) == 1)) {
                    dataModelInit.dataArray().set(row, 0, dataModelInit.baselineMeans().get(faradayIndex, 0));
                    dataWithNoBaseline.set(row, 0, 0.0);
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
        //Matrix Intensity;
        Primitive64Matrix IntensityOJ;
        for (int blockIndex = 0; blockIndex < 1; blockIndex++) {
            //Intensity = massSpecOutputDataRecord.firstBlockInterpolations().times(dataModelInit.blockIntensities());

            IntensityOJ = massSpecOutputDataRecord.firstBlockInterpolationsOJ().multiply(dataModelInit.blockIntensitiesOJ());
            for (int isotopeIndex = 0; isotopeIndex < massSpecOutputDataRecord.isotopeCount(); isotopeIndex++) {
                for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().getRowDimension(); row++) {
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn().get(row, isotopeIndex) == 1)
                            && (massSpecOutputDataRecord.axialFlagsForRawDataColumn().get(row, 0) == 1)
                            && massSpecOutputDataRecord.blockIndicesForRawDataColumn().get(row, 0) == (blockIndex + 1)) {
                        double calcValue =
                                exp(dataModelInit.logratios().get(isotopeIndex, 0))
                                        * IntensityOJ.get((int) massSpecOutputDataRecord.timeIndColumn().get(row, 0) - 1, 0);
                        dataModelInit.dataArray().set(row, 0, calcValue);
                        dataWithNoBaseline.set(row, 0, calcValue);
                    }
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn().get(row, isotopeIndex) == 1)
                            && (massSpecOutputDataRecord.axialFlagsForRawDataColumn().get(row, 0) == 0)
                            && massSpecOutputDataRecord.blockIndicesForRawDataColumn().get(row, 0) == (blockIndex + 1)) {
                        double calcValue =
                                exp(dataModelInit.logratios().get(isotopeIndex, 0))
                                        * 1.0 / dataModelInit.dfGain()
                                        * IntensityOJ.get((int) massSpecOutputDataRecord.timeIndColumn().get(row, 0) - 1, 0);
                        dataWithNoBaseline.set(row, 0, calcValue);
                        dataModelInit.dataArray().set(row, 0,
                                calcValue + dataModelInit.baselineMeans().get((int) massSpecOutputDataRecord.detectorIndicesForRawDataColumn().get(row, 0) - 1, 0));
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

        Matrix dSignalNoise = new Matrix(massSpecOutputDataRecord.detectorIndicesForRawDataColumn().getRowDimension(), 1);
        for (int row = 0; row < massSpecOutputDataRecord.detectorIndicesForRawDataColumn().getRowDimension(); row++) {
            double calculatedValue =
                    StrictMath.sqrt(Math.pow(dataModelInit.signalNoise().get((int) massSpecOutputDataRecord.detectorIndicesForRawDataColumn().get(row, 0) - 1, 0), 2)
                            // faradaycount plus 1 = number of detectors and we subtract 1 for the 1-based matlab indices
                            + dataModelInit.signalNoise().get((int) massSpecOutputDataRecord.isotopeIndicesForRawDataColumn().get(row, 0) + massSpecOutputDataRecord.faradayCount(), 0)
                            * dataWithNoBaseline.get(row, 0));
            dSignalNoise.set(row, 0, calculatedValue);
        }

        Matrix residualTmp = new Matrix(dSignalNoise.getRowDimension(), 1);
        Matrix residualTmp2 = new Matrix(dSignalNoise.getRowDimension(), 1);
        double initialModelErrorWeighted_E = 0.0;
        double initialModelErrorUnWeighted_E0 = 0.0;
        for (int row = 0; row < residualTmp.getRowDimension(); row++) {
            double calculatedValue = StrictMath.pow(massSpecOutputDataRecord.rawDataColumn().get(row, 0) - dataModelInit.dataArray().get(row, 0), 2);
            residualTmp.set(row, 0, calculatedValue);
            initialModelErrorWeighted_E = initialModelErrorWeighted_E + (calculatedValue * baselineMultiplier.get(row, 0) / dSignalNoise.get(row, 0));
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
        Matrix keptUpdates = new Matrix(5, 4, 0.0);
        List<EnsembleRecord> ensembleRecordsList = new ArrayList<>();
        int countOfDFGains = 1;
        int sumNCycle = 0;
        for (int i = 0; i < massSpecOutputDataRecord.nCycleArray().length; i++) {
            sumNCycle = sumNCycle + massSpecOutputDataRecord.nCycleArray()[i];
        }
        int sizeOfModel = massSpecOutputDataRecord.isotopeCount() + sumNCycle + massSpecOutputDataRecord.faradayCount() + countOfDFGains;
        Matrix xDataMean = new Matrix(sizeOfModel, 1, 0.0);
        Matrix xDataCovariance = new Matrix(sizeOfModel, sizeOfModel, 0.0);
        Matrix delx_adapt = new Matrix(sizeOfModel, stepCountForcedSave);
        for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().getRowDimension(); row++) {
            if (massSpecOutputDataRecord.isotopeIndicesForRawDataColumn().get(row, 0) == 0) {
                // TODO: see matlab comment above this seems odd in case of five isotopes
                massSpecOutputDataRecord.isotopeIndicesForRawDataColumn().set(row, 0, massSpecOutputDataRecord.isotopeCount());
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
        Matrix blockStartIndicesFaraday = new Matrix(massSpecOutputDataRecord.blockCount(), 1, 0.0);
        Matrix blockEndIndicesFaraday = new Matrix(massSpecOutputDataRecord.blockCount(), 1, 0.0);
        Matrix blockStartIndicesDaly = new Matrix(massSpecOutputDataRecord.blockCount(), 1, 0.0);
        Matrix blockEndIndicesDaly = new Matrix(massSpecOutputDataRecord.blockCount(), 1, 0.0);
        for (int blockIndex = 0; blockIndex < massSpecOutputDataRecord.blockCount(); blockIndex++) {
            blockStartIndicesFaraday.set(blockIndex, 0,
                    findFirstOrLast(true, blockIndex + 1, massSpecOutputDataRecord.blockIndicesForRawDataColumn(), 0, massSpecOutputDataRecord.axialFlagsForRawDataColumn()));
            blockEndIndicesFaraday.set(blockIndex, 0,
                    findFirstOrLast(false, blockIndex + 1, massSpecOutputDataRecord.blockIndicesForRawDataColumn(), 0, massSpecOutputDataRecord.axialFlagsForRawDataColumn()));
            blockStartIndicesDaly.set(blockIndex, 0,
                    findFirstOrLast(true, blockIndex + 1, massSpecOutputDataRecord.blockIndicesForRawDataColumn(), 1, massSpecOutputDataRecord.axialFlagsForRawDataColumn()));
            blockEndIndicesDaly.set(blockIndex, 0,
                    findFirstOrLast(false, blockIndex + 1, massSpecOutputDataRecord.blockIndicesForRawDataColumn(), 1, massSpecOutputDataRecord.axialFlagsForRawDataColumn()));
        }

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
                    delx_adapt.getMatrix(0, delx_adapt.getRowDimension() - 1, columnChoice, columnChoice),
                    adaptiveFlag,
                    allFlag
            );


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
            Matrix tmpBLind = new Matrix(dataModelUpdaterOutputRecord_x2.baselineMeans().getRowDimension() + 1, 1, 0.0);
            tmpBLind.setMatrix(0, dataModelUpdaterOutputRecord_x2.baselineMeans().getRowDimension() - 1, 0, 0,
                    dataModelUpdaterOutputRecord_x2.baselineMeans().getMatrix(0, dataModelUpdaterOutputRecord_x2.baselineMeans().getRowDimension() - 1, 0, 0));

            Matrix tmpBL = new Matrix(massSpecOutputDataRecord.detectorIndicesForRawDataColumn().getRowDimension(), 1);
            Matrix tmpDF = new Matrix(massSpecOutputDataRecord.detectorIndicesForRawDataColumn().getRowDimension(), 1, 1.0);
            Matrix tmpLR = new Matrix(massSpecOutputDataRecord.detectorIndicesForRawDataColumn().getRowDimension(), 1, 0.0);
            Matrix tmpI = new Matrix(massSpecOutputDataRecord.detectorIndicesForRawDataColumn().getRowDimension(), 1, 0.0);
            for (int row = 0; row < massSpecOutputDataRecord.detectorIndicesForRawDataColumn().getRowDimension(); row++) {
                tmpBL.set(row, 0, tmpBLind.get((int) massSpecOutputDataRecord.detectorIndicesForRawDataColumn().get(row, 0) - 1, 0));
                if (massSpecOutputDataRecord.axialFlagsForRawDataColumn().get(row, 0) == 0) {
                    tmpDF.set(row, 0, 1.0 / dataModelUpdaterOutputRecord_x2.dfGain());
                }
                tmpLR.set(row, 0, exp(dataModelUpdaterOutputRecord_x2.logratios().get((int) massSpecOutputDataRecord.isotopeIndicesForRawDataColumn().get(row, 0) - 1, 0)));
            }


            // todo: reminder only 1 block here
            // next matrix multiplication to tackle
            Primitive64Matrix Intensity2OJ = massSpecOutputDataRecord.firstBlockInterpolationsOJ().multiply(dataModelUpdaterOutputRecord_x2.blockIntensitiesOJ());
            //Matrix intensity2 = massSpecOutputDataRecord.firstBlockInterpolations().times(dataModelUpdaterOutputRecord_x2.blockIntensities());
            for (int row = (int) blockStartIndicesFaraday.get(0, 0); row <= (int) blockEndIndicesFaraday.get(0, 0); row++) {
                tmpI.set(row, 0, Intensity2OJ.get((int) massSpecOutputDataRecord.timeIndColumn().get(row, 0) - 1, 0));
            }
            for (int row = (int) blockStartIndicesDaly.get(0, 0); row <= (int) blockEndIndicesDaly.get(0, 0); row++) {
                tmpI.set(row, 0, Intensity2OJ.get((int) massSpecOutputDataRecord.timeIndColumn().get(row, 0) - 1, 0));
            }

            Matrix dnobl2 = tmpDF.arrayTimes(tmpLR).arrayTimes(tmpI);
            Matrix d2 = dnobl2.plus(tmpBL);
            Matrix dSignalNoise2 = new Matrix(massSpecOutputDataRecord.rawDataColumn().getRowDimension(), 1, 0.0);
            Matrix restmp = new Matrix(massSpecOutputDataRecord.rawDataColumn().getRowDimension(), 1, 0.0);
            Matrix restmp2 = new Matrix(massSpecOutputDataRecord.rawDataColumn().getRowDimension(), 1, 0.0);
            double E02 = 0;
            double E0 = 0;
            double E = 0;
            double E2 = 0;
            double dE = 0;
            double sumLogDSignalNoise = 0;
            double sumLogDSignalNoise2 = 0;
            double keep = 0;
            for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().getRowDimension(); row++) {
                //Dsig2 = x2.sig(d0.det_vec).^2 + x2.sig(d0.iso_vec+d0.Ndet).*dnobl2;
                double term1 = StrictMath.pow(dataModelUpdaterOutputRecord_x2.signalNoise().get((int) massSpecOutputDataRecord.detectorIndicesForRawDataColumn().get(row, 0) - 1, 0), 2);
                double term2 = dataModelUpdaterOutputRecord_x2.signalNoise().get((int) massSpecOutputDataRecord.isotopeIndicesForRawDataColumn().get(row, 0) - 1 + massSpecOutputDataRecord.faradayCount() + 1, 0);
                dSignalNoise2.set(row, 0, term1 + term2 * dnobl2.get(row, 0));
                double residualValue = StrictMath.pow(massSpecOutputDataRecord.rawDataColumn().get(row, 0) - dataModelInit.dataArray().get(row, 0), 2);
                restmp.set(row, 0, residualValue);
                E0 += residualValue;

                double residualValue2 = StrictMath.pow(massSpecOutputDataRecord.rawDataColumn().get(row, 0) - d2.get(row, 0), 2);
                restmp2.set(row, 0, residualValue2);
                E02 += residualValue2;

                /*
                    % Decide whether to accept or reject model
                    keep = AcceptItMS(oper,dE,psig,delx,prior,Dsig,Dsig2,d0);
                 */

                if (operation.toLowerCase(Locale.ROOT).startsWith("n")) {
                    E += residualValue / dSignalNoise.get(row, 0);
                    E2 += residualValue2 / dSignalNoise2.get(row, 0);
                    sumLogDSignalNoise += -1.0 * Math.log(dSignalNoise.get(row, 0));
                    sumLogDSignalNoise2 += -1.0 * Math.log(dSignalNoise2.get(row, 0));
                } else {
                    E += residualValue * baselineMultiplier.get(row, 0) / dSignalNoise.get(row, 0);
                    E2 += residualValue2 * baselineMultiplier.get(row, 0) / dSignalNoise2.get(row, 0);
                }
            } //rows loop
            if (operation.toLowerCase(Locale.ROOT).startsWith("n")) {
                dE = E2 - E;
                double deltaLogNoise = sumLogDSignalNoise2 - sumLogDSignalNoise;//X = sum(-log(Dsig2))-sum(-log(Dsig));
                keep = min(1, exp(deltaLogNoise / 2.0 - (dE) / 2.0));//keep = min(1,exp(X/2-(dE)/2));
            } else {
                dE = 1 / tempering * (E2 - E);
                keep = min(1, 1.0 * exp(-(dE) / 2.0));
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
            keptUpdates.set(operationIndex, 1, keptUpdates.get(operationIndex, 1) + 1);
            keptUpdates.set(operationIndex, 3, keptUpdates.get(operationIndex, 3) + 1);

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
                        (Matrix) d2.clone(),
                        dataModelUpdaterOutputRecord_x2.blockIntensities(),
                        dataModelUpdaterOutputRecord_x2.blockIntensitiesOJ()
                );
                dSignalNoise = (Matrix) dSignalNoise2.clone();

                keptUpdates.set(operationIndex, 0, keptUpdates.get(operationIndex, 0) + 1);
                keptUpdates.set(operationIndex, 2, keptUpdates.get(operationIndex, 2) + 1);
            }
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
                    DataModelUpdater.UpdatedCovariancesRecord updatedCovariancesRecord =
                            updateMeanCovMS(dataModelInit, xDataCovariance, xDataMean, ensembleRecordsList, counter - covStart, false);
                    xDataCovariance = updatedCovariancesRecord.dataCov();
                    xDataMean = updatedCovariancesRecord.dataMean();

                    //todo: delx_adapt, but it is not currently used
                }

                if (modelIndex % (10 * stepCountForcedSave) == 0) {
                    String loggingSnippet =
                            "%%%%%%%%%%%%%%%%%%%%%%% Tripoli in Java test %%%%%%%%%%%%%%%%%%%%%%%"
                                    + "\nElapsed time = " + statsFormat.format(watch.getTime() / 1000.0) + " seconds for " + 10 * stepCountForcedSave + " realizations of total = " + modelIndex
                                    + "\nError function = "
                                    + statsFormat.format(StrictMath.sqrt(initialModelErrorUnWeighted_E0 / massSpecOutputDataRecord.detectorIndicesForRawDataColumn().getRowDimension()))

                                    + "\nChange Log Ratio: "
                                    + keptUpdates.get(0, 0)
                                    + " of "
                                    + keptUpdates.get(0, 1)
                                    + " accepted (" + statsFormat.format(100.0 * keptUpdates.get(0, 2) / keptUpdates.get(0, 3)) + "% total)"

                                    + "\nChange Intensity: "
                                    + keptUpdates.get(1, 0)
                                    + " of "
                                    + keptUpdates.get(1, 1)
                                    + " accepted (" + statsFormat.format(100.0 * keptUpdates.get(1, 2) / keptUpdates.get(1, 3)) + "% total)"

                                    + "\nChange DF Gain: "
                                    + keptUpdates.get(2, 0)
                                    + " of "
                                    + keptUpdates.get(2, 1)
                                    + " accepted (" + statsFormat.format(100.0 * keptUpdates.get(2, 2) / keptUpdates.get(2, 3)) + "% total)"

                                    + "\nChange Baseline: "
                                    + keptUpdates.get(3, 0)
                                    + " of "
                                    + keptUpdates.get(3, 1)
                                    + " accepted (" + statsFormat.format(100.0 * keptUpdates.get(3, 2) / keptUpdates.get(3, 3)) + "% total)"

                                    + (hierarchical ?
                                    ("\nNoise: "
                                            + keptUpdates.get(4, 0)
                                            + " of "
                                            + keptUpdates.get(4, 1)
                                            + " accepted (" + statsFormat.format(100.0 * keptUpdates.get(4, 2) / keptUpdates.get(4, 3)) + "% total)")
                                    : "");

                    System.err.println("\n" + loggingSnippet);
                    loggingCallback.receiveLoggingSnippet(loggingSnippet);

                    for (int i = 0; i < 5; i++) {
                        keptUpdates.set(i, 0, 0);
                        keptUpdates.set(i, 1, 0);
                    }

                    watch.reset();
                    watch.start();
                }
            }
        } // end model loop

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
        burn = 100;//1000;
        int countOfEnsemblesUsed = ensembleRecordsList.size() - burn;

        double[][] ensembleLogRatios = new double[massSpecOutputDataRecord.isotopeCount()][countOfEnsemblesUsed];
        double[][] ensembleRatios = new double[massSpecOutputDataRecord.isotopeCount()][countOfEnsemblesUsed];
        double[] logRatioMeans = new double[massSpecOutputDataRecord.isotopeCount()];
        double[] logRatioStdDev = new double[massSpecOutputDataRecord.isotopeCount()];

        double[][] ensembleBaselines = new double[massSpecOutputDataRecord.isotopeCount()][countOfEnsemblesUsed];
        double[] baselinesMeans = new double[massSpecOutputDataRecord.isotopeCount()];
        double[] baselinesStdDev = new double[massSpecOutputDataRecord.isotopeCount()];

        double[][] ensembleSignalnoise = new double[massSpecOutputDataRecord.isotopeCount()][countOfEnsemblesUsed];
        double[] signalNoiseMeans = new double[massSpecOutputDataRecord.isotopeCount()];
        double[] signalNoiseStdDev = new double[massSpecOutputDataRecord.isotopeCount()];

        double[][] ensembleDalyFaradayGain = new double[massSpecOutputDataRecord.isotopeCount()][countOfEnsemblesUsed];
        double dalyFaradayGainMeans = 0.0;
        double dalyFaradayGainStdDev = 0.0;

        for (int isotopeIndex = 0; isotopeIndex < massSpecOutputDataRecord.isotopeCount(); isotopeIndex++) {
            DescriptiveStatistics descriptiveStatisticsLogRatios = new DescriptiveStatistics();
            DescriptiveStatistics descriptiveStatisticsBaselines = new DescriptiveStatistics();
            DescriptiveStatistics descriptiveStatisticsSignalNoise = new DescriptiveStatistics();
            DescriptiveStatistics descriptiveStatisticsDalyFaradayGain = new DescriptiveStatistics();
            for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
                ensembleLogRatios[isotopeIndex][index - burn] = ensembleRecordsList.get(index).logRatios().get(isotopeIndex, 0);
                descriptiveStatisticsLogRatios.addValue(ensembleLogRatios[isotopeIndex][index - burn]);
                ensembleRatios[isotopeIndex][index - burn] = StrictMath.exp(ensembleLogRatios[isotopeIndex][index - burn]);

                ensembleBaselines[isotopeIndex][index - burn] = ensembleRecordsList.get(index).baseLine().get(isotopeIndex, 0);
                descriptiveStatisticsBaselines.addValue(ensembleBaselines[isotopeIndex][index - burn]);

                ensembleSignalnoise[isotopeIndex][index - burn] = ensembleRecordsList.get(index).signalNoise().get(isotopeIndex, 0);
                descriptiveStatisticsSignalNoise.addValue(ensembleSignalnoise[isotopeIndex][index - burn]);

                ensembleDalyFaradayGain[isotopeIndex][index - burn] = ensembleRecordsList.get(index).dfGain();
                descriptiveStatisticsDalyFaradayGain.addValue(ensembleDalyFaradayGain[isotopeIndex][index - burn]);
            }
            logRatioMeans[isotopeIndex] = descriptiveStatisticsLogRatios.getMean();
            logRatioStdDev[isotopeIndex] = descriptiveStatisticsLogRatios.getStandardDeviation();

            baselinesMeans[isotopeIndex] = descriptiveStatisticsBaselines.getMean();
            baselinesStdDev[isotopeIndex] = descriptiveStatisticsBaselines.getStandardDeviation();

            signalNoiseMeans[isotopeIndex] = descriptiveStatisticsSignalNoise.getMean();
            signalNoiseStdDev[isotopeIndex] = descriptiveStatisticsSignalNoise.getStandardDeviation();

            dalyFaradayGainMeans = descriptiveStatisticsDalyFaradayGain.getMean();
            dalyFaradayGainStdDev = descriptiveStatisticsDalyFaradayGain.getStandardDeviation();
        }


        // visualization
        Histogram histogram = Histogram.initializeHistogram(ensembleRatios[0], 50);


        // todo: missing additional elements of signalNoise (i.e., 0,11,11)
        System.err.println(logRatioMeans[0] + "         " + logRatioMeans[1] + "    " + logRatioStdDev[0] + "     " + logRatioStdDev[1]);
        System.err.println(baselinesMeans[0] + "         " + baselinesMeans[1] + "    " + baselinesStdDev[0] + "     " + baselinesStdDev[1]);
        System.err.println(signalNoiseMeans[0] + "         " + signalNoiseMeans[1] + "    " + signalNoiseStdDev[0] + "     " + signalNoiseStdDev[1]);
        System.err.println(dalyFaradayGainMeans + "    " + dalyFaradayGainStdDev);

        return histogram;
    }

    private static int findFirstOrLast(boolean first, int index, Matrix target, int flag, Matrix flags) {
        // assume column vectors
        int retVal = -1;
        for (int row = 0; row < target.getRowDimension(); row++) {
            if ((target.get(row, 0) == index) && (flags.get(row, 0) == flag)) {
                retVal = row;
                if (first) break;
            }
        }
        return retVal;
    }

    /**
     * Randomly generate next model operation, with or without hierarchical step
     *
     * @param hierFlag
     * @return
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
            Matrix logRatios,
            Matrix intensity,
            Matrix baseLine,
            double dfGain,
            Matrix signalNoise,
            double errorWeighted,
            double errorUnWeighted
    ) {
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
            Matrix priorBaselineFaraday,
            Matrix priorBaselineDaly,
            Matrix priorLogRatio,
            double maxIntensity,
            double minIntensity,
            Matrix priorIntensity,
            Matrix priorDFgain,
            Matrix priorSignalNoiseFaraday,
            Matrix priorSignalNoiseDaly,
            Matrix priorPoissonNoiseDaly
    ) {

    }
}