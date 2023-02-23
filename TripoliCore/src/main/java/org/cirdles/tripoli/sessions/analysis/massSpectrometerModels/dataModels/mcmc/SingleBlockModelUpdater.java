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

import com.google.common.primitives.Ints;
import jama.Matrix;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author James F. Bowring
 */
public enum SingleBlockModelUpdater {
    ;


    /*
    Scott Burdick via email 5 Feb 2023
    Here are the operation permutation generator and two functions that needed to be altered to accommodate it.  PreorderOpsMS takes in
    the number of parameters and permutes them as many times as necessary.
    It can be placed anywhere after the initial model is defined but before the main loop:oper_order = PreorderOpsMS(x,maxcnt*datsav);

    The other two functions replace the existing versions in the main loop:

    oper = RandomOperMS_Preorder(x,oper_order(m));

    [x2,delx] = UpdateMSv2_Preorder(oper_order(m),x,psig,prior,ensemble,xcov,delx_adapt,adaptflag,allflag);

    Unfortunately it seemed easier to keep in the “oper” string variable since it’s used in so many other places,
    but now it’s defined according to the precomputed order.
     */

    /*
    function oper_order = PreorderOpsMS(x,cnt)
    oper_order = PreorderOpsMS(x,maxcnt*datsav);
        % Inputs:
        % x - struct containing model variables
        % cnt - Number of random walk iterations to perform before adaptive MC
        % Outputs:
        % oper_order

        % Find number of variables for each parameter type from model struct
        Niso = length(x.lograt);
        Nblock = length(x.I);
        for ii=1:Nblock;
            Ncycle(ii) = length(x.I{ii});
        end
        Nfar = length(x.BL);
        Ndf = 1;
        Nsig = length(x.sig);

        % Total number of variables
        N = Niso+sum(Ncycle)+Nfar+Ndf+Nsig;

        % How many permutations needed?
        Npermutes = ceil(cnt/N);

        oper_order= zeros(N*Npermutes,1);

        for ii = 1:Npermutes
            oper_order((1+(ii-1)*N):ii*N,1) = randperm(N)';
        end
     */

    public static List<String> operations = new ArrayList<>();
    static int countOfLogRatios;
    static int countOfCycles;
    static int countOfFaradays;
    static int countOfPhotoMultipliers;
    static int countTotalVariables;
    static int countOfTotalModelParameters;
    static int countOfSignalNoiseSigma;
    static double[] ps0DiagArray;
    static double[] priorMinArray;
    static double[] priorMaxArray;

    static {
        operations.add("changer");
        operations.add("changeI");
        operations.add("changedfg");
        operations.add("changebl");
        operations.add("noise");
    }

    public static int[] preOrderOpsMS(SingleBlockModelRecord singleBlockInitialModelRecord, int countOfIterations) {
        countOfLogRatios = singleBlockInitialModelRecord.logRatios().length;
        countOfCycles = singleBlockInitialModelRecord.I0().length;
        countOfFaradays = singleBlockInitialModelRecord.faradayCount();
        countOfPhotoMultipliers = 1;
        countOfSignalNoiseSigma = singleBlockInitialModelRecord.signalNoiseSigma().length;

        countTotalVariables = countOfLogRatios + countOfCycles + countOfFaradays + countOfPhotoMultipliers + countOfSignalNoiseSigma;
        countOfTotalModelParameters = countOfLogRatios + countOfCycles + countOfFaradays + countOfPhotoMultipliers;

        int countOfPermutations = (int) StrictMath.ceil(countOfIterations / countTotalVariables) + 1;

        int[] operationOrder = new int[countOfPermutations * countTotalVariables];

        Integer[] permuteArray = new Integer[countTotalVariables];
        for (int i = 0; i < countTotalVariables; i++) {
            permuteArray[i] = i;
        }
        List<Integer> permuteList = Arrays.asList(permuteArray);

        for (int permIndex = 0; permIndex < countOfPermutations; permIndex++) {
            Collections.shuffle(permuteList);
            System.arraycopy(Ints.toArray(permuteList), 0, operationOrder, permIndex * countTotalVariables, countTotalVariables);
        }
        return operationOrder;
    }

