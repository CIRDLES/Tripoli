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

import jama.Matrix;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.species.IsotopicRatio;

import java.io.Serializable;
import java.util.*;

import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockModelInitForMCMC.modelInitData;

/**
 * @author James F. Bowring
 */
public class EnsemblesStore implements Serializable {

    public static synchronized void produceSummaryModelFromEnsembleStore(
            int blockID,
            AnalysisInterface analysis) {

        List<EnsemblesStore.EnsembleRecord> ensembleRecordsList = analysis.getMapBlockIDToEnsembles().get(blockID);
        AnalysisMethod analysisMethod = analysis.getAnalysisMethod();
        SingleBlockRawDataSetRecord singleBlockRawDataSetRecord = analysis.getMapOfBlockIdToRawData().get(blockID);
        SingleBlockModelRecord singleBlockModelRecord = analysis.getMapOfBlockIdToFinalModel().get(blockID);
        List<IsotopicRatio> isotopicRatioList = analysisMethod.getIsotopicRatiosList();

        int initialModelsBurnCount = ((Analysis) analysis).getMapOfBlockIdToModelsBurnCount().get(blockID);
        int countOfEnsemblesUsed = ensembleRecordsList.size() - initialModelsBurnCount;
        // log ratios
        double[][] ensembleSetOfLogRatios = new double[isotopicRatioList.size()][countOfEnsemblesUsed];
        double[] logRatioMean = new double[isotopicRatioList.size()];
        for (int ratioIndex = 0; ratioIndex < isotopicRatioList.size(); ratioIndex++) {
            DescriptiveStatistics descriptiveStatisticsLogRatios = new DescriptiveStatistics();
            for (int index = initialModelsBurnCount; index < countOfEnsemblesUsed + initialModelsBurnCount; index++) {
                ensembleSetOfLogRatios[ratioIndex][index - initialModelsBurnCount] = ensembleRecordsList.get(index).logRatios()[ratioIndex];
                descriptiveStatisticsLogRatios.addValue(ensembleSetOfLogRatios[ratioIndex][index - initialModelsBurnCount]);
            }
            logRatioMean[ratioIndex] = descriptiveStatisticsLogRatios.getMean();
        }

        // baseLines
        int baselineSize = analysisMethod.getSequenceTable().findFaradayDetectorsUsed().size();
        double[][] ensembleBaselines = new double[baselineSize][countOfEnsemblesUsed];
        double[] baselinesMeans = new double[baselineSize];
        double[] baselinesStdDev = new double[baselineSize];

        for (int row = 0; row < baselineSize; row++) {
            DescriptiveStatistics descriptiveStatisticsBaselines = new DescriptiveStatistics();
            for (int index = initialModelsBurnCount; index < countOfEnsemblesUsed + initialModelsBurnCount; index++) {
                // todo: fix magic number
                ensembleBaselines[row][index - initialModelsBurnCount] = ensembleRecordsList.get(index).baseLine()[row];//TODO: Decide / 6.24e7 * 1e6;
                descriptiveStatisticsBaselines.addValue(ensembleBaselines[row][index - initialModelsBurnCount]);
            }
            baselinesMeans[row] = descriptiveStatisticsBaselines.getMean();
            baselinesStdDev[row] = descriptiveStatisticsBaselines.getStandardDeviation();
        }

        // dalyFaraday gains
        double[] ensembleDalyFaradayGain = new double[countOfEnsemblesUsed];
        DescriptiveStatistics descriptiveStatisticsDalyFaradayGain = new DescriptiveStatistics();
        for (int index = initialModelsBurnCount; index < countOfEnsemblesUsed + initialModelsBurnCount; index++) {
            ensembleDalyFaradayGain[index - initialModelsBurnCount] = ensembleRecordsList.get(index).dfGain();
            descriptiveStatisticsDalyFaradayGain.addValue(ensembleDalyFaradayGain[index - initialModelsBurnCount]);
        }
        double dalyFaradayGainMean = descriptiveStatisticsDalyFaradayGain.getMean();

        // Intensity
        int knotsCount = singleBlockRawDataSetRecord.blockKnotInterpolationArray()[0].length;
        double[][] ensembleI0 = new double[knotsCount][countOfEnsemblesUsed];
        double[] meansI0 = new double[knotsCount];

        for (int knotIndex = 0; knotIndex < knotsCount; knotIndex++) {
            DescriptiveStatistics descriptiveStatisticsI0 = new DescriptiveStatistics();
            for (int index = initialModelsBurnCount; index < countOfEnsemblesUsed + initialModelsBurnCount; index++) {
                ensembleI0[knotIndex][index - initialModelsBurnCount] = ensembleRecordsList.get(index).I0()[knotIndex];
                descriptiveStatisticsI0.addValue(ensembleI0[knotIndex][index - initialModelsBurnCount]);
            }
            meansI0[knotIndex] = descriptiveStatisticsI0.getMean();
        }


        SingleBlockModelRecord summaryMCMCModel = new SingleBlockModelRecord(
                blockID,//original
                baselineSize,
                singleBlockRawDataSetRecord.onPeakStartingIndicesOfCycles().length,//singleBlockModelRecord.cycleCount(),
                analysisMethod.getSpeciesList().size(),
                analysisMethod.retrieveHighestAbundanceSpecies(),
                baselinesMeans,//calculated
                baselinesStdDev,//calculated
                dalyFaradayGainMean,//calculated
                singleBlockModelRecord.mapDetectorOrdinalToFaradayIndex(),
                logRatioMean,//calculated
                singleBlockRawDataSetRecord.mapOfSpeciesToActiveCycles(),//singleBlockModelRecord.mapOfSpeciesToActiveCycles(),
                singleBlockModelRecord.mapLogRatiosToCycleStats(),
                null,//dataModel,
                singleBlockModelRecord.dataSignalNoiseArray(),
                meansI0,//calculated
                singleBlockModelRecord.intensities().clone()
        );

        double[] dataModel = modelInitData(summaryMCMCModel, singleBlockRawDataSetRecord);
        double[] rawData = singleBlockRawDataSetRecord.blockRawDataArray();

        //prep for cycles
        int[] isotopeOrdinalIndicesAccumulatorArray = singleBlockRawDataSetRecord.blockIsotopeOrdinalIndicesArray();
        List<Double> ddver2List;

        List<Integer> tempTime;
        List<Integer> cyclesList;
        double[] ddVer2SortedArray;
        int[] cyclesSortedArray;
        int[] blockCycles = singleBlockRawDataSetRecord.blockCycleArray();
        int[] timeIndForSortingArray = singleBlockRawDataSetRecord.blockTimeIndicesArray();
        Map<IsotopicRatio, Map<Integer, double[]>> mapLogRatiosToCycleStats = new TreeMap<>();
        Map<Integer, double[]> denominatorMapCyclesToStats = new TreeMap<>();
        int startIndexOfPhotoMultiplierData = singleBlockRawDataSetRecord.getCountOfBaselineIntensities() + singleBlockRawDataSetRecord.getCountOfOnPeakFaradayIntensities();
        int[] d0_detVec = singleBlockRawDataSetRecord.blockDetectorOrdinalIndicesArray();

        for (int isotopeIndex = 0; isotopeIndex < singleBlockModelRecord.isotopeCount(); isotopeIndex++) {
            ddver2List = new ArrayList<>();
            cyclesList = new ArrayList<>();
            tempTime = new ArrayList<>();
            for (int dataArrayIndex = 0; dataArrayIndex < rawData.length; dataArrayIndex++) {
                if (isotopeOrdinalIndicesAccumulatorArray[dataArrayIndex] == isotopeIndex + 1) {
                    if (dataArrayIndex < startIndexOfPhotoMultiplierData) {
                        double calculated = (rawData[dataArrayIndex]
                                - baselinesMeans[singleBlockModelRecord.mapDetectorOrdinalToFaradayIndex().get(d0_detVec[dataArrayIndex])]) * dalyFaradayGainMean;
                        ddver2List.add(calculated);
                    } else {
                        ddver2List.add(rawData[dataArrayIndex]);
                    }

                    tempTime.add(timeIndForSortingArray[dataArrayIndex]);
                    cyclesList.add(blockCycles[dataArrayIndex]);
                }
            }

            double[] ddVer2Array = ddver2List.stream().mapToDouble(d -> d).toArray();
            int[] cyclesArray = cyclesList.stream().mapToInt(d -> d).toArray();
            int[] tempTimeIndicesArray = tempTime.stream().mapToInt(d -> d).toArray();
            SingleBlockModelInitForMCMC.ArrayIndexComparator comparatorTime = new SingleBlockModelInitForMCMC.ArrayIndexComparator(tempTimeIndicesArray);
            Integer[] ddVer2sortIndices = comparatorTime.createIndexArray();
            Arrays.sort(ddVer2sortIndices, comparatorTime);

            ddVer2SortedArray = new double[ddVer2Array.length];
            cyclesSortedArray = new int[ddVer2Array.length];
            for (int i = 0; i < ddVer2Array.length; i++) {
                ddVer2SortedArray[i] = ddVer2Array[ddVer2sortIndices[i]];
                cyclesSortedArray[i] = cyclesArray[ddVer2sortIndices[i]];
            }


            // start cycle-based math +++++++++++++++++++++++++++++++++++++++++++++++++++++++++
            // TODO: this is copied from SingleBlockModelInitForMCMC - need to refactor into one procedure
            double[][] interpolatedKnotData_II = singleBlockRawDataSetRecord.blockKnotInterpolationArray();
            Matrix II = new Matrix(interpolatedKnotData_II);
            Matrix I = new Matrix(meansI0, meansI0.length);
            Matrix intensityFn = II.times(I);

            DescriptiveStatistics[] cycleStats = new DescriptiveStatistics[summaryMCMCModel.cycleCount()];

            for (int dataArrayIndex = 0; dataArrayIndex < ddVer2SortedArray.length; dataArrayIndex++) {
                int cycle = cyclesSortedArray[dataArrayIndex] - 1;
                if (null == cycleStats[cycle]) {
                    cycleStats[cycle] = new DescriptiveStatistics();
                }
                // TODO: make this checks for both isotopes (eventually may include denominator as one that is excluded)
                if (singleBlockModelRecord.mapOfSpeciesToActiveCycles().get(analysisMethod.getSpeciesList().get(isotopeIndex))[cycle]) {
                    cycleStats[cycle].addValue(ddVer2SortedArray[dataArrayIndex] / intensityFn.get(dataArrayIndex, 0));
                }
            }


            Map<Integer, double[]> mapCyclesToStats = new TreeMap<>();

            for (int cycleIndex = 0; cycleIndex < cycleStats.length; cycleIndex++) {
                // TODO: fix this - currently using ratios instead of logs for cycles - see ViewCycles in matlab
                double[] cycleLogRatioStats = new double[2];

                cycleLogRatioStats[0] = (cycleStats[cycleIndex].getMean());
                // TODO: does this mean active cycles??
                cycleLogRatioStats[1] = cycleStats[cycleIndex].getStandardDeviation() / Math.sqrt(cycleStats[cycleIndex].getN());

                mapCyclesToStats.put(cycleIndex, cycleLogRatioStats);
            }

            int iden = singleBlockModelRecord.isotopeCount(); // ordinal
            if (isotopeIndex == iden - 1) {
                denominatorMapCyclesToStats = mapCyclesToStats;
            } else {
                mapLogRatiosToCycleStats.put(analysisMethod.getIsotopicRatiosList().get(isotopeIndex), mapCyclesToStats);
            }
        }

        // postprocess to correct by denominator isotope as per ViewCycles in matlab
        for (IsotopicRatio iRatio : mapLogRatiosToCycleStats.keySet()) {
            Map<Integer, double[]> numeratorMapCyclesToStats = mapLogRatiosToCycleStats.get(iRatio);
            for (int cycleIndex = 0; cycleIndex < numeratorMapCyclesToStats.keySet().size(); cycleIndex++) {
                numeratorMapCyclesToStats.get(cycleIndex)[0] /= denominatorMapCyclesToStats.get(cycleIndex)[0];
                double calcSterrCycleRatio =
                        numeratorMapCyclesToStats.get(cycleIndex)[1] = StrictMath.sqrt(StrictMath.pow(numeratorMapCyclesToStats.get(cycleIndex)[1], 2.0)
                                + StrictMath.pow(denominatorMapCyclesToStats.get(cycleIndex)[1], 2.0));
                numeratorMapCyclesToStats.get(cycleIndex)[1] = calcSterrCycleRatio;
            }
        }


        SingleBlockModelRecord finalMCMCModel = new SingleBlockModelRecord(
                blockID,//original
                baselineSize,
                singleBlockRawDataSetRecord.onPeakStartingIndicesOfCycles().length,//singleBlockModelRecord.cycleCount(),
                analysisMethod.getSpeciesList().size(),
                analysisMethod.retrieveHighestAbundanceSpecies(),
                baselinesMeans,//calculated
                baselinesStdDev,//calculated
                dalyFaradayGainMean,//calculated
                singleBlockModelRecord.mapDetectorOrdinalToFaradayIndex(),
                logRatioMean,//calculated
                singleBlockRawDataSetRecord.mapOfSpeciesToActiveCycles(),//singleBlockModelRecord.mapOfSpeciesToActiveCycles(),
                mapLogRatiosToCycleStats,
                dataModel,
                singleBlockModelRecord.dataSignalNoiseArray(),
                meansI0,//calculated
                singleBlockModelRecord.intensities().clone()
        );


        analysis.getMapOfBlockIdToFinalModel().put(blockID, finalMCMCModel);
    }

