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
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.initializers.AllBlockInitForMCMC;

import java.util.Arrays;


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
public class MCMCVectorExporter {

    public static DataVectorsRecord exportData(AnalysisInterface analysis, int blockID) {
        int blockIndex = blockID - 1;
        AllBlockInitForMCMC.PlottingData plottingData = analysis.assemblePostProcessPlottingData();
        SingleBlockRawDataSetRecord[] singleBlockRawDataSetRecords = plottingData.singleBlockRawDataSetRecords();

        SingleBlockRawDataSetRecord singleBlockRawDataSetRecord = singleBlockRawDataSetRecords[blockIndex];
        SingleBlockModelRecord[] singleBlockModelRecords = plottingData.singleBlockModelRecords();

        double[] time = singleBlockRawDataSetRecord.blockTimeArray();
        double[] measuredIntensities = singleBlockRawDataSetRecord.blockRawDataArray();
        double[] measuredIntensityUncertainties = singleBlockModelRecords[blockIndex].dataSignalNoiseArray();
        int[] isotopeIndices = singleBlockRawDataSetRecord.blockIsotopeOrdinalIndicesArray();
        int[] detectorIndices = singleBlockRawDataSetRecord.blockDetectorOrdinalIndicesArray();
        int[] baselineFlags = new int[time.length];
        int countOfBaselineDataEntries = singleBlockRawDataSetRecords[blockIndex].getCountOfBaselineIntensities();
        Arrays.fill(baselineFlags, 0, countOfBaselineDataEntries, 1);
        int[] cycleIndices = singleBlockRawDataSetRecord.blockCycleArray();

        boolean[][] intensityIncludedAccumulatorArray = ((Analysis) analysis).getMapOfBlockIdToIncludedPeakData().get(blockID);
        int countOfIsotopes = intensityIncludedAccumulatorArray.length;
        boolean[] includedIntensities = ((Analysis) analysis).getMapOfBlockIdToIncludedIntensities().get(blockID);

        double[] modeledIntensities = singleBlockModelRecords[blockID - 1].dataModelArray();

        return new DataVectorsRecord(
                time, measuredIntensities, measuredIntensityUncertainties, isotopeIndices, detectorIndices, baselineFlags, cycleIndices, includedIntensities, modeledIntensities);
    }

    public record DataVectorsRecord(
            double[] time,
            double[] measuredIntensities,
            double[] measuredIntensityUncertainties,
            int[] isotopeIndices,
            int[] detectorIndices,
            int[] baselineFlags,
            int[] cycleIndices,
            boolean[] includedIntensities,
            double[] modeledIntensities
    ) {

        public String prettyPrintHeaderAsCSV() {
            String header = "";

            header += "time,";
            header += "measuredIntensities,";
            header += "measuredIntensityUncertainties,";
            header += "isotopeIndices,";
            header += "detectorIndices,";
            header += "baselineFlags,";
            header += "cycleIndices,";
            header += "includedIntensities,";
            header += "modeledIntensities \n";

            return header;
        }

        public String prettyPrintAsCSV(int index) {
            String data = "";

            data += time()[index] + ",";
            data += measuredIntensities()[index] + ",";
            data += measuredIntensityUncertainties()[index] + ",";
            data += isotopeIndices()[index] + ",";
            data += detectorIndices()[index] + ",";
            data += baselineFlags()[index] + ",";
            data += cycleIndices()[index] + ",";
            data += includedIntensities()[index] + ",";
            data += modeledIntensities()[index] + "\n";

            return data;
        }
    }

}