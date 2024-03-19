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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.initializers;

import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.SingleBlockRawDataLiteSetRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockModelRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockRawDataSetRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.ojalgo.RecoverableCondition;

import java.io.Serializable;

import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockModelDriver.prepareSingleBlockDataForMCMC;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.initializers.SingleBlockModelInitForMCMC.initializeModelForSingleBlockMCMC;

/**
 * @author James F. Bowring
 */
public class AllBlockInitForMCMC {

    public static PlottingData initBlockModels(AnalysisInterface analysis) throws TripoliException {
        // check process status
        MassSpecExtractedData massSpecExtractedData = analysis.getMassSpecExtractedData();
        AnalysisMethod analysisMethod = analysis.getAnalysisMethod();

        int countOfBlocks = analysis.getMapOfBlockIdToProcessStatus().keySet().size();
        SingleBlockRawDataSetRecord[] singleBlockRawDataSetRecords = new SingleBlockRawDataSetRecord[countOfBlocks];
        SingleBlockModelRecord[] singleBlockModelRecords = new SingleBlockModelRecord[countOfBlocks];

        for (int blockIndex = 0; blockIndex < countOfBlocks; blockIndex++) {
            singleBlockRawDataSetRecords[blockIndex] = prepareSingleBlockDataForMCMC(blockIndex + 1, massSpecExtractedData, analysisMethod);
            analysis.getMapOfBlockIdToRawData().put(blockIndex + 1, singleBlockRawDataSetRecords[blockIndex]);

            if (null == ((Analysis) analysis).getMapOfBlockIdToIncludedIntensities().get(blockIndex + 1)) {
                ((Analysis) analysis).getMapOfBlockIdToIncludedIntensities().put(blockIndex + 1, singleBlockRawDataSetRecords[blockIndex].blockIncludedIntensitiesArray());
            }

            SingleBlockModelInitForMCMC.SingleBlockModelRecordWithCov singleBlockInitialModelRecordWithNoCov;
            try {
                singleBlockInitialModelRecordWithNoCov = initializeModelForSingleBlockMCMC(
                        analysis, analysis.getAnalysisMethod(), singleBlockRawDataSetRecords[blockIndex], false);
            } catch (RecoverableCondition e) {
                throw new TripoliException("Ojalgo RecoverableCondition");
            }
            singleBlockModelRecords[blockIndex] = singleBlockInitialModelRecordWithNoCov.singleBlockModelRecord();
        }

        return new PlottingData(singleBlockRawDataSetRecords, singleBlockModelRecords, null, singleBlockModelRecords[0].cycleCount(), true, 4);
    }

    public record PlottingData(
            SingleBlockRawDataSetRecord[] singleBlockRawDataSetRecords,
            SingleBlockModelRecord[] singleBlockModelRecords,
            SingleBlockRawDataLiteSetRecord[] singleBlockRawDataLiteSetRecords,
            int cycleCount,
            boolean preview,
            int analysisCaseNumber) implements Serializable {
    }
}