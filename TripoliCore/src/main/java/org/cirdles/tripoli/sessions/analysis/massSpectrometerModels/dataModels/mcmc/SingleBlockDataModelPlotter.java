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

import org.cirdles.tripoli.expressions.species.IsotopicRatio;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;
import org.cirdles.tripoli.plots.linePlots.MultiLinePlotBuilder;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;

import java.util.List;
import java.util.Map;

import static java.lang.StrictMath.exp;
import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.BlockEnsemblesPlotter.blockEnsemblePlotEngine;

/**
 * @author James F. Bowring
 */
public enum SingleBlockDataModelPlotter {
    ;

    public synchronized static PlotBuilder[][] analysisAndPlotting(
            int blockID,
            AnalysisInterface analysis) {

        List<EnsemblesStore.EnsembleRecord> ensembleRecordsList = analysis.getMapBlockIDToEnsembles().get(blockID);
        AnalysisMethod analysisMethod = analysis.getAnalysisMethod();
        SingleBlockModelRecord singleBlockCurrentModelRecord_X = analysis.getMapOfBlockIdToFinalModel().get(blockID);
        SingleBlockRawDataSetRecord singleBlockRawDataSetRecord = analysis.getMapOfBlockIdToRawData().get(blockID);

        PlotBuilder[][] plotBuilders = new PlotBuilder[16][1];
        analysis.getMapOfBlockIdToPlots().put(blockID, plotBuilders);
        blockEnsemblePlotEngine(blockID, analysis);

        List<IsotopicRatio> isotopicRatioList = analysisMethod.getIsotopicRatiosList();
        int knotsCount = ensembleRecordsList.get(0).I0().length;
        int baselineSize = analysisMethod.getSequenceTable().findFaradayDetectorsUsed().size();
        double[] baselinesMeans = singleBlockCurrentModelRecord_X.baselineMeansArray();
        int initialModelsBurnCount = analysis.getMapOfBlockIdToModelsBurnCount().get(blockID);
        List<Detector> faradayDetectorsUsed = analysisMethod.getSequenceTable().findFaradayDetectorsUsed();

        // visualization converge ratio and others TABS
        double[][] convergeIntensities = new double[knotsCount][ensembleRecordsList.size()];
        for (int index = 0; index < ensembleRecordsList.size(); index++) {
            for (int knotsIndex = 0; knotsIndex < knotsCount; knotsIndex++) {
                convergeIntensities[knotsIndex][index] = ensembleRecordsList.get(index).I0()[knotsIndex];
            }
        }

        // new converge plots
        double[][] convergeSetOfLogRatios = new double[isotopicRatioList.size()][ensembleRecordsList.size()];
        double[][] convergeSetOfBaselines = new double[baselineSize][ensembleRecordsList.size()];
        double[] convergeErrWeightedMisfit = new double[ensembleRecordsList.size()];
        double[] convergeErrRawMisfit = new double[ensembleRecordsList.size()];
        double[] xDataConvergeSavedIterations = new double[ensembleRecordsList.size()];
        for (int ensembleIndex = 0; ensembleIndex < ensembleRecordsList.size(); ensembleIndex++) {
            for (int ratioIndex = 0; ratioIndex < isotopicRatioList.size(); ratioIndex++) {
                convergeSetOfLogRatios[ratioIndex][ensembleIndex] = ensembleRecordsList.get(ensembleIndex).logRatios()[ratioIndex];
            }
            for (int faradayIndex = 0; faradayIndex < baselineSize; faradayIndex++) {
                convergeSetOfBaselines[faradayIndex][ensembleIndex] = ensembleRecordsList.get(ensembleIndex).baseLine()[faradayIndex];
            }
            convergeErrWeightedMisfit[ensembleIndex] = StrictMath.sqrt(ensembleRecordsList.get(ensembleIndex).errorWeighted());
            convergeErrRawMisfit[ensembleIndex] = StrictMath.sqrt(ensembleRecordsList.get(ensembleIndex).errorUnWeighted());

            xDataConvergeSavedIterations[ensembleIndex] = ensembleIndex + 1;
        }

        plotBuilders[5] = new PlotBuilder[convergeSetOfLogRatios.length];
        for (int i = 0; i < convergeSetOfLogRatios.length; i++) {
            plotBuilders[5][i] = LinePlotBuilder.initializeLinePlot(
                    xDataConvergeSavedIterations, convergeSetOfLogRatios[i],
                    new String[]{isotopicRatioList.get(i).prettyPrint()}, "Saved iterations", "Log Ratio", initialModelsBurnCount, blockID);
        }

        plotBuilders[6] = new PlotBuilder[convergeSetOfBaselines.length];
        for (int i = 0; i < convergeSetOfBaselines.length; i++) {
            plotBuilders[6][i] = LinePlotBuilder.initializeLinePlot(
                    xDataConvergeSavedIterations, convergeSetOfBaselines[i],
                    new String[]{faradayDetectorsUsed.get(i).getDetectorName() + " Baseline"}, "Saved iterations", "Baseline Counts", initialModelsBurnCount, blockID);
        }

        plotBuilders[8][0] = LinePlotBuilder.initializeLinePlot(xDataConvergeSavedIterations, convergeErrWeightedMisfit,
                new String[]{"Converge Weighted Misfit"}, "Saved iterations", "Weighted Misfit", initialModelsBurnCount, blockID);

        plotBuilders[9][0] = LinePlotBuilder.initializeLinePlot(xDataConvergeSavedIterations, convergeErrRawMisfit,
                new String[]{"Converge Raw Misfit"}, "Saved iterations", "Raw Misfit", initialModelsBurnCount, blockID);


        plotBuilders[10][0] = MultiLinePlotBuilder.initializeLinePlot(
                new double[][]{xDataConvergeSavedIterations}, convergeIntensities, new String[]{"Converge Intensity"}, "Saved iterations", "Knots by color", false, blockID, initialModelsBurnCount);


        // visualization data fit ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        int baselineCount = singleBlockRawDataSetRecord.baselineDataSetMCMC().intensityAccumulatorList().size();
        int onPeakFaradayCount = singleBlockRawDataSetRecord.onPeakFaradayDataSetMCMC().intensityAccumulatorList().size();
        int onPeakPhotoMultCount = singleBlockRawDataSetRecord.onPeakPhotoMultiplierDataSetMCMC().intensityAccumulatorList().size();
        int totalIntensityCount = baselineCount + onPeakFaradayCount + onPeakPhotoMultCount;

        double[] dataArray = new double[totalIntensityCount];
        double[] dataWithNoBaselineArray = new double[totalIntensityCount];
        Map<Integer, Integer> mapDetectorOrdinalToFaradayIndex = singleBlockCurrentModelRecord_X.mapDetectorOrdinalToFaradayIndex();

        double[] logRatios = singleBlockCurrentModelRecord_X.logRatios().clone();
        double[] intensities = singleBlockCurrentModelRecord_X.intensities();

        double detectorFaradayGain = singleBlockCurrentModelRecord_X.detectorFaradayGain();
        double[] dataCountsModelOneSigma_Dsig = new double[totalIntensityCount];
        double[] integrationTimes = new double[totalIntensityCount];

        List<Integer> isotopeOrdinalIndicesAccumulatorList = singleBlockRawDataSetRecord.onPeakFaradayDataSetMCMC().isotopeOrdinalIndicesAccumulatorList();
        List<Integer> detectorOrdinalIndicesAccumulatorList = singleBlockRawDataSetRecord.onPeakFaradayDataSetMCMC().detectorOrdinalIndicesAccumulatorList();
        List<Integer> timeIndexAccumulatorList = singleBlockRawDataSetRecord.onPeakFaradayDataSetMCMC().timeIndexAccumulatorList();
        List<Double> timeAccumulatorList = singleBlockRawDataSetRecord.onPeakFaradayDataSetMCMC().timeAccumulatorList();
        for (int dataArrayIndex = baselineCount; dataArrayIndex < baselineCount + onPeakFaradayCount; dataArrayIndex++) {
            int intensityIndex = timeIndexAccumulatorList.get(dataArrayIndex - baselineCount);
            int isotopeIndex = isotopeOrdinalIndicesAccumulatorList.get(dataArrayIndex - baselineCount) - 1;
            int faradayIndex = mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndicesAccumulatorList.get(dataArrayIndex - baselineCount));
            /*
                itmp = d0.iso_ind(:,mm) & ~d0.axflag & d0.block(:,n);
                d(itmp,1) = (x.lograt(mm))*x.DFgain^-1 *Intensity{n}(d0.time_ind(itmp)) + x.BL(d0.det_vec(itmp)); %debug
                dnobl(itmp,1) = (x.lograt(mm))*x.DFgain^-1 *Intensity{n}(d0.time_ind(itmp)); %debug
             */

            if (isotopeIndex < logRatios.length) {
                dataArray[dataArrayIndex] = exp(logRatios[isotopeIndex]) / detectorFaradayGain
                        * intensities[intensityIndex] + baselinesMeans[faradayIndex];
            } else {
                dataArray[dataArrayIndex] = 1.0 / detectorFaradayGain * intensities[intensityIndex] + baselinesMeans[faradayIndex];
            }
            dataWithNoBaselineArray[dataArrayIndex] = dataArray[dataArrayIndex] - baselinesMeans[faradayIndex];

            /*
            Dsig = sqrt(x.sig(d0.det_vec).^2 + x.sig(end).*dnobl); % New data covar vector
             */
            double calculatedValue = StrictMath.sqrt(1.0 //pow(xSig[faradayIndex], 2)
                    + //xSig[xSig.length - 1]
                    dataWithNoBaselineArray[dataArrayIndex]);
            dataCountsModelOneSigma_Dsig[dataArrayIndex] = calculatedValue;

            integrationTimes[dataArrayIndex] = timeAccumulatorList.get(intensityIndex);
        }

