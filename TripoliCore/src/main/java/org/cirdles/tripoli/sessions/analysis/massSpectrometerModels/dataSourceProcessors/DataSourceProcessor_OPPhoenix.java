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
import jama.Matrix;
import org.cirdles.tripoli.sessions.analysis.analysisMethods.AnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.MassSpecOutputDataRecord;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataSourceProcessor_OPPhoenix implements DataSourceProcessorInterface {
    private final AnalysisMethod analysisMethod;

    private DataSourceProcessor_OPPhoenix(AnalysisMethod analysisMethod) {
        this.analysisMethod = analysisMethod;
    }

    public static DataSourceProcessor_OPPhoenix initializeWithAnalysisMethod(AnalysisMethod analysisMethod) {
        return new DataSourceProcessor_OPPhoenix(analysisMethod);
    }

    @Override
    public MassSpecOutputDataRecord prepareInputDataModelFromFile(Path inputDataFile) throws IOException {

        List<String> contentsByLine = new ArrayList<>(Files.readAllLines(inputDataFile, Charset.defaultCharset()));

        List<String[]> headerByLineSplit = new ArrayList<>();
        List<String[]> columnNamesSplit = new ArrayList<>();
        List<String> sequenceIDByLineSplit = new ArrayList<>();
        List<String> blockNumberByLineSplit = new ArrayList<>();
        List<String> cycleNumberByLineSplit = new ArrayList<>();
        List<String> integrationNumberByLineSplit = new ArrayList<>();
        List<String> timeStampByLineSplit = new ArrayList<>();
        List<String> massByLineSplit = new ArrayList<>();
        List<String[]> detectorDataByLineSplit = new ArrayList<>();

        int phase = 0;
        for (String line : contentsByLine) {
            if (!line.isEmpty()) {
                switch (phase) {
                    case 0 -> headerByLineSplit.add(line.split(","));
                    case 1 -> columnNamesSplit.add(line.split(","));
                    case 2 -> {
                        String[] lineSplit = line.split(",");
                        sequenceIDByLineSplit.add(lineSplit[0]);
                        blockNumberByLineSplit.add(lineSplit[1]);
                        cycleNumberByLineSplit.add(lineSplit[2]);
                        integrationNumberByLineSplit.add(lineSplit[3]);
                        timeStampByLineSplit.add(lineSplit[4]);
                        massByLineSplit.add(lineSplit[5]);

                        detectorDataByLineSplit.add(Arrays.copyOfRange(lineSplit, 6, lineSplit.length));
                    }
                }
                if (line.startsWith("#START")) {
                    phase = 1;
                } else if (phase == 1) {
                    phase = 2;
                }
            }
        }
        String[] sequenceIDs = sequenceIDByLineSplit.toArray(new String[0]);
        int[] blockNumbers = convertListOfNumbersAsStringsToIntegerArray(blockNumberByLineSplit);
        int[] cycleNumbers = convertListOfNumbersAsStringsToIntegerArray(cycleNumberByLineSplit);
        double[] integrationNumber = convertListOfNumbersAsStringsToDoubleArray(integrationNumberByLineSplit);
        double[] timeStamp = convertListOfNumbersAsStringsToDoubleArray(timeStampByLineSplit);
        double[] mass = convertListOfNumbersAsStringsToDoubleArray(massByLineSplit);


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
        int nBlocks = Math.max(1, blockListWithoutDuplicates.size() - 1);
        // extract cycles per block
        int[] nCycle = new int[nBlocks];
        int totalCycles = 0;
        if (nBlocks == 1) {
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
                if (currentRecordNumber >= totalCycles){
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
        int blockIndex = 0;
        // hard coded est of block length since only doing first block for now
        Matrix firstBlockInterpolationsMatrix = null;
        double[][] interpMatArrayForBlock = new double[nCycle[0]][4000];
        for (int cycleIndex = 1; cycleIndex < (nCycle[blockIndex]); cycleIndex++) {
            int startOfCycleIndex = startingIndicesOfCyclesByBlock[cycleIndex][2];

            int startOfNextCycleIndex;
            if ((cycleIndex == nCycle[blockIndex] - 1) && (nCycle.length == blockIndex + 1)) {
                startOfNextCycleIndex = timeStamp.length;
            } else {
                startOfNextCycleIndex = startingIndicesOfCyclesByBlock[cycleIndex + 1][2];
            }

            // detect last cycle because it uses its last entry as the upper limit
            // whereas the matlab code uses the starting entry of the next cycle for all previous cycles
            boolean lastCycle = false;
            lastCycle = (cycleIndex == nCycle[blockIndex] - 1);
            if (lastCycle){
                startOfNextCycleIndex --;
            }
            int countOfEntries = startingIndicesOfCyclesByBlock[cycleIndex][2] - startingIndicesOfCyclesByBlock[1][2];

            double deltaTimeStamp = timeStamp[startOfNextCycleIndex] - timeStamp[startOfCycleIndex];

            for (int timeIndex = startOfCycleIndex; timeIndex < startOfNextCycleIndex; timeIndex++) {
                interpMatArrayForBlock[cycleIndex][(timeIndex - startOfCycleIndex) +  countOfEntries] =
                        (timeStamp[timeIndex] - timeStamp[startOfCycleIndex]) / deltaTimeStamp;
                interpMatArrayForBlock[cycleIndex - 1][(timeIndex - startOfCycleIndex) + countOfEntries] =
                        1.0 - interpMatArrayForBlock[cycleIndex][(timeIndex - startOfCycleIndex) +  countOfEntries];
            }
            if (lastCycle){
                interpMatArrayForBlock[cycleIndex][countOfEntries + startOfNextCycleIndex - startOfCycleIndex] = 1.0;
                interpMatArrayForBlock[cycleIndex - 1][countOfEntries + startOfNextCycleIndex - startOfCycleIndex] = 0.0;

                // generate matrix and then transpose it to match matlab
                Matrix firstPass = new Matrix(interpMatArrayForBlock, cycleIndex + 1,  countOfEntries + startOfNextCycleIndex - startOfCycleIndex + 1);
                firstBlockInterpolationsMatrix = firstPass.transpose();
            }
        }


        // start with Baseline table
        AccumulatedData baselineFaradayAccumulator = accumulateBaselineDataPerSequenceTableSpecs(sequenceIDs, detectorData, analysisMethod.getSequenceTable(), true);
        // now sequence table Faraday
        AccumulatedData sequenceFaradayAccumulator = accumulateDataPerSequenceTableSpecs(sequenceIDs, blockNumbers, blockListWithoutDuplicates, detectorData, analysisMethod.getSequenceTable(), analysisMethod.getSpeciesList(), true);
        // now sequence table NOT Faraday (ion counter)
        AccumulatedData sequenceIonCounterAccumulator = accumulateDataPerSequenceTableSpecs(sequenceIDs, blockNumbers, blockListWithoutDuplicates, detectorData, analysisMethod.getSequenceTable(), analysisMethod.getSpeciesList(), false);

        List<Double> dataAccumulatorList = new ArrayList<>();
        dataAccumulatorList.addAll(baselineFaradayAccumulator.dataAccumulatorList());
        dataAccumulatorList.addAll(sequenceFaradayAccumulator.dataAccumulatorList());
        dataAccumulatorList.addAll(sequenceIonCounterAccumulator.dataAccumulatorList());

        List<Integer> isotopeIndicesForDataAccumulatorList = new ArrayList<>();
        isotopeIndicesForDataAccumulatorList.addAll(baselineFaradayAccumulator.isotopeIndicesForDataAccumulatorList());
        isotopeIndicesForDataAccumulatorList.addAll(sequenceFaradayAccumulator.isotopeIndicesForDataAccumulatorList());
        isotopeIndicesForDataAccumulatorList.addAll(sequenceIonCounterAccumulator.isotopeIndicesForDataAccumulatorList());

        List<int[]> detectorFlagsForDataAccumulatorList = new ArrayList<>();
        detectorFlagsForDataAccumulatorList.addAll(baselineFaradayAccumulator.detectorFlagsForDataAccumulatorList());
        detectorFlagsForDataAccumulatorList.addAll(sequenceFaradayAccumulator.detectorFlagsForDataAccumulatorList());
        detectorFlagsForDataAccumulatorList.addAll(sequenceIonCounterAccumulator.detectorFlagsForDataAccumulatorList());

        List<Integer> baseLineFlagsForDataAccumulatorList = new ArrayList<>();
        baseLineFlagsForDataAccumulatorList.addAll(baselineFaradayAccumulator.baseLineFlagsForDataAccumulatorList());
        baseLineFlagsForDataAccumulatorList.addAll(sequenceFaradayAccumulator.baseLineFlagsForDataAccumulatorList());
        baseLineFlagsForDataAccumulatorList.addAll(sequenceIonCounterAccumulator.baseLineFlagsForDataAccumulatorList());

        // convert to arrays to  build parameters for MassSpecOutputDataRecord record
        double[] dataAccumulatorArray = dataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        Matrix rawDataColumn = new Matrix(dataAccumulatorArray, dataAccumulatorArray.length);

        double[] isotopeIndicesForDataAccumulatorArray = isotopeIndicesForDataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        Matrix isotopeIndicesForRawDataColumn = new Matrix(isotopeIndicesForDataAccumulatorArray, isotopeIndicesForDataAccumulatorArray.length);

        double[][] detectorFlagsForDataAccumulatorArray = new double[detectorFlagsForDataAccumulatorList.size()][];
        int i = 0;
        for (int[] detectorFlags : detectorFlagsForDataAccumulatorList){
            detectorFlagsForDataAccumulatorArray[i] = new double[detectorFlags.length];
            for (int d = 0; d < detectorFlags.length; d++){
                detectorFlagsForDataAccumulatorArray[i][d] = detectorFlags[d];
            }
            i++;
        }
        Matrix detectorFlagsForRawDataColumn = new Matrix(detectorFlagsForDataAccumulatorArray);

        double[] baseLineFlagsForDataAccumulatorArray = baseLineFlagsForDataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        Matrix baseLineFlagsForRawDataColumn = new Matrix(baseLineFlagsForDataAccumulatorArray, baseLineFlagsForDataAccumulatorArray.length);


        // TODO:  add in nBlock, nCycle,
        /*
            Matlab code >> here
            d0.data >> rawDataColumn
            d0.iso_vec >> isotopeIndicesForRawDataColumn (isotopes are indexed starting at 1)
            d0.det_ind >> detectorFlagsForRawDataColumn (each Faraday has a column and the last column is for Daly; 1 flags detector used)
            d0.blflag >> baseLineFlagsForRawDataColumn (contains 1 for baseline, 0 for sequence)
            d0.InterpMat >> firstBlockInterpolationsMatrix  (matlab actually puts matrices into cells)
         */
        return new MassSpecOutputDataRecord(
                rawDataColumn,
                isotopeIndicesForRawDataColumn,
                detectorFlagsForRawDataColumn,
                baseLineFlagsForRawDataColumn,
                firstBlockInterpolationsMatrix);
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