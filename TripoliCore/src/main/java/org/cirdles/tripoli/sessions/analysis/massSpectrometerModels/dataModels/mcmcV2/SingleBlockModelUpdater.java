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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmcV2;

import jama.Matrix;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author James F. Bowring
 */
public enum SingleBlockModelUpdater {
    ;

    public static String randomOperation(boolean hierFlag) {
        Object[][] notHier = {{40, 60, 80, 100}, {"changeI", "changer", "changebl", "changedfg"}};
        Object[][] hier = {{60, 80, 90, 100, 120}, {"changeI", "changer", "changebl", "changedfg", "noise"}};
//        Object[][] hier = new Object[][]{{400, 440, 520, 540, 600}, {"changeI", "changer", "changebl", "changedfg", "noise"}};

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

    static UpdatedCovariancesRecord updateMeanCovMS(
            SingleBlockModelRecord singleBlockModelRecord,
            double[][] dataModelCov,
            double[] dataModelMean,
            List<EnsemblesStoreV2.EnsembleRecord> ensembleRecordsList,
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
        // Oct 2022 per email from Noah, eliminate the iden/iden ratio to guarantee positive definite  covariance matrix >> isotope count - 1
        int countOfLogRatios = singleBlockModelRecord.logRatios().length;
        int sumOfCycleCounts = singleBlockModelRecord.I0().length;
        int countOfFaradays = singleBlockModelRecord.faradayCount();
        int countOfNonFaradays = 1;

        int dataEntryCount = countOfLogRatios + sumOfCycleCounts + countOfFaradays + countOfNonFaradays;
        double[] dataMean;
        double[][] dataCov;
        Covariance cov2 = new Covariance();
        if (iterFlag) {
            // todo: currently iterFlag is always false
            dataMean = null;
            dataCov = null;

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
            PhysicalStore<Double> totalsByRow = storeFactory.make(dataEntryCount, 1);
            PhysicalStore<Double> enso = storeFactory.make(dataEntryCount, modelCount);


            for (int modelIndex = 0; modelIndex < modelCount; modelIndex++) {
                EnsemblesStoreV2.EnsembleRecord ensembleRecord = ensembleRecordsList.get(modelIndex + countOfNewModels - 1);
                int row = 0;
                for (int logRatioIndex = 0; logRatioIndex < countOfLogRatios; logRatioIndex++) {
                    enso.set(row, modelIndex, ensembleRecord.logRatios()[logRatioIndex]);
                    totalsByRow.set(row, 0, totalsByRow.get(row, 0) + ensembleRecord.logRatios()[logRatioIndex]);
                    row++;
                }

                for (int intensityIndex = 0; intensityIndex < singleBlockModelRecord.I0().length; intensityIndex++) {
                    enso.set(row, modelIndex, ensembleRecord.intensities()[intensityIndex]);
                    totalsByRow.set(row, 0, totalsByRow.get(row, 0) + ensembleRecord.intensities()[intensityIndex]);
                    row++;
                }

                for (int baseLineIndex = 0; baseLineIndex < countOfFaradays; baseLineIndex++) {
                    enso.set(row, modelIndex, ensembleRecord.baseLine()[baseLineIndex]);
                    totalsByRow.set(row, 0, totalsByRow.get(row, 0) + ensembleRecord.baseLine()[baseLineIndex]);
                    row++;
                }

                enso.set(row, modelIndex, ensembleRecordsList.get(modelIndex + countOfNewModels - 1).dfGain());
                totalsByRow.set(row, 0, totalsByRow.get(row, 0) + ensembleRecord.dfGain());
            }

            for (int i = 0; i < totalsByRow.getRowDim(); i++) {
                totalsByRow.set(i, 0, totalsByRow.get(i, 0) / modelCount);
            }

            dataMean = totalsByRow.transpose().toRawCopy1D();

            cov2 = new Covariance(enso.transpose().toRawCopy2D());
            Matrix a = new Matrix(cov2.getCovarianceMatrix().getData());
            if (0.0 == a.det()) {
                for (int i = 0; i < a.getRowDimension(); i++) {
                    if (0.0 == a.get(i, i)) {
                        System.out.print("  >" + i);
                    }
                }
            }
        }
        return new UpdatedCovariancesRecord(cov2.getCovarianceMatrix().getData(), dataMean);
    }

    public record UpdatedCovariancesRecord(
            double[][] dataCov,
            double[] dataMean
    ) {

    }
}