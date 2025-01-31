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

package org.cirdles.tripoli.settings.plots.species;

import java.io.Serializable;

import static org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;


public record SpeciesColors(
        String faradayHexColor,
        String pmHexColor,
        String faradayModelHexColor,
        String pmModelHexColor) implements Serializable {

    public String get(DetectorPlotFlavor plotFlavor) {
        String result = "";
        switch (plotFlavor) {
            case PM_DATA -> result = pmHexColor;
            case FARADAY_DATA -> result = faradayHexColor;
            case FARADAY_MODEL -> result = faradayModelHexColor;
            case PM_MODEL -> result = pmModelHexColor;
        }
        return result;
    }

    public SpeciesColors copy() {
        return new SpeciesColors(
                faradayHexColor,
                pmHexColor,
                faradayModelHexColor,
                pmModelHexColor);
    }

    public SpeciesColors altered(DetectorPlotFlavor plotFlavor, String hexColor) {
        String faraday = faradayHexColor;
        String pm = pmHexColor;
        String faradayModel = faradayModelHexColor;
        String pmModel = pmModelHexColor;
        switch (plotFlavor) {
            case PM_DATA -> {
                pm = hexColor;
            }
            case PM_MODEL -> {
                pmModel = hexColor;
            }
            case FARADAY_DATA -> {
                faraday = hexColor;
            }
            case FARADAY_MODEL -> {
                faradayModel = hexColor;
            }
        }
        return new SpeciesColors(faraday, pm, faradayModel, pmModel);
    }
}
