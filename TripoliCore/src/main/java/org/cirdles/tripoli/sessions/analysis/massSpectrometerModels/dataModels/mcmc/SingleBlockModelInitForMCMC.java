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

import com.google.common.collect.Sets;
import jama.Matrix;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.utilities.mathUtilities.MatLab;
import org.ojalgo.RecoverableCondition;

import java.io.Serializable;
import java.util.*;

import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.ProposedModelParameters.buildProposalRangesRecord;
import static org.cirdles.tripoli.utilities.comparators.SerializableIntegerComparator.SERIALIZABLE_COMPARATOR;

/**
 * @author James F. Bowring
 */
enum SingleBlockModelInitForMCMC {
    ;

    static SingleBlockModelRecordWithCov initializeModelForSingleBlockMCMC(SingleBlockDataSetRecord singleBlockDataSetRecord) throws RecoverableCondition {

        int baselineCount = singleBlockDataSetRecord.baselineDataSetMCMC().intensityAccumulatorList().size();
        int onPeakFaradayCount = singleBlockDataSetRecord.onPeakFaradayDataSetMCMC().intensityAccumulatorList().size();
        int onPeakPhotoMultCount = singleBlockDataSetRecord.onPeakPhotoMultiplierDataSetMCMC().intensityAccumulatorList().size();
        int totalIntensityCount = baselineCount + onPeakFaradayCount + onPeakPhotoMultCount;

        // Baseline statistics *****************************************************************************************
        /*
            for m=1:d0.Nfar%+1
                x0.BL(m,1) = mean(d0.data(d0.blflag & d0.det_ind(:,m)));
                x0.BLstd(m,1) = std(d0.data(d0.blflag & d0.det_ind(:,m)));
            end
         */
        SingleBlockDataSetRecord.SingleBlockDataRecord baselineDataSetMCMC = singleBlockDataSetRecord.baselineDataSetMCMC();
        List<Integer> detectorOrdinalIndicesAccumulatorList = baselineDataSetMCMC.detectorOrdinalIndicesAccumulatorList();
        List<Double> intensityAccumulatorList = baselineDataSetMCMC.intensityAccumulatorList();
        Map<Integer, DescriptiveStatistics> mapBaselineDetectorIndicesToStatistics = new TreeMap<>(SERIALIZABLE_COMPARATOR);
        Map<Integer, Integer> mapDetectorOrdinalToFaradayIndex = new TreeMap<>(SERIALIZABLE_COMPARATOR);

        int intensityIndex = 0;
        for (Integer detectorOrdinalIndex : detectorOrdinalIndicesAccumulatorList) {
            if (!mapBaselineDetectorIndicesToStatistics.containsKey(detectorOrdinalIndex)) {
                mapBaselineDetectorIndicesToStatistics.put(detectorOrdinalIndex, new DescriptiveStatistics());
            }
            mapBaselineDetectorIndicesToStatistics.get(detectorOrdinalIndex).addValue(intensityAccumulatorList.get(intensityIndex));
            intensityIndex++;
        }

        double[] baselineMeansArray = new double[mapBaselineDetectorIndicesToStatistics.keySet().size()];
        double[] baselineStandardDeviationsArray = new double[baselineMeansArray.length];
        int faradayIndex = 0;
        DescriptiveStatistics meanOfBaseLineMeansStdDevDescriptiveStatistics = new DescriptiveStatistics();
        for (Integer detectorOrdinalIndex : mapBaselineDetectorIndicesToStatistics.keySet()) {
            baselineMeansArray[faradayIndex] = mapBaselineDetectorIndicesToStatistics.get(detectorOrdinalIndex).getMean();
            baselineStandardDeviationsArray[faradayIndex] = mapBaselineDetectorIndicesToStatistics.get(detectorOrdinalIndex).getStandardDeviation();
            meanOfBaseLineMeansStdDevDescriptiveStatistics.addValue(baselineStandardDeviationsArray[faradayIndex]);
            mapDetectorOrdinalToFaradayIndex.put(detectorOrdinalIndex, faradayIndex);
            faradayIndex++;
        }

        double meanOfBaseLineMeansStdDev = meanOfBaseLineMeansStdDevDescriptiveStatistics.getMean();

        // OnPeak statistics by faraday ********************************************************************************
        /*
        for m=1:d0.Niso;
            tmpCounts(m,1) = mean(d0.data( (d0.iso_ind(:,m) & d0.axflag)));

            itmp = (d0.iso_ind(:,m) & ~d0.axflag);
            tmpFar(m,1)  = mean(d0.data(itmp)-x0.BL(d0.det_vec(itmp)));
        end
         */
        SingleBlockDataSetRecord.SingleBlockDataRecord onPeakFaradayDataSetMCMC = singleBlockDataSetRecord.onPeakFaradayDataSetMCMC();
        List<Integer> isotopeOrdinalIndicesAccumulatorList = onPeakFaradayDataSetMCMC.isotopeOrdinalIndicesAccumulatorList();
        detectorOrdinalIndicesAccumulatorList = onPeakFaradayDataSetMCMC.detectorOrdinalIndicesAccumulatorList();
        intensityAccumulatorList = onPeakFaradayDataSetMCMC.intensityAccumulatorList();
        Map<Integer, DescriptiveStatistics> mapFaradayIsotopeIndicesToStatistics = new TreeMap<>(SERIALIZABLE_COMPARATOR);

        intensityIndex = 0;
        for (Integer isotopeOrdinalIndex : isotopeOrdinalIndicesAccumulatorList) {
            if (!mapFaradayIsotopeIndicesToStatistics.containsKey(isotopeOrdinalIndex)) {
                mapFaradayIsotopeIndicesToStatistics.put(isotopeOrdinalIndex, new DescriptiveStatistics());
            }
            mapFaradayIsotopeIndicesToStatistics.get(isotopeOrdinalIndex).addValue(
                    intensityAccumulatorList.get(intensityIndex)
                            - baselineMeansArray[mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndicesAccumulatorList.get(intensityIndex))]);
            intensityIndex++;
        }


