package org.cirdles.tripoli.sessions.analysis;

public record GeometricMeanStatsRecord(
        double geoMean,
        double geoMeanPlusOneStdDev,
        double geomeanPlusTwoStdDev,
        double geoMeanPlusTwoStdErr,
        double geoMeanMinusOneStdDev,
        double geoMeanMinusTwoStdDev,
        double geoMeanMinusTwoStdErr
) {
    public static GeometricMeanStatsRecord generateGeometricMeanStats(double arithmeticMean, double arithmeticStandardDeviation, double arithmeticStandardError) {
        double geoMeanc = StrictMath.exp(arithmeticMean);
        double geoMeanPlusOneStdDevc = StrictMath.exp(arithmeticMean + arithmeticStandardDeviation);
        double geomeanPlusTwoStdDevc = StrictMath.exp(arithmeticMean + 2.0 * arithmeticStandardDeviation);
        double geoMeanPlusTwoStdErrc = StrictMath.exp(arithmeticMean + 2 * arithmeticStandardError);
        double geoMeanMinusOneStdDevc = StrictMath.exp(arithmeticMean - arithmeticStandardDeviation);;
        double geoMeanMinusTwoStdDevc = StrictMath.exp(arithmeticMean - 2 * arithmeticStandardDeviation);
        double geoMeanMinusTwoStdErrc = StrictMath.exp(arithmeticMean - 2 * arithmeticStandardError);
        return new GeometricMeanStatsRecord(
                geoMeanc,
                geoMeanPlusOneStdDevc,
                geomeanPlusTwoStdDevc,
                geoMeanPlusTwoStdErrc,
                geoMeanMinusOneStdDevc,
                geoMeanMinusTwoStdDevc,
                geoMeanMinusTwoStdErrc
        );
    }
}