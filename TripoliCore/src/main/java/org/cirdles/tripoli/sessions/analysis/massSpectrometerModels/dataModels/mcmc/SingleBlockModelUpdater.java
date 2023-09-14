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

import com.google.common.collect.ImmutableList;
import jama.Matrix;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Arrays;
import java.util.List;

/**
 * @author James F. Bowring
 */
public class SingleBlockModelUpdater {

    public List<String> operations = ImmutableList.of("changer", "changeI", "changedfg", "changebl", "noise");
    private int countOfLogRatios;
    private int countOfIntensities;
    private int countOfFaradays;
    private int countOfPhotoMultipliers;
    private int countOfTotalModelParameters;

    SingleBlockModelUpdater() {
    }

    /**
     * Randomly generate next model operation, with or without hierarchical step
     *
     * @param hierFlag Hierarchical = true
     * @return Random operation by name
     */
    synchronized String randomOperMS(boolean hierFlag) {
        Object[][] notHier = {{40, 60, 80, 100}, {operations.get(1), operations.get(0), operations.get(3), operations.get(2)}};
        Object[][] hier = {{60, 80, 90, 100, 120}, {operations.get(1), operations.get(0), operations.get(3), operations.get(2), operations.get(4)}};

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
            SingleBlockModelRecord singleBlockInitialModelRecord_initial,// i.e. "X"
            ProposedModelParameters.ProposalRangesRecord proposalRangesRecord,
            double[] delx_adapt,
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

        countOfLogRatios = singleBlockInitialModelRecord_initial.logRatios().length;
        countOfIntensities = singleBlockInitialModelRecord_initial.I0().length;
        countOfFaradays = singleBlockInitialModelRecord_initial.faradayCount();
        countOfPhotoMultipliers = 1;
        countOfTotalModelParameters = countOfLogRatios + countOfIntensities + countOfFaradays + countOfPhotoMultipliers;

        double[] xx0 = new double[countOfTotalModelParameters];
        int[] xInd = new int[countOfTotalModelParameters];

        System.arraycopy(singleBlockInitialModelRecord_initial.logRatios(), 0, xx0, 0, countOfLogRatios);
        Arrays.fill(xInd, 1);

        System.arraycopy(singleBlockInitialModelRecord_initial.I0(), 0, xx0, countOfLogRatios, countOfIntensities);
        int[] temp = new int[countOfIntensities];
        Arrays.fill(temp, 2);
        System.arraycopy(temp, 0, xInd, countOfLogRatios, countOfIntensities);

        System.arraycopy(singleBlockInitialModelRecord_initial.baselineMeansArray(), 0, xx0, countOfLogRatios + countOfIntensities, countOfFaradays);
        temp = new int[countOfFaradays];
        Arrays.fill(temp, 3);
        System.arraycopy(temp, 0, xInd, countOfLogRatios + countOfIntensities, countOfFaradays);

        xx0[countOfTotalModelParameters - 1] = singleBlockInitialModelRecord_initial.detectorFaradayGain();
        xInd[countOfTotalModelParameters - 1] = 4;

        SingleBlockModelRecord singleBlockInitialModelRecord_initial2 = null;
        double[] updatedLogRatios = new double[countOfLogRatios];
        double[] updatedIntensities = new double[countOfIntensities];
        double[] updatedBaselineMeans = new double[countOfFaradays];
        double updatedFaradayGain = 0.0;

        if (operation.startsWith("cha") && allFlag) {
            // VARY ALL AT A TIME
                /*
                    delx = delx_adapt;
                    xx =  xx0 + delx;
                    inprior = xx<=priormax & xx>=priormin;
                     xx(~inprior) = xx0(~inprior);
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
                        updatedBaselineMeans[row - countOfLogRatios - countOfIntensities] = xx[row];
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

                /*
                    x2.lograt = xx(xind==1);
                    for ii=1:Nblock
                        x2.I{ii} = xx(xind==(1+ii));
                    end
                    x2.BL = xx(xind==(2+Nblock));
                    x2.DFgain = xx(xind==(3+Nblock));
                    x2.sig = x.sig;
                 */
            singleBlockInitialModelRecord_initial2 = new SingleBlockModelRecord(
                    singleBlockInitialModelRecord_initial.blockID(),
                    singleBlockInitialModelRecord_initial.faradayCount(),
                    singleBlockInitialModelRecord_initial.cycleCount(),
                    singleBlockInitialModelRecord_initial.isotopeCount(),
                    singleBlockInitialModelRecord_initial.highestAbundanceSpecies(),
                    updatedBaselineMeans,
                    singleBlockInitialModelRecord_initial.baselineStandardDeviationsArray().clone(),
                    updatedFaradayGain,
                    singleBlockInitialModelRecord_initial.mapDetectorOrdinalToFaradayIndex(),
                    updatedLogRatios,
                    singleBlockInitialModelRecord_initial.mapOfSpeciesToActiveCycles(),
                    singleBlockInitialModelRecord_initial.mapLogRatiosToCycleStats(),
                    singleBlockInitialModelRecord_initial.dataModelArray().clone(),
                    singleBlockInitialModelRecord_initial.dataSignalNoiseArray().clone(),
                    updatedIntensities,
                    singleBlockInitialModelRecord_initial.intensities().clone()
            );
        }

        return singleBlockInitialModelRecord_initial2;
    }


