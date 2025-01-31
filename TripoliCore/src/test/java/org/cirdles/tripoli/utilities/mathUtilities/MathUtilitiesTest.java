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

import org.cirdles.tripoli.parameters.Parameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import static org.apache.commons.math3.special.Erf.erfc;
import com.google.common.math.Stats;

class MathUtilitiesTest {

    double[] arr;
    boolean[] indices;

    boolean[] solvedIndices;

    @BeforeEach
    /**
     * Created a data set of double values
     *
     * Document the process of using the python script and mention the imports used
     */
    void setUp() {
        //Create an array of doubles
        arr = new double[] {47.3, 50.5, 53.7, 55.8, 55.0, 56.0001, 57.0012345678, 57.004999, 58.153, 58.852, 58.123, 58.543, 60.415, 60.794, 60.351,
                61.751, 61.652, 61.325, 61.587, 61.126, 61.897, 62.543, 62.258, 62.689, 63.5473, 63.789, 64.369, 67.115, 68.142, 72.368};
        indices = new boolean[] {true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true};


        solvedIndices = new boolean[] {false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false};
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void roundedToSize() {

        assertEquals(0.005, MathUtilities.roundedToSize(0.004999, 2), 0.001);
        // 0.004999 rounded to 2 significant figures = 0.005
        assertEquals(123.0, MathUtilities.roundedToSize(123.45, 3), 0.001);
        // 123.45 rounded to 3 significant figures = 123.0
        assertEquals(1000.0, MathUtilities.roundedToSize(999.99, 1), 0.001);
        // 999.99 rounded to 1 significant figure = 1000.0
        assertEquals(0.00123, MathUtilities.roundedToSize(0.0012345678, 4), 0.00001);
        // 0.0012345678 rounded to 4 significant figures = 0.00123
    }

    @Test
    void nChooseR() {
        // This test method is testing the nChooseR method,
        // which is not used in the application code but is being tested for correctness.

        assertEquals(1, MathUtilities.nChooseR(1, 0)); // n=1, r=0
        assertEquals(1, MathUtilities.nChooseR(1, 1)); // n=1, r=1
        assertEquals(10, MathUtilities.nChooseR(5, 2)); // n=5, r=2
        assertEquals(20, MathUtilities.nChooseR(6, 3)); // n=6, r=3
        assertEquals(252, MathUtilities.nChooseR(10, 5)); // n=10, r=5
    }

    @Test
    void applyChauvenetsCriterion() {
        assertArrayEquals(solvedIndices, MathUtilities.applyChauvenetsCriterion(arr, indices, new Parameters()));
    }

    @Test
    void testDescriptiveStatistics() {
        //Add all data points to Descriptive Statistics (descStats)
        DescriptiveStatistics descStats = new DescriptiveStatistics();
        for (int i = 0; i < arr.length; i++) {
            descStats.addValue(arr[i]);
        }

        //Add all data points to Stats (stats)
        Stats stats = Stats.of(arr);

        //Compare their respective methods
        assertEquals(descStats.getMean(), stats.mean(), 0.000000000001);
        assertEquals(descStats.getStandardDeviation(), stats.sampleStandardDeviation(), 0.00000000001);
    }

    @Test
    void testRoundedToSize() {
        int sigFigs = 4;

        double[] nums = {6.1542565, 2.36489, 8.9125, 0.025483, 10.5475, 103.2578, 0.00012563, 0.2000, 0.0000, 2.0, 3};

        double[] actual = new double[nums.length];

        double[] expected = {6.154,2.365,8.913,0.02548,10.55,103.3,0.0001256,0.2000,0.0000,2.000,3.000}; // Iff sigFigs is equal to 4

        for (int i = 0; i < nums.length; i++) {
            actual[i] = MathUtilities.roundedToSize(nums[i], sigFigs);
        }

        assertArrayEquals(expected, actual);
    }


    @Test
    void testCountOfTrailingDigits() {
        int sigFigs = 4;

        /*
        Conditions for a Good Set of Numbers: Include Whole Integers, Numbers with Trailing Zeros, Numbers with Leading Zeros,
        Numbers greater than 10, Numbers less than 10, Numbers with Spurious Digits, Numbers with 0s in between Non-Zero digits,

         */
        double[] nums = {6.1542565, 2.30089, 8.9125, 0.025483, 10.5475, 103.2578, 0.00012563, 0.2000, 0.0000, 2.0, 3};
        double[] actual = new double[nums.length];

        double[] expected = {3,3,3,5,0,0,7,4,0,3,3}; // Iff sigFigs is equal to 4

        for (int i = 0; i < nums.length; i++) {
            actual[i] = FormatterForSigFigN.countOfTrailingDigitsForSigFig(nums[i], sigFigs);
        }

        assertArrayEquals(expected, actual);
    }
}