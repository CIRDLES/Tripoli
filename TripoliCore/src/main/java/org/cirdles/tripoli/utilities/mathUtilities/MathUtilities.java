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
            BigDecimal valueBD = new BigDecimal(value);
            int newScale = sigFigs - (valueBD.precision() - valueBD.scale());
            valueBDtoSize = valueBD.setScale(newScale, RoundingMode.HALF_UP);
        }
        return valueBDtoSize.doubleValue();
    }
}