        isotopeOrdinalIndicesAccumulatorList = singleBlockRawDataSetRecord.onPeakPhotoMultiplierDataSetMCMC().isotopeOrdinalIndicesAccumulatorList();
        timeIndexAccumulatorList = singleBlockRawDataSetRecord.onPeakPhotoMultiplierDataSetMCMC().timeIndexAccumulatorList();
        timeAccumulatorList = singleBlockRawDataSetRecord.onPeakPhotoMultiplierDataSetMCMC().timeAccumulatorList();
        for (int dataArrayIndex = baselineCount + onPeakFaradayCount; dataArrayIndex < baselineCount + onPeakFaradayCount + onPeakPhotoMultCount; dataArrayIndex++) {
            int intensityIndex = timeIndexAccumulatorList.get(dataArrayIndex - baselineCount - onPeakFaradayCount);
            int isotopeIndex = isotopeOrdinalIndicesAccumulatorList.get(dataArrayIndex - baselineCount - onPeakFaradayCount).intValue() - 1;

            /*
                itmp = d0.iso_ind(:,mm) & d0.axflag & d0.block(:,n);
                d(itmp,1) = (x.lograt(mm))*Intensity{n}(d0.time_ind(itmp)); %debug
                dnobl(itmp,1) = (x.lograt(mm))*Intensity{n}(d0.time_ind(itmp)); %debug
             */
            if (isotopeIndex < logRatios.length) {
                dataArray[dataArrayIndex] = exp(logRatios[isotopeIndex]) * intensities[intensityIndex];
            } else {
                dataArray[dataArrayIndex] = intensities[intensityIndex];
            }
            dataWithNoBaselineArray[dataArrayIndex] = dataArray[dataArrayIndex];

            double calculatedValue = StrictMath.sqrt(1.0 //StrictMath.pow(xSig[faradayIndex], 2)
                    + //xSig[xSig.length - 1]
                    dataWithNoBaselineArray[dataArrayIndex]);
            dataCountsModelOneSigma_Dsig[dataArrayIndex] = calculatedValue;

            integrationTimes[dataArrayIndex] = timeAccumulatorList.get(intensityIndex);
        }

