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
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.species.IsotopicRatio;
import org.cirdles.tripoli.utilities.mathUtilities.MatLab;
import org.ojalgo.RecoverableCondition;

import java.io.Serializable;
import java.util.*;

import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.ProposedModelParameters.buildProposalRangesRecord;
import static org.cirdles.tripoli.utilities.comparators.SerializableIntegerComparator.SERIALIZABLE_COMPARATOR;

/**
 * @author James F. Bowring
 */
enum SingleBlockModelInitForMCMC {
    ;

    static SingleBlockModelRecordWithCov initializeModelForSingleBlockMCMC(
            AnalysisInterface analysis, AnalysisMethod analysisMethod, SingleBlockRawDataSetRecord singleBlockRawDataSetRecord, boolean provideCovariance) throws RecoverableCondition {

        int baselineCount = singleBlockRawDataSetRecord.baselineDataSetMCMC().intensityAccumulatorList().size();
        int onPeakFaradayCount = singleBlockRawDataSetRecord.onPeakFaradayDataSetMCMC().intensityAccumulatorList().size();
        int onPeakPhotoMultCount = singleBlockRawDataSetRecord.onPeakPhotoMultiplierDataSetMCMC().intensityAccumulatorList().size();
        int totalIntensityCount = baselineCount + onPeakFaradayCount + onPeakPhotoMultCount;
        int countOfIsotopes = analysisMethod.getSpeciesList().size();

        // build data included array
        boolean[][] blockOnPeakIncluded = ((Analysis) analysis).getMapOfBlockIdToIncludedPeakData().get(singleBlockRawDataSetRecord.blockID());
        boolean[] blockAllDataIncluded = new boolean[totalIntensityCount];
        Arrays.fill(blockAllDataIncluded, true);

        for (int isotopeIndex = 0; isotopeIndex < blockOnPeakIncluded.length; isotopeIndex++) {
            for (int intensityIndex = 0; intensityIndex < blockOnPeakIncluded[isotopeIndex].length; intensityIndex++) {
                blockAllDataIncluded[baselineCount + intensityIndex] &= blockOnPeakIncluded[isotopeIndex][intensityIndex];
            }
        }

        // Baseline statistics *****************************************************************************************
        /*
            for m=1:d0.Nfar%+1
                x0.BL(m,1) = mean(d0.data(d0.blflag & d0.det_ind(:,m)));
                x0.BLstd(m,1) = std(d0.data(d0.blflag & d0.det_ind(:,m)));
            end
         */
        SingleBlockRawDataSetRecord.SingleBlockRawDataRecord baselineDataSetMCMC = singleBlockRawDataSetRecord.baselineDataSetMCMC();
        List<Integer> detectorOrdinalIndicesAccumulatorList = baselineDataSetMCMC.detectorOrdinalIndicesAccumulatorList();
        List<Double> intensityAccumulatorList = baselineDataSetMCMC.intensityAccumulatorList();
        Map<Integer, DescriptiveStatistics> mapBaselineDetectorIndicesToStatistics = new TreeMap<>(SERIALIZABLE_COMPARATOR);
        Map<Integer, Integer> mapDetectorOrdinalToFaradayIndex = new TreeMap<>(SERIALIZABLE_COMPARATOR);

        int intensityIndex = 0;
        for (Integer detectorOrdinalIndex : detectorOrdinalIndicesAccumulatorList) {
            if (!mapBaselineDetectorIndicesToStatistics.containsKey(detectorOrdinalIndex)) {
                mapBaselineDetectorIndicesToStatistics.put(detectorOrdinalIndex, new DescriptiveStatistics());
            }
            mapBaselineDetectorIndicesToStatistics.get(detectorOrdinalIndex).addValue(intensityAccumulatorList.get(intensityIndex));
            intensityIndex++;
        }

        double[] baselineMeansArray = new double[mapBaselineDetectorIndicesToStatistics.keySet().size()];
        double[] baselineStandardDeviationsArray = new double[baselineMeansArray.length];
        int faradayIndex = 0;
        DescriptiveStatistics meanOfBaseLineMeansStdDevDescriptiveStatistics = new DescriptiveStatistics();
        for (Integer detectorOrdinalIndex : mapBaselineDetectorIndicesToStatistics.keySet()) {
            baselineMeansArray[faradayIndex] = mapBaselineDetectorIndicesToStatistics.get(detectorOrdinalIndex).getMean();
            baselineStandardDeviationsArray[faradayIndex] = mapBaselineDetectorIndicesToStatistics.get(detectorOrdinalIndex).getStandardDeviation();
            meanOfBaseLineMeansStdDevDescriptiveStatistics.addValue(baselineStandardDeviationsArray[faradayIndex]);
            mapDetectorOrdinalToFaradayIndex.put(detectorOrdinalIndex, faradayIndex);
            faradayIndex++;
        }

        double meanOfBaseLineMeansStdDev = meanOfBaseLineMeansStdDevDescriptiveStatistics.getMean();

        // TODO:Fix this per Noah
        // NOTE: the speciesList has been sorted by increasing abundances in the original analysisMethod setup
        //  the ratios are between each species and the most abundant species, with one less ratio than species
        int indexOfMostAbundantIsotope = countOfIsotopes - 1;//          mapPhotoMultiplierIsotopeIndicesToStatistics.size() - 1;

        // june 2023 new init line 14 ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        // june 2023 new init - will need to be user input
        int iden = indexOfMostAbundantIsotope + 1; // ordinal

        //TODO: Fix this when using bbase
        //TODO: handle case of only 2 cycles, which blows up calculateDFGain since it thrwos out first and last
        double detectorFaradayGain = 0.9;
        try {
            detectorFaradayGain = calculateDFGain(iden, baselineMeansArray, mapDetectorOrdinalToFaradayIndex, singleBlockRawDataSetRecord);
        } catch (Exception e) {
            System.out.println("BAD GAIN:  BLOCK# " + singleBlockRawDataSetRecord.blockID());
        }

        /*
        %% Initialize Intensity Function
        for m=1:d0.Nblock
            II = d0.InterpMat{m};

            dind = (d0.iso_ind(:,iden) &   d0.block(:,m));
            dd=d0.data(dind);
            tmpdetvec = d0.det_vec(dind);

            dd(~d0.axflag(dind)) = (dd(~d0.axflag(dind)) - x0.BL(tmpdetvec(~d0.axflag(dind))))*x0.DFgain ;


            tmptime = d0.time_ind(dind);
            [~,dsort]=sort(tmptime);

            dd=dd(dsort);

            %I0=(II'*II)^-1*II'*dd;
            I0=II\dd;  % Solve least squares problem for initial intensity values

            %x0.I{m} =  tmpDenIso*ones(Nknots(m),1);
            %x0.I{m} = linspace(maxtmpCounts(m,iden),mintmpCounts(m,iden),d0.Nknots(m))';
            x0.I{m} = I0;
        end
         */
        double[] d0_data = singleBlockRawDataSetRecord.blockRawDataArray();
        int[] d0_detVec = singleBlockRawDataSetRecord.blockDetectorOrdinalIndicesArray();
        int startIndexOfPhotoMultiplierData = baselineCount + onPeakFaradayCount;
        List<Double> ddver2List = new ArrayList<>();
        int[] blockCycles = singleBlockRawDataSetRecord.blockCycleArray();
        List<Integer> tempTime = new ArrayList<>();
        List<Integer> cyclesList = new ArrayList<>();

        int[] isotopeOrdinalIndices = singleBlockRawDataSetRecord.blockIsotopeOrdinalIndicesArray();
        int[] timeIndForSortingArray = singleBlockRawDataSetRecord.blockTimeIndicesArray();
        for (int dataArrayIndex = 0; dataArrayIndex < d0_data.length; dataArrayIndex++) {

            if (isotopeOrdinalIndices[dataArrayIndex] == iden) {
                if (dataArrayIndex < startIndexOfPhotoMultiplierData) {
                    double calculated = (d0_data[dataArrayIndex] - baselineMeansArray[mapDetectorOrdinalToFaradayIndex.get(d0_detVec[dataArrayIndex])]) * detectorFaradayGain;
                    ddver2List.add(calculated);
                } else {
                    ddver2List.add(d0_data[dataArrayIndex]);
                }
                tempTime.add(timeIndForSortingArray[dataArrayIndex]);
                cyclesList.add(blockCycles[dataArrayIndex]);
            }

        }

        double[] ddVer2Array = ddver2List.stream().mapToDouble(d -> d).toArray();
        int[] cyclesArray = cyclesList.stream().mapToInt(d -> d).toArray();

        int[] tempTimeIndicesArray = tempTime.stream().mapToInt(d -> d).toArray();
        ArrayIndexComparator comparatorTime = new ArrayIndexComparator(tempTimeIndicesArray);
        Integer[] ddVer2sortIndices = comparatorTime.createIndexArray();
        Arrays.sort(ddVer2sortIndices, comparatorTime);

        double[] ddVer2SortedArray = new double[ddVer2Array.length];
        int[] cyclesSortedArray = new int[cyclesArray.length];
        for (int i = 0; i < ddVer2Array.length; i++) {
            ddVer2SortedArray[i] = ddVer2Array[ddVer2sortIndices[i]];
            cyclesSortedArray[i] = cyclesArray[ddVer2sortIndices[i]];
        }

        double[][] interpolatedKnotData_II = singleBlockRawDataSetRecord.blockKnotInterpolationArray();
        RealMatrix II = new BlockRealMatrix(interpolatedKnotData_II);
        DecompositionSolver solver = new QRDecomposition(II).getSolver();
        RealVector data = new ArrayRealVector(ddVer2SortedArray);
        RealVector solution = solver.solve(data);
        double[] intensity_I = solution.toArray();

        Matrix IIm = new Matrix(II.getData());
        Matrix intensityFn = IIm.times(new Matrix(intensity_I, intensity_I.length));

        ProposedModelParameters.ProposalRangesRecord proposalRangesRecord =
                buildProposalRangesRecord(intensityFn.getColumnPackedCopy());

        /*
        %% Initialize Log Isotope Ratios
        for m=1:d0.Nblock
            II = d0.InterpMat{m};
            IntensityFn = II*x0.I{m};

            for ii = 1:d0.Niso-1
                dind = (d0.iso_ind(:,ii) &   d0.block(:,m));
                dd=d0.data(dind);
                tmpdetvec = d0.det_vec(dind);

                dd(~d0.axflag(dind)) = (dd(~d0.axflag(dind)) - x0.BL(tmpdetvec(~d0.axflag(dind))))*x0.DFgain ;

                tmptime = d0.time_ind(dind);
                [~,dsort]=sort(tmptime);

                dd=dd(dsort);

                IsoBlockMean(ii,m) = log(mean(dd./IntensityFn));

            end
        end

        for ii=1:d0.Niso-1
            x0.lograt(ii,1) = mean(IsoBlockMean(ii,:));
        end
         */
        int[] isotopeOrdinalIndicesAccumulatorArray = singleBlockRawDataSetRecord.blockIsotopeOrdinalIndicesArray();
        int cycleCount = singleBlockRawDataSetRecord.onPeakStartingIndicesOfCycles().length;
        double[] logRatios = new double[analysisMethod.getIsotopicRatiosList().size()];
        Map<IsotopicRatio, Map<Integer, double[]>> mapLogRatiosToCycleStats = new TreeMap<>();

        Map<Integer, double[]> denominatorMapCyclesToStats = new TreeMap<>();
        for (int isotopeIndex = 0; isotopeIndex < countOfIsotopes; isotopeIndex++) {
            ddver2List = new ArrayList<>();
            cyclesList = new ArrayList<>();
            tempTime = new ArrayList<>();
            for (int dataArrayIndex = 0; dataArrayIndex < d0_data.length; dataArrayIndex++) {
                if (isotopeOrdinalIndicesAccumulatorArray[dataArrayIndex] == isotopeIndex + 1) {
                    if (dataArrayIndex < startIndexOfPhotoMultiplierData) {
                        double calculated = (d0_data[dataArrayIndex] - baselineMeansArray[mapDetectorOrdinalToFaradayIndex.get(d0_detVec[dataArrayIndex])]) * detectorFaradayGain;
                        ddver2List.add(calculated);
                    } else {
                        ddver2List.add(d0_data[dataArrayIndex]);
                    }
                    tempTime.add(timeIndForSortingArray[dataArrayIndex]);
                    cyclesList.add(blockCycles[dataArrayIndex]);
                }
            }

            ddVer2Array = ddver2List.stream().mapToDouble(d -> d).toArray();
            cyclesArray = cyclesList.stream().mapToInt(d -> d).toArray();
            tempTimeIndicesArray = tempTime.stream().mapToInt(d -> d).toArray();
            comparatorTime = new ArrayIndexComparator(tempTimeIndicesArray);
            ddVer2sortIndices = comparatorTime.createIndexArray();
            Arrays.sort(ddVer2sortIndices, comparatorTime);

            ddVer2SortedArray = new double[ddVer2Array.length];
            cyclesSortedArray = new int[ddVer2Array.length];
            for (int i = 0; i < ddVer2Array.length; i++) {
                ddVer2SortedArray[i] = ddVer2Array[ddVer2sortIndices[i]];
                cyclesSortedArray[i] = cyclesArray[ddVer2sortIndices[i]];
            }

            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
            for (int dataArrayIndex = 0; dataArrayIndex < ddVer2SortedArray.length; dataArrayIndex++) {
                //TODO: Check for cycle active - see below where stats accumulator checks
                descriptiveStatistics.addValue(ddVer2SortedArray[dataArrayIndex] / intensityFn.get(dataArrayIndex, 0));
            }
            // only use most abundant isotope (denominator or iden) for cycle-based calculations
            if (isotopeIndex < logRatios.length) {
                logRatios[isotopeIndex] = StrictMath.log(Math.max(descriptiveStatistics.getMean(), Math.exp(proposalRangesRecord.priorLogRatio()[0][0])));
            }

            // start cycle-based math
            DescriptiveStatistics[] cycleStats = new DescriptiveStatistics[cycleCount];
            for (int dataArrayIndex = 0; dataArrayIndex < ddVer2SortedArray.length; dataArrayIndex++) {
                int cycle = cyclesSortedArray[dataArrayIndex] - 1;
                if (cycleStats[cycle] == null) {
                    cycleStats[cycle] = new DescriptiveStatistics();
                }
                // TODO: make this a check for both isotopes (eventually may include denominator as one that is excluded)
                if (singleBlockRawDataSetRecord.mapOfSpeciesToActiveCycles().get(analysisMethod.getSpeciesList().get(isotopeIndex))[cycle]) {
                    cycleStats[cycle].addValue(ddVer2SortedArray[dataArrayIndex] / intensityFn.get(dataArrayIndex, 0));
                }
            }

            Map<Integer, double[]> mapCyclesToStats = new TreeMap<>();

            for (int cycleIndex = 0; cycleIndex < cycleStats.length; cycleIndex++) {
                // TODO: fix this - currently using ratios instead of logs for cycles - see ViewCycles in matlab
                double[] cycleLogRatioStats = new double[2];
                if (cycleStats[cycleIndex].getMean() >= Math.exp(proposalRangesRecord.priorLogRatio()[0][0])) {
                    cycleLogRatioStats[0] = (cycleStats[cycleIndex].getMean());
                    // TODO: does this mean active cycles??
                    cycleLogRatioStats[1] = cycleStats[cycleIndex].getStandardDeviation() / Math.sqrt(cycleStats[cycleIndex].getN());
                } else {
                    cycleLogRatioStats[0] = Math.exp(proposalRangesRecord.priorLogRatio()[0][0]);
                    cycleLogRatioStats[1] = 0.0;
                }
                mapCyclesToStats.put(cycleIndex, cycleLogRatioStats);
            }
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


        /*
        %% Initialize Dsig Noise Variance values
        Dsig = zeros(size(d0.data));

        %ReportInterval = 0.1;
        ReportInterval = d0.ReportInterval; %sb629 Previously hardcoded, should be read from file

        LogRatios = [x0.lograt; 0];  %sb629 Changed to exclude denominator isotope as parameter

        II = d0.InterpMat;
        for n = 1:d0.Nblock
            Intensity{n} = II{n}*x0.I{n};
            for m=1:d0.Niso;
                itmp = d0.iso_ind(:,m) & d0.axflag & d0.block(:,n);
                tempDsig(itmp,1) = exp(LogRatios(m))*Intensity{n}(d0.time_ind(itmp));
                Dsig(itmp,1) = tempDsig(itmp)/ReportInterval;

                itmp = d0.iso_ind(:,m) & ~d0.axflag & d0.block(:,n);
                tempDsig(itmp) = exp(LogRatios(m))*x0.DFgain^-1 *Intensity{n}(d0.time_ind(itmp));
            end
            for m=1:d0.Nfar%+1
                itmp = d0.det_ind(:,m) & ~d0.axflag & d0.block(:,n);
                Dsig(itmp,1) = tempDsig(itmp)/ReportInterval + x0.BLstd(m).^2;
            end

        end
        for m=1:d0.Nfar%+1
            Dsig(d0.blflag & d0.det_ind(:,m),1) = x0.BLstd(m).^2;
        end

        x0.Dsig = Dsig;

         */

        double[] dataWithNoBaselineArray = new double[totalIntensityCount];
        double[] dataSignalNoiseArray_Dsig = new double[totalIntensityCount];
        double[] ddd = new double[totalIntensityCount];
        double reportInterval = 1.0; //%sb629 Previously hardcoded, should be read from file
        int[] detectorOrdinalIndicesAccumulatorArray = singleBlockRawDataSetRecord.blockDetectorOrdinalIndicesArray();

        for (int dataArrayIndex = 0; dataArrayIndex < d0_data.length; dataArrayIndex++) {
            intensityIndex = timeIndForSortingArray[dataArrayIndex];
            int isotopeIndex = isotopeOrdinalIndicesAccumulatorArray[dataArrayIndex] - 1;

            if (0 <= isotopeIndex) {
                if (dataArrayIndex >= startIndexOfPhotoMultiplierData) {
                    if (isotopeIndex < logRatios.length) {
                        ddd[dataArrayIndex] = StrictMath.exp(logRatios[isotopeIndex]) * intensityFn.get(intensityIndex, 0);
                    } else {
                        ddd[dataArrayIndex] = intensityFn.get(intensityIndex, 0);
                    }
                    dataSignalNoiseArray_Dsig[dataArrayIndex] = ddd[dataArrayIndex] / reportInterval;
                } else {
                    if (isotopeIndex < logRatios.length) {
                        ddd[dataArrayIndex] = StrictMath.exp(logRatios[isotopeIndex]) * (1.0 / detectorFaradayGain) * intensityFn.get(intensityIndex, 0);
                    } else {
                        ddd[dataArrayIndex] = 1.0 * (1.0 / detectorFaradayGain) * intensityFn.get(intensityIndex, 0);
                    }
                    faradayIndex = mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndicesAccumulatorArray[dataArrayIndex]);
                    dataSignalNoiseArray_Dsig[dataArrayIndex] = ddd[dataArrayIndex] / reportInterval + Math.pow(baselineStandardDeviationsArray[faradayIndex], 2.0);
                }
            } else {
                // baselines
                faradayIndex = mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndicesAccumulatorArray[dataArrayIndex]);
                dataSignalNoiseArray_Dsig[dataArrayIndex] = Math.pow(baselineStandardDeviationsArray[faradayIndex], 2.0);
            }
        }