        // OnPeak statistics by photomultiplier ************************************************************************
        SingleBlockDataSetRecord.SingleBlockDataRecord onPeakPhotoMultiplierDataSetMCMC = singleBlockDataSetRecord.onPeakPhotoMultiplierDataSetMCMC();
        isotopeOrdinalIndicesAccumulatorList = onPeakPhotoMultiplierDataSetMCMC.isotopeOrdinalIndicesAccumulatorList();
        intensityAccumulatorList = onPeakPhotoMultiplierDataSetMCMC.intensityAccumulatorList();
        Map<Integer, DescriptiveStatistics> mapPhotoMultiplierIsotopeIndicesToStatistics = new TreeMap<>(SERIALIZABLE_COMPARATOR);

        intensityIndex = 0;
        for (Integer isotopeOrdinalIndex : isotopeOrdinalIndicesAccumulatorList) {
            if (!mapPhotoMultiplierIsotopeIndicesToStatistics.containsKey(isotopeOrdinalIndex)) {
                mapPhotoMultiplierIsotopeIndicesToStatistics.put(isotopeOrdinalIndex, new DescriptiveStatistics());
            }
            mapPhotoMultiplierIsotopeIndicesToStatistics.get(isotopeOrdinalIndex).addValue(
                    intensityAccumulatorList.get(intensityIndex));
            intensityIndex++;
        }

        // Updated by Noah 9 Feb 2023
        // find intersection of species in PhotoMultiplier and Faraday cases
        Set<Integer> commonSpeciesOrdinalIndices = Sets.intersection(mapPhotoMultiplierIsotopeIndicesToStatistics.keySet(), mapFaradayIsotopeIndicesToStatistics.keySet());

