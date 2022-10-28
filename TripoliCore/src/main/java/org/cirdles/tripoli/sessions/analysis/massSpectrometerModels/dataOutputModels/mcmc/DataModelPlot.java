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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.mcmc;

import javafx.scene.paint.Color;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.species.IsotopicRatio;
import org.cirdles.tripoli.visualizationUtilities.AbstractPlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.histograms.HistogramBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.ComboPlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.LinePlotBuilder;
import org.cirdles.tripoli.visualizationUtilities.linePlots.MultiLinePlotBuilder;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.exp;

/**
 * @author James F. Bowring
 */
public class DataModelPlot {

    public static AbstractPlotBuilder[][] analysisAndPlotting(
            MassSpecOutputDataRecord massSpecOutputDataRecord,
            List<EnsemblesStore.EnsembleRecord> ensembleRecordsList,
            DataModellerOutputRecord lastDataModelInit,
            AnalysisMethod analysisMethod
            ) {
        List<IsotopicRatio> isotopicRatioList = analysisMethod.getTripoliRatiosList();

        /*
            %% Analysis and Plotting

            burn = 1000; % Number of models to discard
            ens_rat =[ensemble.lograt];

            % Calculate mean and st dev of ratios after burn in time
            ratmean = mean(ens_rat(:,burn:cnt),2);  % Log ratios
            ratstd = std(ens_rat(:,burn:cnt),[],2);

            BLmean = mean(ens_BL(:,burn:cnt),2);  % Baselines
            BLstd = std(ens_BL(:,burn:cnt),[],2);

            sigmean = mean(ens_sig(:,burn:cnt),2);   % Noise hyperparams
            sigstd = std(ens_sig(:,burn:cnt),[],2);

            DFmean = mean(ens_DF(:,burn:cnt),2);   % Daly-Far gain
            DFstd = std(ens_DF(:,burn:cnt),[],2);

         */
        int burn;// = 100;// 500;//1000;
        burn = 450;//Math.min(100, ensembleRecordsList.size() - 50);
        int countOfEnsemblesUsed = ensembleRecordsList.size() - burn;

        // log ratios - only the first row
        double[][] ensembleLogRatios = new double[isotopicRatioList.size()][countOfEnsemblesUsed];
        double[][] ensembleRatios = new double[isotopicRatioList.size()][countOfEnsemblesUsed];
        double[] logRatioMean = new double[isotopicRatioList.size()];
        double[] logRatioStdDev = new double[isotopicRatioList.size()];
        DescriptiveStatistics descriptiveStatisticsLogRatios = new DescriptiveStatistics();
        for (int ratioIndex = 0; ratioIndex < isotopicRatioList.size(); ratioIndex++) {
            for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
                ensembleLogRatios[ratioIndex][index - burn] = ensembleRecordsList.get(index).logRatios()[ratioIndex];
                descriptiveStatisticsLogRatios.addValue(ensembleLogRatios[ratioIndex][index - burn]);
                ensembleRatios[ratioIndex][index - burn] = exp(ensembleLogRatios[ratioIndex][index - burn]);
            }
            logRatioMean[ratioIndex] = descriptiveStatisticsLogRatios.getMean();
            logRatioStdDev[ratioIndex] = descriptiveStatisticsLogRatios.getStandardDeviation();
        }

        // baseLines
        int baselineSize = ensembleRecordsList.get(0).baseLine().length;
        double[][] ensembleBaselines = new double[baselineSize][countOfEnsemblesUsed];
        double[] baselinesMeans = new double[baselineSize];
        double[] baselinesStdDev = new double[baselineSize];

