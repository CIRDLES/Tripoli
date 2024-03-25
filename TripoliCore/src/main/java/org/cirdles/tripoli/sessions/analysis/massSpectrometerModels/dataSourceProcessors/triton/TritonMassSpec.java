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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.triton;

import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputBlockRecordLite;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author James F. Bowring
 */
public enum TritonMassSpec {
    ;

    /**
     * Called by reflection from Analysis.extractMassSpecDataFromPath
     *
     * @param inputDataFile
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public static MassSpecExtractedData extractMetaAndBlockDataFromFileTriton(Path inputDataFile) throws IOException {
        MassSpecExtractedData massSpecExtractedData = new MassSpecExtractedData();
        List<String> contentsByLine = new ArrayList<>();
        FileReader fileReader = new FileReader(inputDataFile.toFile());
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String linea;
        while ((linea = bufferedReader.readLine()) != null) {
            // for infinity symbol
            linea = linea.replace("�", "");
            contentsByLine.add(linea);
        }
        bufferedReader.close();

        List<List<String>> dataByBlocks = new ArrayList<>();
        List<String[]> headerByLineSplit = new ArrayList<>();
        List<String[]> columnNamesSplit = new ArrayList<>();
        List<String> dataByBlock = new ArrayList<>();

        int phase = 0;
        int currentBlockID = 1;
        int cyclesPerBlock = 1;
        contentsByLine.remove(0);
        contentsByLine.remove(0);
        for (String line : contentsByLine) {
            if (!line.trim().isBlank() && (phase >= 0)) {
                if (line.startsWith("Cycle")) {
                    massSpecExtractedData.populateHeader(headerByLineSplit);
                    columnNamesSplit.add(line.split("\t"));
                    massSpecExtractedData.populateColumnNamesList(columnNamesSplit);
                    phase = 4;
                } else if (line.startsWith("***")) {
                    phase = 8;
                }

                switch (phase) {
                    case -1 -> {
                    }
                    case 0 -> headerByLineSplit.add(line.split(": "));
                    case 4 -> phase = 5;
                    case 5 -> {
                        cyclesPerBlock = massSpecExtractedData.getHeader().cyclesPerBlock();
                        String[] lineSplit = line.split("\t");
                        int blockID = (Integer.parseInt(lineSplit[0].trim()) - 1) / cyclesPerBlock + 1;
                        if (blockID != currentBlockID) {
                            dataByBlocks.add(dataByBlock);
                            massSpecExtractedData.addBlockLiteRecord(
                                    parseAndBuildSingleBlockTritonRecord(currentBlockID, cyclesPerBlock, dataByBlocks.get(currentBlockID - 1)));
                            currentBlockID++;
                            dataByBlock = new ArrayList<>();
                            dataByBlock.add(line);
                        } else {
                            dataByBlock.add(line);
                        }
                    }
                    case 8 -> {
                        dataByBlocks.add(dataByBlock);
                        massSpecExtractedData.addBlockLiteRecord(
                                parseAndBuildSingleBlockTritonRecord(currentBlockID, cyclesPerBlock, dataByBlocks.get(currentBlockID - 1)));
                        phase = -1;
                    }
                }
            }
        }

        return massSpecExtractedData;
    }

    private static MassSpecOutputBlockRecordLite parseAndBuildSingleBlockTritonRecord(int blockNumber, int cyclesPerBlock, List<String> blockData) {
        List<String> timeStampByLineSplit = new ArrayList<>();
        List<String[]> cycleDataByLineSplit = new ArrayList<>();
        // case 1:  Triton Cycle,Time, DATA[custom fields]
        for (String line : blockData) {
            String[] lineSplit = line.split("\t");
            timeStampByLineSplit.add(lineSplit[1].trim());
            cycleDataByLineSplit.add(Arrays.copyOfRange(lineSplit, 2, lineSplit.length));
        }

        return buildSingleBlockTritonRecord(
                blockNumber,
                cycleDataByLineSplit);
    }

    private static MassSpecOutputBlockRecordLite buildSingleBlockTritonRecord(
            int blockID,
            List<String[]> cycleDataByLineSplit) {

        double[][] cycleData = new double[cycleDataByLineSplit.size()][];
        int index = 0;
        for (String[] numbersAsStrings : cycleDataByLineSplit) {
            // TODO: wTF
            for (int i = 0; i < numbersAsStrings.length; i++) {
                numbersAsStrings[i] = numbersAsStrings[i].replaceAll("X", "");
                numbersAsStrings[i] = numbersAsStrings[i].replaceAll("D", "");
            }
            cycleData[index] = Arrays.stream(numbersAsStrings)
                    .mapToDouble(Double::parseDouble)
                    .toArray();
            index++;
        }

        return new MassSpecOutputBlockRecordLite(
                blockID,
                cycleData
        );
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

}