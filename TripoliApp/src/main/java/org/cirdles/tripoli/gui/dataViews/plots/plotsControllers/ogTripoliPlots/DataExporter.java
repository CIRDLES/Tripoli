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

package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots;

import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockModelRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockRawDataSetRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.initializers.AllBlockInitForOGTripoli;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputBlockRecordFull;

import java.util.Arrays;
import java.util.List;


/**
 * @author James F. Bowring
 */

/*
Create a new exported CSV file to help test and diagnose issues with the data reduction and MCMC. For each block, write a CSV file that contains the following eight columns (MATLAB variable names in parentheses):

time (d0.time)
measured intensities (d0.data)
measured intensity uncertainties (x0.Dsig)
isotope index (d0.iso_vec)
detector index (d0.det_vec)
baseline flag (d0.blflag)
cycle index (d0.cycle)
rejected true/false (d0.Include)
modeled intensities using the average of MCMC models (d = ModelMSData(x, d0))
All eight columns should be the same length. If Tripoli handles the baseline and on-peak data separately, then they could be output in separate CSV files.
 */
public class DataExporter {



    public static void exportData(AnalysisInterface analysis, int blockID){
        int blockIndex = blockID - 1;
        AllBlockInitForOGTripoli.PlottingData plottingData = analysis.assemblePostProcessPlottingData();
        SingleBlockRawDataSetRecord[] singleBlockRawDataSetRecords = plottingData.singleBlockRawDataSetRecords();


        SingleBlockRawDataSetRecord singleBlockRawDataSetRecord = singleBlockRawDataSetRecords[blockIndex];
        SingleBlockModelRecord[] singleBlockModelRecords = plottingData.singleBlockModelRecords();

        double[] time =  singleBlockRawDataSetRecord.blockTimeArray();
        double[] measuredIntensities =  singleBlockRawDataSetRecord.blockRawDataArray();
        double[] measuredIntensityUncertainties = singleBlockModelRecords[blockIndex].dataSignalNoiseArray();
        int[] isotopeIndices = singleBlockRawDataSetRecord.blockIsotopeOrdinalIndicesArray();
        int[] detectorIndices = singleBlockRawDataSetRecord.blockDetectorOrdinalIndicesArray();
        int[] baselineFlag = new int[time.length];
        int countOfBaselineDataEntries = singleBlockRawDataSetRecords[blockIndex].getCountOfBaselineIntensities();
        Arrays.fill(baselineFlag, countOfBaselineDataEntries, time.length - 1, 1 );
        int[] cycleIndices = singleBlockRawDataSetRecord.blockCycleArray();
        // included
        boolean[] includedIntensities = new boolean[time.length];

        boolean[][] intensityIncludedAccumulatorArray = ((Analysis) analysis).getMapOfBlockIdToIncludedPeakData().get(blockID);
        int countOfIsotopes = intensityIncludedAccumulatorArray.length;
        int countOfFaradayDataEntries = singleBlockRawDataSetRecords[blockIndex].getCountOfOnPeakFaradayIntensities() / countOfIsotopes;
        int dataCount = countOfBaselineDataEntries;// / countOfIsotopes;
        int countOfTimes = intensityIncludedAccumulatorArray[0].length;
        for (int isotopeIndex = 0; isotopeIndex < countOfIsotopes; isotopeIndex++){
            for (int col = 0; col < countOfFaradayDataEntries; col++) {
                includedIntensities[dataCount] = intensityIncludedAccumulatorArray[isotopeIndex][col];
                dataCount++;
            }
        }
        for (int isotopeIndex = 0; isotopeIndex < countOfIsotopes; isotopeIndex++){
            for (int col = countOfFaradayDataEntries; col < countOfTimes; col++) {
                includedIntensities[dataCount] = intensityIncludedAccumulatorArray[isotopeIndex][col];
                dataCount++;
            }
        }


        double[] modeledData = singleBlockModelRecords[blockID - 1].dataModelArray();



        double[] data = null;
    }

}