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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.rjmcmc;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
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

    static AbstractPlotBuilder[] analysisAndPlotting(
            MassSpecOutputDataRecord massSpecOutputDataRecord, List<EnsemblesStore.EnsembleRecord> ensembleRecordsList, DataModellerOutputRecord lastDataModelInit) {
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
        int burn = 500;//1000;
        int countOfEnsemblesUsed = ensembleRecordsList.size() - burn;

        // log ratios - only the first row
        double[] ensembleLogRatios = new double[countOfEnsemblesUsed];
        double[] ensembleRatios = new double[countOfEnsemblesUsed];
        DescriptiveStatistics descriptiveStatisticsLogRatios = new DescriptiveStatistics();
        for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
            ensembleLogRatios[index - burn] = ensembleRecordsList.get(index).logRatios()[0];
            descriptiveStatisticsLogRatios.addValue(ensembleLogRatios[index - burn]);
            ensembleRatios[index - burn] = exp(ensembleLogRatios[index - burn]);
        }
        double logRatioMean = descriptiveStatisticsLogRatios.getMean();
        double logRatioStdDev = descriptiveStatisticsLogRatios.getStandardDeviation();

        // baseLines - first 2 rows
        double[][] ensembleBaselines = new double[ensembleRecordsList.get(0).baseLine().length][countOfEnsemblesUsed];
        double[] baselinesMeans = new double[massSpecOutputDataRecord.isotopeCount()];
        double[] baselinesStdDev = new double[massSpecOutputDataRecord.isotopeCount()];

        for (int row = 0; row < ensembleRecordsList.get(0).baseLine().length; row++) {
            DescriptiveStatistics descriptiveStatisticsBaselines = new DescriptiveStatistics();
            for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
                ensembleBaselines[row][index - burn] = ensembleRecordsList.get(index).baseLine()[row] / 6.24e7 * 1e6;
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
        double[][] ensembleSignalnoise = new double[2][countOfEnsemblesUsed];
        double[] signalNoiseMeans = new double[2];
        double[] signalNoiseStdDev = new double[2];

        for (int row = 0; row < massSpecOutputDataRecord.faradayCount(); row++) {
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
        // meanof 16 items across 400
        // todo handle blocks
        int knotsCount = ensembleRecordsList.get(0).intensity()[0].length;
        double[][] ensembleIntensity = new double[knotsCount][countOfEnsemblesUsed];
        double[] intensityMeans = new double[knotsCount];
        double[] intensityStdDevs = new double[knotsCount];

        for (int knotIndex = 0; knotIndex < knotsCount; knotIndex++) {
            DescriptiveStatistics descriptiveStatisticsIntensity = new DescriptiveStatistics();
            for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
                ensembleIntensity[knotIndex][index - burn] = ensembleRecordsList.get(index).intensity()[0][knotIndex];
                descriptiveStatisticsIntensity.addValue(ensembleIntensity[knotIndex][index - burn]);
            }
            intensityMeans[knotIndex] = descriptiveStatisticsIntensity.getMean();
            intensityStdDevs[knotIndex] = descriptiveStatisticsIntensity.getStandardDeviation();
        }

        // calculate intensity means for plotting
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        MatrixStore<Double> intensityMeansMatrix = storeFactory.columns(intensityMeans);
        MatrixStore<Double> yDataMatrix = massSpecOutputDataRecord.allBlockInterpolations()[0].multiply(intensityMeansMatrix).multiply((1.0 / (dalyFaradayGainMean * 6.24e7)) * 1e6);
        double[] yDataIntensityMeans = yDataMatrix.toRawCopy1D();
        double[] xDataIntensityMeans = new double[massSpecOutputDataRecord.allBlockInterpolations()[0].getRowDim()];
        for (int i = 0; i < xDataIntensityMeans.length; i++) {
            xDataIntensityMeans[i] = i;
        }


        // visualization - Ensembles tab
        AbstractPlotBuilder[] plotBuilders = new AbstractPlotBuilder[15];
        plotBuilders[0] = HistogramBuilder.initializeHistogram(ensembleRatios, 50, "Histogram of ratios");
        plotBuilders[1] = HistogramBuilder.initializeHistogram(true, ensembleBaselines, 50, "Histogram of baseline");
        plotBuilders[2] = HistogramBuilder.initializeHistogram(ensembleDalyFaradayGain, 50, "Histogram of Daly/Faraday Gain");
        plotBuilders[3] = HistogramBuilder.initializeHistogram(true, ensembleSignalnoise, 50, "Histogram of Signal Noise");
        plotBuilders[4] = LinePlotBuilder.initializeLinePlot(xDataIntensityMeans, yDataIntensityMeans, "Mean Intensity");

        // visualization converge ratio and others tabs
        double[] convergeLogRatios = new double[ensembleRecordsList.size()];
        double[] convergeRatios = new double[ensembleRecordsList.size()];
        // todo: hardwired for 2 isotopes
        double[] convergeBaselineFaradayL1 = new double[ensembleRecordsList.size()];
        double[] convergeBaselineFaradayH1 = new double[ensembleRecordsList.size()];
        double[] convergeErrWeightedMisfit = new double[ensembleRecordsList.size()];
        double[] convergeErrRawMisfit = new double[ensembleRecordsList.size()];
        double[] xDataconvergeSavedIterations = new double[ensembleRecordsList.size()];
        double[][] convergeIntensities = new double[ensembleRecordsList.get(0).intensity()[0].length][ensembleRecordsList.size()];
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
                convergeIntensities[intensityIndex][index] = ensembleRecordsList.get(index).intensity()[0][intensityIndex];
            }
            convergeNoiseFaradayL1[index] = ensembleRecordsList.get(index).signalNoise()[0];
            convergeNoiseFaradayH1[index] = ensembleRecordsList.get(index).signalNoise()[1];
            xDataconvergeSavedIterations[index] = index + 1;
        }
        plotBuilders[5] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeRatios, "Converge Ratio");
        plotBuilders[6] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeBaselineFaradayL1, "Converge Baseline Faraday L1");
        plotBuilders[7] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeBaselineFaradayH1, "Converge Baseline Faraday H1");
        plotBuilders[8] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeErrWeightedMisfit, "Converge Weighted Misfit");
        plotBuilders[9] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeErrRawMisfit, "Converge Raw Misfit");
        plotBuilders[10] = MultiLinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeIntensities, "Converge Intensity");
        plotBuilders[11] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeNoiseFaradayL1, "Converge Noise Faraday L1");
        plotBuilders[12] = LinePlotBuilder.initializeLinePlot(xDataconvergeSavedIterations, convergeNoiseFaradayH1, "Converge Noise Faraday H1");


        // visualization data fit
        // only first block for now
        // todo: this is duplicated code from above in part
        double[] data = lastDataModelInit.dataArray();
        double[] dataWithNoBaseline = new double[lastDataModelInit.dataArray().length];
        EnsemblesStore.EnsembleRecord lastModelRecord = ensembleRecordsList.get(ensembleRecordsList.size() - 1);

        for (int blockIndex = 0; blockIndex < massSpecOutputDataRecord.blockCount(); blockIndex++) {
            ArrayList<double[]> intensity = new ArrayList<>(1);
            intensity.add(0, lastDataModelInit.intensityPerBlock().get(blockIndex));
            for (int isotopeIndex = 0; isotopeIndex < massSpecOutputDataRecord.isotopeCount(); isotopeIndex++) {
                for (int row = 0; row < massSpecOutputDataRecord.rawDataColumn().length; row++) {
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn()[row][isotopeIndex] == 1)
                            && (massSpecOutputDataRecord.axialFlagsForRawDataColumn()[row] == 1)
                            && massSpecOutputDataRecord.blockIndicesForRawDataColumn()[row] == (blockIndex + 1)) {
                        double calcValue =
                                exp(lastModelRecord.logRatios()[isotopeIndex])
                                        * intensity.get(0)[(int) massSpecOutputDataRecord.timeIndColumn()[row] - 1];
                        data[row] = calcValue;
                        dataWithNoBaseline[row] = calcValue;
                    }
                    if ((massSpecOutputDataRecord.isotopeFlagsForRawDataColumn()[row][isotopeIndex] == 1)
                            && (massSpecOutputDataRecord.axialFlagsForRawDataColumn()[row] == 0)
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
        plotBuilders[13] = ComboPlotBuilder.initializeLinePlot(xDataIndex, yDataCounts, yDataModelCounts, "Observed Data");

        plotBuilders[14] = ComboPlotBuilder.initializeLinePlotWithOneSigma(xDataIndex, yDataResiduals, yDataSigmas, "Residual Data");


        // todo: missing additional elements of signalNoise (i.e., 0,11,11)
        System.err.println(logRatioMean + "         " + logRatioStdDev);
        System.err.println(baselinesMeans[0] + "         " + baselinesMeans[1] + "    " + baselinesStdDev[0] + "     " + baselinesStdDev[1]);
        System.err.println(dalyFaradayGainMean + "    " + dalyFaradayGainStdDev);
        System.err.println(signalNoiseMeans[0] + "         " + signalNoiseMeans[1] + "    " + signalNoiseStdDev[0] + "     " + signalNoiseStdDev[1]);


        return plotBuilders;
    }

}