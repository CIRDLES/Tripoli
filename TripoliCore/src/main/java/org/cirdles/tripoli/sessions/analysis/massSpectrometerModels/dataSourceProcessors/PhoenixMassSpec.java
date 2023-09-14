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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author James F. Bowring
 */
public enum PhoenixMassSpec {
    ;

    /**
     * Called by reflection from Analysis.extractMassSpecDataFromPath
     *
     * @param inputDataFile
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public static MassSpecExtractedData extractMetaAndBlockDataFromFileVersion_1_0(Path inputDataFile) throws IOException {
        MassSpecExtractedData massSpecExtractedData = new MassSpecExtractedData();
        List<String> contentsByLine = new ArrayList<>(Files.readAllLines(inputDataFile, Charset.defaultCharset()));
        // test for version 1.00
        if (0 != contentsByLine.get(0).trim().compareToIgnoreCase("Version,1.00")) {
            throw new IOException("Expecting Version 1.0 of data file.");
        } else {
            List<String[]> headerByLineSplit = new ArrayList<>();
            List<String[]> columnNamesSplit = new ArrayList<>();
            List<String[]> detectorsByLineSplit = new ArrayList<>();
            List<String> dataByBlock = new ArrayList<>();

            int phase = 0;
            int currentBlockNumber = 1;
            contentsByLine.add("#END,0,");
            for (String line : contentsByLine) {
                if (!line.trim().isBlank()) {
                    if (line.startsWith("#START")) {
                        massSpecExtractedData.populateHeader(headerByLineSplit);
                        massSpecExtractedData.populateDetectors(detectorsByLineSplit);
                        phase = 1;
                    } else if (line.startsWith("#END")) {
                        phase = 4;
                    }

                    switch (phase) {
                        case 0 -> headerByLineSplit.add(line.split(","));
                        case 1 -> phase = 2;
                        case 2 -> {
                            columnNamesSplit.add(line.split(","));
                            massSpecExtractedData.populateColumnNamesList(columnNamesSplit);
                            phase = 3;
                        }
                        case 3 -> {
                            String[] lineSplit = line.split(",");
                            // each block gets treated as a singleton block #1
                            int blockNumber = Integer.parseInt(lineSplit[1].trim());
                            if (blockNumber != currentBlockNumber) {
                                //  save off block and prepare for next block
                                massSpecExtractedData.addBlockRecord(
                                        parseAndBuildSingleBlockRecord(1, currentBlockNumber, dataByBlock));
                                dataByBlock = new ArrayList<>();
                                currentBlockNumber = blockNumber;
                            }
                            dataByBlock.add(line);
                        }
                        case 4 -> {
                            // test if complete block by checking last entry's cycle number != 0
                            boolean isComplete = 0 < Integer.parseInt(dataByBlock.get(dataByBlock.size() - 1).split(",")[2].trim());
                            if (isComplete) {
                                massSpecExtractedData.addBlockRecord(
                                        parseAndBuildSingleBlockRecord(1, currentBlockNumber, dataByBlock));
                            }
                        }
                    }
                }
            }
        }
        return massSpecExtractedData;
    }

    /**
     * Called by reflection from Analysis.extractMassSpecDataFromPath
     *
     * @param inputDataFile
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public static MassSpecExtractedData extractMetaAndBlockDataFromFileVersion_1_2(Path inputDataFile) throws IOException {
        MassSpecExtractedData massSpecExtractedData = new MassSpecExtractedData();
        List<String> contentsByLine = new ArrayList<>(Files.readAllLines(inputDataFile, Charset.defaultCharset()));
        // test for version 1.20
        if ((!contentsByLine.get(2).trim().startsWith("Version,1.")) && (!contentsByLine.get(2).trim().startsWith("Version,2."))) {
            throw new IOException("Expecting Version 1.2.n of data file.");
        } else {
            // first pass is to assemble data by blocks
            List<String[]> headerByLineSplit = new ArrayList<>();
            List<String[]> detectorsByLineSplit = new ArrayList<>();
            List<String[]> columnNamesSplit = new ArrayList<>();
            List<List<String>> dataByBlocks = new ArrayList<>();
            List<String> dataByBlock = new ArrayList<>();

            int phase = 0;
            int currentBlockID = 1;
            for (String line : contentsByLine) {
                if (!line.trim().isBlank()) {
                    if (line.startsWith("#COLLECTORS")) {
                        massSpecExtractedData.populateHeader(headerByLineSplit);
                        phase = 1;
                    } else if (line.startsWith("#BASELINES")) {
                        massSpecExtractedData.populateDetectors(detectorsByLineSplit);
                        phase = 3;
                    } else if (line.startsWith("#ONPEAK")) {
                        phase = 6;
                    } else if (line.startsWith("#END")) {
                        phase = 9;
                    }

                    switch (phase) {
                        case 0 -> headerByLineSplit.add(line.split(","));
                        case 1 -> phase = 2;
                        case 2 -> detectorsByLineSplit.add(line.split(","));
                        case 3 -> phase = 4;
                        case 4 -> {
                            columnNamesSplit.add(line.split(","));
                            massSpecExtractedData.populateColumnNamesList(columnNamesSplit);
                            phase = 5;
                        }
                        case 6 -> phase = 7;
                        case 7 -> phase = 8;

                        case 5, 8 -> {
                            String[] lineSplit = line.split(",");
                            // each block gets treated as a singleton block #1
                            int blockID = Integer.parseInt(lineSplit[1].trim());
                            if (blockID != currentBlockID) {
                                //  save off block and prepare for next block new for BL and add to for OPeak
                                if (8 == phase) {
                                    dataByBlocks.get(currentBlockID - 1).addAll(dataByBlock);
                                    massSpecExtractedData.addBlockRecord(
                                            parseAndBuildSingleBlockRecord(2, currentBlockID, dataByBlocks.get(currentBlockID - 1)));
                                } else {
                                    dataByBlocks.add(dataByBlock);
                                }
                                dataByBlock = new ArrayList<>();
                                currentBlockID = blockID;
                            }
                            dataByBlock.add(line);
                        }
                    }
                } else if ((5 == phase) && !dataByBlock.isEmpty()) {
                    // clean up last block
                    dataByBlocks.add(dataByBlock);
                    dataByBlock = new ArrayList<>();
                    currentBlockID = 1;
                } else if ((8 == phase) && !dataByBlock.isEmpty()) {
                    // clean up last block
                    // check for missing baseline action
                    if (dataByBlocks.isEmpty()) {
                        dataByBlocks.add(dataByBlock);
                    } else {
                        dataByBlocks.get(currentBlockID - 1).addAll(dataByBlock);
                    }
                    massSpecExtractedData.addBlockRecord(
                            parseAndBuildSingleBlockRecord(2, currentBlockID, dataByBlocks.get(currentBlockID - 1)));
                }
            }
        }
        return massSpecExtractedData;
    }

    private static MassSpecOutputSingleBlockRecord parseAndBuildSingleBlockRecord(int version, int blockNumber, List<String> blockData) {
        List<String> sequenceIDByLineSplit = new ArrayList<>();
        List<String> cycleNumberByLineSplit = new ArrayList<>();
        List<String> integrationNumberByLineSplit = new ArrayList<>();
        List<String> timeStampByLineSplit = new ArrayList<>();
        List<String> massByLineSplit = new ArrayList<>();
        List<String[]> detectorDataByLineSplit = new ArrayList<>();

        // version 1:  ID,Block,Cycle,Integ,Time,Mass,Low5,Low4,Low3,Low2,Ax Fara,Axial,High1,High2,High3,High4
        // version 2:  ID,Block,Cycle,Integ,PeakID,AxMass,Time,PM,RS,L5,L4,L3,L2,Ax,H1,H2,H3,H4
        for (String line : blockData) {
            String[] lineSplit = line.split(",");
            sequenceIDByLineSplit.add(lineSplit[0].trim());
            cycleNumberByLineSplit.add(lineSplit[2].trim());
            integrationNumberByLineSplit.add(lineSplit[3].trim());
            timeStampByLineSplit.add(lineSplit[(1 == version) ? 4 : 6].trim());
            massByLineSplit.add(lineSplit[5].trim());
            detectorDataByLineSplit.add(Arrays.copyOfRange(lineSplit, ((1 == version) ? 6 : 7), lineSplit.length));
        }

        return buildSingleBlockRecord(
                blockNumber,
                sequenceIDByLineSplit,
                cycleNumberByLineSplit,
                integrationNumberByLineSplit,
                timeStampByLineSplit,
                massByLineSplit,
                detectorDataByLineSplit);
    }

    private static MassSpecOutputSingleBlockRecord buildSingleBlockRecord(
            int blockID,
            List<String> sequenceIDByLineSplit,
            List<String> cycleNumberByLineSplit,
            List<String> integrationNumberByLineSplit,
            List<String> timeStampByLineSplit,
            List<String> massByLineSplit,
            List<String[]> detectorDataByLineSplit) {

        // process sequenceIDByLineSplit to learn break between Baselines and Onpeaks
        int startingOnPeakIndex = 0;
        for (int lineIndex = 0; lineIndex < sequenceIDByLineSplit.size(); lineIndex++) {
            if (!sequenceIDByLineSplit.get(lineIndex).startsWith("B")) {
                startingOnPeakIndex = lineIndex;
                break;
            }
        }

        List<List<String>> splitIDs = splitStringListIntoBaselineAndOnPeak(sequenceIDByLineSplit, startingOnPeakIndex);
        String[] baselineIDs = splitIDs.get(0).toArray(new String[0]);
        String[] onPeakIDs = splitIDs.get(1).toArray(new String[0]);

        // build maps of IDs to indices
        Map<String, List<Integer>> mapOfBaselineIDsToIndices = buildMapOfIdsToIndices(baselineIDs);
        Map<String, List<Integer>> mapOfOnPeakIDsToIndices = buildMapOfIdsToIndices(onPeakIDs);

        List<List<String>> splitCycleNums = splitStringListIntoBaselineAndOnPeak(cycleNumberByLineSplit, startingOnPeakIndex);
        int[] baselineCycleNumbers = convertListOfNumbersAsStringsToIntegerArray(splitCycleNums.get(0));
        int[] onPeakCycleNumbers = convertListOfNumbersAsStringsToIntegerArray(splitCycleNums.get(1));

        List<List<String>> splitIntegrationNums = splitStringListIntoBaselineAndOnPeak(integrationNumberByLineSplit, startingOnPeakIndex);
        int[] baselineIntegrationNumbers = convertListOfNumbersAsStringsToIntegerArray(splitIntegrationNums.get(0));
        int[] onPeakIntegrationNumbers = convertListOfNumbersAsStringsToIntegerArray(splitIntegrationNums.get(1));

        List<List<String>> splitTimeStamps = splitStringListIntoBaselineAndOnPeak(timeStampByLineSplit, startingOnPeakIndex);
        double[] baselineTimeStamps = convertListOfNumbersAsStringsToDoubleArray(splitTimeStamps.get(0));
        double[] onPeakTimeStamps = convertListOfNumbersAsStringsToDoubleArray(splitTimeStamps.get(1));

        List<List<String>> splitMasses = splitStringListIntoBaselineAndOnPeak(massByLineSplit, startingOnPeakIndex);
        double[] baselineMasses = convertListOfNumbersAsStringsToDoubleArray(splitMasses.get(0));
        double[] onPeakMasses = convertListOfNumbersAsStringsToDoubleArray(splitMasses.get(1));

        List<List<String[]>> splitDetectorData = splitStringArrayListIntoBaselineAndOnPeak(detectorDataByLineSplit, startingOnPeakIndex);
        double[][] baselineIntensities = new double[splitDetectorData.get(0).size()][];
        int index = 0;
        for (String[] numbersAsStrings : splitDetectorData.get(0)) {
            baselineIntensities[index] = Arrays.stream(numbersAsStrings)
                    .mapToDouble(Double::parseDouble)
                    .toArray();
            index++;
        }
        double[][] onPeakIntensities = new double[splitDetectorData.get(1).size()][];
        index = 0;
        for (String[] numbersAsStrings : splitDetectorData.get(1)) {
            onPeakIntensities[index] = Arrays.stream(numbersAsStrings)
                    .mapToDouble(Double::parseDouble)
                    .toArray();
            index++;
        }


        // baseline cycles are all 0 and onpeak cycles start at 1
        int nCycle = onPeakCycleNumbers[onPeakCycleNumbers.length - 1];

        // collect the starting indices of each onPeak cycle
        int currentCycleNumber = 1;
        int currentIndex = 0;
        int currentRecordNumber = 0;
        int row;
        boolean cycleStartRecorded = false;
        int[] onPeakStartingIndicesOfCycles = new int[nCycle];
        for (row = 0; row < onPeakCycleNumbers.length; row++) {
            if (!cycleStartRecorded) {
                onPeakStartingIndicesOfCycles[currentRecordNumber] = currentIndex;
                cycleStartRecorded = true;
            }
            if (onPeakCycleNumbers[row] > currentCycleNumber) {
                currentRecordNumber++;
                currentCycleNumber++;
                currentIndex = row;
                onPeakStartingIndicesOfCycles[currentRecordNumber] = currentIndex;
            }
        }

        return new MassSpecOutputSingleBlockRecord(
                blockID,
                baselineIntensities,
                baselineIDs,
                mapOfBaselineIDsToIndices,
                baselineCycleNumbers,
                baselineIntegrationNumbers,
                baselineTimeStamps,
                baselineMasses,
                onPeakIntensities,
                onPeakIDs,
                mapOfOnPeakIDsToIndices,
                onPeakCycleNumbers,
                onPeakIntegrationNumbers,
                onPeakTimeStamps,
                onPeakMasses,
                onPeakStartingIndicesOfCycles);
    }

    // helper methods **************************************************************************************************
    private static double[] convertListOfNumbersAsStringsToDoubleArray(List<String> listToConvert) {
        double[] retVal = new double[listToConvert.size()];
        int index = 0;
        for (String blockNumberAsString : listToConvert) {
            retVal[index] = Double.parseDouble(blockNumberAsString);
            index++;
        }

        return retVal;
    }

    private static int[] convertListOfNumbersAsStringsToIntegerArray(List<String> listToConvert) {
        int[] retVal = new int[listToConvert.size()];
        int index = 0;
        for (String blockNumberAsString : listToConvert) {
            retVal[index] = Integer.parseInt(blockNumberAsString);
            index++;
        }

        return retVal;
    }

    private static List<List<String>> splitStringListIntoBaselineAndOnPeak(List<String> sourceList, int splitIndex) {
        int size = sourceList.size();
        List<List<String>> retVal = new ArrayList<>();
        retVal.add(new ArrayList<>(sourceList.subList(0, splitIndex)));
        retVal.add(new ArrayList<>(sourceList.subList(splitIndex, size)));
        return retVal;
    }

    private static List<List<String[]>> splitStringArrayListIntoBaselineAndOnPeak(List<String[]> sourceList, int splitIndex) {
        return new ArrayList<>(
                sourceList.stream()
                        .collect(Collectors.partitioningBy(s -> sourceList.indexOf(s) >= splitIndex))
                        .values()
        );
    }

    private static Map<String, List<Integer>> buildMapOfIdsToIndices(String[] ids) {
        Map<String, List<Integer>> mapOfIdsToIndices = new TreeMap<>();
        for (int index = 0; index < ids.length; index++) {
            if (mapOfIdsToIndices.containsKey(ids[index])) {
                mapOfIdsToIndices.get(ids[index]).add(index);
            } else {
                mapOfIdsToIndices.put(ids[index], new ArrayList<>());
                mapOfIdsToIndices.get(ids[index]).add(index);
            }
        }
        return mapOfIdsToIndices;
    }
}