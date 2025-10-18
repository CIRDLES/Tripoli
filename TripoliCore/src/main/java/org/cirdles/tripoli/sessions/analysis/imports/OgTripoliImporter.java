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

package org.cirdles.tripoli.sessions.analysis.imports;

import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.SingleBlockRawDataLiteSetRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputBlockRecordLite;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OgTripoliImporter {

    public static AnalysisInterface importTripolizedData(File ogTripoliFile) {
        try {
            boolean timeExists = false;

            int blockIndex = 1;
            int cyclesPerBlock = 0;

            AnalysisInterface tripoliAnalysis = AnalysisInterface.initializeNewAnalysis(0);
            MassSpecExtractedData massSpecExtractedData = new MassSpecExtractedData();

            MassSpectrometerContextEnum massSpectrometerContext = tripoliAnalysis.getParameters().getMassSpectrometerContext();
            massSpecExtractedData.setMassSpectrometerContext(massSpectrometerContext);

            FileReader fileReader = new FileReader(ogTripoliFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            bufferedReader.readLine(); // Skip the first line
            String line = bufferedReader.readLine();

            tripoliAnalysis.setAnalysisName(line.split("\\.")[0]);

            // Skip 8 lines to get to headers
            for (int i = 0; i < 8; i++) {
                line = bufferedReader.readLine();
            }

            // Create UserFunctions based on headers (skipping Time)
            String[] headers = Arrays.stream(line.split("\t"))
                    .map(String::trim)
                    .toArray(String[]::new);

            List<UserFunction> ufList = tripoliAnalysis.getUserFunctions();
            if (headers[0].equalsIgnoreCase("Time")) timeExists = true;

            for (int i = 0; i < headers.length; i++) {
                if (timeExists) if (i == 0) i++;
                ufList.add(new UserFunction(headers[i], timeExists ? i - 1 : i));
            }

            // Skip over stats
            for (int i = 0; i < 7; i++) {
                line = bufferedReader.readLine();
            }

            // Prepare to accumulate block data
            List<double[]> currentBlockCycles = new ArrayList<>();
            List<boolean[]> currentBlockIncluded = new ArrayList<>();
            String dataLine;

            while ((dataLine = bufferedReader.readLine()) != null) {
                dataLine = dataLine.trim();

                // blank line = end of block
                if (dataLine.isEmpty()) {
                    if (!currentBlockCycles.isEmpty()) {
                        double[][] blockDataLite = currentBlockCycles.toArray(new double[0][]);
                        boolean[][] blockDataLiteIncluded = currentBlockIncluded.toArray(new boolean[0][]);
                        boolean blockIncluded = currentBlockIncluded.stream()
                                .anyMatch(row -> {
                                    for (boolean v : row) {
                                        if (v) return true;
                                    }
                                    return false;
                                }); // If all values are false, then block is not included
                        massSpecExtractedData.addBlockLiteRecord(
                                new MassSpecOutputBlockRecordLite(blockIndex, blockDataLite)
                        );
                        tripoliAnalysis.getMapOfBlockIdToRawDataLiteOne().put(blockIndex, new SingleBlockRawDataLiteSetRecord(
                                blockIndex,
                                blockIncluded,
                                blockDataLite,
                                blockDataLiteIncluded
                        )); // store each block into map
                        blockIndex++;
                        if (cyclesPerBlock == 0) cyclesPerBlock = currentBlockCycles.size();
                        currentBlockCycles.clear();
                        currentBlockIncluded.clear();
                    }
                    continue;
                }

                // Parse data row
                String[] cycleValues = dataLine.split("\t");

                int timeExistsArrayLength = timeExists ? cycleValues.length - 1 : cycleValues.length;
                double[] numericCycle = new double[timeExistsArrayLength];
                boolean[] includedCycle = new boolean[timeExistsArrayLength];

                for (int i = 0; i < cycleValues.length; i++) {
                    if (timeExists && i == 0) {
                        // This is where I'd put my time value. IF I HAD ONE
                        //timeValues.add(Double.parseDouble(cycleValues[0]));
                        continue;
                    }

                    int timeExistsIndex = timeExists ? i - 1 : i;
                    if (cycleValues[i].trim().isEmpty()) cycleValues[i] = "0.0"; // null is 0

                    double value = Double.parseDouble(cycleValues[i]);
                    if (value < 0) {
                        value = Math.abs(value); // negative is not included
                        includedCycle[timeExistsIndex] = false;
                    } else {
                        includedCycle[timeExistsIndex] = true;
                    }
                    numericCycle[timeExistsIndex] = value;
                }

                currentBlockCycles.add(numericCycle);
                currentBlockIncluded.add(includedCycle);
            }

            // Handle final block (if not followed by blank line)
            if (!currentBlockCycles.isEmpty()) {
                double[][] blockData = currentBlockCycles.toArray(new double[0][]);
                massSpecExtractedData.addBlockLiteRecord(
                        new MassSpecOutputBlockRecordLite(blockIndex, blockData)
                );
            }
            // Set headers
            massSpecExtractedData.setHeader(new MassSpecExtractedData.MassSpecExtractedHeader(
                    "",
                    tripoliAnalysis.getAnalysisName(),
                    "",
                    "ogTripoliImport",
                    true,
                    false,
                    "",
                    cyclesPerBlock
            ));

            String[] columnHeaders = new String[headers.length + 2];
            columnHeaders[0] = "Cycle";
            columnHeaders[1] = "Time";
            System.arraycopy(headers, 0, columnHeaders, 2, headers.length);
            massSpecExtractedData.setColumnHeaders(columnHeaders);

            tripoliAnalysis.setMassSpecExtractedData(massSpecExtractedData);
            tripoliAnalysis.setMethod(AnalysisMethod.createAnalysisMethodFromCase1(massSpecExtractedData));

            return tripoliAnalysis;

        } catch (IOException | TripoliException ignored) {
            return null;
        }
    }
}
