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

package org.cirdles.tripoli.utilities.mathUtilities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static java.lang.StrictMath.*;

public class FormatterForSigFigN {

    /**
     * The numerical outputs should be in a font size larger than menus, and vertically aligned to improve readability.
     * Specifically, the equals signs in all three expressions and the decimals in all three numbers should be vertically aligned.
     * The numbers should be in a monospaced font so that they align vertically as well.
     * If the standard error is less than 10, it should be rounded to two significant figures,
     * and the mean and standard deviation should be rounded to the same number of decimal places.
     * If the standard error is greater than 10, round all results to the nearest integer.
     */
    public static int countOfTrailingDigitsForSigFig(double standardError, int sigFig) {
        int countOfTrailingDigitsForSigFig = 0;
        if (Math.abs(standardError) < 10.0 && Math.abs(standardError) > 0.0) {
            double rounded = MathUtilities.roundedToSize(standardError, sigFig);
            String pattern = generatePattern(rounded, sigFig);
            DecimalFormat df = new DecimalFormat(pattern,new DecimalFormatSymbols(Locale.ENGLISH));
            df.setMaximumFractionDigits(8);
            String roundedString = df.format(rounded);
            int scale = roundedString.split("\\.")[1].length();
            countOfTrailingDigitsForSigFig = scale;
        }
        return countOfTrailingDigitsForSigFig;
    }

    /**
     * Generates a DecimalFormat pattern that allows for a certain amount of trailing zeros specified by
     * the amount of significant figures
     * @param rounded
     * @return String pattern to be used in DecimalFormat
     */
    public static String generatePattern(double rounded, int sigFig) {
        String pattern = "#.";
        if (Double.isFinite(rounded)) {
            // Converts the decimal value to a BigDecimal to find the scale
            BigDecimal valueBD = new BigDecimal(String.valueOf(rounded));
            int scale = sigFig - (valueBD.precision() - valueBD.scale());

            // Allows for a trailing zero in each decimal place within the scale
            for (int i = 0; i < scale; i++) {
                pattern += "0";
            }

            return pattern;
        }

        // Returns default pattern if rounded number is infinite
        return "#";
    }

    /**
     * see https://docs.google.com/document/d/14PPEDEJPylNMavpJDpYSuemNb0gF5dz_To3Ek1Y_Agw/edit#bookmark=id.bf8wjg6paqcw
     *
     * @param mean
     * @param stdErr
     * @param stdDev
     * @param countOfSigFigs
     * @return
     */
    public static FormattedStats formatToScientific(double mean, double stdErr, double stdDev, int countOfSigFigs) {
        /*
        % summary statistics
            % power of 10 for second significant figure in 1sigma abs uncertainty
            lastSigFigPosition = floor(log10(stdErrMean)) - 1;
            % round the mean, uncertainty, and standard deviation accordingly
            roundedMean = round(measMean,     -lastSigFigPosition);
            roundedUnct = round(2*stdErrMean, -lastSigFigPosition);
            roundedStdv = round(2*stdvMean,   -lastSigFigPosition);
            % calculate exponent of mean and digits to display in significand
            exponentOfMean = floor(log10(abs(measMean))); % integer valued
            formatString = "%1." + string(exponentOfMean-lastSigFigPosition) + "f";
            % calculate significands -- the numbers before the exponent in sci notation
            significandOfMeanAsString = sprintf(formatString, roundedMean / 10^exponentOfMean);
            significandOfUnctAsString = sprintf(formatString, roundedUnct / 10^exponentOfMean);
            significantOfStdvAsString = sprintf(formatString, roundedStdv / 10^exponentOfMean);
            % create string representations for mean, uncertainty, and standard deviation
            meanAsString = significandOfMeanAsString + "e" + string(exponentOfMean);
            unctAsString = significandOfUnctAsString + "e" + string(exponentOfMean);
            stdvAsString = significantOfStdvAsString + "e" + string(exponentOfMean);

         */
        int lastSigFigPosition = (int) (floor(log10(stdErr)) - (countOfSigFigs - 1));
        BigDecimal roundedMean = new BigDecimal(mean).setScale(-lastSigFigPosition, RoundingMode.HALF_UP);
        BigDecimal roundedUnct = new BigDecimal(stdErr).setScale(-lastSigFigPosition, RoundingMode.HALF_UP);
        BigDecimal roundedStdv = new BigDecimal(stdDev).setScale(-lastSigFigPosition, RoundingMode.HALF_UP);

        int exponentOfMean = (int) floor(log10(abs(mean)));
        String formatString = "%1." + Math.abs(exponentOfMean - lastSigFigPosition) + "f";
        String significandOfMeanAsString = String.format(formatString, (roundedMean.doubleValue() / pow(10, exponentOfMean)));
        String significandOfUnctAsString = String.format(formatString, (roundedUnct.doubleValue() / pow(10, exponentOfMean)));
        String significantOfStdvAsString = String.format(formatString, (roundedStdv.doubleValue() / pow(10, exponentOfMean)));

        String meanAsString = significandOfMeanAsString + "e" + exponentOfMean;
        String unctAsString = significandOfUnctAsString + "e" + exponentOfMean;
        String stdvAsString = significantOfStdvAsString + "e" + exponentOfMean;

        return new FormattedStats(meanAsString, unctAsString, stdvAsString);
    }

    public static FormattedStats formatToSigFig(double mean, double stdErr, double stdDev, int countOfSigFigs) {
        int lastSigFigPosition;
        if (stdErr == 0.0) {
            lastSigFigPosition = 0;
        } else {
            lastSigFigPosition = (int) (floor(log10(stdErr)) - (countOfSigFigs - 1));
        }
        BigDecimal roundedMean = new BigDecimal(mean).setScale(-lastSigFigPosition, RoundingMode.HALF_UP);
        BigDecimal roundedUnct = new BigDecimal(stdErr).setScale(-lastSigFigPosition, RoundingMode.HALF_UP);
        BigDecimal roundedStdv = new BigDecimal(stdDev).setScale(-lastSigFigPosition, RoundingMode.HALF_UP);

        String meanAsString = roundedMean.toPlainString();
        String unctAsString = roundedUnct.toPlainString();
        String stdvAsString = roundedStdv.toPlainString();

        return new FormattedStats(meanAsString, unctAsString, stdvAsString);
    }

    public record FormattedStats(
            String meanAsString,
            String unctAsString,
            String stdvAsString
    ) {
        public FormattedStats padLeft() {
            // pad left
            int maxLength = -100;
            maxLength = max(maxLength, meanAsString.length());
            maxLength = max(maxLength, unctAsString.length());
            maxLength = max(maxLength, unctAsString.length());

            String meanAsStringP = String.format("%1$" + maxLength + "s", meanAsString);
            String unctAsStringP = String.format("%1$" + maxLength + "s", unctAsString);
            String stdvAsStringP = String.format("%1$" + maxLength + "s", stdvAsString);

            return new FormattedStats(meanAsStringP, unctAsStringP, stdvAsStringP);
        }
    }

}
