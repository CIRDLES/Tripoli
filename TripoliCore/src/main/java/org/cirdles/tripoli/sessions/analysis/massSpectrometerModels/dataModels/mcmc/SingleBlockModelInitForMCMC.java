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
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.task.InverterTask;

import java.io.Serializable;
import java.util.*;

import static java.lang.Math.pow;
import static java.lang.StrictMath.exp;
import static java.lang.StrictMath.log;
import static org.cirdles.tripoli.utilities.comparators.SerializableIntegerComparator.SERIALIZABLE_COMPARATOR;

/**
 * @author James F. Bowring
 */
enum SingleBlockModelInitForMCMC {
    ;

    static SingleBlockModelRecord initializeModelForSingleBlockMCMC(SingleBlockDataSetRecord singleBlockDataSetRecord) throws RecoverableCondition {

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
        for (Integer detectorOrdinalIndex : mapBaselineDetectorIndicesToStatistics.keySet()) {
            baselineMeansArray[faradayIndex] = mapBaselineDetectorIndicesToStatistics.get(detectorOrdinalIndex).getMean();
            baselineStandardDeviationsArray[faradayIndex] = mapBaselineDetectorIndicesToStatistics.get(detectorOrdinalIndex).getStandardDeviation();
            mapDetectorOrdinalToFaradayIndex.put(detectorOrdinalIndex, faradayIndex);
            faradayIndex++;
        }

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
        int maxCountFaradayIndex = -1;
        double maxFaradayCountsMean = Double.MIN_VALUE;
        for (Integer isotopeOrdinalIndex : mapFaradayIsotopeIndicesToStatistics.keySet()) {
            faradayMeansArray[isotopeIndex] = mapFaradayIsotopeIndicesToStatistics.get(isotopeOrdinalIndex).getMean();
            if (commonSpeciesOrdinalIndices.contains(isotopeOrdinalIndex)
                    &&
                    (faradayMeansArray[isotopeIndex] > maxFaradayCountsMean)) {
                maxFaradayCountsMean = faradayMeansArray[isotopeIndex];
                maxCountFaradayIndex = isotopeIndex;
            }
            isotopeIndex++;
        }

        double[] photoMultiplierMeansArray = new double[mapPhotoMultiplierIsotopeIndicesToStatistics.keySet().size()];
        isotopeIndex = 0;
        int maxCountPhotoMultiplierIndex = -1;
        double maxPhotoMultiplierCountsMean = Double.MIN_VALUE;
        for (Integer isotopeOrdinalIndex : mapPhotoMultiplierIsotopeIndicesToStatistics.keySet()) {
            photoMultiplierMeansArray[isotopeIndex] = mapPhotoMultiplierIsotopeIndicesToStatistics.get(isotopeOrdinalIndex).getMean();
            if (commonSpeciesOrdinalIndices.contains(isotopeOrdinalIndex) &&
                    (photoMultiplierMeansArray[isotopeIndex] > maxPhotoMultiplierCountsMean)) {
                maxPhotoMultiplierCountsMean = photoMultiplierMeansArray[isotopeIndex];
                maxCountPhotoMultiplierIndex = isotopeIndex;
            }
            isotopeIndex++;
        }

        // NOTE: the speciesList has been sorted by increasing abundances in the original analysisMethod setup
        //  the ratios are between each species and the most abundant species, with one less ratio than species
        int indexOfMostAbundantIsotope = mapPhotoMultiplierIsotopeIndicesToStatistics.size() - 1;
        double detectorFaradayGain = photoMultiplierMeansArray[maxCountPhotoMultiplierIndex] / faradayMeansArray[maxCountFaradayIndex];
        double[] logRatios = new double[indexOfMostAbundantIsotope];
        for (int logRatioIndex = 0; logRatioIndex < logRatios.length; logRatioIndex++) {
            logRatios[logRatioIndex] = log(photoMultiplierMeansArray[logRatioIndex] / photoMultiplierMeansArray[indexOfMostAbundantIsotope]);
        }

        /*
        for m=1:d0.Nblock
            II = d0.InterpMat{m};
            dind = ( d0.axflag & d0.block(:,m));
            dd=d0.data(dind)./exp(x0.lograt(d0.iso_vec(dind)));
            [~,dsort]=sort(d0.time_ind(dind));
            dd=dd(dsort);
            I0=(II'*II)^-1*II'*dd;
            x0.I{m} = I0;
        end
         */

        List<Double> dd = new ArrayList<>();
        // NOTE: using the photomultiplier intensity values as set above == same as faraday
        for (int row = 0; row < intensityAccumulatorList.size(); row++) {
            if (isotopeOrdinalIndicesAccumulatorList.get(row) - 1 < logRatios.length) {
                dd.add(intensityAccumulatorList.get(row)
                        / exp(logRatios[isotopeOrdinalIndicesAccumulatorList.get(row) - 1]));
            } else {
                // this used to be the iden/iden ratio, which we eliminated, was 1.0 anyway
                dd.add(intensityAccumulatorList.get(row));
            }
        }
        double[] ddArray = dd.stream().mapToDouble(d -> d).toArray();

        // get indices used in sorting per Matlab [~,dsort]=sort(d0.time_ind(dind));
        int[] timeIndForSortingArrayOnPeakPhotoMult = onPeakPhotoMultiplierDataSetMCMC.timeIndexAccumulatorList().stream().mapToInt(d -> d).toArray();
        ArrayIndexComparator comparator = new ArrayIndexComparator(timeIndForSortingArrayOnPeakPhotoMult);
        Integer[] dsortIndicesOnPeakPhotoMult = comparator.createIndexArray();
        Arrays.sort(dsortIndicesOnPeakPhotoMult, comparator);

        double[] ddSortedArray = new double[ddArray.length];
        for (int i = 0; i < ddArray.length; i++) {
            ddSortedArray[i] = ddArray[dsortIndicesOnPeakPhotoMult[i]];
        }

        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        MatrixStore<Double> interpolatedKnotData = singleBlockDataSetRecord.blockKnotInterpolationStore();
        MatrixStore<Double> ddMatrix = storeFactory.columns(ddSortedArray);
        MatrixStore<Double> tempMatrix = interpolatedKnotData.transpose().multiply(interpolatedKnotData);
        InverterTask<Double> inverter = InverterTask.PRIMITIVE.make(tempMatrix, false, false);
        MatrixStore<Double> tempMatrix2 = inverter.invert(tempMatrix);
        double[] I0 = tempMatrix2.multiply(interpolatedKnotData.transpose()).multiply(ddMatrix).toRawCopy1D();

        /*
            %%% MODEL DATA WITH INITIAL MODEL
            II = d0.InterpMat;

            for m=1:d0.Nfar%+1
                d(d0.blflag & d0.det_ind(:,m),1) = x0.BL(m);  blMeansArray
            end

            for n = 1:d0.Nblock
                Intensity{n} = II{n}*x0.I{n};
                for m=1:d0.Niso;
                    itmp = d0.iso_ind(:,m) & d0.axflag & d0.block(:,n);
                    d(itmp) = exp(x0.lograt(m))*Intensity{n}(d0.time_ind(itmp));

                    itmp = d0.iso_ind(:,m) & ~d0.axflag & d0.block(:,n);
                    d(itmp) = exp(x0.lograt(m))*x0.DFgain^-1 *Intensity{n}(d0.time_ind(itmp)) + x0.BL(d0.det_vec(itmp));
                end
            end
         */

        int faradayCount = mapDetectorOrdinalToFaradayIndex.size();
        int isotopeCount = logRatios.length + 1;
        double[] signalNoiseSigma = new double[faradayCount + 1 + isotopeCount];
        for (faradayIndex = 0; faradayIndex < faradayCount; faradayIndex++) {
            signalNoiseSigma[faradayIndex] = baselineStandardDeviationsArray[faradayIndex];
        }
        // photomultiplier is last detector in array
        signalNoiseSigma[faradayIndex + 1] = 0.0;

        for (isotopeIndex = 0; isotopeIndex < isotopeCount; isotopeIndex++) {
            signalNoiseSigma[faradayCount + 1 + isotopeIndex] = 11.0;
        }

        // initialize model data vectors
        double[] dataArray = new double[totalIntensityCount];
        double[] dataWithNoBaselineArray = new double[dataArray.length];
        double[] dataSignalNoiseArray = new double[dataArray.length];

        // populate dataArray with baseline entries
        detectorOrdinalIndicesAccumulatorList = baselineDataSetMCMC.detectorOrdinalIndicesAccumulatorList();
        for (int dataArrayIndex = 0; dataArrayIndex < baselineCount; dataArrayIndex++) {
            faradayIndex = mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndicesAccumulatorList.get(dataArrayIndex));
            dataArray[dataArrayIndex] = baselineMeansArray[faradayIndex];
            // NOTE: no baseline component here
            double calculatedValue = StrictMath.sqrt(pow(signalNoiseSigma[faradayIndex], 2));
            dataSignalNoiseArray[dataArrayIndex] = calculatedValue;
        }

