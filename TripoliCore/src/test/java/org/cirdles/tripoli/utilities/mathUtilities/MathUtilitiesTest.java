package org.cirdles.tripoli.utilities.mathUtilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MathUtilitiesTest {

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
}