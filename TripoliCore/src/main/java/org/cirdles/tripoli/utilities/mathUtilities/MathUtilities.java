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

import com.google.common.primitives.Booleans;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.apache.commons.math3.special.Erf.erfc;

/**
 * @author James F. Bowring
 */
public class MathUtilities {

    /**
     * Performs excel-style rounding of double to a given number of significant
     * figures.
     *
     * @param value   double to round
     * @param sigFigs count of significant digits for rounding
     * @return double rounded to sigFigs significant digits
     */
    public static double roundedToSize(double value, int sigFigs) {
        BigDecimal valueBDtoSize = BigDecimal.ZERO;
        if (Double.isFinite(value)) {
            BigDecimal valueBD = new BigDecimal(String.valueOf(value));
            int newScale = sigFigs - (valueBD.precision() - valueBD.scale());
            valueBDtoSize = valueBD.setScale(newScale, RoundingMode.HALF_UP);
        }
        return valueBDtoSize.doubleValue();
    }

    /**
     * @author https://www.geeksforgeeks.org/program-calculate-value-ncr/
     */
    public static int nChooseR(int n, int r) {
        double sum = 1;
        // Calculate the value of n choose r using the
        // binomial coefficient formula
        for (int i = 1; i <= r; i++) {
            sum = sum * (n - r + i) / i;
        }

        return ((int) sum);
    }

    /**
     * see https://docs.google.com/document/d/14PPEDEJPylNMavpJDpYSuemNb0gF5dz_To3Ek1Y_Agw/edit#bookmark=id.k016qg1ghequ
     * seehttps://www.lexjansen.com/wuss/2007/DatabaseMgtWarehousing/DMW_Lin_CleaningData.pdf
     *
     * @param dataIn
     * @return
     */
    public static boolean[] applyChauvenetsCriterion(double[] dataIn, boolean[] includedIndicesIn) {
        /*
        Apply Chauvenet’s criterion to an entire measurement in Cycle Mode if there are 20 or more included cycles.
        Or, apply Chauvenet’s criterion to each block in Block Mode for blocks with greater than or equal to 20 cycles.

        For now, assume a default value of ChauvenetRejectionProbability = 0.5.  In the future, we can ask the user to input
        this constant (must be on [0,1]), but I’ve never heard of anyone using a different value than 0.5.
        Assemble the data to evaluate into a vector called data. Do not include cycles and blocks previously rejected by the user in the data vector.
        The number of elements in data is n
        Calculate the mean and standard deviation of data, xbar and stddev.
        For each element of data, calculate absZ_i = abs(data_i - xbar) / stddev where abs() is the absolute value
        Calculate Chauvenet’s criterion, C_i = n * erfc(absZ_i) for each absZ_i
        Identify all data with C_i > ChauvenetRejectionProbability as an outlier by Chauvenet’s Criterion
        Plot Chauvenet-identified outliers in red and recalculate all statistics after rejecting the identified outliers.
        Gray out the Chauvenet button so that it can’t be re-applied.
         */

        /*
        Logic changed per discussion #261
         */

        // TODO: move these to parameters
        double chauvenetRejectionProbability = 0.5;
        int requiredMinDatumCount = 20;

        boolean[] includedIndices = includedIndicesIn.clone();
        //if ((Booleans.countTrue(includedIndicesIn) == includedIndicesIn.length) && (includedIndicesIn.length >= requiredMinDatumCount)) {
            if ((Booleans.countTrue(includedIndices) >= requiredMinDatumCount)) {
            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
            for (int i = 0; i < dataIn.length; i++) {
                if (includedIndices[i]) {
                    descriptiveStatistics.addValue(dataIn[i]);
                }
            }
            double xbar = descriptiveStatistics.getMean();
            double stddev = descriptiveStatistics.getStandardDeviation();
            double[] absZ = new double[dataIn.length];
            for (int i = 0; i < dataIn.length; i++) {
                absZ[i] = Math.abs(dataIn[i] - xbar) / stddev;
                double chauvenetsCriterion = erfc(absZ[i]) * descriptiveStatistics.getN();
                if (chauvenetsCriterion < chauvenetRejectionProbability) {
                    includedIndices[i] = false;
                }
            }
        }
        return includedIndices;
    }
}