        double[] faradayMeansArray = new double[mapFaradayIsotopeIndicesToStatistics.keySet().size()];
        int isotopeIndex = 0;
        double maxFaradayCountsMean = Double.MIN_VALUE;
        for (Integer isotopeOrdinalIndex : mapFaradayIsotopeIndicesToStatistics.keySet()) {
            faradayMeansArray[isotopeIndex] = mapFaradayIsotopeIndicesToStatistics.get(isotopeOrdinalIndex).getMean();
            if (commonSpeciesOrdinalIndices.contains(isotopeOrdinalIndex)
                    &&
                    (faradayMeansArray[isotopeIndex] > maxFaradayCountsMean)) {
                maxFaradayCountsMean = faradayMeansArray[isotopeIndex];
            }
            isotopeIndex++;
        }

        double[] photoMultiplierMeansArray = new double[mapPhotoMultiplierIsotopeIndicesToStatistics.keySet().size()];
        isotopeIndex = 0;
        double maxPhotoMultiplierCountsMean = Double.MIN_VALUE;
        for (Integer isotopeOrdinalIndex : mapPhotoMultiplierIsotopeIndicesToStatistics.keySet()) {
            photoMultiplierMeansArray[isotopeIndex] = mapPhotoMultiplierIsotopeIndicesToStatistics.get(isotopeOrdinalIndex).getMean();
            if (commonSpeciesOrdinalIndices.contains(isotopeOrdinalIndex) &&
                    (photoMultiplierMeansArray[isotopeIndex] > maxPhotoMultiplierCountsMean)) {
                maxPhotoMultiplierCountsMean = photoMultiplierMeansArray[isotopeIndex];
            }
            isotopeIndex++;
        }

        // NOTE: the speciesList has been sorted by increasing abundances in the original analysisMethod setup
        //  the ratios are between each species and the most abundant species, with one less ratio than species
        int indexOfMostAbundantIsotope = mapPhotoMultiplierIsotopeIndicesToStatistics.size() - 1;

        // june 2023 new init line 14 ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        // june 2023 new init - will need to be user input
        int iden = indexOfMostAbundantIsotope + 1; // ordinal
        double detectorFaradayGain = 0.9;
        // TODO: backtrack and simplify
        double[] d0_data = singleBlockDataSetRecord.blockIntensityArray();
        int startIndexOfPhotoMultiplierData = singleBlockDataSetRecord.getCountOfBaselineIntensities() + singleBlockDataSetRecord.getCountOfOnPeakFaradayIntensities();
        int[] d0_detVec = singleBlockDataSetRecord.blockDetectorOrdinalIndicesArray();
        List<Double> ddver2List = new ArrayList<>();
        int[] isotopeOrdinalIndices = singleBlockDataSetRecord.blockIsotopeOrdinalIndicesArray();
        int[] timeIndForSortingArray = singleBlockDataSetRecord.blockTimeIndicesArray();
        List<Integer> tempTime = new ArrayList<>();
        for (int dataArrayIndex = 0; dataArrayIndex < d0_data.length; dataArrayIndex++) {
            if (isotopeOrdinalIndices[dataArrayIndex] == iden) {
                if (dataArrayIndex < startIndexOfPhotoMultiplierData) {
                    double calculated = (d0_data[dataArrayIndex] - baselineMeansArray[mapDetectorOrdinalToFaradayIndex.get(d0_detVec[dataArrayIndex])]) * detectorFaradayGain;
                    ddver2List.add(calculated);
                } else {
                    ddver2List.add(d0_data[dataArrayIndex]);
                }
                tempTime.add(timeIndForSortingArray[dataArrayIndex]);
            }
        }

        double[] ddVer2Array = ddver2List.stream().mapToDouble(d -> d).toArray();

        int[] tempTimeIndicesArray = tempTime.stream().mapToInt(d -> d).toArray();
        ArrayIndexComparator comparatorTime = new ArrayIndexComparator(tempTimeIndicesArray);
        Integer[] ddVer2sortIndices = comparatorTime.createIndexArray();
        Arrays.sort(ddVer2sortIndices, comparatorTime);

