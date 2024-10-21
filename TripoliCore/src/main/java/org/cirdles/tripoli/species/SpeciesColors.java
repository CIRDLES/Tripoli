package org.cirdles.tripoli.species;

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
            case PM_MODEL -> result = pmModelHexColor();
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
