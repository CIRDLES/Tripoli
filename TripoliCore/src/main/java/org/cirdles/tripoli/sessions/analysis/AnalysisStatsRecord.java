package org.cirdles.tripoli.sessions.analysis;

import com.google.common.primitives.Booleans;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.plots.compoundPlotBuilders.PlotBlockCyclesRecord;
import org.cirdles.tripoli.utilities.mathUtilities.FormatterForSigFigN;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.exp;
import static org.cirdles.tripoli.sessions.analysis.GeometricMeanStatsRecord.generateGeometricMeanStats;
import static org.cirdles.tripoli.utilities.mathUtilities.FormatterForSigFigN.countOfTrailingDigitsForSigFig;

public record AnalysisStatsRecord(
        boolean isRatio,
        BlockStatsRecord[] blockStatsRecords,
        double blockModeWeightedMean, // see package org.cirdles.tripoli.utilities.mathUtilities.weightedMeans;
        double blockModeWeightedMeanOneSigma,
        double blockModeChiSquared,
        int countOfIncludedBlocks,
        double cycleModeMean,
        double cycleModeVariance,
        double cycleModeStandardDeviation,
        double cycleModeStandardError,
        boolean[] cycleModeIncluded,
        double[] cycleModeData,
        int countOfTotalCycles,
        int countOfIncludedCycles) implements Serializable {

    ///TODO: fix signature as userFunction holds all
    public static BlockStatsRecord[] generateAnalysisBlockStatsRecords(UserFunction userFunction, Map<Integer, PlotBlockCyclesRecord> mapBlockIdToBlockCyclesRecord) {
        // Jan 2024 new approach - two modes: block mode and cycle mode
        // BLOCK MODE will be default - calculate and plot stats for each block
        // October 2024 - Block mode is being abandoned for now
        int blockCount = mapBlockIdToBlockCyclesRecord.size();
        BlockStatsRecord[] blockStatsRecords = new BlockStatsRecord[blockCount];
        int arrayIndex = 0;
        for (Map.Entry<Integer, PlotBlockCyclesRecord> entry : mapBlockIdToBlockCyclesRecord.entrySet()) {
            PlotBlockCyclesRecord plotBlockCyclesRecord = entry.getValue();
            if (plotBlockCyclesRecord != null) {
                blockStatsRecords[arrayIndex] = BlockStatsRecord.generateBlockStatsRecord(
                        plotBlockCyclesRecord.blockID(), plotBlockCyclesRecord.blockIncluded(), userFunction.isTreatAsIsotopicRatio(),
                        userFunction.isInverted(), plotBlockCyclesRecord.cycleMeansData(), plotBlockCyclesRecord.cyclesIncluded());
            }
            arrayIndex++;
        }
        return blockStatsRecords;
    }

    public static AnalysisStatsRecord generateAnalysisStatsRecord(BlockStatsRecord[] blockStatsRecords) {
        int countOfIncludedBlocks = 0;
        double wmNumerator = 0.0;
        double wmDenominator = 0.0;
        double weightedMeanC = 0.0;
        double weightedMeanOneSigmaSquaredC;
        double weightedMeanOneSigmaC;
        double chiSquaredTerm = 0.0;
        double chiSquaredC;

        DescriptiveStatistics cycleModeDescriptiveStats = new DescriptiveStatistics();
        List<double[]> cycleModeDataByBlocks = new ArrayList<>();
        List<boolean[]> cycleModeIncludedByBlocks = new ArrayList<>();
        int countOfTotalCycles = 0;

        for (int i = 0; i < blockStatsRecords.length; i++) {
            cycleModeDataByBlocks.add(blockStatsRecords[i].cycleMeansData());
            cycleModeIncludedByBlocks.add(blockStatsRecords[i].cyclesIncluded());
            countOfTotalCycles += blockStatsRecords[i].cyclesIncluded().length;

            //todo fix or remove blockincludedflag
            if (blockStatsRecords[i].blockIncluded()) {
                wmNumerator += blockStatsRecords[i].mean() / StrictMath.pow(blockStatsRecords[i].standardDeviation(), 2);
                wmDenominator += 1.0 / StrictMath.pow(blockStatsRecords[i].standardDeviation(), 2);
                for (int cycleIndex = 0; cycleIndex < blockStatsRecords[i].cycleMeansData().length; cycleIndex++) {
                    if (blockStatsRecords[i].cyclesIncluded()[cycleIndex]) {
                        if (blockStatsRecords[0].isRatio()) {
                            if (blockStatsRecords[0].isRatio() && blockStatsRecords[0].isInverted()) {
                                cycleModeDescriptiveStats.addValue(-StrictMath.log(blockStatsRecords[i].cycleMeansData()[cycleIndex]));
                            } else {
                                cycleModeDescriptiveStats.addValue(StrictMath.log(blockStatsRecords[i].cycleMeansData()[cycleIndex]));
                            }
                        } else {
                            cycleModeDescriptiveStats.addValue(blockStatsRecords[i].cycleMeansData()[cycleIndex]);
                        }
                    }
                }
            }
        }
        weightedMeanC = wmNumerator / wmDenominator;
        weightedMeanOneSigmaSquaredC = 1.0 / wmDenominator;
        weightedMeanOneSigmaC = StrictMath.sqrt(weightedMeanOneSigmaSquaredC);

        for (int i = 0; i < blockStatsRecords.length; i++) {
            if (blockStatsRecords[i].blockIncluded()) {
                chiSquaredTerm += StrictMath.pow(blockStatsRecords[i].mean() - weightedMeanC, 2) / weightedMeanOneSigmaSquaredC;
                countOfIncludedBlocks++;
            }
        }
        chiSquaredC = chiSquaredTerm / (countOfIncludedBlocks - 1);


        double cycleModeMean = cycleModeDescriptiveStats.getMean();
        double cycleModeVariance = cycleModeDescriptiveStats.getVariance();
        double cycleModeStandardDeviation = cycleModeDescriptiveStats.getStandardDeviation();
        double cycleModeStandardError = StrictMath.sqrt(cycleModeVariance / cycleModeDescriptiveStats.getN());

        boolean[] cycleModeIncluded = new boolean[countOfTotalCycles];
        double[] cycleModeData = new double[countOfTotalCycles];
        int index = 0;
        for (boolean[] cyclesIncluded : cycleModeIncludedByBlocks) {
            for (int i = 0; i < cyclesIncluded.length; i++) {
                cycleModeIncluded[index] = cyclesIncluded[i];
                index++;
            }
        }

        index = 0;
        for (double[] cycleData : cycleModeDataByBlocks) {
            for (int i = 0; i < cycleData.length; i++) {
                cycleModeData[index] = cycleData[i];
                index++;
            }
        }

        return new AnalysisStatsRecord(
                blockStatsRecords.length > 0 ? blockStatsRecords[0].isRatio() : false,
                blockStatsRecords,
                weightedMeanC,
                weightedMeanOneSigmaC,
                chiSquaredC,
                countOfIncludedBlocks,
                cycleModeMean,
                cycleModeVariance,
                cycleModeStandardDeviation,
                cycleModeStandardError,
                cycleModeIncluded,
                cycleModeData,
                countOfTotalCycles,
                (int) cycleModeDescriptiveStats.getN());
    }

    public static String prettyPrintRatioBlockMean(UserFunction userFunction) {
        // todo: refactor this code which duplicates cyclesplot code
        String blockMean = "";
        if (userFunction.getAnalysisStatsRecord() != null) {
            AnalysisStatsRecord analysisStatsRecord = userFunction.getAnalysisStatsRecord();
            double geoWeightedMeanRatio = exp(analysisStatsRecord.blockModeWeightedMean());
            if (!Double.isNaN(geoWeightedMeanRatio)) {
                double geoWeightedMeanRatioPlusOneSigma = exp(analysisStatsRecord.blockModeWeightedMean() + analysisStatsRecord.blockModeWeightedMeanOneSigma());
                double geoWeightedMeanRatioMinusOneSigma = exp(analysisStatsRecord.blockModeWeightedMean() - analysisStatsRecord.blockModeWeightedMeanOneSigma());
                double geoWeightedMeanRatioPlusOneSigmaPct = (geoWeightedMeanRatioPlusOneSigma - geoWeightedMeanRatio) / geoWeightedMeanRatio * 100.0;
                double geoWeightedMeanRatioMinusOneSigmaPct = (geoWeightedMeanRatio - geoWeightedMeanRatioMinusOneSigma) / geoWeightedMeanRatio * 100.0;

                double lesserSigmaPct = (geoWeightedMeanRatioPlusOneSigmaPct > geoWeightedMeanRatioMinusOneSigmaPct) ?
                        geoWeightedMeanRatioMinusOneSigmaPct : geoWeightedMeanRatioPlusOneSigmaPct;

                int countOfTrailingDigitsForSigFig = countOfTrailingDigitsForSigFig(lesserSigmaPct, 2);
                double plusSigmaPct = (new BigDecimal(geoWeightedMeanRatioPlusOneSigmaPct).setScale(countOfTrailingDigitsForSigFig, RoundingMode.HALF_UP)).doubleValue();
                double minusSigmaPct = (new BigDecimal(geoWeightedMeanRatioMinusOneSigmaPct).setScale(countOfTrailingDigitsForSigFig, RoundingMode.HALF_UP)).doubleValue();

                lesserSigmaPct = (plusSigmaPct > minusSigmaPct) ? minusSigmaPct : plusSigmaPct;

                FormatterForSigFigN.FormattedStats formattedStats;
                if ((abs(geoWeightedMeanRatio) >= 1e7) || (abs(geoWeightedMeanRatio) <= 1e-5)) {
                    formattedStats =
                            FormatterForSigFigN.formatToScientific(geoWeightedMeanRatio, lesserSigmaPct, 0, 2).padLeft();
                } else {
                    formattedStats =
                            FormatterForSigFigN.formatToSigFig(geoWeightedMeanRatio, lesserSigmaPct, 0, 2).padLeft();
                }
                blockMean = formattedStats.meanAsString();
            }
        }
        return "x\u0304=" + blockMean;
    }

    public static String prettyPrintRatioCycleMean(UserFunction userFunction) {
        /*
                Round the (1-sigma percent) standard error and (1-sigma percent) standard deviation to two significant decimal places.
                 If there is a (+ and -) display on either because they are different, round both to the number of decimal places
                 belonging to the smallest increment.  So, below, 0.85 gets rounded to the hundredths to get two significant figures.,
                 For the Percent standard deviation, +10.0 and -9.1 get rounded to the tenths decimal place, matching the smallest
                 increment (tenths vs. the ones places for the +10).
                 Use two significant figures of the 1-sigma absolute standard error to determine where to round the mean.
                 */
        // todo: refactor this code which duplicates cyclesplot code
        String cycleMean = "";
        if (userFunction.getAnalysisStatsRecord() != null) {
            AnalysisStatsRecord analysisStatsRecord = userFunction.getAnalysisStatsRecord();
            GeometricMeanStatsRecord geometricMeanStatsRecord =
                    generateGeometricMeanStats(analysisStatsRecord.cycleModeMean(), analysisStatsRecord.cycleModeStandardDeviation(), analysisStatsRecord.cycleModeStandardError());
            double geoMean = geometricMeanStatsRecord.geoMean();
            if (!Double.isNaN(geoMean)) {
                double geoMeanPlusOneStandardDeviation = geometricMeanStatsRecord.geoMeanPlusOneStdDev();

                if ((abs(geoMean) >= 1e7) || (abs(geoMean) <= 1e-5)) {
                    FormatterForSigFigN.FormattedStats formattedStats =
                            FormatterForSigFigN.formatToScientific(geoMean, geoMeanPlusOneStandardDeviation - geoMean, 0, 2).padLeft();
                    cycleMean = formattedStats.meanAsString();
                } else {
                    FormatterForSigFigN.FormattedStats formattedStats =
                            FormatterForSigFigN.formatToSigFig(geoMean, geoMeanPlusOneStandardDeviation - geoMean, 0, 2).padLeft();
                    cycleMean = formattedStats.meanAsString();
                }
            }
        }
        return "x\u0304=" + cycleMean;
    }
}