package org.cirdles.tripoli.settings.plots;

import static org.cirdles.tripoli.constants.TripoliConstants.BlockCyclesPlotColorFlavor;

import java.io.Serializable;


public record BlockCyclesPlotColors  (
        String oneSigmaShade,
        String twoSigmaShade,
        String twoStdErrShade,
        String meanColor,
        String dataColor
) implements Serializable {

    public String get(BlockCyclesPlotColorFlavor plotColorFlavor) {
        StringBuilder result = new StringBuilder();
        switch (plotColorFlavor) {
            case ONE_SIGMA_SHADE -> result.append(oneSigmaShade);
            case TWO_SIGMA_SHADE -> result.append(twoSigmaShade);
            case TWO_STD_ERR_SHADE -> result.append(twoStdErrShade);
            case MEAN_COLOR -> result.append(meanColor);
            case DATA_COLOR -> result.append(dataColor);
        }
        return result.toString();
    }

    public BlockCyclesPlotColors altered(BlockCyclesPlotColorFlavor flavor, String hexColor) {
        String oneSigmaShade = oneSigmaShade();
        String twoSigmaShade = twoSigmaShade();
        String twoStdErrShade = twoStdErrShade();
        String meanColor = meanColor();
        String dataColor = dataColor();
        switch (flavor) {
            case ONE_SIGMA_SHADE -> oneSigmaShade = hexColor;
            case TWO_SIGMA_SHADE -> twoSigmaShade = hexColor;
            case TWO_STD_ERR_SHADE -> twoStdErrShade = hexColor;
            case MEAN_COLOR -> meanColor = hexColor;
            case DATA_COLOR -> dataColor = hexColor;
        }
        return new BlockCyclesPlotColors(
                oneSigmaShade,
                twoSigmaShade,
                twoStdErrShade,
                meanColor,
                dataColor
        );
    }


}
