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

import com.google.common.collect.BiMap;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.histograms.HistogramBuilder;
import org.cirdles.tripoli.plots.histograms.RatioHistogramBuilder;
import org.cirdles.tripoli.plots.linePlots.ComboPlotBuilder;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;
import org.cirdles.tripoli.plots.linePlots.MultiLinePlotBuilder;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.species.IsotopicRatio;
import org.cirdles.tripoli.species.SpeciesRecordInterface;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.Math.pow;
import static java.lang.StrictMath.exp;

/**
 * @author James F. Bowring
 */
public enum SingleBlockDataModelPlot {
    ;

    public static final int PLOT_INDEX_RATIOS = 0;

    public static PlotBuilder[][] analysisAndPlotting(
            SingleBlockDataSetRecord singleBlockDataSetRecord,
            List<EnsemblesStore.EnsembleRecord> ensembleRecordsList,
            SingleBlockModelRecord singleBlockInitialModelRecordInitial,
            AnalysisMethod analysisMethod) {
        List<IsotopicRatio> isotopicRatioList = analysisMethod.getIsotopicRatiosList();

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
        burn = 1;//Math.min(100, ensembleRecordsList.size() - 50);
        int countOfEnsemblesUsed = ensembleRecordsList.size() - burn;

        // log ratios
        double[][] ensembleSetOfLogRatios = new double[isotopicRatioList.size()][countOfEnsemblesUsed];
        double[][] ensembleRatios = new double[isotopicRatioList.size()][countOfEnsemblesUsed];
        double[] logRatioMean = new double[isotopicRatioList.size()];
        double[] logRatioStdDev = new double[isotopicRatioList.size()];
        DescriptiveStatistics descriptiveStatisticsLogRatios = new DescriptiveStatistics();
        for (int ratioIndex = 0; ratioIndex < isotopicRatioList.size(); ratioIndex++) {
            for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
                ensembleSetOfLogRatios[ratioIndex][index - burn] = ensembleRecordsList.get(index).logRatios()[ratioIndex];
                descriptiveStatisticsLogRatios.addValue(ensembleSetOfLogRatios[ratioIndex][index - burn]);
                ensembleRatios[ratioIndex][index - burn] = exp(ensembleSetOfLogRatios[ratioIndex][index - burn]);
            }
            logRatioMean[ratioIndex] = descriptiveStatisticsLogRatios.getMean();
            logRatioStdDev[ratioIndex] = descriptiveStatisticsLogRatios.getStandardDeviation();

            isotopicRatioList.get(ratioIndex).setRatioValues(ensembleRatios[ratioIndex]);
        }
        // derived ratios
        List<IsotopicRatio> derivedIsotopicRatiosList = analysisMethod.getDerivedIsotopicRatiosList();
        int countOfDerivedRatios = derivedIsotopicRatiosList.size();
        double[][] derivedEnsembleRatios = new double[countOfDerivedRatios][countOfEnsemblesUsed];
        int derivedRatioIndex = 0;
        // derive the ratios
        for (IsotopicRatio isotopicRatio : derivedIsotopicRatiosList) {
            SpeciesRecordInterface numerator = isotopicRatio.getNumerator();
            SpeciesRecordInterface denominator = isotopicRatio.getDenominator();
            SpeciesRecordInterface highestAbundanceSpecies = analysisMethod.retrieveHighestAbundanceSpecies();
            if (numerator != highestAbundanceSpecies) {
                IsotopicRatio numeratorRatio = new IsotopicRatio(numerator, highestAbundanceSpecies, false);
                int indexNumeratorRatio = isotopicRatioList.indexOf(numeratorRatio);
                IsotopicRatio denominatorRatio = new IsotopicRatio(denominator, highestAbundanceSpecies, false);
                int indexDenominatorRatio = isotopicRatioList.indexOf(denominatorRatio);
                for (int ensembleIndex = 0; ensembleIndex < countOfEnsemblesUsed; ensembleIndex++) {
                    derivedEnsembleRatios[derivedRatioIndex][ensembleIndex] =
                            ensembleRatios[indexNumeratorRatio][ensembleIndex] / ensembleRatios[indexDenominatorRatio][ensembleIndex];
                }
            } else {
                // assume we are dealing with the inverses of isotopicRatiosList
                IsotopicRatio targetRatio = new IsotopicRatio(denominator, highestAbundanceSpecies, false);
                int indexOfTargetRatio = isotopicRatioList.indexOf(targetRatio);
                for (int ensembleIndex = 0; ensembleIndex < countOfEnsemblesUsed; ensembleIndex++) {
                    derivedEnsembleRatios[derivedRatioIndex][ensembleIndex] =
                            1.0 / ensembleRatios[indexOfTargetRatio][ensembleIndex];
                }
            }
            derivedIsotopicRatiosList.get(derivedRatioIndex).setRatioValues(derivedEnsembleRatios[derivedRatioIndex]);
            derivedRatioIndex++;
        }


