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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author James F. Bowring
 */
public class DataModelUpdater {

    static DataModellerOutputRecord updateMSv2(
            String operation,
            DataModellerOutputRecord dataModelInit,
            DataModelDriverExperiment.PsigRecord psigRecord,
            DataModelDriverExperiment.PriorRecord priorRecord,
            List<DataModelDriverExperiment.EnsembleRecord> ensembleRecordsList,
            Matrix xDataCovariance,
            Matrix delx_adapt,
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

        int count = ensembleRecordsList.size();
        int countOfIsotopes = dataModelInit.logratios().getRowDimension();
        int countOfBlocks = dataModelInit.blockIntensities().getColumnDimension();
        int[] nCycle = new int[countOfBlocks];
        int sumOfCycleCounts = 0;
        for (int blockIndex = 0; blockIndex < countOfBlocks; blockIndex++) {
            nCycle[blockIndex] = dataModelInit.blockIntensities().getRowDimension();
            sumOfCycleCounts += nCycle[blockIndex];
        }
        int countOfFaradays = dataModelInit.baselineMeans().getRowDimension();
        int countOfNonFaradays = 1;

        int countOfRows = countOfIsotopes + sumOfCycleCounts + countOfFaradays + countOfNonFaradays;

        double[] ps0DiagArray = new double[countOfRows];
        double[] priorMinArray = new double[countOfRows];
        double[] priorMaxArray = new double[countOfRows];

        for (int isotopeIndex = 0; isotopeIndex < countOfIsotopes; isotopeIndex++) {
            ps0DiagArray[isotopeIndex] = psigRecord.psigLogRatio();
            priorMinArray[isotopeIndex] = priorRecord.priorLogRatio().get(0, 0);
            priorMaxArray[isotopeIndex] = priorRecord.priorLogRatio().get(0, 1);
        }
        priorMinArray[countOfIsotopes - 1] = 0.0;
        priorMaxArray[countOfIsotopes - 1] = 0.0;

        for (int cycleIndex = 0; cycleIndex < sumOfCycleCounts; cycleIndex++) {
            ps0DiagArray[cycleIndex + countOfIsotopes] = psigRecord.psigIntensityPercent();
            priorMinArray[cycleIndex + countOfIsotopes] = priorRecord.priorIntensity().get(0, 0);
            priorMaxArray[cycleIndex + countOfIsotopes] = priorRecord.priorIntensity().get(0, 1);
        }

        for (int faradayIndex = 0; faradayIndex < countOfIsotopes; faradayIndex++) {
            ps0DiagArray[faradayIndex + countOfIsotopes + sumOfCycleCounts] = psigRecord.psigBaselineFaraday();
            priorMinArray[faradayIndex + countOfIsotopes + sumOfCycleCounts] = priorRecord.priorBaselineFaraday().get(0, 0);
            priorMaxArray[faradayIndex + countOfIsotopes + sumOfCycleCounts] = priorRecord.priorBaselineFaraday().get(0, 1);
        }
        ps0DiagArray[countOfRows - 1] = psigRecord.psigDFgain();
        priorMinArray[countOfRows - 1] = priorRecord.priorDFgain().get(0, 0);
        priorMaxArray[countOfRows - 1] = priorRecord.priorDFgain().get(0, 1);

        Matrix ps0Diag = new Matrix(ps0DiagArray, ps0DiagArray.length);
        Matrix priorMin = new Matrix(priorMinArray, priorMinArray.length);
        Matrix priorMax = new Matrix(priorMaxArray, priorMaxArray.length);

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
        System.arraycopy(dataModelInit.logratios().getColumnPackedCopy(), 0, xx0Array, 0, countOfIsotopes);
        Arrays.fill(xIndArray, 1.0);
        // todo: only good for one block
        for (int blockIndex = 0; blockIndex < countOfBlocks; blockIndex++) {
            System.arraycopy(dataModelInit.blockIntensities().getColumnPackedCopy(), 0, xx0Array, countOfIsotopes, nCycle[blockIndex]);
            System.arraycopy((new Matrix(nCycle[blockIndex], 1, blockIndex + 2)).getColumnPackedCopy(), 0, xIndArray, countOfIsotopes, nCycle[blockIndex]);
        }
        System.arraycopy(dataModelInit.baselineMeans().getColumnPackedCopy(), 0, xx0Array, countOfIsotopes + nCycle[0], countOfFaradays);
        System.arraycopy((new Matrix(countOfFaradays, 1, 2.0 + countOfBlocks)).getColumnPackedCopy(), 0, xIndArray, countOfIsotopes + nCycle[0], countOfFaradays);

        xx0Array[countOfRows - 1] = dataModelInit.dfGain();
        xIndArray[countOfRows - 1] = 3.0 + countOfBlocks;

        Matrix xx0 = new Matrix(xx0Array, xx0Array.length);
        Matrix xInd = new Matrix(xIndArray, xIndArray.length);

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

        // clone for creating return value
        DataModellerOutputRecord dataModelInit2 = new DataModellerOutputRecord(
                dataModelInit.baselineMeans(),
                dataModelInit.baselineStandardDeviations(),
                dataModelInit.dfGain(),
                dataModelInit.logratios(),
                dataModelInit.sigmas(),
                dataModelInit.dataArray(),
                dataModelInit.blockIntensities()
        );
        RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
        randomDataGenerator.reSeedSecure();

        int nInd = 0;
        Matrix x2Sigmas = (Matrix) dataModelInit.sigmas().clone();
        Matrix xx = (Matrix) xx0.clone();
        boolean noiseFlag = false;
        if (!allFlag) {
            double delX;
            switch (operation) {
                case "changer":
                    nInd = randomDataGenerator.nextInt(0, countOfIsotopes - 1);
                    break;
                case "changeI":
                    nInd = countOfIsotopes + randomDataGenerator.nextInt(0, sumOfCycleCounts);
                    break;
                case "changebl":
                    nInd = countOfIsotopes + sumOfCycleCounts + randomDataGenerator.nextInt(0, countOfFaradays);
                    break;
                case "changedfg":
                    nInd = countOfIsotopes + sumOfCycleCounts + randomDataGenerator.nextInt(0, countOfFaradays);
                    break;
                case "noise":
                    noiseFlag = true;
                    nInd = randomDataGenerator.nextInt(0, dataModelInit.baselineMeans().getColumnDimension());

                    delX = psigRecord.psigSignalNoiseFaraday() * randomDataGenerator.nextUniform(0, 1);
                    if ((x2Sigmas.get(nInd, 0) + delX >= priorRecord.priorSignalNoiseFaraday().get(0, 0))
                            &&
                            (x2Sigmas.get(nInd, 0) + delX <= priorRecord.priorSignalNoiseFaraday().get(0, 1))) {
                        x2Sigmas.set(nInd, 0, x2Sigmas.get(nInd, 0) + delX);
                    }

                    dataModelInit2 = new DataModellerOutputRecord(
                            dataModelInit.baselineMeans(),
                            dataModelInit.baselineStandardDeviations(),
                            dataModelInit.dfGain(),
                            dataModelInit.logratios(),
                            x2Sigmas,
                            dataModelInit.dataArray(),
                            dataModelInit.blockIntensities()
                    );
            }
            if (!noiseFlag) {
                if (adaptiveFlag) {
                    delX = StrictMath.sqrt(xDataCovariance.get(nInd, nInd)) * randomDataGenerator.nextUniform(0, 1);
                } else {
                    delX = ps0Diag.get(nInd, 0) * randomDataGenerator.nextUniform(0, 1);
                }

                xx = (Matrix) xx0.clone();
                xx.set(nInd, 0, delX);
                for (int row = 0; row < xx.getRowDimension(); row++) {
                    if ((xx.get(row, 0) > priorMax.get(row, 0) || (xx.get(row, 0) < priorMin.get(row, 0)))) {
                        // todo: seems redundant?
                        xx.set(row, 0, xx0.get(row, 0));
                    }
                }
            }
        } else {
            // VARY ALL AT A TIME
            Matrix delx = (Matrix) delx_adapt.clone();
            xx = xx0.plus(delx);
            for (int row = 0; row < xx.getRowDimension(); row++) {
                if ((xx.get(row, 0) > priorMax.get(row, 0) || (xx.get(row, 0) < priorMin.get(row, 0)))) {
                    xx.set(row, 0, xx0.get(row, 0));
                }
            }
        }
        if (!noiseFlag){
            // todo: only 1 block here for now
            List<Double> x2LogRatioList = new ArrayList<>();
            List<Double> x2BlockIntensitiesList= new ArrayList<>();
            List<Double> x2BaselineMeansList = new ArrayList<>();
            double x2DFGain = 0.0;
            for (int row = 0; row < xx.getRowDimension(); row ++){
                if (xInd.get(row,0) == 1){
                    x2LogRatioList.add(xx.get(row,0));
                }
                if (xInd.get(row,0) == 2){
                    x2BlockIntensitiesList.add(xx.get(row,0));
                }
                if (xInd.get(row,0) == 2 + countOfBlocks){
                    x2BaselineMeansList.add(xx.get(row,0));
                }
                if (xInd.get(row,0) == 3 + countOfBlocks){
                    x2DFGain = xx.get(row,0);
                }
            }

            double [] x2LogRatioArray = x2LogRatioList.stream().mapToDouble(d -> d).toArray();
            Matrix x2LogRatio = new Matrix(x2LogRatioArray, x2LogRatioArray.length);
            double [] x2BlockIntensitiesArray = x2BlockIntensitiesList.stream().mapToDouble(d -> d).toArray();
            Matrix x2BlockIntensities = new Matrix(x2BlockIntensitiesArray, x2BlockIntensitiesArray.length);
            double [] x2BaselineMeansArray = x2BaselineMeansList.stream().mapToDouble(d -> d).toArray();
            Matrix x2BaselineMeans = new Matrix(x2BaselineMeansArray, x2BaselineMeansArray.length);

            dataModelInit2 = new DataModellerOutputRecord(
                    x2BaselineMeans,
                    dataModelInit.baselineStandardDeviations(),
                    x2DFGain,
                    x2LogRatio,
                    dataModelInit.sigmas(),
                    dataModelInit.dataArray(),
                    x2BlockIntensities
            );
        }

        System.err.println();
        return dataModelInit2;
    }
}