    public static void buildPriorLimits(ProposedModelParameters.ProposalSigmasRecord proposalSigmasRecord, ProposedModelParameters.ProposalRangesRecord proposalRangesRecord) {
        ps0DiagArray = new double[countOfTotalModelParameters];
        priorMinArray = new double[countOfTotalModelParameters];
        priorMaxArray = new double[countOfTotalModelParameters];

        for (int logRatioIndex = 0; logRatioIndex < countOfLogRatios; logRatioIndex++) {
            ps0DiagArray[logRatioIndex] = proposalSigmasRecord.psigLogRatio();
            priorMinArray[logRatioIndex] = proposalRangesRecord.priorLogRatio()[0][0];
            priorMaxArray[logRatioIndex] = proposalRangesRecord.priorLogRatio()[0][1];
        }

        for (int cycleIndex = 0; cycleIndex < countOfCycles; cycleIndex++) {
            ps0DiagArray[cycleIndex + countOfLogRatios] = proposalSigmasRecord.psigIntensityPercent();
            priorMinArray[cycleIndex + countOfLogRatios] = proposalRangesRecord.priorIntensity()[0][0];
            priorMaxArray[cycleIndex + countOfLogRatios] = proposalRangesRecord.priorIntensity()[0][1];
        }

        for (int faradayIndex = 0; faradayIndex < countOfFaradays; faradayIndex++) {
            ps0DiagArray[faradayIndex + countOfLogRatios + countOfCycles] = proposalSigmasRecord.psigBaselineFaraday();
            priorMinArray[faradayIndex + countOfLogRatios + countOfCycles] = proposalRangesRecord.priorBaselineFaraday()[0][0];
            priorMaxArray[faradayIndex + countOfLogRatios + countOfCycles] = proposalRangesRecord.priorBaselineFaraday()[0][1];
        }
        ps0DiagArray[countOfTotalModelParameters - 1] = proposalSigmasRecord.psigDFgain();
        priorMinArray[countOfTotalModelParameters - 1] = proposalRangesRecord.priorDFgain()[0][0];
        priorMaxArray[countOfTotalModelParameters - 1] = proposalRangesRecord.priorDFgain()[0][1];
    }

    /*
    function oper = RandomOperMS_Preorder(x,oper_order)

    % Randomly generate next model operation, with or without hierarchical step

    % Find number of variables for each parameter type from model struct
    Niso = length(x.lograt);
    Nblock = length(x.I);
    for ii=1:Nblock;
        Ncycle(ii) = length(x.I{ii});
    end
    Nfar = length(x.BL);
    Ndf = 1;
    Nsig = length(x.sig);

    if oper_order <= Niso;
        oper = 'changer';
    elseif oper_order <= Niso + sum(Ncycle);
        oper = 'changeI';
    elseif oper_order <= Niso + sum(Ncycle) + Nfar
        oper = 'changebl';
    elseif oper_order <= Niso + sum(Ncycle) + Nfar + randi(Ndf)
        oper = 'changedfg';
    else
        oper = 'noise';
    end
     */

    public static String randomOperMS_Preorder(int oper_order) {
        RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
        randomDataGenerator.reSeedSecure();

        String oper = "noise";
        if (oper_order <= countOfLogRatios)
            oper = "changer";
        else if (oper_order <= countOfLogRatios + countOfCycles)
            oper = "changeI";
        else if (oper_order <= countOfLogRatios + countOfCycles + countOfFaradays)
            oper = "changebl";
        else if (oper_order <= countOfLogRatios + countOfCycles + countOfFaradays + randomDataGenerator.nextInt(1, countOfPhotoMultipliers))
            oper = "changedfg";

        return oper;
    }


    /*
    function  [x2,delx,xcov] = UpdateMSv2_Preorder(oper,x,psig,prior,ensemble,xcov,delx_adapt,adaptflag,allflag)
        %%
        cnt = length(ensemble);
        ps0diag =  [psig.lograt*ones(Niso,1);          psig.I*ones(sum(Ncycle),1);     psig.BL*ones(Nfar,1);     psig.DFgain*ones(Ndf,1)];
        priormin = [prior.lograt(1)*ones(Niso-1,1); 0; prior.I(1)*ones(sum(Ncycle),1); prior.BL(1)*ones(Nfar,1); prior.DFgain(1)*ones(Ndf,1)];
        priormax = [prior.lograt(2)*ones(Niso-1,1); 0; prior.I(2)*ones(sum(Ncycle),1); prior.BL(2)*ones(Nfar,1); prior.DFgain(2)*ones(Ndf,1)];
        %xx0 = [x.lograt; x.I{1}; x.I{2}; x.BL; x.DFgain];

        xx0 = x.lograt;
        xind = ones(Niso,1);

        for ii=1:Nblock
            xx0 = [xx0; x.I{ii}];
            xind = [xind; 1+ii*ones(Ncycle(ii),1)];
        end

        xx0 = [xx0; x.BL];
        xind = [xind; (2+Nblock)*ones(Nfar,1)];

        xx0 = [xx0; x.DFgain];
        xind = [xind; (3+Nblock)*ones(Ndf,1)];

        %if strcmp(oper(1:3),'cha')
        if oper<=N % If operation is for model parameter, not noise parameter
            if ~allflag
                if adaptflag
                    delx = sqrt(xcov(oper,oper))*randn(1); %mvnrnd(zeros(1),xcov(nind,nind));
                else
                    delx = ps0diag(oper)*randn(1);
                end
                xx =  xx0;
                xx(oper) = xx(oper) + delx;

                inprior = xx<=priormax & xx>=priormin;

                xx(~inprior) = xx0(~inprior);
            else
                %VARY ALL AT A TIME

                %%delx(:,1) = mvnrnd(zeros(size(xx0)),xcov);
                %delx(:,1) = mvnrnd(zeros(size(xx0)),2.38^2*xcov/length(xind));
                delx = delx_adapt;
                xx =  xx0 + delx;
                inprior = xx<=priormax & xx>=priormin;
                xx(~inprior) = xx0(~inprior);
            end
             x2.lograt = xx(xind==1);
            for ii=1:Nblock
                x2.I{ii} = xx(xind==(1+ii));
            end
            x2.BL = xx(xind==(2+Nblock));
            x2.DFgain = xx(xind==(3+Nblock));

            x2.sig = x.sig;

        elseif oper>N   %CHANGE NOISE

            %oper = randi(length(x.sig));
            %oper = randi(length(x.BL)); %Just for the faradays

            % Find preordered random noise variable
            nind = oper - N;
            x2=x;

            delx=psig.sig*randn(1);

            if x2.sig(nind) + delx >= prior.sig(1) && x2.sig(nind) + delx <= prior.sig(2)
                x2.sig(nind) = x2.sig(nind)+delx;
            else
                delx=0;
            end
        else
            disp('Thats not a thing')
        end
     */

