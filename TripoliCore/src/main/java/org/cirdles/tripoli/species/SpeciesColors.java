package org.cirdles.tripoli.species;

import static org.cirdles.tripoli.constants.TripoliConstants.DetectorPlotFlavor;

import java.util.Map;

public record SpeciesColors(
        String faradayHexColor,
        String pmHexColor,
        String faradayModelHexColor,
        String pmModelHexColor) {

        public String get(DetectorPlotFlavor plotFlavor) {
            String result = "";
            switch (plotFlavor) {
                case PM_DATA -> result=pmHexColor;
                case FARADAY_DATA -> result=faradayHexColor;
                case FARADAY_MODEL -> result=faradayModelHexColor;
                case PM_MODEL -> result=pmModelHexColor();
            }
            return result;
        }
}
