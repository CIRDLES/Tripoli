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

// import jama.Matrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.ojalgo.data.DataProcessors;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author James F. Bowring
 */
public class DataModelUpdater {

    public static List<String> operations = new ArrayList<>();

    static {
        operations.add("changer");
        operations.add("changeI");
        operations.add("changedfg");
        operations.add("changebl");
        operations.add("noise");
    }

    // function  [x2,delx,xcov] = UpdateMSv2(oper,x,psig,prior,ensemble,xcov,delx_adapt,adaptflag,allflag)
    static DataModellerOutputRecord updateMSv2(
            String operation,
            DataModellerOutputRecord dataModelInit,
            DataModelDriverExperiment.PsigRecord psigRecord,
            DataModelDriverExperiment.PriorRecord priorRecord,
            // Matrix xDataCovariance,
            double[][] xDataCovariance,
            // Matrix delx_adapt,
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
        // int countOfIsotopes = dataModelInit.logratios().getRowDimension();
        int countOfIsotopes = dataModelInit.logratios().length;
        // int countOfBlocks = dataModelInit.blockIntensities().getColumnDimension();
        // todo check if this should be 2d array
        int countOfBlocks = storeFactory.columns(dataModelInit.blockIntensities()).getColDim();
        int[] nCycle = new int[countOfBlocks];
        int sumOfCycleCounts = 0;
        for (int blockIndex = 0; blockIndex < countOfBlocks; blockIndex++) {
            // nCycle[blockIndex] = dataModelInit.blockIntensities().getRowDimension();
            nCycle[blockIndex] = dataModelInit.blockIntensities().length;
            sumOfCycleCounts += nCycle[blockIndex];
        }
        // int countOfFaradays = dataModelInit.baselineMeans().getRowDimension();
        int countOfFaradays = dataModelInit.baselineMeans().length;
        int countOfNonFaradays = 1;

        int countOfRows = countOfIsotopes + sumOfCycleCounts + countOfFaradays + countOfNonFaradays;

        double[] ps0DiagArray = new double[countOfRows];
        double[] priorMinArray = new double[countOfRows];
        double[] priorMaxArray = new double[countOfRows];

        for (int isotopeIndex = 0; isotopeIndex < countOfIsotopes; isotopeIndex++) {
            ps0DiagArray[isotopeIndex] = psigRecord.psigLogRatio();
            // priorMinArray[isotopeIndex] = priorRecord.priorLogRatio().get(0, 0);
            priorMinArray[isotopeIndex] = priorRecord.priorLogRatio()[0][0];
            // priorMaxArray[isotopeIndex] = priorRecord.priorLogRatio().get(0, 1);
            priorMaxArray[isotopeIndex] = priorRecord.priorLogRatio()[0][1];
        }
        priorMinArray[countOfIsotopes - 1] = 0.0;
        priorMaxArray[countOfIsotopes - 1] = 0.0;

        for (int cycleIndex = 0; cycleIndex < sumOfCycleCounts; cycleIndex++) {
            ps0DiagArray[cycleIndex + countOfIsotopes] = psigRecord.psigIntensityPercent();
            // priorMinArray[cycleIndex + countOfIsotopes] = priorRecord.priorIntensity().get(0, 0);
            priorMinArray[cycleIndex + countOfIsotopes] = priorRecord.priorIntensity()[0][0];
            // priorMaxArray[cycleIndex + countOfIsotopes] = priorRecord.priorIntensity().get(0, 1);
            priorMaxArray[cycleIndex + countOfIsotopes] = priorRecord.priorIntensity()[0][1];
        }

        for (int faradayIndex = 0; faradayIndex < countOfIsotopes; faradayIndex++) {
            ps0DiagArray[faradayIndex + countOfIsotopes + sumOfCycleCounts] = psigRecord.psigBaselineFaraday();
            // priorMinArray[faradayIndex + countOfIsotopes + sumOfCycleCounts] = priorRecord.priorBaselineFaraday().get(0, 0);
            priorMinArray[faradayIndex + countOfIsotopes + sumOfCycleCounts] = priorRecord.priorBaselineFaraday()[0][0];
            // priorMaxArray[faradayIndex + countOfIsotopes + sumOfCycleCounts] = priorRecord.priorBaselineFaraday().get(0, 1);
            priorMaxArray[faradayIndex + countOfIsotopes + sumOfCycleCounts] = priorRecord.priorBaselineFaraday()[0][1];
        }
        ps0DiagArray[countOfRows - 1] = psigRecord.psigDFgain();
        // priorMinArray[countOfRows - 1] = priorRecord.priorDFgain().get(0, 0);
        priorMinArray[countOfRows - 1] = priorRecord.priorDFgain()[0][0];
        // priorMaxArray[countOfRows - 1] = priorRecord.priorDFgain().get(0, 1);
        priorMaxArray[countOfRows - 1] = priorRecord.priorDFgain()[0][1];

        // Matrix ps0Diag = new Matrix(ps0DiagArray, ps0DiagArray.length);
        // Matrix priorMin = new Matrix(priorMinArray, priorMinArray.length);
        // Matrix priorMax = new Matrix(priorMaxArray, priorMaxArray.length);

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

        double[] xx0Array = new double[countOfRows];
        double[] xIndArray = new double[countOfRows];
        // System.arraycopy(dataModelInit.logratios().getColumnPackedCopy(), 0, xx0Array, 0, countOfIsotopes);
        System.arraycopy(dataModelInit.logratios(), 0, xx0Array, 0, countOfIsotopes);
        Arrays.fill(xIndArray, 1.0);
        // todo: only good for one block
        for (int blockIndex = 0; blockIndex < countOfBlocks; blockIndex++) {
            // System.arraycopy(dataModelInit.blockIntensities().getColumnPackedCopy(), 0, xx0Array, countOfIsotopes, nCycle[blockIndex]);
            System.arraycopy(dataModelInit.blockIntensities(), 0, xx0Array, countOfIsotopes, nCycle[blockIndex]);
            // System.arraycopy((new Matrix(nCycle[blockIndex], 1, blockIndex + 2)).getColumnPackedCopy(), 0, xIndArray, countOfIsotopes, nCycle[blockIndex]);
            double[] temp = new double[nCycle[blockIndex]];
            Arrays.fill(temp, blockIndex + 2);
            System.arraycopy(temp, 0, xIndArray, countOfIsotopes, nCycle[blockIndex]);
        }
        // System.arraycopy(dataModelInit.baselineMeans().getColumnPackedCopy(), 0, xx0Array, countOfIsotopes + nCycle[0], countOfFaradays);
        System.arraycopy(dataModelInit.baselineMeans(), 0, xx0Array, countOfIsotopes + nCycle[0], countOfFaradays);
        // System.arraycopy((new Matrix(countOfFaradays, 1, 2.0 + countOfBlocks)).getColumnPackedCopy(), 0, xIndArray, countOfIsotopes + nCycle[0], countOfFaradays);
        double[] temp = new double[countOfFaradays];
        Arrays.fill(temp, 2.0 + countOfBlocks);
        System.arraycopy(temp, 0, xIndArray, countOfIsotopes + nCycle[0], countOfFaradays);

        xx0Array[countOfRows - 1] = dataModelInit.dfGain();
        xIndArray[countOfRows - 1] = 3.0 + countOfBlocks;

        // Matrix xx0 = new Matrix(xx0Array, xx0Array.length);
        // Matrix xInd = new Matrix(xIndArray, xIndArray.length);

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

        DataModellerOutputRecord dataModelInit2 = null;

        RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
        randomDataGenerator.reSeedSecure();

        int nInd = 0;
        int randomSigma = 1;
        // Matrix x2SignalNoise = (Matrix) dataModelInit.signalNoise().clone();
        double[] x2SignalNoise = dataModelInit.signalNoise().clone();
        // Matrix xx = (Matrix) xx0.clone();
        double[] xx = xx0Array.clone();

        boolean noiseFlag = false;
        if (!allFlag) {
            double delX;
            switch (operation) {
                case "changer":
                    nInd = randomDataGenerator.nextInt(1, countOfIsotopes) - 1;
                    break;
                case "changeI":
                    nInd = countOfIsotopes + randomDataGenerator.nextInt(1, sumOfCycleCounts) - 1;
                    break;
                case "changebl":
                    nInd = countOfIsotopes + sumOfCycleCounts + randomDataGenerator.nextInt(1, countOfFaradays) - 1;
                    break;
                case "changedfg":
                    nInd = countOfIsotopes + sumOfCycleCounts + countOfFaradays;// + randomDataGenerator.nextInt(0, 1);
                    break;
                case "noise":
                    noiseFlag = true;
                    // nInd = randomDataGenerator.nextInt(1, dataModelInit.baselineMeans().getRowDimension()) - 1;
                    nInd = randomDataGenerator.nextInt(1, dataModelInit.baselineMeans().length) - 1;

                    delX = psigRecord.psigSignalNoiseFaraday() * randomDataGenerator.nextGaussian(0, randomSigma);
                    // double testDelta = x2SignalNoise.get(nInd, 0) + delX;
                    double testDelta = x2SignalNoise[nInd] + delX;
                    // if ((testDelta >= priorRecord.priorSignalNoiseFaraday().get(0, 0))
                    if ((testDelta >= priorRecord.priorSignalNoiseFaraday()[0][0])
                            &&
                            // (testDelta <= priorRecord.priorSignalNoiseFaraday().get(0, 1))) {
                            (testDelta <= priorRecord.priorSignalNoiseFaraday()[0][1])) {
                        // x2SignalNoise.set(nInd, 0, testDelta);
                        x2SignalNoise[nInd] =  testDelta;
                    }

                    dataModelInit2 = new DataModellerOutputRecord(
                            // (Matrix) dataModelInit.baselineMeans().clone(),
                            dataModelInit.baselineMeans().clone(),
                            // (Matrix) dataModelInit.baselineStandardDeviations().clone(),
                            dataModelInit.baselineStandardDeviations().clone(),
                            dataModelInit.dfGain(),
                            // (Matrix) dataModelInit.logratios().clone(),
                            dataModelInit.logratios().clone(),
                            x2SignalNoise,
                            // (Matrix) dataModelInit.dataArray().clone(),
                            dataModelInit.dataArray().clone(),
                            // (Matrix) dataModelInit.blockIntensities().clone(),
                            dataModelInit.blockIntensities().clone(),
                            dataModelInit.intensityPerBlock() //? deep clone??
                    );
            }
            if (!noiseFlag) {
                if (adaptiveFlag) {
                    // delX = StrictMath.sqrt(xDataCovariance.get(nInd, nInd)) * randomDataGenerator.nextGaussian(0, randomSigma);
                    delX = StrictMath.sqrt(xDataCovariance[nInd][nInd]) * randomDataGenerator.nextGaussian(0, randomSigma);
                } else {
                    // delX = ps0Diag.get(nInd, 0) * randomDataGenerator.nextGaussian(0, randomSigma);
                    delX = ps0DiagArray[nInd] * randomDataGenerator.nextGaussian(0, randomSigma);
                }

                // xx = (Matrix) xx0.clone();
                xx = xx0Array.clone();
                // double changed = xx.get(nInd, 0) + delX;
                double changed = xx[nInd] + delX;
                // if ((changed <= priorMax.get(nInd, 0) && (changed >= priorMin.get(nInd, 0)))) {
                if ((changed <= priorMaxArray[nInd] && (changed >= priorMinArray[nInd]))) {
                    // xx.set(nInd, 0, changed);
                    xx[nInd] = changed;
                }
            }
        } else {
            // VARY ALL AT A TIME
            /*
            Matrix delx = (Matrix) delx_adapt.clone();
            xx = (Matrix) xx0.clone();
            xx = xx.plus(delx);
            for (int row = 0; row < xx.getRowDimension(); row++) {
                if ((xx.get(row, 0) > priorMax.get(row, 0) || (xx.get(row, 0) < priorMin.get(row, 0)))) {
                    xx.set(row, 0, xx0.get(row, 0));
                }
            }
            */
            PhysicalStore<Double> delx= storeFactory.column(delx_adapt.clone());
            PhysicalStore<Double> xxStore = storeFactory.column(xx0Array.clone());
            xx = xxStore.add(delx).toRawCopy1D();
            for (int row = 0; row < xx.length; row++) {
                if ((xx[row] > priorMaxArray[row] || (xx[row] < priorMinArray[row]))) {
                    xx[row] = xx0Array[row];
                }
            }
        }

        if (!noiseFlag) {
            // todo: only 1 block here for now
            List<Double> x2LogRatioList = new ArrayList<>();
            List<Double> x2BlockIntensitiesList = new ArrayList<>();
            List<Double> x2BaselineMeansList = new ArrayList<>();
            double x2DFGain = 0.0;
            /*
            for (int row = 0; row < xx.getRowDimension(); row++) {
                if (xInd.get(row, 0) == 1) {
                    x2LogRatioList.add(xx.get(row, 0));
                }
                if (xInd.get(row, 0) == 2) {
                    x2BlockIntensitiesList.add(xx.get(row, 0));
                }
                if (xInd.get(row, 0) == 2 + countOfBlocks) {
                    x2BaselineMeansList.add(xx.get(row, 0));
                }
                if (xInd.get(row, 0) == 3 + countOfBlocks) {
                    x2DFGain = xx.get(row, 0);
                }
            }
            */
            for (int row = 0; row < xx.length; row++) {
                if (xIndArray[row] == 1) {
                    x2LogRatioList.add(xx[row]);
                }
                if (xIndArray[row] == 2) {
                    x2BlockIntensitiesList.add(xx[row]);
                }
                if (xIndArray[row] == 2 + countOfBlocks) {
                    x2BaselineMeansList.add(xx[row]);
                }
                if (xIndArray[row] == 3 + countOfBlocks) {
                    x2DFGain = xx[row];
                }
            }
            double[] x2LogRatioArray = x2LogRatioList.stream().mapToDouble(d -> d).toArray();
            // Matrix x2LogRatio = new Matrix(x2LogRatioArray, x2LogRatioArray.length);
            // double[] x2BlockIntensitiesArray = x2BlockIntensitiesList.stream().mapToDouble(d -> d).toArray();
            // Matrix x2BlockIntensities = new Matrix(x2BlockIntensitiesArray, x2BlockIntensitiesArray.length);
            MatrixStore<Double> x2BlockIntensities = storeFactory.column(x2BlockIntensitiesList.stream().mapToDouble(d -> d).toArray());
            double[] x2BaselineMeansArray = x2BaselineMeansList.stream().mapToDouble(d -> d).toArray();
            // Matrix x2BaselineMeans = new Matrix(x2BaselineMeansArray, x2BaselineMeansArray.length);

            dataModelInit2 = new DataModellerOutputRecord(
                    // x2BaselineMeans,
                    x2BaselineMeansArray,
                    // (Matrix) dataModelInit.baselineStandardDeviations().clone(),
                    dataModelInit.baselineStandardDeviations(),
                    x2DFGain,
                    // x2LogRatio,
                    x2LogRatioArray,
                    // (Matrix) dataModelInit.signalNoise().clone(),
                    dataModelInit.signalNoise(),
                    // (Matrix) dataModelInit.dataArray().clone(),
                    dataModelInit.dataArray(),
                    x2BlockIntensities.toRawCopy1D(),
                    dataModelInit.intensityPerBlock() // ?deep clone
            );
        }
        return dataModelInit2;
    }