    public static SingleBlockModelRecord updateMSv2Preorder(
            int operationIndex,
            SingleBlockModelRecord singleBlockInitialModelRecord_initial,
            ProposedModelParameters.ProposalSigmasRecord proposalSigmasRecord,
            ProposedModelParameters.ProposalRangesRecord proposalRangesRecord,
            double[][] xDataCovariance,
            double[] delx_adapt,
            boolean adaptiveFlag,
            boolean allFlag) {

        double[] parametersModel_xx0 = new double[countOfTotalModelParameters];
        // parametersModelTypeFlags contains indices where 1 = logratio, 2 = cycles from I0, 3 = baseline means, 4 = faradayGain
        double[] parametersModelTypeFlags = new double[countOfTotalModelParameters];
        System.arraycopy(singleBlockInitialModelRecord_initial.logRatios(), 0, parametersModel_xx0, 0, countOfLogRatios);
        Arrays.fill(parametersModelTypeFlags, 1.0);

        System.arraycopy(singleBlockInitialModelRecord_initial.I0(), 0, parametersModel_xx0, countOfLogRatios, countOfCycles);
        double[] temp = new double[countOfCycles];
        Arrays.fill(temp, 2.0);
        System.arraycopy(temp, 0, parametersModelTypeFlags, countOfLogRatios, countOfCycles);

        System.arraycopy(singleBlockInitialModelRecord_initial.baselineMeansArray(), 0, parametersModel_xx0, countOfLogRatios + countOfCycles, countOfFaradays);
        temp = new double[countOfFaradays];
        Arrays.fill(temp, 3.0);
        System.arraycopy(temp, 0, parametersModelTypeFlags, countOfLogRatios + countOfCycles, countOfFaradays);

        parametersModel_xx0[countOfTotalModelParameters - 1] = singleBlockInitialModelRecord_initial.detectorFaradayGain();
        parametersModelTypeFlags[countOfTotalModelParameters - 1] = 4;

        SingleBlockModelRecord singleBlockInitialModelRecord_initial2;
        RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
        randomDataGenerator.reSeedSecure();

        double randomSigma = 1.0;
        double[] signalNoiseSigmaUpdated = singleBlockInitialModelRecord_initial.signalNoiseSigma().clone();
        double[] parametersModel_updated = parametersModel_xx0.clone();

        double deltaX;
        if (operationIndex < countOfTotalModelParameters) {
            if (!allFlag) {
                if (adaptiveFlag) {
                    deltaX = StrictMath.sqrt(xDataCovariance[operationIndex][operationIndex]) * randomDataGenerator.nextGaussian(0.0, randomSigma);
                } else {
                    deltaX = ps0DiagArray[operationIndex] * randomDataGenerator.nextGaussian(0.0, randomSigma);
                }
                double changed = parametersModel_updated[operationIndex] + deltaX;
                if ((changed <= priorMaxArray[operationIndex] && (changed >= priorMinArray[operationIndex]))) {
                    parametersModel_updated[operationIndex] = changed;
                }
            } else {
                /*
                    %VARY ALL AT A TIME
                        delx = delx_adapt;
                        xx = xx0 + delx;
                        inprior = xx <= priormax & xx >= priormin;
                        xx(~inprior) = xx0(~inprior);
                */
                PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
                parametersModel_updated = storeFactory.column(parametersModel_xx0.clone()).add(storeFactory.columns(delx_adapt.clone())).toRawCopy1D();
                for (int row = 0; row < parametersModel_updated.length; row++) {
                    if ((parametersModel_updated[row] > priorMaxArray[row] || (parametersModel_updated[row] < priorMinArray[row]))) {
                        // restore values if new values are out of prior range
                        parametersModel_updated[row] = parametersModel_xx0[row];
                    }
                }
            }

            List<Double> updatedLogRatioList = new ArrayList<>();
            List<Double> updatedBlockintensitiesListII = new ArrayList<>();
            List<Double> updatedBaselineMeansList = new ArrayList<>();
            double updatedDFGain = 0.0;

            for (int row = 0; row < parametersModel_updated.length; row++) {
                if (1 == parametersModelTypeFlags[row]) {
                    updatedLogRatioList.add(parametersModel_updated[row]);
                }
                if (2 == parametersModelTypeFlags[row]) {
                    updatedBlockintensitiesListII.add(parametersModel_updated[row]);
                }
                if (3 == parametersModelTypeFlags[row]) {
                    updatedBaselineMeansList.add(parametersModel_updated[row]);
                }
                if (4 == parametersModelTypeFlags[row]) {
                    updatedDFGain = parametersModel_updated[row];
                }
            }
            double[] updatedLogRatio = updatedLogRatioList.stream().mapToDouble(d -> d).toArray();
            double[] updatedBaselineMeans = updatedBaselineMeansList.stream().mapToDouble(d -> d).toArray();
            double[] updatedBlockIntensities_I0 = updatedBlockintensitiesListII.stream().mapToDouble(d -> d).toArray();

            singleBlockInitialModelRecord_initial2 = new SingleBlockModelRecord(
                    updatedBaselineMeans,
                    singleBlockInitialModelRecord_initial.baselineStandardDeviationsArray().clone(),
                    updatedDFGain,
                    singleBlockInitialModelRecord_initial.mapDetectorOrdinalToFaradayIndex(),
                    updatedLogRatio,
                    singleBlockInitialModelRecord_initial.signalNoiseSigma().clone(),
                    singleBlockInitialModelRecord_initial.dataArray().clone(),
                    singleBlockInitialModelRecord_initial.dataWithNoBaselineArray().clone(),
                    singleBlockInitialModelRecord_initial.dataSignalNoiseArray().clone(),
                    updatedBlockIntensities_I0,
                    singleBlockInitialModelRecord_initial.intensities(),
                    singleBlockInitialModelRecord_initial.faradayCount(),
                    singleBlockInitialModelRecord_initial.isotopeCount()
            );

        } else {
            // noise case
            /*
            % Find preordered random noise variable
            nind = oper - N;
            x2=x;
            delx=psig.sig*randn(1);

            if x2.sig(nind) + delx >= prior.sig(1) && x2.sig(nind) + delx <= prior.sig(2)
                x2.sig(nind) = x2.sig(nind)+delx;
            else
                delx=0;
            end
             */
            int nInd = operationIndex - countOfTotalModelParameters;
            deltaX = proposalSigmasRecord.psigSignalNoiseFaraday() * randomDataGenerator.nextGaussian(0.0, randomSigma);
            double testDelta = signalNoiseSigmaUpdated[nInd] + deltaX;
            if ((testDelta >= proposalRangesRecord.priorSignalNoiseFaraday()[0][0])
                    &&
                    (testDelta <= proposalRangesRecord.priorSignalNoiseFaraday()[0][1])) {
                signalNoiseSigmaUpdated[nInd] = testDelta;
            }
            singleBlockInitialModelRecord_initial2 = new SingleBlockModelRecord(
                    singleBlockInitialModelRecord_initial.baselineMeansArray(),
                    singleBlockInitialModelRecord_initial.baselineStandardDeviationsArray(),
                    singleBlockInitialModelRecord_initial.detectorFaradayGain(),
                    singleBlockInitialModelRecord_initial.mapDetectorOrdinalToFaradayIndex(),
                    singleBlockInitialModelRecord_initial.logRatios(),
                    signalNoiseSigmaUpdated,
                    singleBlockInitialModelRecord_initial.dataArray(),
                    singleBlockInitialModelRecord_initial.dataWithNoBaselineArray(),
                    singleBlockInitialModelRecord_initial.dataSignalNoiseArray(),
                    singleBlockInitialModelRecord_initial.I0(),
                    singleBlockInitialModelRecord_initial.intensities(),
                    singleBlockInitialModelRecord_initial.faradayCount(),
                    singleBlockInitialModelRecord_initial.isotopeCount());
        }
        return singleBlockInitialModelRecord_initial2;
    }

