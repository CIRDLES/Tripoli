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

package org.cirdles.tripoli.settings.plots;

import java.io.Serializable;

import static org.cirdles.tripoli.constants.TripoliConstants.*;


public record RatiosColors(
        String oneSigmaShade,
        String twoSigmaShade,
        String twoStdErrShade,
        String meanColor,
        String dataColor,
        String dataAntiColor
) implements Serializable {

    public static RatiosColors defaultBlockCyclesPlotColors() {
        return new RatiosColors(
                OGTRIPOLI_ONESIGMA_HEX,
                OGTRIPOLI_TWOSIGMA_HEX,
                OGTRIPOLI_TWOSTDERR_HEX,
                OGTRIPOLI_MEAN_HEX,
                OGTRIPOLI_DATA_HEX,
                OGTRIPOLI_ANTI_DATA_HEX
        );
    }

    public String get(RatiosPlotColorFlavor plotColorFlavor) {
        StringBuilder result = new StringBuilder();
        switch (plotColorFlavor) {
            case ONE_SIGMA_SHADE -> result.append(oneSigmaShade);
            case TWO_SIGMA_SHADE -> result.append(twoSigmaShade);
            case TWO_STD_ERR_SHADE -> result.append(twoStdErrShade);
            case MEAN_COLOR -> result.append(meanColor);
            case DATA_COLOR -> result.append(dataColor);
            case REJECTED_COLOR -> result.append(dataAntiColor);
        }
        return result.toString();
    }

    public RatiosColors altered(RatiosPlotColorFlavor flavor, String hexColor) {
        String oneSigmaShade = oneSigmaShade();
        String twoSigmaShade = twoSigmaShade();
        String twoStdErrShade = twoStdErrShade();
        String meanColor = meanColor();
        String dataColor = dataColor();
        String dataAntiColor = dataAntiColor();
        switch (flavor) {
            case ONE_SIGMA_SHADE -> oneSigmaShade = hexColor;
            case TWO_SIGMA_SHADE -> twoSigmaShade = hexColor;
            case TWO_STD_ERR_SHADE -> twoStdErrShade = hexColor;
            case MEAN_COLOR -> meanColor = hexColor;
            case DATA_COLOR -> dataColor = hexColor;
            case REJECTED_COLOR -> dataAntiColor = hexColor;
        }
        return new RatiosColors(
                oneSigmaShade,
                twoSigmaShade,
                twoStdErrShade,
                meanColor,
                dataColor,
                dataAntiColor
        );
    }


}