        detectorOrdinalIndicesAccumulatorList = singleBlockRawDataSetRecord.baselineDataSetMCMC().detectorOrdinalIndicesAccumulatorList();
        timeIndexAccumulatorList = singleBlockRawDataSetRecord.baselineDataSetMCMC().timeIndexAccumulatorList();
        timeAccumulatorList = singleBlockRawDataSetRecord.baselineDataSetMCMC().timeAccumulatorList();
        for (int dataArrayIndex = 0; dataArrayIndex < baselineCount; dataArrayIndex++) {
            int intensityIndex = timeIndexAccumulatorList.get(dataArrayIndex);
            int faradayIndex = mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndicesAccumulatorList.get(dataArrayIndex));
            dataArray[dataArrayIndex] = baselinesMeans[faradayIndex];

            //TODO: WTF???
            double calculatedValue = StrictMath.sqrt(1.0//pow(xSig[faradayIndex], 2)
                    + //xSig[xSig.length - 1]
                    dataWithNoBaselineArray[dataArrayIndex]);
            dataCountsModelOneSigma_Dsig[dataArrayIndex] = calculatedValue;

            integrationTimes[dataArrayIndex] = timeAccumulatorList.get(intensityIndex);
        }


//        double[] dataOriginalCounts = singleBlockRawDataSetRecord.blockRawDataArray().clone();
//        double[] yDataResiduals = new double[dataOriginalCounts.length];

//        Arrays.sort(integrationTimes);
//        for (int i = 0; i < dataOriginalCounts.length; i++) {
//            yDataResiduals[i] = dataOriginalCounts[i] - dataArray[i];
//        }
//
//        plotBuilders[13][0] = ComboPlotBuilder.initializeLinePlot(
//                integrationTimes, dataOriginalCounts, dataArray, new String[]{"Observed Data"}, "Integration Time (secs)", "Intensity");
//        plotBuilders[15][0] = ComboPlotBuilder.initializeLinePlotWithSubsets(
//                integrationTimes, dataOriginalCounts, dataArray, singleBlockRawDataSetRecord.blockMapIdsToDataTimes(),
//                new String[]{"Observed Data by Sequence"}, "Integration Time (secs)", "Intensity");
//        plotBuilders[14][0] = ComboPlotBuilder.initializeLinePlotWithOneSigma(
//                integrationTimes, yDataResiduals, dataCountsModelOneSigma_Dsig, new String[]{"Residual Data"}, "Integration Time (secs)", "Intensity");

        return plotBuilders;
    }
}