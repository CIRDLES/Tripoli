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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputSingleBlockRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.methods.baseline.BaselineCell;
import org.cirdles.tripoli.sessions.analysis.methods.baseline.BaselineTable;
import org.cirdles.tripoli.sessions.analysis.methods.sequence.SequenceCell;
import org.cirdles.tripoli.sessions.analysis.methods.sequence.SequenceTable;
import org.cirdles.tripoli.species.SpeciesRecordInterface;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.mathUtilities.SplineBasisModel;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.task.InverterTask;

import java.util.*;

import static java.lang.StrictMath.exp;
import static java.lang.StrictMath.log;

/**
 * @author James F. Bowring
 */
public enum DataPrepForMCMC {
    ;

    public static void initializeModelForSingleBlockMCMC(AccumulatedsingleBlockDataForMCMC accumulatedsingleBlockDataForMCMC) throws RecoverableCondition {

        int baselineCount = accumulatedsingleBlockDataForMCMC.baselineDataSetMCMC().intensityAccumulatorList().size();
        int onPeakFaradayCount = accumulatedsingleBlockDataForMCMC.onPeakFaradayDataSetMCMC().intensityAccumulatorList().size();
        int onPeakPhotoMultCount = accumulatedsingleBlockDataForMCMC.onPeakPhotoMultiplierDataSetMCMC().intensityAccumulatorList().size();
        int totalIntensityCount = baselineCount + onPeakFaradayCount + onPeakPhotoMultCount;

        // Baseline statistics *****************************************************************************************
        /*
            for m=1:d0.Nfar%+1
                x0.BL(m,1) = mean(d0.data(d0.blflag & d0.det_ind(:,m)));
                x0.BLstd(m,1) = std(d0.data(d0.blflag & d0.det_ind(:,m)));
            end
         */
        AccumulatedMCMCData baselineDataSetMCMC = accumulatedsingleBlockDataForMCMC.baselineDataSetMCMC();
        List<Integer> detectorOrdinalIndicesAccumulatorList = baselineDataSetMCMC.detectorOrdinalIndicesAccumulatorList();
        List<Double> intensityAccumulatorList = baselineDataSetMCMC.intensityAccumulatorList();
        Map<Integer, DescriptiveStatistics> mapFaradayDetectorIndicesToStatistics = new TreeMap<>();
        Map<Integer, Integer> mapDetectorOrdinalToFaradayIndex = new TreeMap<>();

        int intensityIndex = 0;
        for (Integer detectorOrdinalIndex : detectorOrdinalIndicesAccumulatorList) {
            if (!mapFaradayDetectorIndicesToStatistics.keySet().contains(detectorOrdinalIndex)) {
                mapFaradayDetectorIndicesToStatistics.put(detectorOrdinalIndex, new DescriptiveStatistics());
            }
            mapFaradayDetectorIndicesToStatistics.get(detectorOrdinalIndex).addValue(intensityAccumulatorList.get(intensityIndex));
            intensityIndex++;
        }

        double[] baselineMeansArray = new double[mapFaradayDetectorIndicesToStatistics.keySet().size()];
        double[] baselineStandardDeviationArray = new double[baselineMeansArray.length];
        int faradayIndex = 0;
        for (Integer detectorOrdinalIndex : mapFaradayDetectorIndicesToStatistics.keySet()) {
            baselineMeansArray[faradayIndex] = mapFaradayDetectorIndicesToStatistics.get(detectorOrdinalIndex).getMean();
            baselineStandardDeviationArray[faradayIndex] = mapFaradayDetectorIndicesToStatistics.get(detectorOrdinalIndex).getStandardDeviation();
            mapDetectorOrdinalToFaradayIndex.put(detectorOrdinalIndex, faradayIndex);
            faradayIndex++;
        }

        // initialize model data vector
        double[] dataArray = new double[totalIntensityCount];
        // populate baseline entries
        int dataArrayIndex = 0;
        for (Integer detectorOrdinalIndex : detectorOrdinalIndicesAccumulatorList){
            faradayIndex = mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndex);
            dataArray[dataArrayIndex] = baselineMeansArray[faradayIndex];
            dataArrayIndex ++;
        }

