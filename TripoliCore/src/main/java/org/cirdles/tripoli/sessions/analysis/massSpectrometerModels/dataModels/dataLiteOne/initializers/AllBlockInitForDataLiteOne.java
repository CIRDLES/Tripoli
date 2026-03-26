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

import java.util.Arrays;
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
                        int blockID = singleBlockRawDataLiteSetRecords[blockIndex].blockID();

                        mapBlockIdToBlockCyclesRecord.put(blockID, (BlockCyclesBuilder.initializeBlockCycles(
                                blockID,
                                true,
                                true, // TODO: not needed here
                                singleBlockRawDataLiteSetRecords[blockIndex].assembleCyclesIncludedForUserFunction(userFunction),
                                singleBlockRawDataLiteSetRecords[blockIndex].assembleCycleMeansForUserFunction(userFunction),
                                singleBlockRawDataLiteSetRecords[blockIndex].assembleCycleStdDevForUserFunction(),
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

        //TODO: fix this cyclecount for concat Feb 2026
        return singleBlockRawDataLiteSetRecords[0] != null ?
                new AllBlockInitForMCMC.PlottingData(
                        null,
                        null,
                        singleBlockRawDataLiteSetRecords,
                        //TODO: fix this cyclecount for concat Feb 2026
                        singleBlockRawDataLiteSetRecords[0].blockRawDataLiteArray().length, true, 1)
                : null;
    }


    public static SingleBlockRawDataLiteSetRecord prepareSingleBlockDataLiteCaseOne(
            int blockID, MassSpecExtractedData massSpecExtractedData) {
        MassSpecOutputBlockRecordLite massSpecOutputBlockRecordLite = massSpecExtractedData.getBlocksDataLite().get(blockID);
        boolean[][] rawDataIncluded =
                (massSpecOutputBlockRecordLite.cycleData().length > 0) ?
                        new boolean[massSpecOutputBlockRecordLite.cycleData().length]
                                [massSpecOutputBlockRecordLite.cycleData()[0].length]
                        : new boolean[0][0];
        for (boolean[] booleans : rawDataIncluded) {
            Arrays.fill(booleans, true);
        }

        return new SingleBlockRawDataLiteSetRecord(
                blockID,
                true,
                massSpecOutputBlockRecordLite.cycleData(),
                rawDataIncluded);
    }
}