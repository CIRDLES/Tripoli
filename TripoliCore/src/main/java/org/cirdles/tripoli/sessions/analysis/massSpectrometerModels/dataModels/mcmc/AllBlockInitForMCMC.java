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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc;

import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.ojalgo.RecoverableCondition;

import java.util.ArrayList;
import java.util.List;

import static org.cirdles.tripoli.sessions.analysis.Analysis.SKIP;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockModelDriver.prepareSingleBlockDataForMCMC;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockModelInitForMCMC.initializeModelForSingleBlockMCMC;

/**
 * @author James F. Bowring
 */
public class AllBlockInitForMCMC {

    public static SingleBlockModelRecord[] initBlockModels(AnalysisInterface analysis) throws TripoliException {
        // check process status
        MassSpecExtractedData massSpecExtractedData = analysis.getMassSpecExtractedData();
        AnalysisMethod analysisMethod = analysis.getAnalysisMethod();
        List<Integer> blocksToProcess = new ArrayList<>();

        for (Integer blockID : analysis.getMapOfBlockIdToProcessStatus().keySet()) {
            if (SKIP != analysis.getMapOfBlockIdToProcessStatus().get(blockID)) {
                blocksToProcess.add(blockID);
            }
        }
        int countOfBlocks = blocksToProcess.size();
        SingleBlockModelRecord[] singleBlockModelRecords = new SingleBlockModelRecord[countOfBlocks];

        for (int blockIndex = 0; blockIndex < countOfBlocks; blockIndex++) {
            SingleBlockDataSetRecord singleBlockDataSetRecord = prepareSingleBlockDataForMCMC(blockIndex + 1, massSpecExtractedData, analysisMethod);
            SingleBlockModelInitForMCMC.SingleBlockModelRecordWithCov singleBlockInitialModelRecordWithNoCov;
            try {
                singleBlockInitialModelRecordWithNoCov = initializeModelForSingleBlockMCMC(analysis.getAnalysisMethod().getSpeciesList().size(), singleBlockDataSetRecord, false);
            } catch (RecoverableCondition e) {
                throw new TripoliException("Ojalgo RecoverableCondition");
            }
            singleBlockModelRecords[blockIndex] = singleBlockInitialModelRecordWithNoCov.singleBlockModelRecord();
        }

        return singleBlockModelRecords;
    }
}