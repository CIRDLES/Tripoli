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

import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.SingleBlockRawDataLiteSetRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.initializers.AllBlockInitForMCMC;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputBlockRecordLite;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;

/**
 * @author James F. Bowring
 */
public class AllBlockInitForDataLiteOne {
    public static AllBlockInitForMCMC.PlottingData initBlockModels(AnalysisInterface analysis) {
        // check process status
        MassSpecExtractedData massSpecExtractedData = analysis.getMassSpecExtractedData();
        AnalysisMethod analysisMethod = analysis.getAnalysisMethod();

        int countOfBlocks = massSpecExtractedData.getBlocksDataLite().size();
        SingleBlockRawDataLiteSetRecord[] singleBlockRawDataLiteSetRecords = new SingleBlockRawDataLiteSetRecord[countOfBlocks];

        for (int blockIndex = 0; blockIndex < countOfBlocks; blockIndex++) {
            if (analysis.getMapOfBlockIdToRawDataLiteOne().get(blockIndex + 1) == null) {
                singleBlockRawDataLiteSetRecords[blockIndex] = prepareSingleBlockDataLiteCaseOne(blockIndex + 1, massSpecExtractedData);
                analysis.getMapOfBlockIdToRawDataLiteOne().put(blockIndex + 1, singleBlockRawDataLiteSetRecords[blockIndex]);
            } else {
                // preserves cycle selections
                singleBlockRawDataLiteSetRecords[blockIndex] = analysis.getMapOfBlockIdToRawDataLiteOne().get(blockIndex + 1);
            }
        }

        return new AllBlockInitForMCMC.PlottingData(
                null,
                null,
                singleBlockRawDataLiteSetRecords,
                singleBlockRawDataLiteSetRecords[0].blockRawDataLiteArray().length, true, 1);
    }

    private static SingleBlockRawDataLiteSetRecord prepareSingleBlockDataLiteCaseOne(int blockID, MassSpecExtractedData massSpecExtractedData) {
        MassSpecOutputBlockRecordLite massSpecOutputBlockRecordLite = massSpecExtractedData.getBlocksDataLite().get(blockID);
        boolean[][] rawDataIncluded = new boolean[massSpecOutputBlockRecordLite.cycleData().length][massSpecOutputBlockRecordLite.cycleData()[0].length];
        for (int row = 0; row < rawDataIncluded.length; row++) {
            for (int col = 0; col < rawDataIncluded[row].length; col++) {
                rawDataIncluded[row][col] = true;
            }
        }

        SingleBlockRawDataLiteSetRecord singleBlockRawDataLiteSetRecord = new SingleBlockRawDataLiteSetRecord(
                blockID,
                true,
                massSpecOutputBlockRecordLite.cycleData(),
                rawDataIncluded);

        return singleBlockRawDataLiteSetRecord;
    }
}