        double[] ddVer2SortedArray = new double[ddVer2Array.length];
        for (int i = 0; i < ddVer2Array.length; i++) {
            ddVer2SortedArray[i] = ddVer2Array[ddVer2sortIndices[i]];
        }

        double[][] interpolatedKnotData_II = singleBlockDataSetRecord.blockKnotInterpolationStore().toRawCopy2D();
        RealMatrix II = new BlockRealMatrix(interpolatedKnotData_II);
        DecompositionSolver solver = new QRDecomposition(II).getSolver();
        RealVector data = new ArrayRealVector(ddVer2SortedArray);
        RealVector solution = solver.solve(data);
        double[] intensity_I = solution.toArray();

        Matrix IIm = new Matrix(II.getData());
        Matrix intensityFn = IIm.times(new Matrix(intensity_I, intensity_I.length));
        ProposedModelParameters.ProposalRangesRecord proposalRangesRecord =
                buildProposalRangesRecord(intensityFn.getColumnPackedCopy());
        int[] isotopeOrdinalIndicesAccumulatorArray = singleBlockDataSetRecord.blockIsotopeOrdinalIndicesArray();
        double[] logRatios = new double[indexOfMostAbundantIsotope];
        int isotopeCount = logRatios.length + 1;
        for (isotopeIndex = 0; isotopeIndex < logRatios.length; isotopeIndex++) {
            ddver2List = new ArrayList<>();
            tempTime = new ArrayList<>();
            for (int dataArrayIndex = 0; dataArrayIndex < d0_data.length; dataArrayIndex++) {
                if (isotopeOrdinalIndicesAccumulatorArray[dataArrayIndex] == isotopeIndex + 1) {
                    if (dataArrayIndex < startIndexOfPhotoMultiplierData) {
                        double calculated = (d0_data[dataArrayIndex] - baselineMeansArray[mapDetectorOrdinalToFaradayIndex.get(d0_detVec[dataArrayIndex])]) * detectorFaradayGain;
                        ddver2List.add(calculated);
                    } else {
                        ddver2List.add(d0_data[dataArrayIndex]);
                    }
                    tempTime.add(timeIndForSortingArray[dataArrayIndex]);
                }
            }

            ddVer2Array = ddver2List.stream().mapToDouble(d -> d).toArray();
            tempTimeIndicesArray = tempTime.stream().mapToInt(d -> d).toArray();
            comparatorTime = new ArrayIndexComparator(tempTimeIndicesArray);
            ddVer2sortIndices = comparatorTime.createIndexArray();
            Arrays.sort(ddVer2sortIndices, comparatorTime);

            ddVer2SortedArray = new double[ddVer2Array.length];
            for (int i = 0; i < ddVer2Array.length; i++) {
                ddVer2SortedArray[i] = ddVer2Array[ddVer2sortIndices[i]];
            }

            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
            for (int dataArrayIndex = 0; dataArrayIndex < ddVer2SortedArray.length; dataArrayIndex++) {
                descriptiveStatistics.addValue(ddVer2SortedArray[dataArrayIndex] / intensityFn.get(dataArrayIndex, 0));
            }
            logRatios[isotopeIndex] = StrictMath.log(Math.max(descriptiveStatistics.getMean(), 3e-9));
        }

        // initialize model data vectors
        double[] dataArray = new double[totalIntensityCount];
        double[] dataWithNoBaselineArray = new double[dataArray.length];
        double[] dataSignalNoiseArray_Dsig = new double[dataArray.length];
        double[] ddd = new double[dataArray.length];
        double reportInterval = 0.1;  //TODO: data-detected
        int[] detectorOrdinalIndicesAccumulatorArray = singleBlockDataSetRecord.blockDetectorOrdinalIndicesArray();