        SingleBlockModelRecord originalX0 = new SingleBlockModelRecord(
                singleBlockRawDataSetRecord.blockID(),
                mapDetectorOrdinalToFaradayIndex.size(),
                cycleCount,
                countOfIsotopes,
                analysisMethod.retrieveHighestAbundanceSpecies(),
                baselineMeansArray,//calculated
                baselineStandardDeviationsArray,//calculated
                detectorFaradayGain,//calculated
                mapDetectorOrdinalToFaradayIndex,
                logRatios,//calculated
                singleBlockRawDataSetRecord.mapOfSpeciesToActiveCycles(),
                mapLogRatiosToCycleStats,
                singleBlockRawDataSetRecord.blockRawDataArray(), // ie dataModelArray
                dataSignalNoiseArray_Dsig,//calculated
                intensity_I,//calculated
                intensityFn.getColumnPackedCopy()
        );

        double[] dataModel = modelInitData(originalX0, singleBlockRawDataSetRecord);
        // note: this datamodel replicates matlab datamodel when using linear knots on old 2 isotope data files

        SingleBlockModelRecord calculatedX0 = new SingleBlockModelRecord(
                singleBlockRawDataSetRecord.blockID(),//original
                mapDetectorOrdinalToFaradayIndex.size(),
                cycleCount,
                countOfIsotopes,
                analysisMethod.retrieveHighestAbundanceSpecies(),
                baselineMeansArray,//calculated
                baselineStandardDeviationsArray,//calculated
                detectorFaradayGain,//calculated
                mapDetectorOrdinalToFaradayIndex,//calculated
                logRatios,//calculated
                singleBlockRawDataSetRecord.mapOfSpeciesToActiveCycles(),
                mapLogRatiosToCycleStats,
                dataModel,
                dataSignalNoiseArray_Dsig,//calculated
                intensity_I,//calculated
                intensityFn.getColumnPackedCopy()//calculated
        );

