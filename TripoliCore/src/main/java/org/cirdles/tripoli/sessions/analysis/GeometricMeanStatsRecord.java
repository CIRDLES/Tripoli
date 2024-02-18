package org.cirdles.tripoli.sessions.analysis;

public record GeometricMeanStatsRecord(
        double geoMean,
        double geoMeanPlusOneStdDev,
        double geomeanPlusTwoStdDev,
        double geoMeanPlusOneStdErr,
        double geoMeanPlusTwoStdErr,
        double geoMeanMinusOneStdDev,
        double geoMeanMinusTwoStdDev,
        double geoMeanMinusOneStdErr,
        double geoMeanMinusTwoStdErr
) {
    public static GeometricMeanStatsRecord generateGeometricMeanStats(double arithmeticMean, double arithmeticStandardDeviation, double arithmeticStandardError) {
        double geoMeanc = StrictMath.exp(arithmeticMean);
        double geoMeanPlusOneStdDevc = StrictMath.exp(arithmeticMean + arithmeticStandardDeviation);
        double geomeanPlusTwoStdDevc = StrictMath.exp(arithmeticMean + 2.0 * arithmeticStandardDeviation);
        double geoMeanPlusOneStdErrc = StrictMath.exp(arithmeticMean + arithmeticStandardError);
        double geoMeanPlusTwoStdErrc = StrictMath.exp(arithmeticMean + 2 * arithmeticStandardError);
        double geoMeanMinusOneStdDevc = StrictMath.exp(arithmeticMean - arithmeticStandardDeviation);
        ;
        double geoMeanMinusTwoStdDevc = StrictMath.exp(arithmeticMean - 2 * arithmeticStandardDeviation);
        double geoMeanMinusOneStdErrc = StrictMath.exp(arithmeticMean - arithmeticStandardError);
        double geoMeanMinusTwoStdErrc = StrictMath.exp(arithmeticMean - 2 * arithmeticStandardError);

        // for display


        return new GeometricMeanStatsRecord(
                geoMeanc,
                geoMeanPlusOneStdDevc,
                geomeanPlusTwoStdDevc,
                geoMeanPlusOneStdErrc,
                geoMeanPlusTwoStdErrc,
                geoMeanMinusOneStdDevc,
                geoMeanMinusTwoStdDevc,
                geoMeanMinusOneStdErrc,
                geoMeanMinusTwoStdErrc
        );
    }
}