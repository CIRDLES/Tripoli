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
import org.apache.commons.math3.random.RandomDataGenerator;
import org.cirdles.tripoli.sessions.analysis.analysisMethods.AnalysisMethodBuiltinFactory;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.DataSourceProcessor_OPPhoenix;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.exp;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.DataModelUpdater.updateMSv2;

/**
 * @author James F. Bowring
 */
public class DataModelDriverExperiment {

    public static DataModellerOutputRecord driveModelTest(Path dataFilePath) throws IOException {
        DataSourceProcessor_OPPhoenix dataSourceProcessorOPPhoenix
                = DataSourceProcessor_OPPhoenix.initializeWithAnalysisMethod(AnalysisMethodBuiltinFactory.analysisMethodsBuiltinMap.get("BurdickBlSyntheticData"));
        MassSpecOutputDataRecord massSpecOutputDataRecord = dataSourceProcessorOPPhoenix.prepareInputDataModelFromFile(dataFilePath);
        DataModellerOutputRecord dataModelInit = DataModelInitializer.modellingTest(massSpecOutputDataRecord);

        applyInversionWithRJ_MCMC(massSpecOutputDataRecord, dataModelInit);


        return dataModelInit;
    }

    static void applyInversionWithRJ_MCMC(MassSpecOutputDataRecord massSpecOutputDataRecord, DataModellerOutputRecord dataModelInit) {
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
        int maxCount = 2000;
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
        for (int row = 0; row < dataModelInit.blockIntensities().getRowDimension(); row++) {
            maxIntensity = Math.max(dataModelInit.blockIntensities().get(row, 0), maxIntensity);
            minIntensity = Math.min(dataModelInit.blockIntensities().get(row, 0), minIntensity);
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
        for (int blockIndex = 0; blockIndex < 1; blockIndex++) {
            Matrix Intensity = massSpecOutputDataRecord.firstBlockInterpolations().times(dataModelInit.blockIntensities());

            for (int isotopeIndex = 0; isotopeIndex < massSpecOutputDataRecord.isotopeCount(); isotopeIndex++) {
                for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().getRowDimension(); row++) {
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn().get(row, isotopeIndex) == 1)
                            && (massSpecOutputDataRecord.axialFlagsForRawDataColumn().get(row, 0) == 1)
                            && massSpecOutputDataRecord.blockIndicesForRawDataColumn().get(row, 0) == (blockIndex + 1)) {
                        double calcValue =
                                exp(dataModelInit.logratios().get(isotopeIndex, 0))
                                        * Intensity.get((int) massSpecOutputDataRecord.timeIndColumn().get(row, 0) - 1, 0);
                        dataModelInit.dataArray().set(row, 0, calcValue);
                        dataWithNoBaseline.set(row, 0, calcValue);
                    }
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn().get(row, isotopeIndex) == 1)
                            && (massSpecOutputDataRecord.axialFlagsForRawDataColumn().get(row, 0) == 0)
                            && massSpecOutputDataRecord.blockIndicesForRawDataColumn().get(row, 0) == (blockIndex + 1)) {
                        double calcValue =
                                exp(dataModelInit.logratios().get(isotopeIndex, 0))
                                        * 1.0 / dataModelInit.dfGain()
                                        * Intensity.get((int) massSpecOutputDataRecord.timeIndColumn().get(row, 0) - 1, 0);
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
                    StrictMath.sqrt(Math.pow(dataModelInit.sigmas().get((int) massSpecOutputDataRecord.detectorIndicesForRawDataColumn().get(row, 0) - 1, 0), 2)
                            // faradaycount plus 1 = number of detectors and we subtract 1 for the 1-based matlab indices
                            + dataModelInit.sigmas().get((int) massSpecOutputDataRecord.isotopeIndicesForRawDataColumn().get(row, 0) + massSpecOutputDataRecord.faradayCount(), 0)
                            * dataWithNoBaseline.get(row, 0));
            dSignalNoise.set(row, 0, calculatedValue);
        }

        Matrix residualTmp = new Matrix(dSignalNoise.getRowDimension(), 1);
        Matrix residualTmp2 = new Matrix(dSignalNoise.getRowDimension(), 1);
        double errorWeighted = 0.0;
        double errorUnWeighted = 0.0;
        for (int row = 0; row < residualTmp.getRowDimension(); row++) {
            double calculatedValue = StrictMath.pow(massSpecOutputDataRecord.rawDataColumn().get(row, 0) - dataModelInit.dataArray().get(row, 0), 2);
            residualTmp.set(row, 0, calculatedValue);
            errorWeighted = errorWeighted + (calculatedValue * baselineMultiplier.get(row, 0) / dSignalNoise.get(row, 0));
            errorUnWeighted = errorUnWeighted + calculatedValue;
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
        Matrix blockStartIndicesFaraday = new Matrix(massSpecOutputDataRecord.blockCount(), 1, 0);
        Matrix blockEndIndicesFaraday = new Matrix(massSpecOutputDataRecord.blockCount(), 1, 0);
        Matrix blockStartIndicesDaly = new Matrix(massSpecOutputDataRecord.blockCount(), 1, 0);
        Matrix blockEndIndicesDaly = new Matrix(massSpecOutputDataRecord.blockCount(), 1, 0);
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

        for (int modelIndex = 0; modelIndex < maxCount * stepCountForcedSave; modelIndex++) {
            String operation = randomOperMS(hierarchical);
            boolean adaptiveFlag = (counter >= 100000);
            boolean allFlag = adaptiveFlag;
            int columnChoice = modelIndex % stepCountForcedSave;
            DataModellerOutputRecord dataModelUpdaterOutputRecord = updateMSv2(
                    operation,
                    dataModelInit,
                    psigRecord,
                    priorRecord,
                    ensembleRecordsList,
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
                tmpDF = ones(d0.Ndata,1); tmpDF(~d0.axflag) = x2.DFgain^-1;
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

        }
        System.err.println();
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
        Object[][] hier = new Object[][]{{60, 80, 95, 100, 120}, {"changeI", "changer", "changebl", "changedfg", "noise"}};

        RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
        int choice = hierFlag ? randomDataGenerator.nextInt(0, 100) : randomDataGenerator.nextInt(0, 120);
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