    static UpdatedCovariancesRecord updateMeanCovMS(
            SingleBlockModelRecord singleBlockModelRecord,
            double[][] dataModelCov,
            double[] dataModelMean,
            List<EnsemblesStore.EnsembleRecord> ensembleRecordsList,
            int countOfNewModels,
            boolean iterFlag) {
        // [xmean,xcov] = UpdateMeanCovMS(x,xcov,xmean,ensemble,cnt-covstart,0);
        // function [xmean,xcov] = UpdateMeanCovMS(x,xcov,xmean,ensemble,m,iterflag)
        /*
            Niso = length(x.lograt);
            Nblock = length(x.I);
            for ii=1:Nblock;
                Ncycle(ii) = length(x.I{ii});
            end
            Nfar = length(x.BL);
            Ndf = 1;
            Nmod = Niso + sum(Ncycle) + Nfar + Ndf;

            if iterflag
                xx = x.lograt;
                for ii=1:Nblock
                    xx = [xx; x.I{ii}];
                end
                xx = [xx; x.BL(1:Nfar)];
                xx = [xx; x.DFgain];
                xmean = (xmean*(m-1) + xx)/m;
                xctmp = (xx-xmean)*(xx-xmean)';
                xctmp = (xctmp+xctmp')/2;
                xcov = (xcov*(m-1) + (m-1)/m*xctmp)/m;
            end

            if ~iterflag
                cnt = length(ensemble);
                enso = [ensemble.lograt];
                for ii = 1:Nblock
                    for n = 1:cnt;
                        ens_I{ii}(:,n) =[ensemble(n).I{ii}];
                    end
                    enso = [enso; ens_I{ii}];
                end
                enso = [enso; [ensemble.BL]];
                enso = [enso; [ensemble.DFgain]];
                %xcov = cov(enso(:,ceil(end/2):end)');
                xmean = mean(enso(:,m:end)');
                xcov = cov(enso(:,m:end)');
            end
         */

        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        double[] dataMean;
//        double[][] dataCov;
        Covariance cov2 = new Covariance();
        if (iterFlag) {
            // todo: currently iterFlag is always false
            dataMean = null;
//            dataCov = null;

                /*
                xx = x.lograt;
                for ii=1:Nblock
                    xx = [xx; x.I{ii}];
                end
                xx = [xx; x.BL(1:Nfar)];
                xx = [xx; x.DFgain];
                xmean = (xmean*(m-1) + xx)/m;
                xctmp = (xx-xmean)*(xx-xmean)';
                xctmp = (xctmp+xctmp')/2;
                xcov = (xcov*(m-1) + (m-1)/m*xctmp)/m;
             */
        } else {
            /*
                cnt = length(ensemble);
                enso = [ensemble.lograt];
                for ii = 1:Nblock
                    for n = 1:cnt;
                        ens_I{ii}(:,n) =[ensemble(n).I{ii}];
                    end
                    enso = [enso; ens_I{ii}];
                end
                enso = [enso; [ensemble.BL]];
                enso = [enso; [ensemble.DFgain]];

                %xcov = cov(enso(:,ceil(end/2):end)');
                xmean = mean(enso(:,m:end)');
                xcov = cov(enso(:,m:end)');
             */
            int modelCount = ensembleRecordsList.size() - countOfNewModels + 1;
            double[] totalsByRow2 = new double[countOfTotalModelParameters];
            double[][] enso2 = new double[countOfTotalModelParameters][modelCount];
            for (int modelIndex = 0; modelIndex < modelCount; modelIndex++) {
                EnsemblesStore.EnsembleRecord ensembleRecord = ensembleRecordsList.get(modelIndex + countOfNewModels - 1);
                int row = 0;
                for (int logRatioIndex = 0; logRatioIndex < countOfLogRatios; logRatioIndex++) {
                    enso2[row][modelIndex] = ensembleRecord.logRatios()[logRatioIndex];
                    totalsByRow2[row] += ensembleRecord.logRatios()[logRatioIndex];
                    row++;
                }

                for (int intensityIndex = 0; intensityIndex < singleBlockModelRecord.I0().length; intensityIndex++) {
                    enso2[row][modelIndex] = ensembleRecord.intensities()[intensityIndex];
                    totalsByRow2[row] += ensembleRecord.intensities()[intensityIndex];
                    row++;
                }

                for (int baseLineIndex = 0; baseLineIndex < countOfFaradays; baseLineIndex++) {
                    enso2[row][modelIndex] = ensembleRecord.baseLine()[baseLineIndex];
                    totalsByRow2[row] += ensembleRecord.baseLine()[baseLineIndex];
                    row++;
                }

                enso2[row][modelIndex] = enso2[row][modelIndex] = ensembleRecord.dfGain();
                totalsByRow2[row] += ensembleRecord.dfGain();
            }

            for (int i = 0; i < totalsByRow2.length; i++) {
                totalsByRow2[i] /= modelCount;
            }

            dataMean = totalsByRow2.clone();
            Matrix ensoTransposeM = new Matrix(enso2).transpose();
            double[][] ensoTranspose = ensoTransposeM.getArray();
            cov2 = new Covariance(ensoTranspose);
        }
        return new UpdatedCovariancesRecord(cov2.getCovarianceMatrix().getData(), dataMean);
    }