        // OnPeak statistics by faraday ********************************************************************************
        /*
        for m=1:d0.Niso;
            tmpCounts(m,1) = mean(d0.data( (d0.iso_ind(:,m) & d0.axflag)));

            itmp = (d0.iso_ind(:,m) & ~d0.axflag);
            tmpFar(m,1)  = mean(d0.data(itmp)-x0.BL(d0.det_vec(itmp)));
        end
         */
        AccumulatedMCMCData onPeakFaradayDataSetMCMC = accumulatedsingleBlockDataForMCMC.onPeakFaradayDataSetMCMC();
        List<Integer> isotopeOrdinalIndicesAccumulatorList = onPeakFaradayDataSetMCMC.isotopeOrdinalIndicesAccumulatorList();
        detectorOrdinalIndicesAccumulatorList = onPeakFaradayDataSetMCMC.detectorOrdinalIndicesAccumulatorList();
        intensityAccumulatorList = onPeakFaradayDataSetMCMC.intensityAccumulatorList();
        Map<Integer, DescriptiveStatistics> mapFaradayIsotopeIndicesToStatistics = new TreeMap<>();

        intensityIndex = 0;
        for (Integer isotopeOrdinalIndex : isotopeOrdinalIndicesAccumulatorList) {
            if (!mapFaradayIsotopeIndicesToStatistics.keySet().contains(isotopeOrdinalIndex)) {
                mapFaradayIsotopeIndicesToStatistics.put(isotopeOrdinalIndex, new DescriptiveStatistics());
            }
            mapFaradayIsotopeIndicesToStatistics.get(isotopeOrdinalIndex).addValue(
                    intensityAccumulatorList.get(intensityIndex)
                            - baselineMeansArray[mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndicesAccumulatorList.get(intensityIndex))]);
            intensityIndex++;
        }

        double[] faradayMeansArray = new double[mapFaradayIsotopeIndicesToStatistics.keySet().size()];
        int isotopeIndex = 0;
        for (Integer isotopeOrdinalIndex : mapFaradayIsotopeIndicesToStatistics.keySet()) {
            faradayMeansArray[isotopeIndex] = mapFaradayIsotopeIndicesToStatistics.get(isotopeOrdinalIndex).getMean();
            isotopeIndex++;
        }

