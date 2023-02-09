package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmc2Plots;

import org.cirdles.tripoli.plots.AbstractPlotBuilder;

public interface PlotBuildersTaskInterface {
    AbstractPlotBuilder[] getRatiosHistogramBuilder();

    AbstractPlotBuilder[] getBaselineHistogramBuilder();

    AbstractPlotBuilder[] getDalyFaradayGainHistogramBuilder();

    AbstractPlotBuilder[] getSignalNoiseHistogramBuilder();

    AbstractPlotBuilder[] getMeanIntensityLineBuilder();

    AbstractPlotBuilder[] getConvergeRatioLineBuilder();

    AbstractPlotBuilder getObservedDataLineBuilder();

    AbstractPlotBuilder getResidualDataLineBuilder();

    AbstractPlotBuilder[] getConvergeBLFaradayLineBuilder();

    AbstractPlotBuilder[] getConvergeErrWeightedMisfitLineBuilder();

    AbstractPlotBuilder[] getConvergeErrRawMisfitLineBuilder();

    AbstractPlotBuilder getConvergeIntensityLinesBuilder();

    AbstractPlotBuilder[] getConvergeNoiseFaradayLineBuilder();

    String call() throws Exception;

    void receiveLoggingSnippet(String loggingSnippet);
}