    static UpdatedCovariancesRecord updateMeanCovMS(
            DataModellerOutputRecord dataModelInit,
            // Matrix dataModelCov,
            double[][] dataModelCov,
            // Matrix dataModelMean,
            double[] dataModelMean,
            List<DataModelDriverExperiment.EnsembleRecord> ensembleRecordsList,
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
        // int countOfIsotopes = dataModelInit.logratios().getRowDimension();
        int countOfIsotopes = dataModelInit.logratios().length;
        // int countOfBlocks = dataModelInit.blockIntensities().getColumnDimension();
        // int countOfBlocks = dataModelInit.blockIntensities().length;
        int countOfBlocks = 1;
        int[] nCycle = new int[countOfBlocks];
        int sumOfCycleCounts = 0;
        for (int blockIndex = 0; blockIndex < countOfBlocks; blockIndex++) {
            // nCycle[blockIndex] = dataModelInit.blockIntensities().getRowDimension();
            nCycle[blockIndex] = dataModelInit.blockIntensities().length;
            sumOfCycleCounts += nCycle[blockIndex];
        }
        // int countOfFaradays = dataModelInit.baselineMeans().getRowDimension();
        int countOfFaradays = dataModelInit.baselineMeans().length;
        int countOfNonFaradays = 1;

        int dataEntryCount = countOfIsotopes + sumOfCycleCounts + countOfFaradays + countOfNonFaradays;
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        // Matrix dataMean;
        double[] dataMean;
        // Matrix dataCov;
        double[][] dataCov;
        if (iterFlag) {
            // currently iterFlag is always false
            dataMean = null;
            dataCov = null;
        } else {

            int modelCount = ensembleRecordsList.size() - countOfNewModels + 1;
            // Matrix totalsByRow = new Matrix(dataEntryCount, 1);
            PhysicalStore<Double> totalsByRow = storeFactory.make(dataEntryCount,1);
            // Matrix enso = new Matrix(dataEntryCount, modelCount);
            PhysicalStore<Double> enso = storeFactory.make(dataEntryCount,modelCount);

            /*
            for (int modelIndex = 0; modelIndex < modelCount; modelIndex++) {
                int row = 0;
                enso.set(row, modelIndex, ensembleRecordsList.get(modelIndex + countOfNewModels - 1).logRatios().get(0, 0));
                totalsByRow.set(row, 0, totalsByRow.get(row, 0) + ensembleRecordsList.get(modelIndex + countOfNewModels - 1).logRatios().get(0, 0));
                row++;
                enso.set(row, modelIndex, ensembleRecordsList.get(modelIndex + countOfNewModels - 1).logRatios().get(1, 0));
                totalsByRow.set(row, 0, totalsByRow.get(row, 0) + ensembleRecordsList.get(modelIndex + countOfNewModels - 1).logRatios().get(1, 0));
                row++;
                for (int intensityIndex = 0; intensityIndex < dataModelInit.blockIntensities().getRowDimension(); intensityIndex++) {
                    enso.set(row, modelIndex, ensembleRecordsList.get(modelIndex + countOfNewModels - 1).intensity().get(intensityIndex, 0));
                    totalsByRow.set(row, 0, totalsByRow.get(row, 0) + ensembleRecordsList.get(modelIndex + countOfNewModels - 1).intensity().get(intensityIndex, 0));
                    row++;
                }
                enso.set(row, modelIndex, ensembleRecordsList.get(modelIndex + countOfNewModels - 1).baseLine().get(0, 0));
                totalsByRow.set(row, 0, totalsByRow.get(row, 0) + ensembleRecordsList.get(modelIndex + countOfNewModels - 1).baseLine().get(0, 0));
                row++;
                enso.set(row, modelIndex, ensembleRecordsList.get(modelIndex + countOfNewModels - 1).baseLine().get(1, 0));
                totalsByRow.set(row, 0, totalsByRow.get(row, 0) + ensembleRecordsList.get(modelIndex + countOfNewModels - 1).baseLine().get(1, 0));
                row++;
                enso.set(row, modelIndex, ensembleRecordsList.get(modelIndex + countOfNewModels - 1).dfGain());
                totalsByRow.set(row, 0, totalsByRow.get(row, 0) + ensembleRecordsList.get(modelIndex + countOfNewModels - 1).dfGain());
            }
            for (int i = 0; i < totalsByRow.getRowDimension(); i++) {
                totalsByRow.set(i, 0, totalsByRow.get(i, 0) / modelCount);
            }

            for (int i = 0; i < totalsByRow.getRowDimension(); i++) {
                totalsByRow.set(i, 0, totalsByRow.get(i, 0) / modelCount);
            }
            */
            for (int modelIndex = 0; modelIndex < modelCount; modelIndex++) {
                int row = 0;
                enso.set(row, modelIndex, ensembleRecordsList.get(modelIndex + countOfNewModels - 1).logRatios()[0]);
                totalsByRow.set(row, 0, totalsByRow.get(row, 0) + ensembleRecordsList.get(modelIndex + countOfNewModels - 1).logRatios()[0]);
                row++;
                enso.set(row, modelIndex, ensembleRecordsList.get(modelIndex + countOfNewModels - 1).logRatios()[1]);
                totalsByRow.set(row, 0, totalsByRow.get(row, 0) + ensembleRecordsList.get(modelIndex + countOfNewModels - 1).logRatios()[1]);
                row++;
                for (int intensityIndex = 0; intensityIndex < dataModelInit.blockIntensities().length; intensityIndex++) {
                    enso.set(row, modelIndex, ensembleRecordsList.get(modelIndex + countOfNewModels - 1).intensity()[intensityIndex]);
                    totalsByRow.set(row, 0, totalsByRow.get(row, 0) + ensembleRecordsList.get(modelIndex + countOfNewModels - 1).intensity()[intensityIndex]);
                    row++;
                }
                enso.set(row, modelIndex, ensembleRecordsList.get(modelIndex + countOfNewModels - 1).baseLine()[0]);
                totalsByRow.set(row, 0, totalsByRow.get(row, 0) + ensembleRecordsList.get(modelIndex + countOfNewModels - 1).baseLine()[0]);
                row++;
                enso.set(row, modelIndex, ensembleRecordsList.get(modelIndex + countOfNewModels - 1).baseLine()[1]);
                totalsByRow.set(row, 0, totalsByRow.get(row, 0) + ensembleRecordsList.get(modelIndex + countOfNewModels - 1).baseLine()[1]);
                row++;
                enso.set(row, modelIndex, ensembleRecordsList.get(modelIndex + countOfNewModels - 1).dfGain());
                totalsByRow.set(row, 0, totalsByRow.get(row, 0) + ensembleRecordsList.get(modelIndex + countOfNewModels - 1).dfGain());
            }
            for (int i = 0; i < totalsByRow.getRowDim(); i++) {
                totalsByRow.set(i, 0, totalsByRow.get(i, 0) / modelCount);
            }

            for (int i = 0; i < totalsByRow.getRowDim(); i++) {
                totalsByRow.set(i, 0, totalsByRow.get(i, 0) / modelCount);
            }

            // dataMean = totalsByRow.transpose();
            dataMean = totalsByRow.transpose().toRawCopy1D();

            // double[][] ensoTransposeArray = enso.transpose().getArray();
            // Covariance cov = new Covariance(ensoTransposeArray);
            // RealMatrix covMatrix = cov.getCovarianceMatrix();
            // double[][] covarianceArray = covMatrix.getData();
            // dataCov = new Matrix(covarianceArray);
            dataCov = DataProcessors.covariances(storeFactory, enso.transpose()).toRawCopy2D();
        }
        return new UpdatedCovariancesRecord(dataCov, dataMean);
    }

    public record UpdatedCovariancesRecord(
            // Matrix dataCov,
            double[][] dataCov,
            // Matrix dataMean
            double[] dataMean
    ) {

    }
}