    synchronized UpdatedCovariancesRecord updateMeanCovMS2(
            SingleBlockModelRecord singleBlockModelRecord,
            double[][] dataModelCov,
            double[] dataModelMean,
            long countOfModels) {
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
        System.arraycopy(singleBlockModelRecord.I0(), 0, xx, countOfLogRatios, countOfIntensities);
        System.arraycopy(singleBlockModelRecord.baselineMeansArray(), 0, xx, countOfLogRatios + countOfIntensities, countOfFaradays);
        xx[countOfTotalModelParameters - 1] = singleBlockModelRecord.detectorFaradayGain();

        double[] xMeanTemp = dataModelMean.clone();
        double[] xMean = new double[dataModelMean.length];
        double[] diffXwithXmean = new double[dataModelMean.length];
        double[] diffXwithXmeanTemp = new double[dataModelMean.length];
        for (int row = 0; row < xMeanTemp.length; row++) {
            diffXwithXmeanTemp[row] = xx[row] - xMeanTemp[row];
            xMean[row] = xMeanTemp[row] + diffXwithXmeanTemp[row] / countOfModels;
            diffXwithXmean[row] = xx[row] - xMean[row];
        }

        /*
         xcov = xcov*(m-1)/m + (m-1)/m^2*(xx-xmean)*(xx-xmeantmp)';
         */
        Matrix xCovM = new Matrix(dataModelCov);
        Matrix diffXwithXmeanMatrix = new Matrix(diffXwithXmean, diffXwithXmean.length);
        Matrix diffXwithXmeanTempMatrix = new Matrix(diffXwithXmeanTemp, diffXwithXmeanTemp.length);
        // June 2023 discovered need to pre-calculate to guarantee results
        double countMinusOneOverCount = ((countOfModels - 1.0) / countOfModels);
        double countMinusOneOverSquareCount = (countOfModels - 1.0) / Math.pow(countOfModels, 2.0);
        Matrix updated_xCovM =
                xCovM.times(countMinusOneOverCount)
                        .plus(diffXwithXmeanMatrix
                                .times(countMinusOneOverSquareCount)
                                .times(diffXwithXmeanTempMatrix.transpose()));

        return new UpdatedCovariancesRecord(updated_xCovM.getArray(), xMean);
    }