        for (int dataArrayIndex = 0; dataArrayIndex < d0_data.length; dataArrayIndex++) {
            intensityIndex = timeIndForSortingArray[dataArrayIndex];
            isotopeIndex = isotopeOrdinalIndicesAccumulatorArray[dataArrayIndex] - 1;

            if (0 <= isotopeIndex) {
                if (dataArrayIndex >= startIndexOfPhotoMultiplierData) {
                    if (isotopeIndex < logRatios.length) {
                        ddd[dataArrayIndex] = StrictMath.exp(logRatios[isotopeIndex]) * intensityFn.get(intensityIndex, 0);
                    } else {
                        ddd[dataArrayIndex] = intensityFn.get(intensityIndex, 0);
                    }
                    dataSignalNoiseArray_Dsig[dataArrayIndex] = ddd[dataArrayIndex] / reportInterval;
                } else {
                    if (isotopeIndex < logRatios.length) {
                        ddd[dataArrayIndex] = StrictMath.exp(logRatios[isotopeIndex]) * (1.0 / detectorFaradayGain) * intensityFn.get(intensityIndex, 0);
                    } else {
                        ddd[dataArrayIndex] = 1.0 * (1.0 / detectorFaradayGain) * intensityFn.get(intensityIndex, 0);
                    }
                    faradayIndex = mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndicesAccumulatorArray[dataArrayIndex]);
                    dataSignalNoiseArray_Dsig[dataArrayIndex] = ddd[dataArrayIndex] / reportInterval + Math.pow(baselineStandardDeviationsArray[faradayIndex], 2.0);
                }
            } else {
                // baselines
                faradayIndex = mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndicesAccumulatorArray[dataArrayIndex]);
                dataSignalNoiseArray_Dsig[dataArrayIndex] = Math.pow(baselineStandardDeviationsArray[faradayIndex], 2.0);
            }
        }

        double[] logRatioVar = new double[logRatios.length];
        for (int logRatioIndex = 0; logRatioIndex < logRatios.length; logRatioIndex++) {
            double[] testLR = MatLab.linspace(-0.5, 0.5, 1001).toRawCopy1D();
            double[] eTmp = new double[testLR.length];
            double minETmp = Double.MAX_VALUE;
            for (int ii = 0; ii < testLR.length; ii++) {
                double[] testLogRatios = logRatios.clone();
                testLogRatios[logRatioIndex] = logRatios[logRatioIndex] + testLR[ii];

                SingleBlockModelRecord testX0 = new SingleBlockModelRecord(
                        singleBlockDataSetRecord.blockNumber(),
                        baselineMeansArray,
                        baselineStandardDeviationsArray,
                        detectorFaradayGain,
                        mapDetectorOrdinalToFaradayIndex,
                        testLogRatios,
                        null,
                        singleBlockDataSetRecord.blockIntensityArray(),
                        dataWithNoBaselineArray,
                        dataSignalNoiseArray_Dsig,
                        intensity_I,
                        intensityFn.getColumnPackedCopy(),
                        mapDetectorOrdinalToFaradayIndex.size(),
                        isotopeCount);
                try {
                    double[] dataModel = modelInitData(testX0, singleBlockDataSetRecord);
                    eTmp[ii] = calcError(singleBlockDataSetRecord.blockIntensityArray(), dataModel, dataSignalNoiseArray_Dsig);
                    minETmp = Math.min(eTmp[ii], minETmp);
                } catch (Exception e) {
                    System.err.println("Dsig error during init line 302");
                }
            }
            logRatioVar[logRatioIndex] = calcVariance(eTmp, minETmp, testLR);
        }

        double[] intensityVar = new double[intensity_I.length];
        for (intensityIndex = 0; intensityIndex < intensity_I.length; intensityIndex++) {
            double[] testI = MatLab.linspace(-meanOfBaseLineMeansStdDev, meanOfBaseLineMeansStdDev, 101).toRawCopy1D();
            double[] eTmp = new double[testI.length];
            double minETmp = Double.MAX_VALUE;
            for (int ii = 0; ii < testI.length; ii++) {
                double[] testIntensity = intensity_I.clone();
                testIntensity[intensityIndex] = intensity_I[intensityIndex] + testI[ii];

                SingleBlockModelRecord testX0 = new SingleBlockModelRecord(
                        singleBlockDataSetRecord.blockNumber(),
                        baselineMeansArray,
                        baselineStandardDeviationsArray,
                        detectorFaradayGain,
                        mapDetectorOrdinalToFaradayIndex,
                        logRatios,
                        null,
                        singleBlockDataSetRecord.blockIntensityArray(),
                        dataWithNoBaselineArray,
                        dataSignalNoiseArray_Dsig,
                        testIntensity,
                        intensityFn.getColumnPackedCopy(),
                        mapDetectorOrdinalToFaradayIndex.size(),
                        isotopeCount);
                double[] dataModel = modelInitData(testX0, singleBlockDataSetRecord);
                eTmp[ii] = calcError(singleBlockDataSetRecord.blockIntensityArray(), dataModel, dataSignalNoiseArray_Dsig);
                minETmp = Math.min(eTmp[ii], minETmp);
            }
            intensityVar[intensityIndex] = calcVariance(eTmp, minETmp, testI);
        }


        double[] testDF = MatLab.linspace(-.1, .1, 1001).toRawCopy1D();
        double[] eTmp = new double[testDF.length];
        double minETmp = Double.MAX_VALUE;
        for (int ii = 0; ii < testDF.length; ii++) {
            double testDFGain = detectorFaradayGain + testDF[ii];

            SingleBlockModelRecord testX0 = new SingleBlockModelRecord(
                    singleBlockDataSetRecord.blockNumber(),
                    baselineMeansArray,
                    baselineStandardDeviationsArray,
                    testDFGain,
                    mapDetectorOrdinalToFaradayIndex,
                    logRatios,
                    null,
                    singleBlockDataSetRecord.blockIntensityArray(),
                    dataWithNoBaselineArray,
                    dataSignalNoiseArray_Dsig,
                    intensity_I,
                    intensityFn.getColumnPackedCopy(),
                    mapDetectorOrdinalToFaradayIndex.size(),
                    isotopeCount);

            double[] dataModel = modelInitData(testX0, singleBlockDataSetRecord);
            eTmp[ii] = calcError(singleBlockDataSetRecord.blockIntensityArray(), dataModel, dataSignalNoiseArray_Dsig);
            minETmp = Math.min(eTmp[ii], minETmp);
        }
        double dfGainVar = calcVariance(eTmp, minETmp, testDF);


        double[] baseLineVar = new double[baselineMeansArray.length];

        for (int baseLineIndex = 0; baseLineIndex < baselineMeansArray.length; baseLineIndex++) {
            double[] testBL = MatLab.linspace(-baselineStandardDeviationsArray[baseLineIndex], baselineStandardDeviationsArray[baseLineIndex], 1001).toRawCopy1D();
            eTmp = new double[testBL.length];
            minETmp = Double.MAX_VALUE;
            for (int ii = 0; ii < testBL.length; ii++) {
                double[] testBaseLineMeans = baselineMeansArray.clone();
                testBaseLineMeans[baseLineIndex] = baselineMeansArray[baseLineIndex] + testBL[ii];

                SingleBlockModelRecord testX0 = new SingleBlockModelRecord(
                        singleBlockDataSetRecord.blockNumber(),
                        testBaseLineMeans,
                        baselineStandardDeviationsArray,
                        detectorFaradayGain,
                        mapDetectorOrdinalToFaradayIndex,
                        logRatios,
                        null,
                        singleBlockDataSetRecord.blockIntensityArray(),
                        dataWithNoBaselineArray,
                        dataSignalNoiseArray_Dsig,
                        intensity_I,
                        intensityFn.getColumnPackedCopy(),
                        mapDetectorOrdinalToFaradayIndex.size(),
                        isotopeCount);
                double[] dataModel = modelInitData(testX0, singleBlockDataSetRecord);
                eTmp[ii] = calcError(singleBlockDataSetRecord.blockIntensityArray(), dataModel, dataSignalNoiseArray_Dsig);
                minETmp = Math.min(eTmp[ii], minETmp);
            }
            baseLineVar[baseLineIndex] = calcVariance(eTmp, minETmp, testBL);
        }

        SingleBlockModelRecord originalX0 = new SingleBlockModelRecord(
                singleBlockDataSetRecord.blockNumber(),
                baselineMeansArray,//calculated
                baselineStandardDeviationsArray,
                detectorFaradayGain,
                mapDetectorOrdinalToFaradayIndex,
                logRatios,//calculated
                null,
                singleBlockDataSetRecord.blockIntensityArray(),
                dataWithNoBaselineArray,
                dataSignalNoiseArray_Dsig,//calculated
                intensity_I,//calculated
                intensityFn.getColumnPackedCopy(),
                mapDetectorOrdinalToFaradayIndex.size(),
                isotopeCount);
        double[] dataModel = modelInitData(originalX0, singleBlockDataSetRecord);
        // note: this datamodel replicates matlab datamodel when using linear knots

        int countOfParameters = logRatioVar.length + intensityVar.length + baseLineVar.length + 1;
        double covarianceFactor = Math.pow(0.1, 2) * (1.0 / countOfParameters);
        double[][] diagC0 = new double[countOfParameters][countOfParameters];
        int diagIndex = 0;
        for (int i = 0; i < logRatioVar.length; i++) {
            diagC0[diagIndex][diagIndex] = StrictMath.sqrt(logRatioVar[i]) * covarianceFactor;
            diagIndex++;
        }
        for (int i = 0; i < intensityVar.length; i++) {
            diagC0[diagIndex][diagIndex] = StrictMath.sqrt(intensityVar[i]) * covarianceFactor;
            diagIndex++;
        }
        for (int i = 0; i < baseLineVar.length; i++) {
            diagC0[diagIndex][diagIndex] = StrictMath.sqrt(baseLineVar[i]) * covarianceFactor;
            diagIndex++;
        }
        diagC0[diagIndex][diagIndex] = StrictMath.sqrt(dfGainVar) * covarianceFactor;

        Matrix covarianceMatrix_C0 = new Matrix(diagC0);

        SingleBlockModelRecord calculatedX0 = new SingleBlockModelRecord(
                singleBlockDataSetRecord.blockNumber(),
                baselineMeansArray,//calculated
                baselineStandardDeviationsArray,
                detectorFaradayGain,
                mapDetectorOrdinalToFaradayIndex,
                logRatios,//calculated
                new double[]{1},
                singleBlockDataSetRecord.blockIntensityArray(),
                dataModel,
                dataSignalNoiseArray_Dsig,//calculated
                intensity_I,//calculated
                intensityFn.getColumnPackedCopy(),
                mapDetectorOrdinalToFaradayIndex.size(),
                isotopeCount);

        System.out.println("completed init with covariance");

        return new SingleBlockModelRecordWithCov(calculatedX0, proposalRangesRecord, covarianceMatrix_C0);

    }

    public synchronized static double[] modelInitData(SingleBlockModelRecord x0, SingleBlockDataSetRecord singleBlockDataSetRecord) {
        int baselineCount = singleBlockDataSetRecord.baselineDataSetMCMC().intensityAccumulatorList().size();
        int onPeakFaradayCount = singleBlockDataSetRecord.onPeakFaradayDataSetMCMC().intensityAccumulatorList().size();
        int onPeakPhotoMultCount = singleBlockDataSetRecord.onPeakPhotoMultiplierDataSetMCMC().intensityAccumulatorList().size();
        int totalIntensityCount = baselineCount + onPeakFaradayCount + onPeakPhotoMultCount;

        int[] isotopeOrdinalIndicesArray = singleBlockDataSetRecord.blockIsotopeOrdinalIndicesArray();
        int[] timeIndForSortingArray = singleBlockDataSetRecord.blockTimeIndicesArray();

        double[][] interpolatedKnotData_II = singleBlockDataSetRecord.blockKnotInterpolationStore().toRawCopy2D();
        Matrix II = new Matrix(interpolatedKnotData_II);
        Matrix I = new Matrix(x0.I0(), x0.I0().length);
        Matrix intensityFn = II.times(I);

        double[] dataModel = new double[totalIntensityCount];
        int[] detectorOrdinalIndicesAccumulatorArray = singleBlockDataSetRecord.blockDetectorOrdinalIndicesArray();
        for (int dataArrayIndex = 0; dataArrayIndex < totalIntensityCount; dataArrayIndex++) {
            int faradayIndex = 0;
            if (dataArrayIndex < baselineCount + onPeakFaradayCount) {
                faradayIndex = x0.mapDetectorOrdinalToFaradayIndex().get(detectorOrdinalIndicesAccumulatorArray[dataArrayIndex]);
            }
            int intensityIndex = timeIndForSortingArray[dataArrayIndex];
            int isotopeIndex = isotopeOrdinalIndicesArray[dataArrayIndex] - 1;
            if (dataArrayIndex < baselineCount) {
                dataModel[dataArrayIndex] = x0.baselineMeansArray()[faradayIndex];
            } else if (dataArrayIndex < baselineCount + onPeakFaradayCount) {
                if (isotopeIndex < x0.logRatios().length) {
                    dataModel[dataArrayIndex] =
                            StrictMath.exp(x0.logRatios()[isotopeIndex])
                                    * (1.0 / x0.detectorFaradayGain()) * intensityFn.get(intensityIndex, 0)
                                    + x0.baselineMeansArray()[faradayIndex];
                } else {
                    dataModel[dataArrayIndex] =
                            (1.0 / x0.detectorFaradayGain()) * intensityFn.get(intensityIndex, 0)
                                    + x0.baselineMeansArray()[faradayIndex];
                }
            } else {
                if (isotopeIndex < x0.logRatios().length) {
                    dataModel[dataArrayIndex] =
                            StrictMath.exp(x0.logRatios()[isotopeIndex])
                                    * intensityFn.get(intensityIndex, 0);
                } else {
                    dataModel[dataArrayIndex] =
                            intensityFn.get(intensityIndex, 0);
                }
            }
        }

        return dataModel;
    }

    private synchronized static double calcError(double[] origData, double[] modelData, double[] dataSignalNoiseArray_Dsig) {
        double sum = 0.0;
        for (int i = 0; i < origData.length; i++) {
            sum += Math.pow((origData[i] - modelData[i]), 2.0) / dataSignalNoiseArray_Dsig[i];
        }
        return sum;
    }

    private synchronized static double calcVariance(double[] eTmp, double minETmp, double[] testArray) {
        double[] ee = new double[eTmp.length];
        double sumExpEE = 0.0;
        for (int i = 0; i < ee.length; i++) {
            ee[i] = eTmp[i] - minETmp;
            sumExpEE += StrictMath.exp(-ee[i] / 2.0);
        }
        double[] p = new double[eTmp.length];
        double varSum = 0.0;
        for (int i = 0; i < ee.length; i++) {
            p[i] = StrictMath.exp(-ee[i] / 2.0) / sumExpEE;
            varSum += p[i] * StrictMath.pow((testArray[i] - 0.0), 2.0);
        }

        return varSum;
    }

    public record SingleBlockModelRecordWithCov(
            SingleBlockModelRecord singleBlockModelRecord,
            ProposedModelParameters.ProposalRangesRecord proposalRangesRecord,
            Matrix covarianceMatrix_C0) {

    }

    private static class ArrayIndexComparator implements Comparator<Integer>, Serializable {
        private final int[] array;

        public ArrayIndexComparator(int[] array) {
            this.array = array;
        }

        public Integer[] createIndexArray() {
            Integer[] indexes = new Integer[array.length];
            for (int i = 0; i < array.length; i++) {
                indexes[i] = i; // Autoboxing
            }
            return indexes;
        }

        @Override
        public int compare(Integer index1, Integer index2) {
            return Integer.compare(array[index1], array[index2]);
        }
    }
}