        for (int row = 0; row < baselineSize; row++) {
            DescriptiveStatistics descriptiveStatisticsBaselines = new DescriptiveStatistics();
            for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
                // todo: fix magic number
                ensembleBaselines[row][index - burn] = ensembleRecordsList.get(index).baseLine()[row];//TODO: Decide / 6.24e7 * 1e6;
                descriptiveStatisticsBaselines.addValue(ensembleBaselines[row][index - burn]);
            }
            baselinesMeans[row] = descriptiveStatisticsBaselines.getMean();
            baselinesStdDev[row] = descriptiveStatisticsBaselines.getStandardDeviation();
        }

        // dalyFaraday gains
        double[] ensembleDalyFaradayGain = new double[countOfEnsemblesUsed];
        DescriptiveStatistics descriptiveStatisticsDalyFaradayGain = new DescriptiveStatistics();
        for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
            ensembleDalyFaradayGain[index - burn] = ensembleRecordsList.get(index).dfGain();
            descriptiveStatisticsDalyFaradayGain.addValue(ensembleDalyFaradayGain[index - burn]);
        }
        double dalyFaradayGainMean = descriptiveStatisticsDalyFaradayGain.getMean();
        double dalyFaradayGainStdDev = descriptiveStatisticsDalyFaradayGain.getStandardDeviation();

        // signal noise
        int faradayCount = massSpecOutputDataRecord.faradayCount();
        double[][] ensembleSignalnoise = new double[faradayCount][countOfEnsemblesUsed];
        double[] signalNoiseMeans = new double[faradayCount];
        double[] signalNoiseStdDev = new double[faradayCount];

        for (int row = 0; row < faradayCount; row++) {
            DescriptiveStatistics descriptiveStatisticsSignalNoise = new DescriptiveStatistics();
            for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
                ensembleSignalnoise[row][index - burn] = ensembleRecordsList.get(index).signalNoise()[row];
                descriptiveStatisticsSignalNoise.addValue(ensembleSignalnoise[row][index - burn]);
            }
            signalNoiseMeans[row] = descriptiveStatisticsSignalNoise.getMean();
            signalNoiseStdDev[row] = descriptiveStatisticsSignalNoise.getStandardDeviation();
        }

        /*
            for m=1:d0.Nblock
                for n = 1:cnt;
                    ens_I{m}(:,n) =[ensemble(n).I{m}];
                end
                Imean{m} = mean(ens_I{m}(:,burn:cnt),2);
                Istd{m} = std(ens_I{m}(:,burn:cnt),[],2);
            end
         */

        // Intensity
        int knotsCount = ensembleRecordsList.get(0).blockIntensities()[0].length;
        int blockCount = massSpecOutputDataRecord.blockCount();
        double[][] ensembleIntensity = new double[knotsCount][countOfEnsemblesUsed];
        double[][] intensityMeans = new double[blockCount][knotsCount];
        double[][] intensityStdDevs = new double[blockCount][knotsCount];

        for (int blockIndex = 0; blockIndex < blockCount; blockIndex++) {
            for (int knotIndex = 0; knotIndex < knotsCount; knotIndex++) {
                DescriptiveStatistics descriptiveStatisticsIntensity = new DescriptiveStatistics();
                for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
                    ensembleIntensity[knotIndex][index - burn] = ensembleRecordsList.get(index).blockIntensities()[blockIndex][knotIndex];
                    descriptiveStatisticsIntensity.addValue(ensembleIntensity[knotIndex][index - burn]);
                }
                intensityMeans[blockIndex][knotIndex] = descriptiveStatisticsIntensity.getMean();
                intensityStdDevs[blockIndex][knotIndex] = descriptiveStatisticsIntensity.getStandardDeviation();
            }
        }

        // calculate blockIntensities means for plotting
        double[][] yDataIntensityMeans = new double[blockCount][];
        for (int blockIndex = 0; blockIndex < blockCount; blockIndex++) {
            PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
            MatrixStore<Double> intensityMeansMatrix = storeFactory.columns(intensityMeans[blockIndex]);
            MatrixStore<Double> yDataMatrix = massSpecOutputDataRecord.allBlockInterpolations()[blockIndex].multiply(intensityMeansMatrix).multiply((1.0 / (dalyFaradayGainMean * 6.24e7)) * 1e6);
            yDataIntensityMeans[blockIndex] = yDataMatrix.toRawCopy1D();
        }
        double[] xDataIntensityMeans = new double[massSpecOutputDataRecord.allBlockInterpolations()[0].getRowDim()];
        for (int i = 0; i < xDataIntensityMeans.length; i++) {
            xDataIntensityMeans[i] = i;
        }

        // visualization - Ensembles tab
        AbstractPlotBuilder[][] plotBuilders = new AbstractPlotBuilder[15][1];

        plotBuilders[0] = new AbstractPlotBuilder[ensembleRatios.length];
        for (int i = 0; i < ensembleRatios.length; i++) {
            plotBuilders[0][i] = HistogramBuilder.initializeHistogram(ensembleRatios[i],
                    50, isotopicRatioList.get(i).prettyPrint(), "Ratios", "Frequency", Color.BLUE);
        }

        plotBuilders[1] = new AbstractPlotBuilder[ensembleBaselines.length];
        List<Detector> faradayDetectorsUsed = analysisMethod.getSequenceTable().findFaradayDetectorsUsed();
        for (int i = 0; i < ensembleBaselines.length; i++) {
            plotBuilders[1][i] = HistogramBuilder.initializeHistogram(ensembleBaselines[i],
                    50, faradayDetectorsUsed.get(i).getDetectorName() + " Baseline", "Baseline Counts", "Frequency", Color.BLUE);
        }

        plotBuilders[2][0] = HistogramBuilder.initializeHistogram(ensembleDalyFaradayGain,
                50, "Daly/Faraday Gain", "Gain", "Frequency", Color.BLUE);

        plotBuilders[3] = new AbstractPlotBuilder[ensembleSignalnoise.length];
        for (int i = 0; i < ensembleSignalnoise.length; i++) {
            plotBuilders[3][i] = HistogramBuilder.initializeHistogram(ensembleSignalnoise[i],
                    50, faradayDetectorsUsed.get(i).getDetectorName() + " Signal Noise", "Noise hyperparameter", "Frequency", Color.GREEN);
        }

        plotBuilders[4][0] = MultiLinePlotBuilder.initializeLinePlot(xDataIntensityMeans, yDataIntensityMeans, "Mean Intensity");

        // visualization converge ratio and others tabs
        double[] convergeLogRatios = new double[ensembleRecordsList.size()];
        double[] convergeRatios = new double[ensembleRecordsList.size()];
        // todo: hardwired for 2 isotopes
        double[] convergeBaselineFaradayL1 = new double[ensembleRecordsList.size()];
        double[] convergeBaselineFaradayH1 = new double[ensembleRecordsList.size()];
        double[] convergeErrWeightedMisfit = new double[ensembleRecordsList.size()];
        double[] convergeErrRawMisfit = new double[ensembleRecordsList.size()];
        double[] xDataconvergeSavedIterations = new double[ensembleRecordsList.size()];
        double[][] convergeIntensities = new double[ensembleRecordsList.get(0).blockIntensities()[0].length][ensembleRecordsList.size()];
        double[] convergeNoiseFaradayL1 = new double[ensembleRecordsList.size()];
        double[] convergeNoiseFaradayH1 = new double[ensembleRecordsList.size()];
        for (int index = 0; index < ensembleRecordsList.size(); index++) {
            convergeLogRatios[index] = ensembleRecordsList.get(index).logRatios()[0];
            convergeRatios[index] = exp(convergeLogRatios[index]);
            convergeBaselineFaradayL1[index] = ensembleRecordsList.get(index).baseLine()[0];
            convergeBaselineFaradayH1[index] = ensembleRecordsList.get(index).baseLine()[1];
            convergeErrWeightedMisfit[index] = StrictMath.sqrt(ensembleRecordsList.get(index).errorWeighted());
            convergeErrRawMisfit[index] = StrictMath.sqrt(ensembleRecordsList.get(index).errorUnWeighted());
            for (int intensityIndex = 0; intensityIndex < convergeIntensities.length; intensityIndex++) {
                // todo: fix this block indexing issue
                convergeIntensities[intensityIndex][index] = ensembleRecordsList.get(index).blockIntensities()[0][intensityIndex];
            }
            convergeNoiseFaradayL1[index] = ensembleRecordsList.get(index).signalNoise()[0];
            convergeNoiseFaradayH1[index] = ensembleRecordsList.get(index).signalNoise()[1];
            xDataconvergeSavedIterations[index] = index + 1;
        }
        plotBuilders[5][0] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeRatios, "Converge Ratio");
        plotBuilders[6][0] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeBaselineFaradayL1, "Converge Baseline Faraday L1");
        plotBuilders[7][0] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeBaselineFaradayH1, "Converge Baseline Faraday H1");
        plotBuilders[8][0] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeErrWeightedMisfit, "Converge Weighted Misfit");
        plotBuilders[9][0] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeErrRawMisfit, "Converge Raw Misfit");
        plotBuilders[10][0] = MultiLinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeIntensities, "Converge Intensity");
        plotBuilders[11][0] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeNoiseFaradayL1, "Converge Noise Faraday L1");
        plotBuilders[12][0] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeNoiseFaradayH1, "Converge Noise Faraday H1");


        // visualization data fit
        // todo: this is duplicated code from above in part
        double[] data = lastDataModelInit.dataArray();
        double[] dataWithNoBaseline = new double[lastDataModelInit.dataArray().length];
        EnsemblesStore.EnsembleRecord lastModelRecord = ensembleRecordsList.get(ensembleRecordsList.size() - 1);

        for (int blockIndex = 0; blockIndex < massSpecOutputDataRecord.blockCount(); blockIndex++) {
            ArrayList<double[]> intensity = new ArrayList<>(1);
            intensity.add(0, lastDataModelInit.intensityPerBlock().get(blockIndex));
            // Oct 2022 per email from Noah, eliminate the iden/iden ratio to guarantee positive definite  covariance matrix >> isotope count - 1
            for (int isotopeIndex = 0; isotopeIndex < massSpecOutputDataRecord.isotopeCount() - 1; isotopeIndex++) {
                for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().length; row++) {
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn()[row][isotopeIndex] == 1)
                            && (massSpecOutputDataRecord.ionCounterFlagsForRawDataColumn()[row] == 1)
                            && massSpecOutputDataRecord.blockIndicesForRawDataColumn()[row] == (blockIndex + 1)) {
                        double calcValue =
                                exp(lastModelRecord.logRatios()[isotopeIndex])
                                        * intensity.get(0)[(int) massSpecOutputDataRecord.timeIndColumn()[row] - 1];
                        data[row] = calcValue;
                        dataWithNoBaseline[row] = calcValue;
                    }
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn()[row][isotopeIndex] == 1)
                            && (massSpecOutputDataRecord.ionCounterFlagsForRawDataColumn()[row] == 0)
                            && massSpecOutputDataRecord.blockIndicesForRawDataColumn()[row] == (blockIndex + 1)) {
                        double calcValue =
                                exp(lastModelRecord.logRatios()[isotopeIndex]) / lastModelRecord.dfGain()
                                        * intensity.get(0)[(int) massSpecOutputDataRecord.timeIndColumn()[row] - 1];
                        dataWithNoBaseline[row] = calcValue;
                        data[row] =
                                calcValue + lastModelRecord.baseLine()[(int) massSpecOutputDataRecord.detectorIndicesForRawDataColumn()[row] - 1];
                    }
                }
            }
        }

        double[] xSig = lastModelRecord.signalNoise();
        double[] detectorIndicesForRawDataColumn = massSpecOutputDataRecord.detectorIndicesForRawDataColumn();
        double[] dataCountsModelOneSigma = new double[detectorIndicesForRawDataColumn.length];
        for (int row = 0; row < detectorIndicesForRawDataColumn.length; row++) {
            dataCountsModelOneSigma[row]
                    = StrictMath.sqrt(StrictMath.pow(xSig[(int) detectorIndicesForRawDataColumn[row] - 1], 2)
                    + xSig[xSig.length - 1] * dataWithNoBaseline[row]);
        }

        int plottingStep = 10;
        double[] dataOriginalCounts = massSpecOutputDataRecord.rawDataColumn();
        double[] xDataIndex = new double[dataOriginalCounts.length / plottingStep];
        double[] yDataCounts = new double[dataOriginalCounts.length / plottingStep];
        double[] yDataModelCounts = new double[dataOriginalCounts.length / plottingStep];

        double[] yDataResiduals = new double[dataOriginalCounts.length / plottingStep];
        double[] yDataSigmas = new double[dataOriginalCounts.length / plottingStep];

        for (int i = 0; i < dataOriginalCounts.length / plottingStep; i++) {
            xDataIndex[i] = i * plottingStep;
            yDataCounts[i] = dataOriginalCounts[i * plottingStep];
            yDataModelCounts[i] = data[i * plottingStep];
            yDataResiduals[i] = dataOriginalCounts[i * plottingStep] - data[i * plottingStep];
            yDataSigmas[i] = dataCountsModelOneSigma[i * plottingStep];
        }
        plotBuilders[13][0] = ComboPlotBuilder.initializeLinePlot(xDataIndex, yDataCounts, yDataModelCounts, "Observed Data");

        plotBuilders[14][0] = ComboPlotBuilder.initializeLinePlotWithOneSigma(xDataIndex, yDataResiduals, yDataSigmas, "Residual Data");


        // todo: missing additional elements of signalNoise (i.e., 0,11,11)
        System.err.println(logRatioMean + "         " + logRatioStdDev);
        System.err.println(baselinesMeans[0] + "         " + baselinesMeans[1] + "    " + baselinesStdDev[0] + "     " + baselinesStdDev[1]);
        System.err.println(dalyFaradayGainMean + "    " + dalyFaradayGainStdDev);
        System.err.println(signalNoiseMeans[0] + "         " + signalNoiseMeans[1] + "    " + signalNoiseStdDev[0] + "     " + signalNoiseStdDev[1]);


        return plotBuilders;
    }

}