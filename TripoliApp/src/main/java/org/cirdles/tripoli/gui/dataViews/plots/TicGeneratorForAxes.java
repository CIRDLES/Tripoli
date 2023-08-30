/*
 * TicGeneratorForAxes.java
 *
 * Created Aug 3, 2011
 *
 * Copyright 2006 James F. Bowring and www.Earth-Time.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cirdles.tripoli.gui.dataViews.plots;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author James F. Bowring
 */
public enum TicGeneratorForAxes {
    ;

    /**
     * @param axisMin
     * @param axisMax
     * @param numberTics
     * @return
     */
    public static BigDecimal[] generateTics(double axisMin, double axisMax, int numberTics) {
        /* Adapted from
         * Nice Numbers for Graph Labels
         * by Paul Heckbert
         * from "Graphics Gems", Academic Press, 1990
         */

        int nfrac;
        double d;
        double ticMin;
        double ticMax;
        double ticRange;

        ticRange = niceNum(axisMax - axisMin, true);
        d = niceNum(ticRange / (numberTics - 1), false);
        ticMin = StrictMath.floor(axisMin / d) * d;
        ticMax = StrictMath.ceil(axisMax / d) * d;

        nfrac = (int) StrictMath.max(-StrictMath.floor(StrictMath.log10(d)), 0);

        BigDecimal[] tics;

        try {
            tics = new BigDecimal[(int) ((ticMax + 0.5 * d - ticMin) / d) + 1];
            int index = 0;
            for (double x = ticMin; x < ticMax + 0.5 * d; x += d) {
                tics[index] = new BigDecimal(Double.toString(x)).setScale(nfrac, RoundingMode.HALF_UP);
                index++;
            }
        } catch (Exception e) {
            tics = new BigDecimal[0];
        }

        return formatTicsWhenAllInteger(tics);
    }

    private static boolean isIntegerValue(BigDecimal bd) {
        return 0 == bd.signum() || 0 >= bd.scale() || 0 >= bd.stripTrailingZeros().scale();
    }

    private static BigDecimal[] formatTicsWhenAllInteger(BigDecimal[] origTics) {
        BigDecimal[] tics = new BigDecimal[origTics.length];
        for (int i = 0; i < origTics.length; i++) {
            if (isIntegerValue(origTics[i])) {
                tics[i] = origTics[i].setScale(0);
            } else {
                // at least one non-integer
                tics = origTics.clone();
                break;
            }
        }

        return tics;
    }

    /**
     * @param min
     * @param max
     * @param marginStretchFactor
     * @return
     */
    public static double generateMarginAdjustment(double min, double max, double marginStretchFactor) {
        return marginStretchFactor * (max - min);
    }

    private static double niceNum(double x, boolean round) {
        /* Adapted from
         * Nice Numbers for Graph Labels
         * by Paul Heckbert
         * from "Graphics Gems", Academic Press, 1990
         */

        int expv;
        /* exponent of x */
        double f;
        /* fractional part of x */
        double nf;
        /* nice, rounded fraction */

        expv = (int) StrictMath.floor(StrictMath.log10(x));
        f = x / StrictMath.pow(10.0, expv);
        /* between 1 and 10 */
        if (round) {
            if (1.5 > f) {
                nf = 1.0;
            } else if (3.0 > f) {
                nf = 2.0;
            } else if (7.0 > f) {
                nf = 5.0;
            } else {
                nf = 10.0;
            }
        } else {
            if (1.0 >= f) {
                nf = 1.0;
            } else if (2.0 >= f) {
                nf = 2.0;
            } else if (5.0 >= f) {
                nf = 5.0;
            } else {
                nf = 10.0;
            }

        }

        return nf * StrictMath.pow(10.0, expv);
    }
}