        // baseLines
        int baselineSize = singleBlockInitialModelRecordInitial.faradayCount();
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

//        // signal noise
//        int faradayCount = singleBlockInitialModelRecordInitial.faradayCount();
//        double[][] ensembleSignalnoise = new double[faradayCount][countOfEnsemblesUsed];
//        double[] signalNoiseMeans = new double[faradayCount];
//        double[] signalNoiseStdDev = new double[faradayCount];
//
//        for (int row = 0; row < faradayCount; row++) {
//            DescriptiveStatistics descriptiveStatisticsSignalNoise = new DescriptiveStatistics();
//            for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
//                ensembleSignalnoise[row][index - burn] = ensembleRecordsList.get(index).signalNoise()[row];
//                descriptiveStatisticsSignalNoise.addValue(ensembleSignalnoise[row][index - burn]);
//            }
//            signalNoiseMeans[row] = descriptiveStatisticsSignalNoise.getMean();
//            signalNoiseStdDev[row] = descriptiveStatisticsSignalNoise.getStandardDeviation();
//        }

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
        int knotsCount = singleBlockInitialModelRecordInitial.I0().length;
        double[][] ensembleIntensity = new double[knotsCount][countOfEnsemblesUsed];
        double[] intensityMeans = new double[knotsCount];
        double[] intensityStdDevs = new double[knotsCount];

        for (int knotIndex = 0; knotIndex < knotsCount; knotIndex++) {
            DescriptiveStatistics descriptiveStatisticsIntensity = new DescriptiveStatistics();
            for (int index = burn; index < countOfEnsemblesUsed + burn; index++) {
                ensembleIntensity[knotIndex][index - burn] = ensembleRecordsList.get(index).I0()[knotIndex];
                descriptiveStatisticsIntensity.addValue(ensembleIntensity[knotIndex][index - burn]);
            }
            intensityMeans[knotIndex] = descriptiveStatisticsIntensity.getMean();
            intensityStdDevs[knotIndex] = descriptiveStatisticsIntensity.getStandardDeviation();
        }

        // calculate mean Intensities and knots for plotting
        double[][] yDataIntensityMeans = new double[2][];
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        MatrixStore<Double> intensityMeansMatrix = storeFactory.columns(intensityMeans);
        MatrixStore<Double> yDataMeanIntensitiesMatrix = singleBlockDataSetRecord.blockKnotInterpolationStore().multiply(intensityMeansMatrix).multiply(1.0 / dalyFaradayGainMean);//(1.0 / (dalyFaradayGainMean * 6.24e7)) * 1e6);
        yDataIntensityMeans[0] = yDataMeanIntensitiesMatrix.toRawCopy1D();
        MatrixStore<Double> yDataTrueIntensitiesMatrix = intensityMeansMatrix.multiply(1.0 / dalyFaradayGainMean);//(1.0 / (dalyFaradayGainMean * 6.24e7)) * 1e6);
        yDataIntensityMeans[1] = yDataTrueIntensitiesMatrix.toRawCopy1D();