        Matrix covarianceMatrix_C0 = null;
        if (provideCovariance) {
            // Covariance Matrix
        /*
            %%  Initialize Diagonal Model Covariance Matrix

            for m = 1:d0.Niso-1
                testLR = linspace(-.5,.5,1001);
                %sb629
                delta_testLR = testLR(2)-testLR(1);
                minvarLR = (delta_testLR/2)^2;
                Etmp = zeros(size(testLR));
                for ii = 1:length(testLR)
                    testx0 = x0;
                    testx0.lograt(m) = x0.lograt(m) + testLR(ii);
                    d = ModelMSData(testx0,d0);
                    Etmp(ii) = sum((d0.data-d).^2./Dsig);
                end
                EE=(Etmp-min(Etmp));
                p = exp(-EE/2)/sum(exp(-EE/2));
                x0.logratVar(m) = max(sum(p.*(testLR-0).^2),minvarLR);%sb629

            end
         */

            double[] logRatioVar = new double[logRatios.length];
            for (int logRatioIndex = 0; logRatioIndex < logRatios.length; logRatioIndex++) {
                double[] testLR = MatLab.linspace(-0.5, 0.5, 1001).toRawCopy1D();
                double delta_testLR = testLR[1] - testLR[0];
                double minvarLR = Math.pow(delta_testLR / 2.0, 2.0);
                double[] eTmp = new double[testLR.length];
                double minETmp = Double.MAX_VALUE;
                for (int ii = 0; ii < testLR.length; ii++) {
                    double[] testLogRatios = logRatios.clone();
                    testLogRatios[logRatioIndex] = logRatios[logRatioIndex] + testLR[ii];

                    SingleBlockModelRecord testX0 = new SingleBlockModelRecord(
                            singleBlockRawDataSetRecord.blockID(),
                            mapDetectorOrdinalToFaradayIndex.size(),
                            cycleCount,
                            countOfIsotopes,
                            analysisMethod.retrieveHighestAbundanceSpecies(),
                            baselineMeansArray,
                            baselineStandardDeviationsArray,
                            detectorFaradayGain,
                            mapDetectorOrdinalToFaradayIndex,
                            testLogRatios,
                            singleBlockRawDataSetRecord.mapOfSpeciesToActiveCycles(),
                            mapLogRatiosToCycleStats,
                            singleBlockRawDataSetRecord.blockRawDataArray(),
                            dataSignalNoiseArray_Dsig,
                            intensity_I,
                            intensityFn.getColumnPackedCopy()
                    );
                    try {
                        dataModel = modelInitData(testX0, singleBlockRawDataSetRecord);
                        eTmp[ii] = calcError(singleBlockRawDataSetRecord.blockRawDataArray(), dataModel, dataSignalNoiseArray_Dsig);
                        minETmp = Math.min(eTmp[ii], minETmp);
                    } catch (Exception e) {
                        System.err.println("Dsig error during init line 302");
                    }
                }
                logRatioVar[logRatioIndex] = Math.max(calcVariance(eTmp, minETmp, testLR), minvarLR);
            }

        /*
        for n = 1:d0.Nblock
            for m = 1:length(x0.I{n})
                testI = linspace(-mean(x0.BLstd),mean(x0.BLstd),101);
                %sb629
                delta_testI = testI(2)-testI(1);
                minvarI = (delta_testI/2)^2;
                Etmp = zeros(size(testI));
                for ii = 1:length(testI)
                    testx0 = x0;
                    testx0.I{n}(m) = x0.I{n}(m) + testI(ii);
                    d = ModelMSData(testx0,d0);
                    Etmp(ii) = sum((d0.data-d).^2./Dsig);
                end
                EE=(Etmp-min(Etmp));
                p = exp(-EE/2)/sum(exp(-EE/2));
                x0.IVar{n}(m) = max(sum(p.*(testI-0).^2),minvarI);%sb629

            end
        end

         */
            double[] intensityVar = new double[intensity_I.length];
            for (intensityIndex = 0; intensityIndex < intensity_I.length; intensityIndex++) {
                double[] testI = MatLab.linspace(-meanOfBaseLineMeansStdDev, meanOfBaseLineMeansStdDev, 101).toRawCopy1D();
                double delta_testI = testI[1] - testI[0];
                double minvarI = Math.pow(delta_testI / 2.0, 2.0);
                double[] eTmp = new double[testI.length];
                double minETmp = Double.MAX_VALUE;
                for (int ii = 0; ii < testI.length; ii++) {
                    double[] testIntensity = intensity_I.clone();
                    testIntensity[intensityIndex] = intensity_I[intensityIndex] + testI[ii];

                    SingleBlockModelRecord testX0 = new SingleBlockModelRecord(
                            singleBlockRawDataSetRecord.blockID(),
                            mapDetectorOrdinalToFaradayIndex.size(), cycleCount, countOfIsotopes, analysisMethod.retrieveHighestAbundanceSpecies(), baselineMeansArray,
                            baselineStandardDeviationsArray,
                            detectorFaradayGain,
                            mapDetectorOrdinalToFaradayIndex,
                            logRatios,
                            singleBlockRawDataSetRecord.mapOfSpeciesToActiveCycles(),
                            mapLogRatiosToCycleStats,
                            singleBlockRawDataSetRecord.blockRawDataArray(),
                            dataSignalNoiseArray_Dsig,
                            testIntensity,
                            intensityFn.getColumnPackedCopy()
                    );
                    dataModel = modelInitData(testX0, singleBlockRawDataSetRecord);
                    eTmp[ii] = calcError(singleBlockRawDataSetRecord.blockRawDataArray(), dataModel, dataSignalNoiseArray_Dsig);
                    minETmp = Math.min(eTmp[ii], minETmp);
                }
                intensityVar[intensityIndex] = Math.max(calcVariance(eTmp, minETmp, testI), minvarI);
            }

        /*
            testDF = linspace(-.1,.1,1001);
            %sb629
            delta_testDF = testDF(2)-testDF(1);
            minvarDF = (delta_testDF/2)^2;
            Etmp = zeros(size(testDF));
            for ii = 1:length(testDF)
                testx0 = x0;
                testx0.DFgain = x0.DFgain + testDF(ii);
                d = ModelMSData(testx0,d0);
                Etmp(ii) = sum((d0.data-d).^2./Dsig);
            end
            EE=(Etmp-min(Etmp));
            p = exp(-EE/2)/sum(exp(-EE/2));
            x0.DFgainVar = max(sum(p.*(testDF-0).^2),minvarDF);%sb629
        */
            double[] testDF = MatLab.linspace(-.1, .1, 1001).toRawCopy1D();
            double delta_testDF = testDF[1] - testDF[0];
            double minvarDF = Math.pow(delta_testDF / 2.0, 2.0);
            double[] eTmp = new double[testDF.length];
            double minETmp = Double.MAX_VALUE;
            for (int ii = 0; ii < testDF.length; ii++) {
                double testDFGain = detectorFaradayGain + testDF[ii];

                SingleBlockModelRecord testX0 = new SingleBlockModelRecord(
                        singleBlockRawDataSetRecord.blockID(),
                        mapDetectorOrdinalToFaradayIndex.size(), cycleCount, countOfIsotopes, analysisMethod.retrieveHighestAbundanceSpecies(), baselineMeansArray,
                        baselineStandardDeviationsArray,
                        testDFGain,
                        mapDetectorOrdinalToFaradayIndex,
                        logRatios,
                        singleBlockRawDataSetRecord.mapOfSpeciesToActiveCycles(),
                        mapLogRatiosToCycleStats,
                        singleBlockRawDataSetRecord.blockRawDataArray(),
                        dataSignalNoiseArray_Dsig,
                        intensity_I,
                        intensityFn.getColumnPackedCopy()
                );

                dataModel = modelInitData(testX0, singleBlockRawDataSetRecord);
                eTmp[ii] = calcError(singleBlockRawDataSetRecord.blockRawDataArray(), dataModel, dataSignalNoiseArray_Dsig);
                minETmp = Math.min(eTmp[ii], minETmp);
            }
            double dfGainVar = Math.max(calcVariance(eTmp, minETmp, testDF), minvarDF);

        /*
            for m = 1:d0.Nfar
                testBL = linspace(-x0.BLstd(m),x0.BLstd(m),1001);
                %sb629
                delta_testBL = testBL(2)-testBL(1);
                minvarBL = (delta_testBL/2)^2;
                Etmp = zeros(size(testBL));
                for ii = 1:length(testBL)
                    testx0 = x0;
                    testx0.BL(m) = x0.BL(m) + testBL(ii);
                    d = ModelMSData(testx0,d0);
                    Etmp(ii) = sum((d0.data-d).^2./Dsig);
                end
                EE=(Etmp-min(Etmp));
                p = exp(-EE/2)/sum(exp(-EE/2));
                x0.BLVar(m) = max(sum(p.*(testBL-0).^2),minvarBL); %sb629

            end
         */
            double[] baseLineVar = new double[baselineMeansArray.length];
            for (int baseLineIndex = 0; baseLineIndex < baselineMeansArray.length; baseLineIndex++) {
                double[] testBL = MatLab.linspace(-baselineStandardDeviationsArray[baseLineIndex], baselineStandardDeviationsArray[baseLineIndex], 1001).toRawCopy1D();
                double delta_testBL = testBL[1] - testBL[0];
                double minvarBL = Math.pow(delta_testBL / 2.0, 2.0);
                eTmp = new double[testBL.length];
                minETmp = Double.MAX_VALUE;
                for (int ii = 0; ii < testBL.length; ii++) {
                    double[] testBaseLineMeans = baselineMeansArray.clone();
                    testBaseLineMeans[baseLineIndex] = baselineMeansArray[baseLineIndex] + testBL[ii];

                    SingleBlockModelRecord testX0 = new SingleBlockModelRecord(
                            singleBlockRawDataSetRecord.blockID(),
                            mapDetectorOrdinalToFaradayIndex.size(),
                            cycleCount,
                            countOfIsotopes,
                            analysisMethod.retrieveHighestAbundanceSpecies(),
                            testBaseLineMeans,
                            baselineStandardDeviationsArray,
                            detectorFaradayGain,
                            mapDetectorOrdinalToFaradayIndex,
                            logRatios,
                            singleBlockRawDataSetRecord.mapOfSpeciesToActiveCycles(),
                            mapLogRatiosToCycleStats,
                            singleBlockRawDataSetRecord.blockRawDataArray(),
                            dataSignalNoiseArray_Dsig,
                            intensity_I,
                            intensityFn.getColumnPackedCopy()
                    );
                    dataModel = modelInitData(testX0, singleBlockRawDataSetRecord);
                    eTmp[ii] = calcError(singleBlockRawDataSetRecord.blockRawDataArray(), dataModel, dataSignalNoiseArray_Dsig);
                    minETmp = Math.min(eTmp[ii], minETmp);
                }
                baseLineVar[baseLineIndex] = Math.max(calcVariance(eTmp, minETmp, testBL), minvarBL);
            }


            int countOfParameters = logRatioVar.length + intensityVar.length + baseLineVar.length + 1;
            double covarianceFactor = Math.pow(0.1, 2) * (1.0 / countOfParameters);
            double[][] diagC0 = new double[countOfParameters][countOfParameters];
            int diagIndex = 0;
            for (int i = 0; i < logRatioVar.length; i++) {
                diagC0[diagIndex][diagIndex] = StrictMath.sqrt(logRatioVar[i]) * covarianceFactor;
                diagIndex++;
            }
            for (int i = 0; i < intensityVar.length; i++) {
                diagC0[diagIndex][diagIndex] = StrictMath.sqrt(intensityVar[i]) * covarianceFactor;
                diagIndex++;
            }
            for (int i = 0; i < baseLineVar.length; i++) {
                diagC0[diagIndex][diagIndex] = StrictMath.sqrt(baseLineVar[i]) * covarianceFactor;
                diagIndex++;
            }
            diagC0[diagIndex][diagIndex] = StrictMath.sqrt(dfGainVar) * covarianceFactor;

            covarianceMatrix_C0 = new Matrix(diagC0);
        }