//        // populate onPeak Faraday entries
//        for (Integer isotopeOrdinalIndex : isotopeOrdinalIndicesAccumulatorList){
//
//            if (isotopeIndex < logRatios.length - 1) {
//                dataArray[row] = exp(logRatios[isotopeIndex]) / dfGain
//                        * intensity.get((int) massSpecOutputDataRecord.timeIndColumn()[row] - 1, 0)
//                        + blMeansArray[(int) massSpecOutputDataRecord.detectorIndicesForRawDataColumn()[row] - 1];
//            } else {
//                dataArray[row] = 1.0 / dfGain
//                        * intensity.get((int) massSpecOutputDataRecord.timeIndColumn()[row] - 1, 0)
//                        + blMeansArray[(int) massSpecOutputDataRecord.detectorIndicesForRawDataColumn()[row] - 1];
//            }
//
//            dataArray[dataArrayIndex] = baselineMeansArray[faradayIndex];
//            dataArrayIndex ++;
//        }

        // OnPeak statistics by photomultiplier ************************************************************************
        AccumulatedMCMCData onPeakPhotoMultiplierDataSetMCMC = accumulatedsingleBlockDataForMCMC.onPeakPhotoMultiplierDataSetMCMC();
        isotopeOrdinalIndicesAccumulatorList = onPeakPhotoMultiplierDataSetMCMC.isotopeOrdinalIndicesAccumulatorList();
        intensityAccumulatorList = onPeakPhotoMultiplierDataSetMCMC.intensityAccumulatorList();
        Map<Integer, DescriptiveStatistics> mapPhotoMultiplierIsotopeIndicesToStatistics = new TreeMap<>();

        intensityIndex = 0;
        for (Integer isotopeOrdinalIndex : isotopeOrdinalIndicesAccumulatorList) {
            if (!mapPhotoMultiplierIsotopeIndicesToStatistics.keySet().contains(isotopeOrdinalIndex)) {
                mapPhotoMultiplierIsotopeIndicesToStatistics.put(isotopeOrdinalIndex, new DescriptiveStatistics());
            }
            mapPhotoMultiplierIsotopeIndicesToStatistics.get(isotopeOrdinalIndex).addValue(
                    intensityAccumulatorList.get(intensityIndex));
            intensityIndex++;
        }

        double[] photoMultiplierMeansArray = new double[mapPhotoMultiplierIsotopeIndicesToStatistics.keySet().size()];
        isotopeIndex = 0;
        for (Integer isotopeOrdinalIndex : mapPhotoMultiplierIsotopeIndicesToStatistics.keySet()) {
            photoMultiplierMeansArray[isotopeIndex] = mapPhotoMultiplierIsotopeIndicesToStatistics.get(isotopeOrdinalIndex).getMean();
            isotopeIndex++;
        }

        /*
            [~,imaxC] = max(tmpCounts);
            iden = d0.Niso;
            x0.DFgain = tmpCounts(imaxC)/tmpFar(imaxC);
            for m=1:d0.Niso
                x0.lograt(m,1) = log(tmpCounts(m)/tmpCounts(iden));
            end
        */
        // find index of photoMultiplierMeansArray max value
        int maxCountIndex = -1;
        double maxCountsMean = Double.MIN_VALUE;
        for (int i = 0; i < photoMultiplierMeansArray.length; i++) {
            if (photoMultiplierMeansArray[i] > maxCountsMean) {
                maxCountsMean = photoMultiplierMeansArray[i];
                maxCountIndex = i;
            }
        }

        // NOTE: the speciesList has been sorted by increasing abundances in the original analysisMethod setup
        //  the ratios are between each species and the most abundant species, with one less ratio than species
        int indexOfMostAbundantIsotope = mapPhotoMultiplierIsotopeIndicesToStatistics.size() - 1;
        double dfGain = photoMultiplierMeansArray[maxCountIndex] / faradayMeansArray[maxCountIndex];
        double[] logRatios = new double[mapPhotoMultiplierIsotopeIndicesToStatistics.size() - 1];
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
        // NOTE: using the photomultiplier values as set above
        for (int row = 0; row < intensityAccumulatorList.size(); row++) {
            if (isotopeOrdinalIndicesAccumulatorList.get(row) - 1 < logRatios.length) {
                dd.add(intensityAccumulatorList.get(row)
                        / exp(logRatios[(int) isotopeOrdinalIndicesAccumulatorList.get(row) - 1]));
            } else {
                // this used to be the iden/iden ratio, which we eliminated, was 1.0 anyway
                dd.add(intensityAccumulatorList.get(row));
            }
        }
        double[] ddArray = dd.stream().mapToDouble(d -> d).toArray();

        // get indices used in sorting per Matlab [~,dsort]=sort(d0.time_ind(dind));
        double[] timeIndForSortingArray = onPeakPhotoMultiplierDataSetMCMC.timeAccumulatorList.stream().mapToDouble(d -> d).toArray();
        ArrayIndexComparator comparator = new ArrayIndexComparator(timeIndForSortingArray);
        Integer[] dsortIndices = comparator.createIndexArray();
        Arrays.sort(dsortIndices, comparator);

        double[] ddSortedArray = new double[ddArray.length];
        for (int i = 0; i < ddArray.length; i++) {
            ddSortedArray[i] = ddArray[dsortIndices[i]];
        }

        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        MatrixStore<Double> interpolatedKnotData = accumulatedsingleBlockDataForMCMC.blockKnotInterpolationStore();
        MatrixStore<Double> ddMatrix = storeFactory.columns(ddSortedArray);
        MatrixStore<Double> tempMatrix = interpolatedKnotData.transpose().multiply(interpolatedKnotData);
        InverterTask<Double> inverter = InverterTask.PRIMITIVE.make(tempMatrix, false, false);
        MatrixStore<Double> tempMatrix2 = inverter.invert(tempMatrix);
        double[] IO = tempMatrix2.multiply(interpolatedKnotData.transpose()).multiply(ddMatrix).toRawCopy1D();

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




    }

    static class ArrayIndexComparator implements Comparator<Integer> {
        private final double[] array;

        public ArrayIndexComparator(double[] array) {
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
            return Double.compare(array[index1], array[index2]);
        }
    }

    public static AccumulatedsingleBlockDataForMCMC prepareSingleBlockDataForMCMC(int blockNumber, MassSpecExtractedData massSpecExtractedData, AnalysisMethod analysisMethod) throws TripoliException {
        MassSpecOutputSingleBlockRecord massSpecOutputSingleBlockRecord = massSpecExtractedData.getBlocksData().get(blockNumber);
        Primitive64Store blockKnotInterpolationStore = generateKnotsMatrixForBlock(massSpecOutputSingleBlockRecord, 1);
        AccumulatedMCMCData baselineDataSetMCMC = accumulateBaselineDataPerBaselineTableSpecs(massSpecOutputSingleBlockRecord, analysisMethod);
        AccumulatedMCMCData onPeakFaradayDataSetMCMC = accumulateOnPeakDataPerSequenceTableSpecs(massSpecOutputSingleBlockRecord, analysisMethod, true);
        AccumulatedMCMCData onPeakPhotoMultiplierDataSetMCMC = accumulateOnPeakDataPerSequenceTableSpecs(massSpecOutputSingleBlockRecord, analysisMethod, false);

        AccumulatedsingleBlockDataForMCMC accumulatedsingleBlockDataForMCMC =
                new AccumulatedsingleBlockDataForMCMC(baselineDataSetMCMC, onPeakFaradayDataSetMCMC, onPeakPhotoMultiplierDataSetMCMC, blockKnotInterpolationStore);

        // TODO: break this up
        try {
            initializeModelForSingleBlockMCMC(accumulatedsingleBlockDataForMCMC);
        } catch (RecoverableCondition e) {
            throw new TripoliException("Ojalgo RecoverableCondition");
        }


        return accumulatedsingleBlockDataForMCMC;
    }

    private static Primitive64Store generateKnotsMatrixForBlock(
            MassSpecOutputSingleBlockRecord massSpecOutputSingleBlockRecord, int basisDegree) {

        int knotCount = massSpecOutputSingleBlockRecord.onPeakStartingIndicesOfCycles().length + 1;
        double[] timeStamps = massSpecOutputSingleBlockRecord.onPeakTimeStamps();

        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        Primitive64Store bBaseOutput = SplineBasisModel.bBase(
                storeFactory.rows(massSpecOutputSingleBlockRecord.onPeakTimeStamps()),
                timeStamps[0],
                timeStamps[timeStamps.length - 1],
                knotCount - basisDegree,
                basisDegree);

        return bBaseOutput;
    }

    private static AccumulatedMCMCData accumulateBaselineDataPerBaselineTableSpecs(
            MassSpecOutputSingleBlockRecord massSpecOutputSingleBlockRecord, AnalysisMethod analysisMethod) {

        BaselineTable baselineTable = analysisMethod.getBaselineTable();
        List<Integer> indexAccumulatorList = new ArrayList<>();
        List<Integer> detectorOrdinalIndicesAccumulatorList = new ArrayList<>();
        List<Double> intensityAccumulatorList = new ArrayList<>();
        List<Double> timeAccumulatorList = new ArrayList<>();
        List<String> baselineSequenceAccumulatorList = new ArrayList<>();
        List<String> onPeakSequenceAccumulatorList = new ArrayList<>();
        List<Integer> isotopeOrdinalIndicesAccumulatorList = new ArrayList<>();

        double[][] baselineIntensities = massSpecOutputSingleBlockRecord.baselineIntensities();
        Map<String, List<Integer>> mapOfBaselineIdsToIndices = massSpecOutputSingleBlockRecord.mapOfBaselineIdsToIndices();

        // this map is in ascending detector order
        Map<Detector, List<BaselineCell>> detectorToBaselineCellMap = baselineTable.getMapOfDetectorsToBaselineCells();
        for (Detector detector : detectorToBaselineCellMap.keySet()) {
            if (detector.isFaraday()) {
                int detectorDataColumnIndex = detector.getOrdinalIndex();
                List<BaselineCell> baselineCells = detectorToBaselineCellMap.get(detector);
                for (BaselineCell baselineCell : baselineCells) {
                    String baselineID = baselineCell.getBaselineID();
                    List<Integer> baselineIndices = mapOfBaselineIdsToIndices.get(baselineID);
                    Collections.sort(baselineIndices);
                    for (Integer index : baselineIndices) {
                        indexAccumulatorList.add(index);
                        detectorOrdinalIndicesAccumulatorList.add(detectorDataColumnIndex);
                        intensityAccumulatorList.add(baselineIntensities[index][detectorDataColumnIndex]);
                        timeAccumulatorList.add(0.0);
                        baselineSequenceAccumulatorList.add(baselineID);
                        onPeakSequenceAccumulatorList.add("");
                        isotopeOrdinalIndicesAccumulatorList.add(0);
                    }
                }
            }
        }

        return new AccumulatedMCMCData(
                indexAccumulatorList,
                detectorOrdinalIndicesAccumulatorList,
                intensityAccumulatorList,
                timeAccumulatorList,
                baselineSequenceAccumulatorList,
                onPeakSequenceAccumulatorList,
                isotopeOrdinalIndicesAccumulatorList);
    }

    private static AccumulatedMCMCData accumulateOnPeakDataPerSequenceTableSpecs(
            MassSpecOutputSingleBlockRecord massSpecOutputSingleBlockRecord, AnalysisMethod analysisMethod, boolean isFaraday) {

        SequenceTable sequenceTable = analysisMethod.getSequenceTable();
        List<SpeciesRecordInterface> speciesList = analysisMethod.getSpeciesList();

        List<Integer> indexAccumulatorList = new ArrayList<>();
        List<Integer> detectorOrdinalIndicesAccumulatorList = new ArrayList<>();
        List<Double> intensityAccumulatorList = new ArrayList<>();
        List<Double> timeAccumulatorList = new ArrayList<>();
        List<String> baselineSequenceAccumulatorList = new ArrayList<>();
        List<String> onPeakSequenceAccumulatorList = new ArrayList<>();
        List<Integer> isotopeOrdinalIndicesAccumulatorList = new ArrayList<>();

        double[][] onPeakIntensities = massSpecOutputSingleBlockRecord.onPeakIntensities();
        double[] onPeakTimeStamps = massSpecOutputSingleBlockRecord.onPeakTimeStamps();
        Map<String, List<Integer>> mapOfOnPeakIdsToIndices = massSpecOutputSingleBlockRecord.mapOfOnPeakIdsToIndices();

        // this map is in ascending detector order
        Map<Detector, List<SequenceCell>> detectorToSequenceCellMap = sequenceTable.getMapOfDetectorsToSequenceCells();
        for (Detector detector : detectorToSequenceCellMap.keySet()) {
            if (detector.isFaraday() == isFaraday) {
                int detectorDataColumnIndex = detector.getOrdinalIndex();
                List<SequenceCell> sequenceCells = detectorToSequenceCellMap.get(detector);
                for (SequenceCell sequenceCell : sequenceCells) {
                    String onPeakID = sequenceCell.getSequenceId();
                    SpeciesRecordInterface targetSpecies = sequenceCell.getTargetSpecies();
                    int speciesOrdinalIndex = speciesList.indexOf(targetSpecies) + 1;
                    List<Integer> onPeakIndices = mapOfOnPeakIdsToIndices.get(onPeakID);
                    Collections.sort(onPeakIndices);
                    for (Integer index : onPeakIndices) {
                        indexAccumulatorList.add(index);
                        detectorOrdinalIndicesAccumulatorList.add(detectorDataColumnIndex);
                        intensityAccumulatorList.add(onPeakIntensities[index][detectorDataColumnIndex]);
                        timeAccumulatorList.add(onPeakTimeStamps[index]);
                        baselineSequenceAccumulatorList.add("");
                        onPeakSequenceAccumulatorList.add(onPeakID);
                        isotopeOrdinalIndicesAccumulatorList.add(speciesOrdinalIndex);
                    }
                }
            }
        }

        return new AccumulatedMCMCData(
                indexAccumulatorList,
                detectorOrdinalIndicesAccumulatorList,
                intensityAccumulatorList,
                timeAccumulatorList,
                baselineSequenceAccumulatorList,
                onPeakSequenceAccumulatorList,
                isotopeOrdinalIndicesAccumulatorList);
    }

    record AccumulatedsingleBlockDataForMCMC(
            AccumulatedMCMCData baselineDataSetMCMC,
            AccumulatedMCMCData onPeakFaradayDataSetMCMC,
            AccumulatedMCMCData onPeakPhotoMultiplierDataSetMCMC,
            Primitive64Store blockKnotInterpolationStore
    ) {
    }

    record AccumulatedMCMCData(
            List<Integer> indexAccumulatorList,
            List<Integer> detectorOrdinalIndicesAccumulatorList,
            List<Double> intensityAccumulatorList,
            List<Double> timeAccumulatorList,
            List<String> baselineSequenceAccumulatorList,
            List<String> onPeakSequenceAccumulatorList,
            List<Integer> isotopeOrdinalIndicesAccumulatorList
    ) {
    }
}