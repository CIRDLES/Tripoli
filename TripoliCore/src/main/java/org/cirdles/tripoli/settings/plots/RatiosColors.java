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

    public String get(RatiosPlotColorFlavor plotColorFlavor) {
        StringBuilder result = new StringBuilder();
        switch (plotColorFlavor) {
            case ONE_SIGMA_SHADE -> result.append(oneSigmaShade);
            case TWO_SIGMA_SHADE -> result.append(twoSigmaShade);
            case TWO_STD_ERR_SHADE -> result.append(twoStdErrShade);
            case MEAN_COLOR -> result.append(meanColor);
            case DATA_COLOR -> result.append(dataColor);
            case ANTI_DATA_COLOR -> result.append(dataAntiColor);
        }
        return result.toString();
    }

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
            case ANTI_DATA_COLOR -> dataAntiColor = hexColor;
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