        double[][] xDataIntensityMeans = new double[2][];
        int xDataSize = yDataIntensityMeans[0].length;
        xDataIntensityMeans[0] = new double[xDataSize];
        for (int i = 0; i < xDataSize; i++) {
            xDataIntensityMeans[0][i] = i;
        }
        int xKnotsSize = singleBlockDataSetRecord.onPeakStartingIndicesOfCycles().length;
        xDataIntensityMeans[1] = new double[xKnotsSize];
        for (int i = 0; i < xKnotsSize; i++) {
            xDataIntensityMeans[1][i] = singleBlockDataSetRecord.onPeakStartingIndicesOfCycles()[i];
        }

        // visualization - Ensembles tab
        PlotBuilder[][] plotBuilders = new PlotBuilder[16][1];

        BiMap<IsotopicRatio, IsotopicRatio> biMapOfRatiosAndInverses = analysisMethod.getBiMapOfRatiosAndInverses();
        plotBuilders[PLOT_INDEX_RATIOS] = new PlotBuilder[ensembleRatios.length + derivedEnsembleRatios.length];
        for (int i = 0; i < ensembleRatios.length; i++) {
            plotBuilders[PLOT_INDEX_RATIOS][i] =
                    RatioHistogramBuilder.initializeRatioHistogram(
                            singleBlockDataSetRecord.blockNumber(),
                            isotopicRatioList.get(i),
                            biMapOfRatiosAndInverses.get(isotopicRatioList.get(i)),
                            25);
            analysisMethod.getMapOfRatioNamesToInvertedFlag().put(isotopicRatioList.get(i).prettyPrint(), false);
        }
        for (int i = 0; i < derivedEnsembleRatios.length; i++) {
            plotBuilders[PLOT_INDEX_RATIOS][i + ensembleRatios.length] =
                    RatioHistogramBuilder.initializeRatioHistogram(
                            singleBlockDataSetRecord.blockNumber(),
                            derivedIsotopicRatiosList.get(i),
                            (null != biMapOfRatiosAndInverses.get(derivedIsotopicRatiosList.get(i))) ?
                                    (biMapOfRatiosAndInverses.get(derivedIsotopicRatiosList.get(i))) :
                                    (biMapOfRatiosAndInverses.inverse().get(derivedIsotopicRatiosList.get(i))),
                            25);
            analysisMethod.getMapOfRatioNamesToInvertedFlag().put(derivedIsotopicRatiosList.get(i).prettyPrint(), false);
        }

        plotBuilders[1] = new PlotBuilder[ensembleBaselines.length];
        List<Detector> faradayDetectorsUsed = analysisMethod.getSequenceTable().findFaradayDetectorsUsed();
        for (int i = 0; i < ensembleBaselines.length; i++) {
            plotBuilders[1][i] = HistogramBuilder.initializeHistogram(singleBlockDataSetRecord.blockNumber(), ensembleBaselines[i],
                    25, new String[]{faradayDetectorsUsed.get(i).getDetectorName() + " Baseline"}, "Baseline Counts", "Frequency", true);
        }

        plotBuilders[2][0] = HistogramBuilder.initializeHistogram(singleBlockDataSetRecord.blockNumber(), ensembleDalyFaradayGain,
                25, new String[]{"Daly/Faraday Gain"}, "Gain", "Frequency", true);

//        plotBuilders[3] = new PlotBuilder[ensembleSignalnoise.length];
//        for (int i = 0; i < ensembleSignalnoise.length; i++) {
//            plotBuilders[3][i] = HistogramBuilder.initializeHistogram(singleBlockDataSetRecord.blockNumber(), ensembleSignalnoise[i],
//                    25, new String[]{faradayDetectorsUsed.get(i).getDetectorName() + " Signal Noise"}, "Noise hyperparameter", "Frequency", true);
//        }

        plotBuilders[4][0] = MultiLinePlotBuilder.initializeLinePlot(
                xDataIntensityMeans, yDataIntensityMeans, new String[]{"Mean Intensity w/ Knots"}, "Time Index", "Intensity (counts)", true);

