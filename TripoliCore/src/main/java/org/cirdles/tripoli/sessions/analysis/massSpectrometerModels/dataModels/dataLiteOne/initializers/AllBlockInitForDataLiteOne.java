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
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.SingleBlockRawDataLiteOneSetRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.initializers.AllBlockInitForMCMC;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputBlockRecordLite;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

/**
 * @author James F. Bowring
 */
public class AllBlockInitForDataLiteOne {
    public static AllBlockInitForMCMC.PlottingData initBlockModels(AnalysisInterface analysis) throws TripoliException {
        // check process status
        MassSpecExtractedData massSpecExtractedData = analysis.getMassSpecExtractedData();
        AnalysisMethod analysisMethod = analysis.getAnalysisMethod();

        int countOfBlocks = massSpecExtractedData.getBlocksDataLite().size();
        SingleBlockRawDataLiteOneSetRecord[] singleBlockRawDataLiteOneSetRecords = new SingleBlockRawDataLiteOneSetRecord[countOfBlocks];

        for (int blockIndex = 0; blockIndex < countOfBlocks; blockIndex++) {
            singleBlockRawDataLiteOneSetRecords[blockIndex] = prepareSingleBlockDataLiteCaseOne(blockIndex + 1, massSpecExtractedData, analysisMethod);
            analysis.getMapOfBlockIdToRawDataLiteOne().put(blockIndex + 1, singleBlockRawDataLiteOneSetRecords[blockIndex]);
        }

        return new AllBlockInitForMCMC.PlottingData(
                null, null, singleBlockRawDataLiteOneSetRecords, singleBlockRawDataLiteOneSetRecords[0].cycleCount(), true, 1);
    }

    private static SingleBlockRawDataLiteOneSetRecord prepareSingleBlockDataLiteCaseOne(int blockID, MassSpecExtractedData massSpecExtractedData, AnalysisMethod analysisMethod) {
        MassSpecOutputBlockRecordLite massSpecOutputBlockRecordLite = massSpecExtractedData.getBlocksDataLite().get(blockID);
        SingleBlockRawDataLiteOneSetRecord singleBlockRawDataLiteOneSetRecord = new SingleBlockRawDataLiteOneSetRecord(
                blockID, massSpecOutputBlockRecordLite.cycleNumbers().length, massSpecOutputBlockRecordLite.cycleData()
        );

        return singleBlockRawDataLiteOneSetRecord;
    }
}