    //**************
    // function  [x2,delx,xcov] = UpdateMSv2(oper,x,psig,prior,ensemble,xcov,delx_adapt,adaptflag,allflag)
    static SingleBlockModelRecord updateMSv2(
            String operation,
            SingleBlockModelRecord singleBlockInitialModelRecord_initial,
            ProposedModelParameters.ProposalSigmasRecord psigRecord,
            ProposedModelParameters.ProposalRangesRecord priorRecord,
            double[][] xDataCovariance,
            double[] delx_adapt,
            boolean adaptiveFlag,
            boolean allFlag) {

        /*
            cnt = length(ensemble);

            Niso = length(x.lograt);
            Nblock = length(x.I);
            for ii=1:Nblock;
                Ncycle(ii) = length(x.I{ii});
            end
            Nfar = length(x.BL);
            Ndf = 1;

            ps0diag =  [psig.lograt*ones(Niso,1);          psig.I*ones(sum(Ncycle),1);     psig.BL*ones(Nfar,1);     psig.DFgain*ones(Ndf,1)];
            priormin = [prior.lograt(1)*ones(Niso-1,1); 0; prior.I(1)*ones(sum(Ncycle),1); prior.BL(1)*ones(Nfar,1); prior.DFgain(1)*ones(Ndf,1)];
            priormax = [prior.lograt(2)*ones(Niso-1,1); 0; prior.I(2)*ones(sum(Ncycle),1); prior.BL(2)*ones(Nfar,1); prior.DFgain(2)*ones(Ndf,1)];
         */
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        int countOfLogRatios = singleBlockInitialModelRecord_initial.logRatios().length;

        int countOfFaradays = singleBlockInitialModelRecord_initial.baselineMeansArray().length;
        int countOfNonFaradays = 1;
        int countOfCycles = singleBlockInitialModelRecord_initial.I0().length;
        int countOfRows = countOfLogRatios + countOfCycles + countOfFaradays + countOfNonFaradays;

        double[] ps0DiagArray = new double[countOfRows];
        double[] priorMinArray = new double[countOfRows];
        double[] priorMaxArray = new double[countOfRows];

        for (int logRatioIndex = 0; logRatioIndex < countOfLogRatios; logRatioIndex++) {
            ps0DiagArray[logRatioIndex] = psigRecord.psigLogRatio();
            priorMinArray[logRatioIndex] = priorRecord.priorLogRatio()[0][0];
            priorMaxArray[logRatioIndex] = priorRecord.priorLogRatio()[0][1];
        }

        for (int cycleIndex = 0; cycleIndex < countOfCycles; cycleIndex++) {
            ps0DiagArray[cycleIndex + countOfLogRatios] = psigRecord.psigIntensityPercent();
            priorMinArray[cycleIndex + countOfLogRatios] = priorRecord.priorIntensity()[0][0];
            priorMaxArray[cycleIndex + countOfLogRatios] = priorRecord.priorIntensity()[0][1];
        }

        for (int faradayIndex = 0; faradayIndex < countOfFaradays; faradayIndex++) {
            ps0DiagArray[faradayIndex + countOfLogRatios + countOfCycles] = psigRecord.psigBaselineFaraday();
            priorMinArray[faradayIndex + countOfLogRatios + countOfCycles] = priorRecord.priorBaselineFaraday()[0][0];
            priorMaxArray[faradayIndex + countOfLogRatios + countOfCycles] = priorRecord.priorBaselineFaraday()[0][1];
        }
        ps0DiagArray[countOfRows - 1] = psigRecord.psigDFgain();
        priorMinArray[countOfRows - 1] = priorRecord.priorDFgain()[0][0];
        priorMaxArray[countOfRows - 1] = priorRecord.priorDFgain()[0][1];

        /*
            xx0 = x.lograt;
            xind = ones(Niso,1);

            for ii=1:Nblock
                xx0 = [xx0; x.I{ii}];
                xind = [xind; 1+ii*ones(Ncycle(ii),1)];
            end

            xx0 = [xx0; x.BL];
            xind = [xind; (2+Nblock)*ones(Nfar,1)];

            xx0 = [xx0; x.DFgain];
            xind = [xind; (3+Nblock)*ones(Ndf,1)];
         */

        double[] parametersModel_xx0 = new double[countOfRows];
        // xInd contains indices where 1 = logratio, 2 = cycles from I0, 3 = baseline means, 4 = faradayGain
        double[] parametersModelTypeFlags = new double[countOfRows];
        System.arraycopy(singleBlockInitialModelRecord_initial.logRatios(), 0, parametersModel_xx0, 0, countOfLogRatios);
        Arrays.fill(parametersModelTypeFlags, 1.0);

        System.arraycopy(singleBlockInitialModelRecord_initial.I0(), 0, parametersModel_xx0, countOfLogRatios, countOfCycles);
        double[] temp = new double[countOfCycles];
        Arrays.fill(temp, 2.0);
        System.arraycopy(temp, 0, parametersModelTypeFlags, countOfLogRatios, countOfCycles);

        System.arraycopy(singleBlockInitialModelRecord_initial.baselineMeansArray(), 0, parametersModel_xx0, countOfLogRatios + countOfCycles, countOfFaradays);
        temp = new double[countOfFaradays];
        Arrays.fill(temp, 3.0);
        System.arraycopy(temp, 0, parametersModelTypeFlags, countOfLogRatios + countOfCycles, countOfFaradays);

        parametersModel_xx0[countOfRows - 1] = singleBlockInitialModelRecord_initial.detectorFaradayGain();
        parametersModelTypeFlags[countOfRows - 1] = 4;

        /*
        if strcmp(oper(1:3),'cha')
            if ~allflag
                if strcmp(oper,'changer')
                    nind = randi(Niso-1);
                elseif strcmp(oper,'changeI')
                    nind = Niso+randi(sum(Ncycle));
                elseif strcmp(oper,'changebl')
                    nind = Niso + sum(Ncycle) + randi(Nfar);
                elseif strcmp(oper,'changedfg')
                    nind = Niso + sum(Ncycle) + Nfar + randi(Ndf);
                end
                if adaptflag
                    delx = sqrt(xcov(nind,nind))*randn(1); %mvnrnd(zeros(1),xcov(nind,nind));
                else
                    delx = ps0diag(nind)*randn(1);
                end

                xx =  xx0;
                xx(nind) = xx(nind) + delx;
                inprior = xx<=priormax & xx>=priormin;
                xx(~inprior) = xx0(~inprior);
            else
                %VARY ALL AT A TIME
                delx = delx_adapt;
                xx =  xx0 + delx;
                inprior = xx<=priormax & xx>=priormin;
                xx(~inprior) = xx0(~inprior);
            end (if ~allflag)

            x2.lograt = xx(xind==1);
            for ii=1:Nblock
                x2.I{ii} = xx(xind==(1+ii));
            end
            x2.BL = xx(xind==(2+Nblock));
            x2.DFgain = xx(xind==(3+Nblock));
            x2.sig = x.sig;

        elseif strcmp(oper(1:3),'noi')    %CHANGE NOISE
            nind = randi(length(x.BL)); %Just for the faradays
            x2=x;
            delx=psig.sig*randn(1);
            if x2.sig(nind) + delx >= prior.sig(1) && x2.sig(nind) + delx <= prior.sig(2)
                x2.sig(nind) = x2.sig(nind)+delx;
            else
                delx=0;
            end
         */

        SingleBlockModelRecord singleBlockInitialModelRecord_initial2 = null;

        RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
        randomDataGenerator.reSeedSecure();

        int nInd = 0;
        double randomSigma = 1.0;
        double[] signalNoiseSigmaUpdated = singleBlockInitialModelRecord_initial.signalNoiseSigma().clone();
        double[] parametersModel_updated = parametersModel_xx0.clone();

        boolean noiseFlag = false;
        if (!allFlag) {
            double deltaX;
            switch (operation) {
                case "changer":
                    nInd = randomDataGenerator.nextInt(1, countOfLogRatios) - 1;
                    break;
                case "changeI":
                    nInd = countOfLogRatios + randomDataGenerator.nextInt(1, countOfCycles) - 1;
                    break;
                case "changebl":
                    nInd = countOfLogRatios + countOfCycles + randomDataGenerator.nextInt(1, countOfFaradays) - 1;
                    break;
                case "changedfg":
                    nInd = countOfLogRatios + countOfCycles + countOfFaradays + randomDataGenerator.nextInt(1, countOfNonFaradays) - 1;
                    break;
                case "noise":
                    noiseFlag = true;
                    nInd = randomDataGenerator.nextInt(1, singleBlockInitialModelRecord_initial.baselineMeansArray().length) - 1;

                    deltaX = psigRecord.psigSignalNoiseFaraday() * randomDataGenerator.nextGaussian(0.0, randomSigma);
                    double testDelta = signalNoiseSigmaUpdated[nInd] + deltaX;
                    if ((testDelta >= priorRecord.priorSignalNoiseFaraday()[0][0])
                            &&
                            (testDelta <= priorRecord.priorSignalNoiseFaraday()[0][1])) {
                        signalNoiseSigmaUpdated[nInd] = testDelta;
                    }

                    singleBlockInitialModelRecord_initial2 = new SingleBlockModelRecord(
                            singleBlockInitialModelRecord_initial.baselineMeansArray(),
                            singleBlockInitialModelRecord_initial.baselineStandardDeviationsArray(),
                            singleBlockInitialModelRecord_initial.detectorFaradayGain(),
                            singleBlockInitialModelRecord_initial.mapDetectorOrdinalToFaradayIndex(),
                            singleBlockInitialModelRecord_initial.logRatios(),
                            signalNoiseSigmaUpdated,
                            singleBlockInitialModelRecord_initial.dataArray(),
                            singleBlockInitialModelRecord_initial.dataWithNoBaselineArray(),
                            singleBlockInitialModelRecord_initial.dataSignalNoiseArray(),
                            singleBlockInitialModelRecord_initial.I0(),
                            singleBlockInitialModelRecord_initial.intensities(),
                            singleBlockInitialModelRecord_initial.faradayCount(),
                            singleBlockInitialModelRecord_initial.isotopeCount()
                    );
                    break;
            }
            if (!noiseFlag) {
                if (adaptiveFlag) {
                    deltaX = StrictMath.sqrt(xDataCovariance[nInd][nInd]) * randomDataGenerator.nextGaussian(0.0, randomSigma);
                } else {
                    deltaX = ps0DiagArray[nInd] * randomDataGenerator.nextGaussian(0.0, randomSigma);
                }

                parametersModel_updated = parametersModel_xx0.clone();
                double changed = parametersModel_updated[nInd] + deltaX;
                if ((changed <= priorMaxArray[nInd] && (changed >= priorMinArray[nInd]))) {
                    parametersModel_updated[nInd] = changed;
                }
            }
        } else {
            // VARY ALL AT A TIME
            // PhysicalStore<Double> delx = storeFactory.columns(delx_adapt.clone());
            // PhysicalStore<Double> xxStore = storeFactory.column(xx0.clone());
            // xx = xxStore.add(delx).toRawCopy1D();
            parametersModel_updated = storeFactory.column(parametersModel_xx0.clone()).add(storeFactory.columns(delx_adapt.clone())).toRawCopy1D();
            for (int row = 0; row < parametersModel_updated.length; row++) {
                if ((parametersModel_updated[row] > priorMaxArray[row] || (parametersModel_updated[row] < priorMinArray[row]))) {
                    // restore values if new values are out of prior range
                    parametersModel_updated[row] = parametersModel_xx0[row];
                }
            }
        }

        if (!noiseFlag) {
            List<Double> updatedLogRatioList = new ArrayList<>();
            List<Double> updatedBlockintensitiesListII = new ArrayList<>();
            List<Double> updatedBaselineMeansList = new ArrayList<>();
            double updatedDFGain = 0.0;

            for (int row = 0; row < parametersModel_updated.length; row++) {
                if (1 == parametersModelTypeFlags[row]) {
                    updatedLogRatioList.add(parametersModel_updated[row]);
                }
                if (2 == parametersModelTypeFlags[row]) {
                    updatedBlockintensitiesListII.add(parametersModel_updated[row]);
                }

                if (3 == parametersModelTypeFlags[row]) {
                    updatedBaselineMeansList.add(parametersModel_updated[row]);
                }
                if (4 == parametersModelTypeFlags[row]) {
                    updatedDFGain = parametersModel_updated[row];
                }
            }
            double[] updatedLogRatio = updatedLogRatioList.stream().mapToDouble(d -> d).toArray();
            double[] updatedBaselineMeans = updatedBaselineMeansList.stream().mapToDouble(d -> d).toArray();
            double[] updatedBlockIntensities_I0 = updatedBlockintensitiesListII.stream().mapToDouble(d -> d).toArray();

            singleBlockInitialModelRecord_initial2 = new SingleBlockModelRecord(
                    updatedBaselineMeans,
                    singleBlockInitialModelRecord_initial.baselineStandardDeviationsArray().clone(),
                    updatedDFGain,
                    singleBlockInitialModelRecord_initial.mapDetectorOrdinalToFaradayIndex(),
                    updatedLogRatio,
                    singleBlockInitialModelRecord_initial.signalNoiseSigma().clone(),
                    singleBlockInitialModelRecord_initial.dataArray().clone(),
                    singleBlockInitialModelRecord_initial.dataWithNoBaselineArray().clone(),
                    singleBlockInitialModelRecord_initial.dataSignalNoiseArray().clone(),
                    updatedBlockIntensities_I0,
                    singleBlockInitialModelRecord_initial.intensities(),
                    singleBlockInitialModelRecord_initial.faradayCount(),
                    singleBlockInitialModelRecord_initial.isotopeCount()
            );
        }
        return singleBlockInitialModelRecord_initial2;
    }

    public record UpdatedCovariancesRecord(
            double[][] dataCov,
            double[] dataMean
    ) {

    }
}