        // visualization converge ratio and others tabs
        double[][] convergeIntensities = new double[knotsCount][ensembleRecordsList.size()];
        for (int index = 0; index < ensembleRecordsList.size(); index++) {
            for (int knotsIndex = 0; knotsIndex < knotsCount; knotsIndex++) {
                // todo: fix this block indexing issue
                convergeIntensities[knotsIndex][index] = ensembleRecordsList.get(index).I0()[knotsIndex];
            }
        }

        // new converge plots
        double[][] convergeSetOfLogRatios = new double[isotopicRatioList.size()][ensembleRecordsList.size()];
        double[][] convergeSetOfBaselines = new double[baselineSize][ensembleRecordsList.size()];
//        double[][] convergeSetOfFaradayNoise = new double[baselineSize][ensembleRecordsList.size()];
        double[] convergeErrWeightedMisfit = new double[ensembleRecordsList.size()];
        double[] convergeErrRawMisfit = new double[ensembleRecordsList.size()];
        double[] xDataConvergeSavedIterations = new double[ensembleRecordsList.size()];
        for (int ensembleIndex = 0; ensembleIndex < ensembleRecordsList.size(); ensembleIndex++) {
            for (int ratioIndex = 0; ratioIndex < isotopicRatioList.size(); ratioIndex++) {
                convergeSetOfLogRatios[ratioIndex][ensembleIndex] = ensembleRecordsList.get(ensembleIndex).logRatios()[ratioIndex];
            }
            for (int faradayIndex = 0; faradayIndex < baselineSize; faradayIndex++) {
                convergeSetOfBaselines[faradayIndex][ensembleIndex] = ensembleRecordsList.get(ensembleIndex).baseLine()[faradayIndex];
//                convergeSetOfFaradayNoise[faradayIndex][ensembleIndex] = ensembleRecordsList.get(ensembleIndex).signalNoise()[faradayIndex];
            }
            convergeErrWeightedMisfit[ensembleIndex] = StrictMath.sqrt(ensembleRecordsList.get(ensembleIndex).errorWeighted());
            convergeErrRawMisfit[ensembleIndex] = StrictMath.sqrt(ensembleRecordsList.get(ensembleIndex).errorUnWeighted());

            xDataConvergeSavedIterations[ensembleIndex] = ensembleIndex + 1;
        }

        plotBuilders[5] = new PlotBuilder[convergeSetOfLogRatios.length];
        for (int i = 0; i < convergeSetOfLogRatios.length; i++) {
            plotBuilders[5][i] = LinePlotBuilder.initializeLinePlot(
                    xDataConvergeSavedIterations, convergeSetOfLogRatios[i],
                    new String[]{isotopicRatioList.get(i).prettyPrint()}, "Saved iterations", "Log Ratio");
        }

        plotBuilders[6] = new PlotBuilder[convergeSetOfBaselines.length];
        for (int i = 0; i < convergeSetOfBaselines.length; i++) {
            plotBuilders[6][i] = LinePlotBuilder.initializeLinePlot(
                    xDataConvergeSavedIterations, convergeSetOfBaselines[i],
                    new String[]{faradayDetectorsUsed.get(i).getDetectorName() + " Baseline"}, "Saved iterations", "Baseline Counts");
        }

//        plotBuilders[11] = new PlotBuilder[convergeSetOfFaradayNoise.length];
//        for (int i = 0; i < convergeSetOfFaradayNoise.length; i++) {
//            plotBuilders[11][i] = LinePlotBuilder.initializeLinePlot(
//                    xDataConvergeSavedIterations, convergeSetOfFaradayNoise[i],
//                    new String[]{faradayDetectorsUsed.get(i).getDetectorName() + " Noise"}, "Saved iterations", "Noise");
//        }

        plotBuilders[8][0] = LinePlotBuilder.initializeLinePlot(xDataConvergeSavedIterations, convergeErrWeightedMisfit, new String[]{"Converge Weighted Misfit"}, "Saved iterations", "Weighted Misfit");