    public record EnsembleRecord(
            double[] logRatios,
            double[] I0,
            double[] baseLine,
            double dfGain,
            double[] signalNoise,
            double errorWeighted,
            double errorUnWeighted
    ) implements Serializable {
        public String prettyPrintHeaderAsCSV(String indexTitle, List<IsotopicRatio> isotopicRatiosList) {
            String header = "";
            for (int i = 0; i < logRatios.length; i++) {
                header += isotopicRatiosList.get(i).prettyPrint().replaceAll(" ", "") + ",";
            }
            for (int i = 0; i < I0.length; i++) {
                header += "I-" + i + ",";
            }
            for (int i = 0; i < baseLine.length; i++) {
                header += "BL-" + i + ",";
            }
            header += "DFGain,";
            header += "errorWeighted,";
            header += "errorUnWeighted \n";

            return header;
        }

        public String prettyPrintAsCSV() {
            String data = "";
            for (int i = 0; i < logRatios.length; i++) {
                data += logRatios[i] + ",";
            }
            for (int i = 0; i < I0.length; i++) {
                data += I0[i] + ",";
            }
            for (int i = 0; i < baseLine.length; i++) {
                data += baseLine[i] + ",";
            }
            data += dfGain() + ",";
            data += errorWeighted + ",";
            data += errorUnWeighted + "\n";

            return data;
        }
    }
}