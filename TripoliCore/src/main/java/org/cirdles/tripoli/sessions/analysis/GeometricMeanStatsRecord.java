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