        plotBuilders[9][0] = LinePlotBuilder.initializeLinePlot(xDataConvergeSavedIterations, convergeErrRawMisfit, new String[]{"Converge Raw Misfit"}, "Saved iterations", "Raw Misfit");


        plotBuilders[10][0] = MultiLinePlotBuilder.initializeLinePlot(
                new double[][]{xDataConvergeSavedIterations}, convergeIntensities, new String[]{"Converge Intensity"}, "", "", false);


        // visualization data fit ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        int baselineCount = singleBlockDataSetRecord.baselineDataSetMCMC().intensityAccumulatorList().size();
        int onPeakFaradayCount = singleBlockDataSetRecord.onPeakFaradayDataSetMCMC().intensityAccumulatorList().size();
        int onPeakPhotoMultCount = singleBlockDataSetRecord.onPeakPhotoMultiplierDataSetMCMC().intensityAccumulatorList().size();
        int totalIntensityCount = baselineCount + onPeakFaradayCount + onPeakPhotoMultCount;

        double[] dataArray = new double[totalIntensityCount];
        double[] dataWithNoBaselineArray = new double[totalIntensityCount];
        Map<Integer, Integer> mapDetectorOrdinalToFaradayIndex = singleBlockInitialModelRecordInitial.mapDetectorOrdinalToFaradayIndex();
        EnsemblesStore.EnsembleRecord lastModelRecord = ensembleRecordsList.get(ensembleRecordsList.size() - 1);
        double[] logRatios = lastModelRecord.logRatios().clone();
        double[] intensities = singleBlockInitialModelRecordInitial.intensities();
//        double[] xSig = lastModelRecord.signalNoise();
        double detectorFaradayGain = singleBlockInitialModelRecordInitial.detectorFaradayGain();
//        double[] baselineMeansArray = singleBlockInitialModelRecordInitial.baselineMeansArray();
        double[] dataCountsModelOneSigma_Dsig = new double[totalIntensityCount];
        double[] integrationTimes = new double[totalIntensityCount];

        List<Integer> isotopeOrdinalIndicesAccumulatorList = singleBlockDataSetRecord.onPeakFaradayDataSetMCMC().isotopeOrdinalIndicesAccumulatorList();
        List<Integer> detectorOrdinalIndicesAccumulatorList = singleBlockDataSetRecord.onPeakFaradayDataSetMCMC().detectorOrdinalIndicesAccumulatorList();
        List<Integer> timeIndexAccumulatorList = singleBlockDataSetRecord.onPeakFaradayDataSetMCMC().timeIndexAccumulatorList();
        List<Double> timeAccumulatorList = singleBlockDataSetRecord.onPeakFaradayDataSetMCMC().timeAccumulatorList();
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
                    + 1.0//xSig[xSig.length - 1]
                    * dataWithNoBaselineArray[dataArrayIndex]);
            dataCountsModelOneSigma_Dsig[dataArrayIndex] = calculatedValue;

            integrationTimes[dataArrayIndex] = timeAccumulatorList.get(intensityIndex);
        }

        isotopeOrdinalIndicesAccumulatorList = singleBlockDataSetRecord.onPeakPhotoMultiplierDataSetMCMC().isotopeOrdinalIndicesAccumulatorList();
        timeIndexAccumulatorList = singleBlockDataSetRecord.onPeakPhotoMultiplierDataSetMCMC().timeIndexAccumulatorList();
        timeAccumulatorList = singleBlockDataSetRecord.onPeakPhotoMultiplierDataSetMCMC().timeAccumulatorList();
        for (int dataArrayIndex = baselineCount + onPeakFaradayCount; dataArrayIndex < baselineCount + onPeakFaradayCount + onPeakPhotoMultCount; dataArrayIndex++) {
            int intensityIndex = timeIndexAccumulatorList.get(dataArrayIndex - baselineCount - onPeakFaradayCount);
            int isotopeIndex = isotopeOrdinalIndicesAccumulatorList.get(dataArrayIndex - baselineCount - onPeakFaradayCount).intValue() - 1;
            int faradayIndex = mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndicesAccumulatorList.get(dataArrayIndex - baselineCount - onPeakFaradayCount));

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
                    + 1.0//xSig[xSig.length - 1]
                    * dataWithNoBaselineArray[dataArrayIndex]);
            dataCountsModelOneSigma_Dsig[dataArrayIndex] = calculatedValue;

            integrationTimes[dataArrayIndex] = timeAccumulatorList.get(intensityIndex);
        }

