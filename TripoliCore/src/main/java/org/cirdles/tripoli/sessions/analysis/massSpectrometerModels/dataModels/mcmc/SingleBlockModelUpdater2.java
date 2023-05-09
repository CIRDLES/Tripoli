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
import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;

/**
 * @author James F. Bowring
 */
public class SingleBlockModelUpdater2 {

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
    private static int countOfLogRatios;
    private static int countOfCycles;
    private static int countOfFaradays;
    private static int countOfTotalModelParameters;

    static {
        operations.add("changer");
        operations.add("changeI");
        operations.add("changedfg");
        operations.add("changebl");
        operations.add("noise");
    }

    SingleBlockModelUpdater2() {
    }

    /**
     * Randomly generate next model operation, with or without hierarchical step
     *
     * @param hierFlag Hierarchical = true
     * @return Random operation by name
     */
    synchronized String randomOperMS(boolean hierFlag) {
        Object[][] notHier = {{40, 60, 80, 100}, {"changeI", "changer", "changebl", "changedfg"}};
        Object[][] hier = {{60, 80, 90, 100, 120}, {"changeI", "changer", "changebl", "changedfg", "noise"}};

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

    // function  [x2,delx,xcov] = UpdateMSv2(oper,x,psig,prior,ensemble,xcov,delx_adapt,adaptflag,allflag)
    synchronized SingleBlockModelRecord updateMSv2(
            String operation,
            SingleBlockModelRecord singleBlockInitialModelRecord_initial,
            ProposedModelParameters.ProposalSigmasRecord proposalSigmasRecord,
            ProposedModelParameters.ProposalRangesRecord proposalRangesRecord,
            double[][] xDataCovariance,
            double[] delx_adapt,
            boolean adaptiveFlag,
            boolean allFlag) {
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

        double[] xx0 = new double[countOfTotalModelParameters];
        int[] xInd = new int[countOfTotalModelParameters];

        System.arraycopy(singleBlockInitialModelRecord_initial.logRatios(), 0, xx0, 0, countOfLogRatios);
        Arrays.fill(xInd, 1);

        System.arraycopy(singleBlockInitialModelRecord_initial.I0(), 0, xx0, countOfLogRatios, countOfCycles);
        int[] temp = new int[countOfCycles];
        Arrays.fill(temp, 2);
        System.arraycopy(temp, 0, xInd, countOfLogRatios, countOfCycles);

        System.arraycopy(singleBlockInitialModelRecord_initial.baselineMeansArray(), 0, xx0, countOfLogRatios + countOfCycles, countOfFaradays);
        temp = new int[countOfFaradays];
        Arrays.fill(temp, 3);
        System.arraycopy(temp, 0, xInd, countOfLogRatios + countOfCycles, countOfFaradays);

        xx0[countOfTotalModelParameters - 1] = singleBlockInitialModelRecord_initial.detectorFaradayGain();
        xInd[countOfTotalModelParameters - 1] = 4;

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
        double[] updatedLogRatios = new double[countOfLogRatios];
        double[] updatedIntensities = new double[countOfCycles];
        double[] updatedBaselineMeans = new double[countOfFaradays];
        double updatedFaradayGain = 0.0;

        if (operation.startsWith("cha")) {
            if (allFlag) {
                // VARY ALL AT A TIME
                /*
                    delx = delx_adapt;
                    xx =  xx0 + delx;
                    inprior = xx<=priormax & xx>=priormin;
                    xx(~inprior) = xx0(~inprior);

                    x2.lograt = xx(xind==1);
                    for ii=1:Nblock
                        x2.I{ii} = xx(xind==(1+ii));
                    end
                    x2.BL = xx(xind==(2+Nblock));
                    x2.DFgain = xx(xind==(3+Nblock));
                    x2.sig = x.sig;
                */
                double[] delX = delx_adapt.clone();
                double[] xx = new double[delX.length];

                for (int row = 0; row < xx.length; row++) {
                    xx[row] = xx0[row] + delx_adapt[row];

                    switch (xInd[row]) {
                        case 1 -> {
                            if ((xx[row] > proposalRangesRecord.priorLogRatio()[0][1]) || (xx[row] < proposalRangesRecord.priorLogRatio()[0][0])) {
                                xx[row] = xx0[row];
                            }
                            updatedLogRatios[row] = xx[row];
                        }
                        case 2 -> {
                            if ((xx[row] > proposalRangesRecord.priorIntensity()[0][1]) || (xx[row] < proposalRangesRecord.priorIntensity()[0][0])) {
                                xx[row] = xx0[row];
                            }
                            updatedIntensities[row - countOfLogRatios] = xx[row];
                        }
                        case 3 -> {
                            if ((xx[row] > proposalRangesRecord.priorBaselineFaraday()[0][1]) || (xx[row] < proposalRangesRecord.priorBaselineFaraday()[0][0])) {
                                xx[row] = xx0[row];
                            }
                            updatedBaselineMeans[row - countOfLogRatios - countOfCycles] = xx[row];
                        }
                        case 4 -> {
                            if ((xx[row] > proposalRangesRecord.priorDFgain()[0][1]) || (xx[row] < proposalRangesRecord.priorDFgain()[0][0])) {
                                xx[row] = xx0[row];
                            }
                            updatedFaradayGain = xx[row];
                        }
                        default -> {
                        }
                    }
                }


                singleBlockInitialModelRecord_initial2 = new SingleBlockModelRecord(
                        singleBlockInitialModelRecord_initial.blockNumber(),
                        updatedBaselineMeans,
                        singleBlockInitialModelRecord_initial.baselineStandardDeviationsArray().clone(),
                        updatedFaradayGain,
                        singleBlockInitialModelRecord_initial.mapDetectorOrdinalToFaradayIndex(),
                        updatedLogRatios,
                        singleBlockInitialModelRecord_initial.signalNoiseSigma().clone(),
                        singleBlockInitialModelRecord_initial.dataArray().clone(),
                        singleBlockInitialModelRecord_initial.dataWithNoBaselineArray().clone(),
                        singleBlockInitialModelRecord_initial.dataSignalNoiseArray().clone(),
                        updatedIntensities,
                        singleBlockInitialModelRecord_initial.intensities(),
                        singleBlockInitialModelRecord_initial.faradayCount(),
                        singleBlockInitialModelRecord_initial.isotopeCount()
                );

            }
        } else {
            // noise case
            /*
                nind = randi(length(x.BL)); %Just for the faradays
                x2=x;
                delx=psig.sig*randn(1);

                if x2.sig(nind) + delx >= prior.sig(1) && x2.sig(nind) + delx <= prior.sig(2)
                    x2.sig(nind) = x2.sig(nind)+delx;
                else
                    delx=0;
                end
             */

            double randomSigma = 1.0;
            SplittableRandom splittableRandom = new SplittableRandom();
            double[] updatedSignalNoiseSigma = singleBlockInitialModelRecord_initial.signalNoiseSigma().clone();
            int nInd = splittableRandom.nextInt(0, countOfFaradays - 1);
            double deltaX = proposalSigmasRecord.psigSignalNoiseFaraday() * splittableRandom.nextGaussian(0.0, randomSigma);

            double testDelta = updatedSignalNoiseSigma[nInd] + deltaX;
            if ((testDelta >= proposalRangesRecord.priorSignalNoiseFaraday()[0][0])
                    &&
                    (testDelta <= proposalRangesRecord.priorSignalNoiseFaraday()[0][1])) {
                updatedSignalNoiseSigma[nInd] = testDelta;
            }
            singleBlockInitialModelRecord_initial2 = new SingleBlockModelRecord(
                    singleBlockInitialModelRecord_initial.blockNumber(),
                    singleBlockInitialModelRecord_initial.baselineMeansArray(),
                    singleBlockInitialModelRecord_initial.baselineStandardDeviationsArray(),
                    singleBlockInitialModelRecord_initial.detectorFaradayGain(),
                    singleBlockInitialModelRecord_initial.mapDetectorOrdinalToFaradayIndex(),
                    singleBlockInitialModelRecord_initial.logRatios(),
                    updatedSignalNoiseSigma,
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

    public synchronized void buildPriorLimits(
            SingleBlockModelRecord singleBlockModelRecord,
            ProposedModelParameters.ProposalSigmasRecord proposalSigmasRecord,
            ProposedModelParameters.ProposalRangesRecord proposalRangesRecord) {

        countOfLogRatios = singleBlockModelRecord.logRatios().length;
        countOfCycles = singleBlockModelRecord.I0().length;
        countOfFaradays = singleBlockModelRecord.faradayCount();
        int countOfPhotoMultipliers = 1;
        countOfTotalModelParameters = countOfLogRatios + countOfCycles + countOfFaradays + countOfPhotoMultipliers;

        double[] ps0DiagArray = new double[countOfTotalModelParameters];
        double[] priorMinArray = new double[countOfTotalModelParameters];
        double[] priorMaxArray = new double[countOfTotalModelParameters];

        for (int logRatioIndex = 0; logRatioIndex < countOfLogRatios; logRatioIndex++) {
            ps0DiagArray[logRatioIndex] = proposalSigmasRecord.psigLogRatio();
            priorMinArray[logRatioIndex] = proposalRangesRecord.priorLogRatio()[0][0];
            priorMaxArray[logRatioIndex] = proposalRangesRecord.priorLogRatio()[0][1];
        }

        for (int cycleIndex = 0; cycleIndex < countOfCycles; cycleIndex++) {
            ps0DiagArray[cycleIndex + countOfLogRatios] = proposalSigmasRecord.psigIntensity();
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


    synchronized UpdatedCovariancesRecord updateMeanCovMS2(
            SingleBlockModelRecord singleBlockModelRecord,
            double[][] dataModelCov,
            double[] dataModelMean,
            int countOfNewModels) {
        // function [xmean,xcov] = UpdateMeanCovMS(x,xmean,xcov,m)
        /*
            xx = x.lograt;
            for ii=1:Nblock
                xx = [xx; x.I{ii}];
            end
            xx = [xx; x.BL(1:Nfar)];
            xx = [xx; x.DFgain];

            xmeantmp = xmean;
            xmean = xmeantmp + (xx-xmeantmp)/m;

            xcov = xcov*(m-1)/m + (m-1)/m^2*(xx-xmean)*(xx-xmeantmp)';
         */
        double[] xx = new double[countOfTotalModelParameters];
        System.arraycopy(singleBlockModelRecord.logRatios(), 0, xx, 0, countOfLogRatios);
        System.arraycopy(singleBlockModelRecord.I0(), 0, xx, countOfLogRatios, countOfCycles);
        System.arraycopy(singleBlockModelRecord.baselineMeansArray(), 0, xx, countOfLogRatios + countOfCycles, countOfFaradays);
        xx[countOfTotalModelParameters - 1] = singleBlockModelRecord.detectorFaradayGain();

        double[] xMeanTemp = new double[dataModelMean.length];
        double[] diffXwithXmean = new double[dataModelMean.length];
        double[] diffXwithXmeanTemp = new double[dataModelMean.length];
        for (int row = 0; row < xMeanTemp.length; row++) {
            xMeanTemp[row] = dataModelMean[row] + (xx[row] - dataModelMean[row]) / countOfNewModels;
            diffXwithXmean[row] = xx[row] - dataModelMean[row];
            diffXwithXmeanTemp[row] = xx[row] - xMeanTemp[row];
        }

        Matrix xCovM = new Matrix(dataModelCov);
        Matrix diffXwithXmeanMatrix = new Matrix(diffXwithXmean, diffXwithXmean.length);
        Matrix diffXwithXmeanTempMatrix = new Matrix(diffXwithXmeanTemp, diffXwithXmeanTemp.length);
        Matrix updated_xCovM =
                xCovM.times(((countOfNewModels - 1) / countOfNewModels))
                        .plus(diffXwithXmeanMatrix
                                .times((countOfNewModels - 1) / Math.pow(countOfNewModels, 2))
                                .times(diffXwithXmeanTempMatrix.transpose()));

        return new UpdatedCovariancesRecord(updated_xCovM.getArray(), xMeanTemp);
    }

    public record UpdatedCovariancesRecord(
            double[][] dataCov,
            double[] dataMean
    ) {

    }
}