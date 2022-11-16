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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.mcmc.MassSpecOutputDataRecord;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class DataSourceProcessor_PhoenixSyntheticTextFile implements DataSourceProcessorInterface {
    private final AnalysisMethod analysisMethod;

    private DataSourceProcessor_PhoenixSyntheticTextFile(AnalysisMethod analysisMethod) {
        this.analysisMethod = analysisMethod;
    }

    public static DataSourceProcessor_PhoenixSyntheticTextFile initializeWithAnalysisMethod(AnalysisMethod analysisMethod) {
        return new DataSourceProcessor_PhoenixSyntheticTextFile(analysisMethod);
    }

    @Override
    public MassSpecOutputDataRecord prepareInputDataModelFromFile(Path inputDataFile) throws IOException {

        List<String> contentsByLine = new ArrayList<>(Files.readAllLines(inputDataFile, Charset.defaultCharset()));

//        List<String[]> headerByLineSplit = new ArrayList<>();
//        List<String[]> columnNamesSplit = new ArrayList<>();
        List<String> sequenceIDByLineSplit = new ArrayList<>();
        List<String> blockNumberByLineSplit = new ArrayList<>();
        List<String> cycleNumberByLineSplit = new ArrayList<>();
        List<String> integrationNumberByLineSplit = new ArrayList<>();
        List<String> timeStampByLineSplit = new ArrayList<>();
        List<String> massByLineSplit = new ArrayList<>();
        List<String[]> detectorDataByLineSplit = new ArrayList<>();

        int phase = 0;
        for (String line : contentsByLine) {
            if (!line.isEmpty() && (phase == 2)) {
//                switch (phase) {
//                    case 0 -> headerByLineSplit.add(line.split(","));
//                    case 1 -> columnNamesSplit.add(line.split(","));
//                    case 2 -> {
                String[] lineSplit = line.split(",");
                sequenceIDByLineSplit.add(lineSplit[0]);
                blockNumberByLineSplit.add(lineSplit[1]);
                cycleNumberByLineSplit.add(lineSplit[2]);
                integrationNumberByLineSplit.add(lineSplit[3]);
                timeStampByLineSplit.add(lineSplit[4]);
                massByLineSplit.add(lineSplit[5]);

                detectorDataByLineSplit.add(Arrays.copyOfRange(lineSplit, 6, lineSplit.length));
//                    }
            }
            if (line.startsWith("#START")) {
                phase = 1;
            } else if (phase == 1) {
                phase = 2;
            }
//            }
        }
        String[] sequenceIDs = sequenceIDByLineSplit.toArray(new String[0]);
        int[] blockNumbers = convertListOfNumbersAsStringsToIntegerArray(blockNumberByLineSplit);
        int[] cycleNumbers = convertListOfNumbersAsStringsToIntegerArray(cycleNumberByLineSplit);
//        double[] integrationNumber = convertListOfNumbersAsStringsToDoubleArray(integrationNumberByLineSplit);
        double[] timeStamp = convertListOfNumbersAsStringsToDoubleArray(timeStampByLineSplit);
//        double[] mass = convertListOfNumbersAsStringsToDoubleArray(massByLineSplit);


        // convert detectorDataByLineSplit to doubles array
        int totalCountOfIntegrations = sequenceIDByLineSplit.size();
        double[][] detectorData = new double[totalCountOfIntegrations][];
        int index = 0;
        for (String[] numbersAsStrings : detectorDataByLineSplit) {
            String[] detectorValues = Arrays.copyOfRange(numbersAsStrings, 0, numbersAsStrings.length);
            detectorData[index] = Arrays.stream(detectorValues)
                    .mapToDouble(Double::parseDouble)
                    .toArray();
            index++;
        }


        // blocks start at 1, cycles start at 0 but cycle 1 starts the Sequences
        // new block starts with BL# and cycles restart at 0

        List<Integer> blockList = Ints.asList(blockNumbers);
        List<Integer> blockListWithoutDuplicates
                = Lists.newArrayList(Sets.newLinkedHashSet(blockList));
        // following matlab code
        int blockCount = Math.max(1, blockListWithoutDuplicates.size() - 1);
        // extract cycles per block
        int[] nCycle = new int[blockCount];
        int totalCycles = 0;
        if (blockCount == 1) {
            nCycle[0] = cycleNumbers[cycleNumbers.length - 1] + 1;
            totalCycles = nCycle[0];
        } else {
            int startIndex = 0;
            for (Integer blockNumber : blockListWithoutDuplicates) {
                for (int i = startIndex; i < blockNumbers.length; i++) {
                    if (blockNumbers[i] > blockNumber) {
                        nCycle[blockNumber - 1] = cycleNumbers[i - 1] + 1;
                        totalCycles += nCycle[blockNumber - 1];
                        startIndex = i;
                        break;
                    }
                }
            }
        }

        // collect the starting indices of each cycle in each block
        int currentBlockNumber = 1;
        int currentCycleNumber = 0;
        int currentIndex = 0;
        int currentRecordNumber = 0;
        int row;
        boolean cycleStartRecorded = false;
        int[][] startingIndicesOfCyclesByBlock = new int[totalCycles][4]; //blockNum, cycleNum, startIndex
        for (row = 0; row < blockNumbers.length; row++) {
            if (blockNumbers[row] == currentBlockNumber) {
                if (!cycleStartRecorded) {
                    startingIndicesOfCyclesByBlock[currentRecordNumber] = new int[]{currentBlockNumber, currentCycleNumber, currentIndex};
                    cycleStartRecorded = true;
                }
                if (cycleNumbers[row] > currentCycleNumber) {
                    currentRecordNumber++;
                    currentCycleNumber++;
                    currentIndex = row;
                    startingIndicesOfCyclesByBlock[currentRecordNumber] = new int[]{currentBlockNumber, currentCycleNumber, currentIndex};
                }
            } else {
                // new block
                currentIndex = row;
                currentRecordNumber++;
                currentCycleNumber = 0;
                currentBlockNumber++;
                if (currentRecordNumber >= totalCycles) {
                    break;
                }
                startingIndicesOfCyclesByBlock[currentRecordNumber] = new int[]{currentBlockNumber, currentCycleNumber, currentIndex};
            }
        }

        // build InterpMat for each block using linear approach
        // june 2022 assume 1 block for now
        // the general approach for a block is to create a knot at the start of each cycle and
        // linearly interpolate between knots to create fractional placement of each recorded timestamp
        // which takes the form of (1 - fractional distance of time with knot range, fractional distance of time with knot range)

        // determine max dimension of interpMatArrayForBlock rows
        int maxDelta = Integer.MIN_VALUE;
        for (int cycleByBlockIndex = nCycle[0] - 1; cycleByBlockIndex < startingIndicesOfCyclesByBlock.length; cycleByBlockIndex += nCycle[0]) {
            int delta = startingIndicesOfCyclesByBlock[cycleByBlockIndex][2] - startingIndicesOfCyclesByBlock[cycleByBlockIndex - (nCycle[0] - 1)][2];
            if (delta > maxDelta) {
                maxDelta = delta;
            }
        }

        int[][] indicesOfKnotsByBlock = new int[blockCount][];
        @SuppressWarnings("unchecked")
        MatrixStore<Double>[] allBlockInterpolations = new MatrixStore[nCycle.length];
        for (int blockIndex = 0; blockIndex < blockCount; blockIndex++) {
            PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
            allBlockInterpolations[blockIndex] = null;
            double[][] interpMatArrayForBlock = new double[nCycle[blockIndex]][];
            interpMatArrayForBlock[0] = new double[maxDelta];
            int knotIndex = 0;
            indicesOfKnotsByBlock[blockIndex] = new int[nCycle[blockIndex]];
            for (int cycleIndex = 1; cycleIndex < (nCycle[blockIndex]); cycleIndex++) {
                int startOfCycleIndex = startingIndicesOfCyclesByBlock[cycleIndex][2];
                int startOfNextCycleIndex;
                if ((cycleIndex == nCycle[blockIndex] - 1) && (blockCount == 1)) {
                    startOfNextCycleIndex = timeStamp.length;
                } else {
                    startOfNextCycleIndex = startingIndicesOfCyclesByBlock[cycleIndex + 1][2];
                }

                // detect last cycle because it uses its last entry as the upper limit
                // whereas the matlab code uses the starting entry of the next cycle for all previous cycles
                boolean lastCycle = (cycleIndex == nCycle[blockIndex] - 1);
                if (lastCycle) {
                    startOfNextCycleIndex--;
                }

                int countOfEntries = startingIndicesOfCyclesByBlock[cycleIndex][2] - startingIndicesOfCyclesByBlock[1][2];
                double deltaTimeStamp = timeStamp[startOfNextCycleIndex] - timeStamp[startOfCycleIndex];
                interpMatArrayForBlock[cycleIndex] = new double[maxDelta];

                for (int timeIndex = startOfCycleIndex; timeIndex < startOfNextCycleIndex; timeIndex++) {
                    interpMatArrayForBlock[cycleIndex][(timeIndex - startOfCycleIndex) + countOfEntries] =
                            (timeStamp[timeIndex] - timeStamp[startOfCycleIndex]) / deltaTimeStamp;
                    interpMatArrayForBlock[cycleIndex - 1][(timeIndex - startOfCycleIndex) + countOfEntries] =
                            1.0 - interpMatArrayForBlock[cycleIndex][(timeIndex - startOfCycleIndex) + countOfEntries];
                    if (interpMatArrayForBlock[cycleIndex - 1][(timeIndex - startOfCycleIndex) + countOfEntries] == 1.0) {
                        indicesOfKnotsByBlock[blockIndex][knotIndex++] = (timeIndex - startOfCycleIndex + countOfEntries);
                    }
                }

                if (lastCycle) {
                    interpMatArrayForBlock[cycleIndex][countOfEntries + startOfNextCycleIndex - startOfCycleIndex] = 1.0;
                    indicesOfKnotsByBlock[blockIndex][knotIndex] = countOfEntries + startOfNextCycleIndex - startOfCycleIndex;
                    interpMatArrayForBlock[cycleIndex - 1][countOfEntries + startOfNextCycleIndex - startOfCycleIndex] = 0.0;

                    // generate matrix and then transpose it to match matlab
                    MatrixStore<Double> firstPass = storeFactory.rows(interpMatArrayForBlock).limits(
                            cycleIndex + 1,
                            countOfEntries + startOfNextCycleIndex - startOfCycleIndex + 1);
                    allBlockInterpolations[blockIndex] = firstPass.transpose();
                }
            }
        }

        int faradayCount = analysisMethod.getSequenceTable().getMapOfDetectorsToSequenceCells().keySet().size() - 1;
        int isotopeCount = analysisMethod.getSpeciesList().size();

        // start with Baseline table
        AccumulatedSyntheticData baselineFaradayAccumulator = accumulateBaselineDataPerSequenceTableSpecs(sequenceIDs, detectorData, analysisMethod, true);
        // now sequence table Faraday
        AccumulatedSyntheticData sequenceFaradayAccumulator = accumulateOnPeakDataPerSequenceTableSpecs(sequenceIDs, blockNumbers, blockListWithoutDuplicates, detectorData, timeStamp, analysisMethod, true);
        // now sequence table NOT Faraday (ion counter)
        AccumulatedSyntheticData sequenceIonCounterAccumulator = accumulateOnPeakDataPerSequenceTableSpecs(sequenceIDs, blockNumbers, blockListWithoutDuplicates, detectorData, timeStamp, analysisMethod, false);

        List<Double> dataAccumulatorList = new ArrayList<>();
        dataAccumulatorList.addAll(baselineFaradayAccumulator.dataAccumulatorList());
        dataAccumulatorList.addAll(sequenceFaradayAccumulator.dataAccumulatorList());
        dataAccumulatorList.addAll(sequenceIonCounterAccumulator.dataAccumulatorList());

        List<Double> timeAccumulatorList = new ArrayList<>();
        timeAccumulatorList.addAll(baselineFaradayAccumulator.timeAccumulatorList());
        timeAccumulatorList.addAll(sequenceFaradayAccumulator.timeAccumulatorList());
        timeAccumulatorList.addAll(sequenceIonCounterAccumulator.timeAccumulatorList());

        List<Integer> timeIndAccumulatorList = new ArrayList<>();
        timeIndAccumulatorList.addAll(baselineFaradayAccumulator.timeIndAccumulatorList());
        timeIndAccumulatorList.addAll(sequenceFaradayAccumulator.timeIndAccumulatorList());
        timeIndAccumulatorList.addAll(sequenceIonCounterAccumulator.timeIndAccumulatorList());

        List<Integer> blockIndicesForDataAccumulatorList = new ArrayList<>();
        blockIndicesForDataAccumulatorList.addAll(baselineFaradayAccumulator.blockIndicesForDataAccumulatorList());
        blockIndicesForDataAccumulatorList.addAll(sequenceFaradayAccumulator.blockIndicesForDataAccumulatorList());
        blockIndicesForDataAccumulatorList.addAll(sequenceIonCounterAccumulator.blockIndicesForDataAccumulatorList());

        List<Integer> isotopeIndicesForDataAccumulatorList = new ArrayList<>();
        isotopeIndicesForDataAccumulatorList.addAll(baselineFaradayAccumulator.isotopeIndicesForDataAccumulatorList());
        isotopeIndicesForDataAccumulatorList.addAll(sequenceFaradayAccumulator.isotopeIndicesForDataAccumulatorList());
        isotopeIndicesForDataAccumulatorList.addAll(sequenceIonCounterAccumulator.isotopeIndicesForDataAccumulatorList());

        List<int[]> isotopeFlagsForDataAccumulatorList = new ArrayList<>();
        isotopeFlagsForDataAccumulatorList.addAll(baselineFaradayAccumulator.isotopeFlagsForDataAccumulatorList());
        isotopeFlagsForDataAccumulatorList.addAll(sequenceFaradayAccumulator.isotopeFlagsForDataAccumulatorList());
        isotopeFlagsForDataAccumulatorList.addAll(sequenceIonCounterAccumulator.isotopeFlagsForDataAccumulatorList());

        List<Integer> detectorIndicesForDataAccumulatorList = new ArrayList<>();
        detectorIndicesForDataAccumulatorList.addAll(baselineFaradayAccumulator.detectorIndicesForDataAccumulatorList());
        detectorIndicesForDataAccumulatorList.addAll(sequenceFaradayAccumulator.detectorIndicesForDataAccumulatorList());
        detectorIndicesForDataAccumulatorList.addAll(sequenceIonCounterAccumulator.detectorIndicesForDataAccumulatorList());

        List<int[]> detectorFlagsForDataAccumulatorList = new ArrayList<>();
        detectorFlagsForDataAccumulatorList.addAll(baselineFaradayAccumulator.detectorFlagsForDataAccumulatorList());
        detectorFlagsForDataAccumulatorList.addAll(sequenceFaradayAccumulator.detectorFlagsForDataAccumulatorList());
        detectorFlagsForDataAccumulatorList.addAll(sequenceIonCounterAccumulator.detectorFlagsForDataAccumulatorList());

        List<Integer> baseLineFlagsForDataAccumulatorList = new ArrayList<>();
        baseLineFlagsForDataAccumulatorList.addAll(baselineFaradayAccumulator.baseLineFlagsForDataAccumulatorList());
        baseLineFlagsForDataAccumulatorList.addAll(sequenceFaradayAccumulator.baseLineFlagsForDataAccumulatorList());
        baseLineFlagsForDataAccumulatorList.addAll(sequenceIonCounterAccumulator.baseLineFlagsForDataAccumulatorList());

        List<Integer> ionCounterFlagsForDataAccumulatorList = new ArrayList<>();
        ionCounterFlagsForDataAccumulatorList.addAll(baselineFaradayAccumulator.ionCounterFlagsForDataAccumulatorList());
        ionCounterFlagsForDataAccumulatorList.addAll(sequenceFaradayAccumulator.ionCounterFlagsForDataAccumulatorList());
        ionCounterFlagsForDataAccumulatorList.addAll(sequenceIonCounterAccumulator.ionCounterFlagsForDataAccumulatorList());

        List<Integer> signalIndexForDataAccumulatorList = new ArrayList<>();
        signalIndexForDataAccumulatorList.addAll(baselineFaradayAccumulator.signalIndexForDataAccumulatorList());
        // add baseline highest signal index to Faraday items
        int lastSignalIndexUsed = signalIndexForDataAccumulatorList.get(signalIndexForDataAccumulatorList.size() - 1);
        List<Integer> faradaySignalIndices = sequenceFaradayAccumulator.signalIndexForDataAccumulatorList();
        for (final ListIterator<Integer> iterator = faradaySignalIndices.listIterator(); iterator.hasNext(); ) {
            final Integer element = iterator.next();
            iterator.set(element + lastSignalIndexUsed);
        }
        signalIndexForDataAccumulatorList.addAll(faradaySignalIndices);
        // add faraday highest signal index to Axial items
        lastSignalIndexUsed = signalIndexForDataAccumulatorList.get(signalIndexForDataAccumulatorList.size() - 1);
        List<Integer> axialSignalIndices = sequenceIonCounterAccumulator.signalIndexForDataAccumulatorList();
        for (final ListIterator<Integer> iterator = axialSignalIndices.listIterator(); iterator.hasNext(); ) {
            final Integer element = iterator.next();
            iterator.set(element + lastSignalIndexUsed);
        }
        signalIndexForDataAccumulatorList.addAll(axialSignalIndices);

        // convert to arrays to  build parameters for MassSpecOutputDataRecord record
        double[] dataAccumulatorArray = dataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        double[] timeAccumulatorArray = timeAccumulatorList.stream().mapToDouble(d -> d).toArray();
        double[] timeIndAccumulatorArray = timeIndAccumulatorList.stream().mapToDouble(d -> d).toArray();
        double[] blockIndicesForDataAccumulatorArray = blockIndicesForDataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        double[] isotopeIndicesForDataAccumulatorArray = isotopeIndicesForDataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        double[][] isotopeFlagsForDataAccumulatorArray = new double[isotopeFlagsForDataAccumulatorList.size()][];

        int i = 0;
        for (int[] isotopeFlags : isotopeFlagsForDataAccumulatorList) {
            isotopeFlagsForDataAccumulatorArray[i] = new double[isotopeFlags.length];
            for (int iso = 0; iso < isotopeFlags.length; iso++) {
                isotopeFlagsForDataAccumulatorArray[i][iso] = isotopeFlags[iso];
            }
            i++;
        }

        double[] detectorIndicesForDataAccumulatorArray = detectorIndicesForDataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        double[][] detectorFlagsForDataAccumulatorArray = new double[detectorFlagsForDataAccumulatorList.size()][];
        i = 0;
        for (int[] detectorFlags : detectorFlagsForDataAccumulatorList) {
            detectorFlagsForDataAccumulatorArray[i] = new double[detectorFlags.length];
            for (int d = 0; d < detectorFlags.length; d++) {
                detectorFlagsForDataAccumulatorArray[i][d] = detectorFlags[d];
            }
            i++;
        }

        double[] baseLineFlagsForDataAccumulatorArray = baseLineFlagsForDataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        double[] ionCounterFlagsForDataAccumulatorArray = ionCounterFlagsForDataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        double[] signalIndicesForDataAccumulatorArray = signalIndexForDataAccumulatorList.stream().mapToDouble(d -> d).toArray();

        // this stuff might get used

//        // Time_Far = repmat(Time,1,Nfar);
//        double[][] timeFarArray = new double[faradayCount][timeStamp.length];
//        for (int far = 0; far < faradayCount; far++) {
//            timeFarArray[far] = timeStamp.clone();
//        }
//        Matrix timeFar = new Matrix(timeFarArray).transpose();

        /*
            for m = 1:Niso
                mmass = abs(Mass-Isotopes(m))<0.25;
                Ax_ind(mmass,1) = m;
                Far_ind(mmass,:) = repmat(F_ind(m,:),sum(mmass),1);
            end
         */
        // Far_ind and Ax_ind
//        double[][] isotopeIndicesPerFaraday = sequenceFaradayAccumulator.isotopeIndicesPerFaradayOrAxial().clone();
//        double[][] isotopeIndicesPerAxial = sequenceIonCounterAccumulator.isotopeIndicesPerFaradayOrAxial().clone();

        /*
            for m = 1:Nblock
                for n = 1:Ncycle(m)-1
                    medCycleTime(m,n) = median(Time(Block==m & Cycle==n));
                    minCycleTime(m,n) = min(Time(Block==m & Cycle==n));
                    maxCycleTime(m,n) = max(Time(Block==m & Cycle==n));

                    iminCT(m,n) = find(Time==minCycleTime(m,n));
                    imaxCT(m,n) = find(Time==maxCycleTime(m,n));

                end
            end

            Tknots0 = [minCycleTime maxCycleTime(:,end)];
            iTknots0 =  [iminCT (:,end)];
         */

////        double[][] medCycleTime = new double[blockCount][nCycle[blockIndex] - 1];
//        double[][] minCycleTime = new double[blockCount][nCycle[blockIndex] - 1];
//        double[][] maxCycleTime = new double[blockCount][nCycle[blockIndex] - 1];
//        double[][] iminCT = new double[blockCount][nCycle[blockIndex] - 1];
//        double[][] imaxCT = new double[blockCount][nCycle[blockIndex] - 1];
//        for (blockIndex = 0; blockIndex < blockCount; blockIndex++) {
//            for (int cycleIndex = 1; cycleIndex < nCycle[blockIndex]; cycleIndex++) {
//                DescriptiveStatistics descriptiveStatisticsA = new DescriptiveStatistics();
//                for (row = 0; row < blockNumbers.length; row++) {
//                    if ((blockNumbers[row] == blockIndex + 1) && (cycleNumbers[row] == cycleIndex)) {
//                        descriptiveStatisticsA.addValue(timeStamp[row]);
//                    }
//                }
////                medCycleTime[blockIndex][cycleIndex - 1] = descriptiveStatisticsA.getPercentile(50);
//                minCycleTime[blockIndex][cycleIndex - 1] = descriptiveStatisticsA.getMin();
//                maxCycleTime[blockIndex][cycleIndex - 1] = descriptiveStatisticsA.getMax();
//                for (row = 0; row < timeStamp.length; row++) {
//                    if (timeStamp[row] >= minCycleTime[blockIndex][cycleIndex - 1]) {
//                        iminCT[blockIndex][cycleIndex - 1] = row;
//                        break;
//                    }
//                }
//                for (row = 0; row < timeStamp.length; row++) {
//                    if (timeStamp[row] >= maxCycleTime[blockIndex][cycleIndex - 1]) {
//                        imaxCT[blockIndex][cycleIndex - 1] = row;
//                        break;
//                    }
//                }
//            }
//        }

//        // to make knots, copy min array and add new column from last column of max array
//        blockIndex = 0;
//        double[][] tKnots0 = new double[minCycleTime.length][nCycle[blockIndex]];
//        double[][] iTKnots0 = new double[minCycleTime.length][nCycle[blockIndex]];
//        for (row = 0; row < minCycleTime.length; row++) {
//            for (int col = 0; col < (nCycle[blockIndex] - 1); col++) {
//                tKnots0[row][col] = minCycleTime[row][col];
//                iTKnots0[row][col] = iminCT[row][col];
//            }
//            tKnots0[row][nCycle[blockIndex] - 1] = maxCycleTime[row][nCycle[blockIndex] - 2];
//            iTKnots0[row][nCycle[blockIndex] - 1] = imaxCT[row][nCycle[blockIndex] - 2];
//        }

    /*
        for m=1:Nblock
            Block_Time{m} = Time(iTknots0(m,1):iTknots0(m,end));
            InterpMat{m} = interp1(Tknots0(m,:),eye(length(Tknots0(m,:))),Block_Time{m},'linear');

            Nknots(m) = length(Tknots0(m,:));
            Ntb(m) = length(Block_Time{m});
        end

        ftimeind = repmat([1:Nsamptot]',1,Nfar);
     */
//        double[][] blockTime = new double[blockCount][];
//        double[] nKnots = new double[blockCount];
//        double[] nTb = new double[blockCount];
//        for (blockIndex = 0; blockIndex < blockCount; blockIndex++) {
//            blockTime[blockIndex] = Arrays.copyOfRange(timeStamp, (int) iTKnots0[blockIndex][0], (int) iTKnots0[blockIndex][nCycle[blockIndex] - 1] + 1);
//            // interpolation for block 1 done above
//            // TODO: Extend to all blocks
//            nKnots[blockIndex] = tKnots0[blockIndex].length;
//            nTb[blockIndex] = blockTime[blockIndex].length;
//        }

//        // d0.Nsamptot >> countOfSamples
//        int countOfSamples = sequenceIDByLineSplit.size();
//        // identical columns for each faraday
//        double[][] faradayTimeIndices = new double[countOfSamples][faradayCount];
//        row = 0;
//        for (double[] rowS : faradayTimeIndices) {
//            Arrays.fill(rowS, row);
//            row++;
//        }

        /*
            Matlab code >> here
            d0.data >> rawDataColumn
            d0.time >> timeColumn
            d0.time_ind >> timeIndColumn
            d0.sig_ind >> signalIndicesForRawDataColumn
            d0.block >> blockIndicesForRawDataColumn (1-based block number for sequence data, BaseLine data is set to block 0)
            d0.iso_vec >> isotopeIndicesForRawDataColumn (isotopes are indexed starting at 1)
            d0.iso_ind >> isotopeFlagsForRawDataColumn (each isotope has a column and a 1 denotes it is being read)
            d0.det_vec >> detectorIndicesForRawDataColumn (detectors are indexed from 1 through all Faraday and the last is the Axial (Daly)))
            d0.det_ind >> detectorFlagsForRawDataColumn (each Faraday has a column and the last column is for Daly; 1 flags detector used)
            d0.blflag >> baseLineFlagsForRawDataColumn (contains 1 for baseline, 0 for sequence)
            d0.axflag >> ionCounterFlagsForRawDataColumn (contains 1 for data from DALY detector, 0 otherwise)
            d0.InterpMat >> firstBlockInterpolationsMatrix  (matlab actually puts matrices into cells)
            d0.Nfar >> faradayCount
            d0.Niso >> isotopeCount
            d0.Nblock >> blockCount
         */

        return new MassSpecOutputDataRecord(
                dataAccumulatorArray,
                timeAccumulatorArray,
                timeIndAccumulatorArray,
                signalIndicesForDataAccumulatorArray,
                blockIndicesForDataAccumulatorArray,
                isotopeIndicesForDataAccumulatorArray,
                isotopeFlagsForDataAccumulatorArray,
                detectorIndicesForDataAccumulatorArray,
                detectorFlagsForDataAccumulatorArray,
                baseLineFlagsForDataAccumulatorArray,
                ionCounterFlagsForDataAccumulatorArray,
                allBlockInterpolations,
                indicesOfKnotsByBlock,
                faradayCount,
                isotopeCount,
                blockCount,
                nCycle);
    }


    // helper methods **************************************************************************************************
    private double[] convertListOfNumbersAsStringsToDoubleArray(List<String> listToConvert) {
        double[] retVal = new double[listToConvert.size()];
        int index = 0;
        for (String blockNumberAsString : listToConvert) {
            retVal[index] = Double.parseDouble(blockNumberAsString);
            index++;
        }

        return retVal;
    }

    private int[] convertListOfNumbersAsStringsToIntegerArray(List<String> listToConvert) {
        int[] retVal = new int[listToConvert.size()];
        int index = 0;
        for (String blockNumberAsString : listToConvert) {
            retVal[index] = Integer.parseInt(blockNumberAsString);
            index++;
        }

        return retVal;
    }
}