//        isotopeOrdinalIndicesAccumulatorList = singleBlockDataSetRecord.baselineDataSetMCMC().isotopeOrdinalIndicesAccumulatorList();
        detectorOrdinalIndicesAccumulatorList = singleBlockDataSetRecord.baselineDataSetMCMC().detectorOrdinalIndicesAccumulatorList();
        timeIndexAccumulatorList = singleBlockDataSetRecord.baselineDataSetMCMC().timeIndexAccumulatorList();
        timeAccumulatorList = singleBlockDataSetRecord.baselineDataSetMCMC().timeAccumulatorList();
        for (int dataArrayIndex = 0; dataArrayIndex < baselineCount; dataArrayIndex++) {
            int intensityIndex = timeIndexAccumulatorList.get(dataArrayIndex);
            int faradayIndex = mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndicesAccumulatorList.get(dataArrayIndex));
            dataArray[dataArrayIndex] = baselinesMeans[faradayIndex];
//            dataWithNoBaselineArray[dataArrayIndex] = dataArray[dataArrayIndex] - baselinesMeans[faradayIndex];

            double calculatedValue = StrictMath.sqrt(1.0//pow(xSig[faradayIndex], 2)
                    + 1.0//xSig[xSig.length - 1]
                    * dataWithNoBaselineArray[dataArrayIndex]);
            dataCountsModelOneSigma_Dsig[dataArrayIndex] = calculatedValue;

            integrationTimes[dataArrayIndex] = timeAccumulatorList.get(intensityIndex);
        }


        double[] dataOriginalCounts = singleBlockDataSetRecord.blockIntensityArray().clone();
        double[] yDataResiduals = new double[dataOriginalCounts.length];

        Arrays.sort(integrationTimes);
        for (int i = 0; i < dataOriginalCounts.length; i++) {
            yDataResiduals[i] = dataOriginalCounts[i] - dataArray[i];
        }

        plotBuilders[13][0] = ComboPlotBuilder.initializeLinePlot(
                integrationTimes, dataOriginalCounts, dataArray, new String[]{"Observed Data"}, "Integration Time", "Intensity");
        plotBuilders[15][0] = ComboPlotBuilder.initializeLinePlotWithSubsets(
                integrationTimes, dataOriginalCounts, dataArray, singleBlockDataSetRecord.blockMapIdsToDataTimes(),
                new String[]{"Observed Data by Sequence"}, "Integration Time", "Intensity");
        plotBuilders[14][0] = ComboPlotBuilder.initializeLinePlotWithOneSigma(
                integrationTimes, yDataResiduals, dataCountsModelOneSigma_Dsig, new String[]{"Residual Data"}, "Integration Time", "Intensity");


        // todo: missing additional elements of signalNoiseSigma (i.e., 0,11,11)
        System.err.println(logRatioMean + "         " + logRatioStdDev);
        System.err.println(baselinesMeans[0] + "         " + baselinesMeans[1] + "    " + baselinesStdDev[0] + "     " + baselinesStdDev[1]);
        System.err.println(dalyFaradayGainMean + "    " + dalyFaradayGainStdDev);
//        System.err.println(signalNoiseMeans[0] + "         " + signalNoiseMeans[1] + "    " + signalNoiseStdDev[0] + "     " + signalNoiseStdDev[1]);


        return plotBuilders;
    }


}