        MatrixStore<Double> intensities = singleBlockDataSetRecord.blockKnotInterpolationStore().multiply(storeFactory.columns(I0));

        // populate dataArray with onpeak faraday entries
        isotopeOrdinalIndicesAccumulatorList = onPeakFaradayDataSetMCMC.isotopeOrdinalIndicesAccumulatorList();
        detectorOrdinalIndicesAccumulatorList = onPeakFaradayDataSetMCMC.detectorOrdinalIndicesAccumulatorList();
        List<Integer> timeIndexAccumulatorList = onPeakFaradayDataSetMCMC.timeIndexAccumulatorList();
        for (int dataArrayIndex = baselineCount; dataArrayIndex < baselineCount + onPeakFaradayCount; dataArrayIndex++) {
            intensityIndex = timeIndexAccumulatorList.get(dataArrayIndex - baselineCount);
            isotopeIndex = isotopeOrdinalIndicesAccumulatorList.get(dataArrayIndex - baselineCount) - 1;
            faradayIndex = mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndicesAccumulatorList.get(dataArrayIndex - baselineCount));
            if (isotopeIndex < logRatios.length) {
                dataArray[dataArrayIndex] = exp(logRatios[isotopeIndex]) / detectorFaradayGain * intensities.get(intensityIndex, 0) + baselineMeansArray[faradayIndex];
            } else {
                dataArray[dataArrayIndex] = 1.0 / detectorFaradayGain * intensities.get(intensityIndex, 0) + baselineMeansArray[faradayIndex];
            }
            dataWithNoBaselineArray[dataArrayIndex] = dataArray[dataArrayIndex] - baselineMeansArray[faradayIndex];

