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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.initializers;

import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.plots.compoundPlotBuilders.BlockCyclesBuilder;
import org.cirdles.tripoli.plots.compoundPlotBuilders.PlotBlockCyclesRecord;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.SingleBlockRawDataLiteSetRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.initializers.AllBlockInitForMCMC;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputBlockRecordLite;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author James F. Bowring
 */
public class AllBlockInitForDataLiteOne {
    public static AllBlockInitForMCMC.PlottingData initBlockModels(AnalysisInterface analysis) {
        // check process status
        MassSpecExtractedData massSpecExtractedData = analysis.getMassSpecExtractedData();

        int countOfBlocks = massSpecExtractedData.getBlocksDataLite().size();
        SingleBlockRawDataLiteSetRecord[] singleBlockRawDataLiteSetRecords = new SingleBlockRawDataLiteSetRecord[countOfBlocks];

        for (int blockIndex = 0; blockIndex < countOfBlocks; blockIndex++) {
            int blockID = blockIndex + 1;
            if (analysis.getMapOfBlockIdToRawDataLiteOne().get(blockID) == null) {
                singleBlockRawDataLiteSetRecords[blockIndex] = prepareSingleBlockDataLiteCaseOne(blockID, massSpecExtractedData);
                analysis.getMapOfBlockIdToRawDataLiteOne().put(blockID, singleBlockRawDataLiteSetRecords[blockIndex]);
            } else {
                // preserves cycle selections
                singleBlockRawDataLiteSetRecords[blockIndex] = analysis.getMapOfBlockIdToRawDataLiteOne().get(blockID);
            }
        }

        for (UserFunction userFunction : analysis.getUserFunctions()) {
            // todo: simplify since analysis carries most of the info
            if (userFunction.getMapBlockIdToBlockCyclesRecord().isEmpty()) {
                Map<Integer, PlotBlockCyclesRecord> mapBlockIdToBlockCyclesRecord = new TreeMap<>();
                for (int blockIndex = 0; blockIndex < singleBlockRawDataLiteSetRecords.length; blockIndex++) {
                    if (null != singleBlockRawDataLiteSetRecords[blockIndex]) {
                        Integer blockID = singleBlockRawDataLiteSetRecords[blockIndex].blockID();

                        mapBlockIdToBlockCyclesRecord.put(blockID, (BlockCyclesBuilder.initializeBlockCycles(
                                blockID,
                                true,
                                true, // TODO: not needed here
                                singleBlockRawDataLiteSetRecords[blockIndex].assembleCyclesIncludedForUserFunction(userFunction),
                                singleBlockRawDataLiteSetRecords[blockIndex].assembleCycleMeansForUserFunction(userFunction),
                                singleBlockRawDataLiteSetRecords[blockIndex].assembleCycleStdDevForUserFunction(userFunction),
                                new String[]{userFunction.getName()},
                                true,
                                userFunction.isTreatAsIsotopicRatio()).getBlockCyclesRecord()));
                    } else {
                        mapBlockIdToBlockCyclesRecord.put(blockIndex - 1, null);
                    }
                }

                userFunction.setMapBlockIdToBlockCyclesRecord(mapBlockIdToBlockCyclesRecord);
                userFunction.calculateAnalysisStatsRecord(analysis);
            }
        }

        return (countOfBlocks > 0) ? new AllBlockInitForMCMC.PlottingData(
                null,
                null,
                singleBlockRawDataLiteSetRecords,
                singleBlockRawDataLiteSetRecords[0].blockRawDataLiteArray().length, true, 1) : null;
    }


    public static SingleBlockRawDataLiteSetRecord prepareSingleBlockDataLiteCaseOne(
            int blockID, MassSpecExtractedData massSpecExtractedData) {
        MassSpecOutputBlockRecordLite massSpecOutputBlockRecordLite = massSpecExtractedData.getBlocksDataLite().get(blockID);
        boolean[][] rawDataIncluded = new boolean[massSpecOutputBlockRecordLite.cycleData().length][massSpecOutputBlockRecordLite.cycleData()[0].length]; // Array Index Out of Bounds when cycleData is empty
        for (int row = 0; row < rawDataIncluded.length; row++) {
            for (int col = 0; col < rawDataIncluded[row].length; col++) {
                rawDataIncluded[row][col] = true;
            }
        }

        return new SingleBlockRawDataLiteSetRecord(
                blockID,
                true,
                massSpecOutputBlockRecordLite.cycleData(),
                rawDataIncluded);
    }
}