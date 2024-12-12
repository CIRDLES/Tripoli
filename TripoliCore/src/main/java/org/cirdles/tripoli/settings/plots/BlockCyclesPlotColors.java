package org.cirdles.tripoli.settings.plots;

import java.io.Serializable;

public record BlockCyclesPlotColors  (
        String oneSigmaShade,
        String twoSigmaShade,
        String twoStdErrShade,
        String meanColor,
        String dataColor
) implements Serializable {



}