        return new SingleBlockModelRecordWithCov(calculatedX0, proposalRangesRecord, covarianceMatrix_C0);
    }

    private static synchronized double calculateDFGain(int iden, double[] baselineMeansArray, Map<Integer, Integer> mapDetectorOrdinalToFaradayIndex, SingleBlockRawDataSetRecord singleBlockRawDataSetRecord) {
        // new DFGain calculator
        /*
        %x0.DFgain = user_DFgain;  %sb629 Now going to set according to data(?)

            %% Initialize DF Gain based on data  %sb629
            % Solve for initial intensity function based on denominator isotope
            % for Daly by itself and Faradays by themselves. Initial DFgain is then
            % determined by ratio between intensity knot values.
            for m=1:d0.Nblock
                II = d0.InterpMat{m};

                % Choose denominator iso for this block
                dind = (d0.iso_ind(:,iden) &   d0.block(:,m));
                dd=d0.data(dind);
                tmpdetvec = d0.det_vec(dind);

                % Construct data vector for denominator isotope WITHOUT DFgain
                % Subtract appropriate baselines
                dd(~d0.axflag(dind)) = (dd(~d0.axflag(dind)) - x0.BL(tmpdetvec(~d0.axflag(dind)))) ;

                tmpdalyflag = d0.axflag(dind);
                tmptime = d0.time_ind(dind);

                % Sort data vector and Daly flag (axflag) by time sample
                [~,dsort]=sort(tmptime);
                dd=dd(dsort);
                dalyflag=tmpdalyflag(dsort);

                % Solve for intensity knots using Daly and Faradays separately
                I0daly = II(dalyflag,:)\dd(dalyflag);
                I0far = II(~dalyflag,:)\dd(~dalyflag);

                % Find ratio. Throw out first and last knots since time periods may not
                % overlap for Daly and Faradays
                BlockDFgain(m) = mean(I0daly(2:end-1)./I0far(2:end-1));
            end

            % Take average of DFgain for all blocks
            x0.DFgain = mean(BlockDFgain);

            % Warning if user DF gain is different from calculation
            if 100*abs(x0.DFgain - user_DFgain)/user_DFgain > 5
                disp(sprintf('Warning, discrepancy with user-specified DF Gain %.4f %.4f',user_DFgain,x0.DFgain))
            end

         */

        double[] d0_data = singleBlockRawDataSetRecord.blockRawDataArray();
        int startIndexOfPhotoMultiplierData = singleBlockRawDataSetRecord.getCountOfBaselineIntensities() + singleBlockRawDataSetRecord.getCountOfOnPeakFaradayIntensities();
        int[] d0_detVec = singleBlockRawDataSetRecord.blockDetectorOrdinalIndicesArray();
        List<Double> ddNoPMList = new ArrayList<>();
        List<Integer> tmpPMflagList = new ArrayList<>();
        int[] isotopeOrdinalIndices = singleBlockRawDataSetRecord.blockIsotopeOrdinalIndicesArray();
        int[] timeIndForSortingArray = singleBlockRawDataSetRecord.blockTimeIndicesArray();

        List<Integer> tempTime = new ArrayList<>();
        for (int dataArrayIndex = 0; dataArrayIndex < d0_data.length; dataArrayIndex++) {
            if (isotopeOrdinalIndices[dataArrayIndex] == iden) {
                if (dataArrayIndex < startIndexOfPhotoMultiplierData) {
                    double calculated = (d0_data[dataArrayIndex] - baselineMeansArray[mapDetectorOrdinalToFaradayIndex.get(d0_detVec[dataArrayIndex])]);
                    ddNoPMList.add(calculated);
                    tmpPMflagList.add(0);
                } else {
                    ddNoPMList.add(d0_data[dataArrayIndex]);
                    tmpPMflagList.add(1);
                }
                tempTime.add(timeIndForSortingArray[dataArrayIndex]);
            }
        }
        double[] ddNoPMArray = ddNoPMList.stream().mapToDouble(d -> d).toArray();
        int[] tmpPMflagArray = tmpPMflagList.stream().mapToInt(d -> d).toArray();
        int[] tempTimeIndicesArray = tempTime.stream().mapToInt(d -> d).toArray();

        ArrayIndexComparator comparatorTime = new ArrayIndexComparator(tempTimeIndicesArray);
        Integer[] ddSortIndices = comparatorTime.createIndexArray();
        Arrays.sort(ddSortIndices, comparatorTime);

        double[] ddSortedArray = new double[ddNoPMArray.length];
        double[][] interpolatedKnotData_II = singleBlockRawDataSetRecord.blockKnotInterpolationArray();
        List<double[]> IIFar = new ArrayList<>();
        List<double[]> IIPM = new ArrayList<>();
        List<Double> dataFar = new ArrayList<>();
        List<Double> dataPM = new ArrayList<>();
        int[] tmpPMflagSortedArray = new int[tmpPMflagArray.length];
        for (int i = 0; i < ddNoPMArray.length; i++) {
            ddSortedArray[i] = ddNoPMArray[ddSortIndices[i]];
            tmpPMflagSortedArray[i] = tmpPMflagArray[ddSortIndices[i]];
            if (tmpPMflagSortedArray[i] == 0) {
                IIFar.add(interpolatedKnotData_II[i]);
                dataFar.add(ddSortedArray[i]);
            } else {
                IIPM.add(interpolatedKnotData_II[i]);
                dataPM.add(ddSortedArray[i]);
            }
        }

        double[][] IIFarArray = new double[IIFar.size()][];
        for (int row = 0; row < IIFar.size(); row++) {
            IIFarArray[row] = IIFar.get(row);
        }
        double[] dataFarArray = dataFar.stream().mapToDouble(d -> d).toArray();

        double[][] IIPMArray = new double[IIPM.size()][];
        for (int row = 0; row < IIPM.size(); row++) {
            IIPMArray[row] = IIPM.get(row);
        }
        double[] dataPMArray = dataPM.stream().mapToDouble(d -> d).toArray();

        RealMatrix IIFarM = new BlockRealMatrix(IIFarArray);
        DecompositionSolver solver = new QRDecomposition(IIFarM).getSolver();
        RealVector dataFarV = new ArrayRealVector(dataFarArray);
        RealVector solution = solver.solve(dataFarV);
        double[] intensityFar_I = solution.toArray();

        RealMatrix IIPMM = new BlockRealMatrix(IIPMArray);
        solver = new QRDecomposition(IIPMM).getSolver();
        RealVector dataPMV = new ArrayRealVector(dataPMArray);
        solution = solver.solve(dataPMV);
        double[] intensityPM_I = solution.toArray();

        DescriptiveStatistics dfGainDescriptiveStatistics = new DescriptiveStatistics();
        for (int row = 1; row < (intensityPM_I.length - 1); row++) {
            dfGainDescriptiveStatistics.addValue(intensityPM_I[row] / intensityFar_I[row]);
        }

        return dfGainDescriptiveStatistics.getMean();

    }

    public synchronized static double[] modelInitData(SingleBlockModelRecord singleBlockModelRecord_x, SingleBlockRawDataSetRecord singleBlockRawDataSetRecord_d0) {
        int baselineCount = singleBlockRawDataSetRecord_d0.baselineDataSetMCMC().intensityAccumulatorList().size();
        int onPeakFaradayCount = singleBlockRawDataSetRecord_d0.onPeakFaradayDataSetMCMC().intensityAccumulatorList().size();
        int onPeakPhotoMultCount = singleBlockRawDataSetRecord_d0.onPeakPhotoMultiplierDataSetMCMC().intensityAccumulatorList().size();
        int totalIntensityCount = baselineCount + onPeakFaradayCount + onPeakPhotoMultCount;

        int[] isotopeOrdinalIndicesArray = singleBlockRawDataSetRecord_d0.blockIsotopeOrdinalIndicesArray();
        int[] timeIndForSortingArray = singleBlockRawDataSetRecord_d0.blockTimeIndicesArray();

        double[][] interpolatedKnotData_II = singleBlockRawDataSetRecord_d0.blockKnotInterpolationArray();
        Matrix II = new Matrix(interpolatedKnotData_II);
        Matrix I = new Matrix(singleBlockModelRecord_x.I0(), singleBlockModelRecord_x.I0().length);
        Matrix intensityFn = II.times(I);

        double[] dataModel = new double[totalIntensityCount];
        int[] detectorOrdinalIndicesAccumulatorArray = singleBlockRawDataSetRecord_d0.blockDetectorOrdinalIndicesArray();
        for (int dataArrayIndex = 0; dataArrayIndex < totalIntensityCount; dataArrayIndex++) {
            int faradayIndex = 0;
            if (dataArrayIndex < baselineCount + onPeakFaradayCount) {
                faradayIndex = singleBlockModelRecord_x.mapDetectorOrdinalToFaradayIndex().get(detectorOrdinalIndicesAccumulatorArray[dataArrayIndex]);
            }
            int intensityIndex = timeIndForSortingArray[dataArrayIndex];
            int isotopeIndex = isotopeOrdinalIndicesArray[dataArrayIndex] - 1;
            if (dataArrayIndex < baselineCount) {
                dataModel[dataArrayIndex] = singleBlockModelRecord_x.baselineMeansArray()[faradayIndex];
            } else if (dataArrayIndex < baselineCount + onPeakFaradayCount) {
                if (isotopeIndex < singleBlockModelRecord_x.logRatios().length) {
                    dataModel[dataArrayIndex] =
                            (StrictMath.exp(singleBlockModelRecord_x.logRatios()[isotopeIndex])
                                    * (1.0 / singleBlockModelRecord_x.detectorFaradayGain())
                                    * intensityFn.get(intensityIndex, 0))
                                    + singleBlockModelRecord_x.baselineMeansArray()[faradayIndex];
                } else {
                    dataModel[dataArrayIndex] =
                            (1.0 / singleBlockModelRecord_x.detectorFaradayGain()) * intensityFn.get(intensityIndex, 0)
                                    + singleBlockModelRecord_x.baselineMeansArray()[faradayIndex];
                }
            } else {
                if (isotopeIndex < singleBlockModelRecord_x.logRatios().length) {
                    dataModel[dataArrayIndex] =
                            StrictMath.exp(singleBlockModelRecord_x.logRatios()[isotopeIndex])
                                    * intensityFn.get(intensityIndex, 0);
                } else {
                    dataModel[dataArrayIndex] =
                            intensityFn.get(intensityIndex, 0);
                }
            }
        }

        return dataModel;
    }

    private synchronized static double calcError(double[] origData, double[] modelData, double[] dataSignalNoiseArray_Dsig) {
        double sum = 0.0;
        for (int i = 0; i < origData.length; i++) {
            sum += Math.pow((origData[i] - modelData[i]), 2.0) / dataSignalNoiseArray_Dsig[i];
        }
        return sum;
    }

    private synchronized static double calcVariance(double[] eTmp, double minETmp, double[] testArray) {
        double[] ee = new double[eTmp.length];
        double sumExpEE = 0.0;
        for (int i = 0; i < ee.length; i++) {
            ee[i] = eTmp[i] - minETmp;
            sumExpEE += StrictMath.exp(-ee[i] / 2.0);
        }
        double[] p = new double[eTmp.length];
        double varSum = 0.0;
        for (int i = 0; i < ee.length; i++) {
            p[i] = StrictMath.exp(-ee[i] / 2.0) / sumExpEE;
            varSum += p[i] * StrictMath.pow((testArray[i] - 0.0), 2.0);
        }

        return varSum;
    }

    public record SingleBlockModelRecordWithCov(
            SingleBlockModelRecord singleBlockModelRecord,
            ProposedModelParameters.ProposalRangesRecord proposalRangesRecord,
            Matrix covarianceMatrix_C0) {

    }

    public static class ArrayIndexComparator implements Comparator<Integer>, Serializable {
        private final int[] array;

        public ArrayIndexComparator(int[] array) {
            this.array = array;
        }

        public Integer[] createIndexArray() {
            Integer[] indexes = new Integer[array.length];
            for (int i = 0; i < array.length; i++) {
                indexes[i] = i; // Autoboxing
            }
            return indexes;
        }

        @Override
        public int compare(Integer index1, Integer index2) {
            return Integer.compare(array[index1], array[index2]);
        }
    }
}