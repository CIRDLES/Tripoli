package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;
import static org.cirdles.tripoli.utilities.mathUtilities.FormatterForSigFigN.countOfTrailingDigitsForSigFig;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class AnalysisBlockCyclesPlotOGTest {

    /**
     * A modified version of calcSigmaPctsCM from AnalysisBlockCyclesPlotOG
     * for testing purposes.
     * geometricMeanStatsRecord determined from observing values with the debugger with NBS981 230024b-154.TIMSDP.
     * @return
     */
    private HashMap<String, Double> calcSigmaPctsCM() {
        HashMap<String, Double> geometricMeanStatsRecord = new HashMap<>();
        geometricMeanStatsRecord.put("geoMean", 0.05932789573477027);
        geometricMeanStatsRecord.put("geoMeanPlusOneStdDev", 0.05942628541058665);
        geometricMeanStatsRecord.put("geoMeanPlusTwoStdDev", 0.05952483825632817);
        geometricMeanStatsRecord.put("geoMeanPlusOneStdErr", 0.5933477908344331);
        geometricMeanStatsRecord.put("geoMeanPlusTwoStdErr", 0.05934166323073708);
        geometricMeanStatsRecord.put("geoMeanMinusOneStdDev", 0.05922966895872527);
        geometricMeanStatsRecord.put("geoMeanMinusTwoStdDev", 0.05913160481274517);
        geometricMeanStatsRecord.put("geoMeanMinusOneStdErr", 0.05932101318462535);
        geometricMeanStatsRecord.put("geoMeanMinusTwoStdErr", 0.05931413143291592);

        double geoMeanPlusOneStandardError = geometricMeanStatsRecord.get("geoMeanPlusOneStdErr");
        double geoMeanMinusOneStandardError = geometricMeanStatsRecord.get("geoMeanMinusOneStdErr");
        double geoMeanRatioPlusOneStdErrPct = (geoMeanPlusOneStandardError - geometricMeanStatsRecord.get("geoMean")) / geometricMeanStatsRecord.get("geoMean") * 100.0;
        double geoMeanRatioMinusOneStdErrPct = (geometricMeanStatsRecord.get("geoMean") - geoMeanMinusOneStandardError) / geometricMeanStatsRecord.get("geoMean") * 100.0;

        double smallerGeoMeanRatioOneStdErrPct = Math.min(geoMeanRatioPlusOneStdErrPct, geoMeanRatioMinusOneStdErrPct);
        int countOfTrailingDigitsForStdErrPct = countOfTrailingDigitsForSigFig(smallerGeoMeanRatioOneStdErrPct, 2);
        double plusErrPct = (new BigDecimal(geoMeanRatioPlusOneStdErrPct).setScale(countOfTrailingDigitsForStdErrPct, RoundingMode.HALF_UP)).doubleValue();
        double minusErrPct = (new BigDecimal(geoMeanRatioMinusOneStdErrPct).setScale(countOfTrailingDigitsForStdErrPct, RoundingMode.HALF_UP)).doubleValue();

        double geoMeanPlusOneStandardDeviation = geometricMeanStatsRecord.get("geoMeanPlusOneStdDev");
        double geoMeanMinusOneStandardDeviation = geometricMeanStatsRecord.get("geoMeanMinusOneStdDev");
        double geoMeanRatioPlusOneSigmaPct = (geoMeanPlusOneStandardDeviation - geometricMeanStatsRecord.get("geoMean")) / geometricMeanStatsRecord.get("geoMean") * 100.0;
        double geoMeanRatioMinusOneSigmaPct = (geometricMeanStatsRecord.get("geoMean") - geoMeanMinusOneStandardDeviation) / geometricMeanStatsRecord.get("geoMean") * 100.0;
        double smallerGeoMeanRatioForOneSigmaPct = Math.min(geoMeanRatioPlusOneSigmaPct, geoMeanRatioMinusOneSigmaPct);
        int countOfTrailingDigitsForOneSigmaPct = countOfTrailingDigitsForSigFig(smallerGeoMeanRatioForOneSigmaPct, 2);
        double plusSigmaPct = (new BigDecimal(geoMeanRatioPlusOneSigmaPct).setScale(countOfTrailingDigitsForOneSigmaPct, RoundingMode.HALF_UP)).doubleValue();
        double minusSigmaPct = (new BigDecimal(geoMeanRatioMinusOneSigmaPct).setScale(countOfTrailingDigitsForOneSigmaPct, RoundingMode.HALF_UP)).doubleValue();

        HashMap<String, Double> output = new HashMap<>();
        output.put("plusErrPct", plusErrPct);
        output.put("minusErrPct", minusErrPct);
        output.put("plusSigmaPct", plusSigmaPct);
        output.put("minusSigmaPct", minusSigmaPct);
        output.put("geoMeanPlusOneStandardDeviation", geoMeanPlusOneStandardDeviation);
        output.put("countOfTrailingDigitsForStdErrPct", (double) countOfTrailingDigitsForStdErrPct);
        output.put("countOfTrailingDigitsForOneSigmaPct", (double) countOfTrailingDigitsForOneSigmaPct);

        return output;
    }

    @Test
    public void calcSigmaPctsCMTest() {

        HashMap<String, Double> expectedValues = new HashMap<>();

        expectedValues.put("plusErrPct", 900.116);
        expectedValues.put("minusErrPct", 0.012);
        expectedValues.put("plusSigmaPct", 0.17);
        expectedValues.put("minusSigmaPct", 0.17);
        expectedValues.put("geoMeanPlusOneStandardDeviation", 0.05942628541058665);
        expectedValues.put("countOfTrailingDigitsForStdErrPct", 3.0);
        expectedValues.put("countOfTrailingDigitsForOneSigmaPct", 2.0);

        // Can not figure out how to run calcSigmaPctsCM
        HashMap<String, Double> actualValues = calcSigmaPctsCM();

        for (String key : expectedValues.keySet()) {
            assertEquals(expectedValues.get(key), actualValues.get(key));
        }
    }

    /**
     * A modified version of calcPlotStatsCM from AnalysisBlockCyclesPlotOG
     * for testing purposes.
     * mean, stdDev, and stdErr determined from observing values with the debugger with NBS981 230024b-154.TIMSDP.
     * @return
     */
    private HashMap<String, Double> calcPlotStatsCM() {
        double mean = -2.824675666477416, stdDev = 0.001657031312585908, stdErr = 1.1601539728124193E-4;
        double meanPlusOneStandardDeviation = mean + stdDev;
        double meanPlusTwoStandardDeviation = mean + 2.0 * stdDev;
        double meanPlusTwoStandardError = mean + 2.0 * stdErr;
        double meanMinusOneStandardDeviation = mean - stdDev;
        double meanMinusTwoStandardDeviation = mean - 2.0 * stdDev;
        double meanMinusTwoStandardError = mean - 2.0 * stdErr;

        HashMap<String, Double> output = new HashMap<>(Map.ofEntries(
                entry("mean", mean),
                entry("stdDev", stdDev),
                entry("stdErr", stdErr),
                entry("meanPlusOneStandardDeviation", meanPlusOneStandardDeviation),
                entry("meanPlusTwoStandardDeviation", meanPlusTwoStandardDeviation),
                entry("meanPlusTwoStandardError", meanPlusTwoStandardError),
                entry("meanMinusOneStandardDeviation", meanMinusOneStandardDeviation),
                entry("meanMinusTwoStandardDeviation", meanMinusTwoStandardDeviation),
                entry("meanMinusTwoStandardError", meanMinusTwoStandardError)
        ));

        return output;
    }

    @Test
    public void calcPlotStatsCMTest() {

    }

}