            double calculatedValue = StrictMath.sqrt(pow(signalNoiseSigma[faradayIndex], 2)
                    + signalNoiseSigma[isotopeIndex + faradayCount + 1]
                    * dataWithNoBaselineArray[dataArrayIndex]);
            dataSignalNoiseArray[dataArrayIndex] = calculatedValue;
        }

        // populate dataArray with onpeak photomultiplier entries
        isotopeOrdinalIndicesAccumulatorList = onPeakPhotoMultiplierDataSetMCMC.isotopeOrdinalIndicesAccumulatorList();
        detectorOrdinalIndicesAccumulatorList = onPeakPhotoMultiplierDataSetMCMC.detectorOrdinalIndicesAccumulatorList();
        // NOTE: onpeak photomultiplier only has one detector and it goes last
        mapDetectorOrdinalToFaradayIndex.put(detectorOrdinalIndicesAccumulatorList.get(0), Integer.valueOf(mapDetectorOrdinalToFaradayIndex.size()));
        timeIndexAccumulatorList = onPeakPhotoMultiplierDataSetMCMC.timeIndexAccumulatorList();
        for (int dataArrayIndex = baselineCount + onPeakFaradayCount; dataArrayIndex < baselineCount + onPeakFaradayCount + onPeakPhotoMultCount; dataArrayIndex++) {
            intensityIndex = timeIndexAccumulatorList.get(dataArrayIndex - baselineCount - onPeakFaradayCount);
            isotopeIndex = isotopeOrdinalIndicesAccumulatorList.get(dataArrayIndex - baselineCount - onPeakFaradayCount).intValue() - 1;
            faradayIndex = mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndicesAccumulatorList.get(dataArrayIndex - baselineCount - onPeakFaradayCount));
            if (isotopeIndex < logRatios.length) {
                dataArray[dataArrayIndex] = exp(logRatios[isotopeIndex]) * intensities.get(intensityIndex, 0);
            } else {
                dataArray[dataArrayIndex] = intensities.get(intensityIndex, 0);
            }
            dataWithNoBaselineArray[dataArrayIndex] = dataArray[dataArrayIndex];

            double calculatedValue = StrictMath.sqrt(StrictMath.pow(signalNoiseSigma[faradayIndex], 2)
                    + signalNoiseSigma[isotopeIndex + faradayCount + 1]
                    * dataWithNoBaselineArray[dataArrayIndex]);
            dataSignalNoiseArray[dataArrayIndex] = calculatedValue;
        }

        /*
            % Define initial sigmas based on baseline
            for m = 1:d0.Nfar%+1
                itmp = d0.det_vec==m & d0.blflag==1;
                x0.sig(m,1) = 1*std(d0.data(itmp));
            end

            x0.sig(d0.Nfar+1,1) = 0;

            for m = 1: d0.Niso;
                itmp = d0.iso_vec==m ;
                x0.sig(d0.Ndet + m,1) = 1.1*10;
            end
         */


        return new SingleBlockModelRecord(
                singleBlockDataSetRecord.blockNumber(),
                baselineMeansArray,
                baselineStandardDeviationsArray,
                detectorFaradayGain,
                mapDetectorOrdinalToFaradayIndex,
                logRatios,
                signalNoiseSigma,
                dataArray,
                dataWithNoBaselineArray,
                dataSignalNoiseArray,
                I0,
                intensities.toRawCopy1D(),
                faradayCount,
                isotopeCount);
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