    synchronized double grConverge(List<EnsemblesStore.EnsembleRecord> ensembleRecordsList) {
        /*
        function Rexit = GRConverge(x,ensemble);
            cnt = length(ensemble);
            xall = [ensemble.lograt];
            xall = xall(1:Niso,:);
            for ii = 1:Nblock
                for n = 1:cnt;
                    ens_I{ii}(:,n) =[ensemble(n).I{ii}];
                end
                xall = [xall; ens_I{ii}];

            end
            xall = [xall; [ensemble.BL]];
            xall = [xall; [ensemble.DFgain]];
            xall = xall';
         */
        int countOfEnsembles = ensembleRecordsList.size();
        double[][] xAll = new double[countOfEnsembles][countOfTotalModelParameters];
        int ensIndex = 0;
        for (EnsemblesStore.EnsembleRecord ens : ensembleRecordsList) {
            System.arraycopy(ens.logRatios(), 0, xAll[ensIndex], 0, countOfLogRatios);
            System.arraycopy(ens.I0(), 0, xAll[ensIndex], countOfLogRatios, countOfIntensities);
            System.arraycopy(ens.baseLine(), 0, xAll[ensIndex], countOfLogRatios + countOfIntensities, countOfFaradays);
            xAll[ensIndex][countOfTotalModelParameters - 1] = ens.dfGain();
            ensIndex++;
        }

    /*
        ngroup = round(sqrt(cnt));
        gsize = round(sqrt(cnt));
        for jj = 1:ngroup % Iterate over ngroups groups of size gsize
            tmpxs(:,:,jj) = cov(xall(1+(jj-1)*gsize:jj*gsize,:));
            tmpxm(:,jj) = mean(xall(1+(jj-1)*gsize:jj*gsize,:));
        end
        MeanofVar = sum(tmpxs(:,:,1:ngroup),3)/ngroup; % Mean of variances
        VarofMean = diag(std(tmpxm(:,1:ngroup),[],2).^2); % Variance of means

        %Rexit(ngroup) = sqrt((ngroup-1)/ngroup+(det(VarofMean)/det(MeanofVar))^(1/N)/ngroup);
        Rexit = sqrt((ngroup-1)/ngroup+(det(VarofMean)/det(MeanofVar))^(1/Nmod)/ngroup);
     */

        int nGroup = (int) Math.round(Math.sqrt(countOfEnsembles));
        int groupSize = (int) Math.round(Math.sqrt(countOfEnsembles));

        double[][] extractedArray = new double[groupSize][countOfTotalModelParameters];
        double[][] meanOfVarArray = new double[countOfTotalModelParameters][countOfTotalModelParameters];
        double[][] meansOverGroups = new double[countOfTotalModelParameters][groupSize];
        double[][] varOfMeansArray = new double[countOfTotalModelParameters][countOfTotalModelParameters];

        for (int groupIndex = 0; groupIndex < nGroup; groupIndex++) {
            for (int row = groupIndex * groupSize; row < (groupIndex + 1) * groupSize; row++) {
                extractedArray[row - groupIndex * groupSize] = xAll[row];
            }

            Covariance cov = new Covariance(extractedArray);
            double[][] covArray = cov.getCovarianceMatrix().getData();
            for (int row = 0; row < countOfTotalModelParameters; row++) {
                for (int col = 0; col < countOfTotalModelParameters; col++) {
                    meanOfVarArray[row][col] += covArray[row][col] / nGroup;
                }
            }

            for (int row = 0; row < countOfTotalModelParameters; row++) {
                for (int col = 0; col < groupSize; col++) {
                    meansOverGroups[row][col] += extractedArray[col][row] / nGroup;
                }
            }
        }

        for (int row = 0; row < countOfTotalModelParameters; row++) {
            DescriptiveStatistics descriptiveStatisticsParametersPerGroups = new DescriptiveStatistics();
            for (int col = 0; col < groupSize; col++) {
                descriptiveStatisticsParametersPerGroups.addValue(meansOverGroups[row][col]);
            }
            varOfMeansArray[row][row] = descriptiveStatisticsParametersPerGroups.getStandardDeviation();
        }

        //Rexit = sqrt(   (ngroup-1)/ngroup + (det(VarofMean)/det(MeanofVar))^(1/Nmod)/ngroup   );
        Matrix varOfMeansM = new Matrix(varOfMeansArray);
        Matrix meanOfVarM = new Matrix(meanOfVarArray);

        double term1 = (nGroup - 1.0) / nGroup;
        double term2 = varOfMeansM.det() / meanOfVarM.det();
        double rExit = StrictMath.sqrt(term1 + StrictMath.pow(term2, 1.0 / countOfTotalModelParameters) / nGroup);

        return rExit;
    }

    public List<String> getOperations() {
        return operations;
    }

    public record UpdatedCovariancesRecord(
            double[][] dataCov,
            double[] dataMean
    ) {

    }


}