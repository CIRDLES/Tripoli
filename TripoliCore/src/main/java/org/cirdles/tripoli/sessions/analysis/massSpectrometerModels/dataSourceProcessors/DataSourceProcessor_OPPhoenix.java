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
        int[][] startingIndicesOfCyclesByBlock = new int[totalCycles][4]; //blockNum, cycleNum, startIndex
        for (row = 0; row < blockNumbers.length; row++) {
            if (blockNumbers[row] == currentBlockNumber) {
                if (cycleNumbers[row] > currentCycleNumber) {
                    startingIndicesOfCyclesByBlock[currentRecordNumber] = new int[]{currentBlockNumber, currentCycleNumber, currentIndex};
                    currentRecordNumber++;
                    currentCycleNumber++;
                    currentIndex = row;
                }
            } else {
                // new block
                startingIndicesOfCyclesByBlock[currentRecordNumber] = new int[]{currentBlockNumber, currentCycleNumber, currentIndex};
                currentRecordNumber++;
                currentBlockNumber++;
                currentCycleNumber = 0;
            }
        }

        if (currentRecordNumber < startingIndicesOfCyclesByBlock.length) {
            startingIndicesOfCyclesByBlock[currentRecordNumber] = new int[]{currentBlockNumber, currentCycleNumber, currentIndex};
        }

        // build InterpMat for each block using linear approach
        // june 2022 assume 1 block for now
        double[][] interpMatArrayForBlock;// = new double[nCycle[0]][];
        for (int cycleStartIndex = 1; cycleStartIndex < (startingIndicesOfCyclesByBlock.length); cycleStartIndex++) {
            int startOfCycleIndex = startingIndicesOfCyclesByBlock[cycleStartIndex][2];

            // detect last cycle
            boolean lastCycle = false;
            int startOfNextCycleIndex;
            if (cycleStartIndex == startingIndicesOfCyclesByBlock.length - 1) {
                startOfNextCycleIndex = timeStamp.length - 1;
                lastCycle = true;
            } else {
                startOfNextCycleIndex = startingIndicesOfCyclesByBlock[cycleStartIndex + 1][2];
            }

            int countOfEntries = startOfNextCycleIndex - startOfCycleIndex;
            if (lastCycle)  countOfEntries ++;
            interpMatArrayForBlock = new double[2][countOfEntries];
            double deltaTimeStamp = timeStamp[startOfNextCycleIndex] - timeStamp[startOfCycleIndex];

            for (int timeIndex = startOfCycleIndex; timeIndex < startOfNextCycleIndex; timeIndex++) {
                interpMatArrayForBlock[1][timeIndex - startOfCycleIndex] = (timeStamp[timeIndex] - timeStamp[startOfCycleIndex]) / deltaTimeStamp;
                interpMatArrayForBlock[0][timeIndex - startOfCycleIndex] = 1.0 - interpMatArrayForBlock[1][timeIndex - startOfCycleIndex];
            }
            if (lastCycle){
                interpMatArrayForBlock[1][countOfEntries - 1] = 1.0;
                interpMatArrayForBlock[0][countOfEntries - 1] = 0.0;
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

        List<Integer> baseLineFlagsForDataAccumulatorList = new ArrayList<>();
        baseLineFlagsForDataAccumulatorList.addAll(baselineFaradayAccumulator.baseLineFlagsForDataAccumulatorList());
        baseLineFlagsForDataAccumulatorList.addAll(sequenceFaradayAccumulator.baseLineFlagsForDataAccumulatorList());
        baseLineFlagsForDataAccumulatorList.addAll(sequenceIonCounterAccumulator.baseLineFlagsForDataAccumulatorList());

        // convert to arrays to  build parameters for MassSpecOutputDataRecord record
        double[] dataAccumulatorArray = dataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        Matrix rawDataColumn = new Matrix(dataAccumulatorArray, dataAccumulatorArray.length);

        double[] isotopeIndicesForDataAccumulatorArray = isotopeIndicesForDataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        Matrix isotopeIndicesForRawDataColumn = new Matrix(isotopeIndicesForDataAccumulatorArray, isotopeIndicesForDataAccumulatorArray.length);

        double[] baseLineFlagsForDataAccumulatorArray = baseLineFlagsForDataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        Matrix baseLineFlagsForRawDataColumn = new Matrix(baseLineFlagsForDataAccumulatorArray, baseLineFlagsForDataAccumulatorArray.length);

        // TODO:  add in nBlock, nCycle,
        return new MassSpecOutputDataRecord(
                rawDataColumn,
                isotopeIndicesForRawDataColumn,
                baseLineFlagsForRawDataColumn);
    }


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