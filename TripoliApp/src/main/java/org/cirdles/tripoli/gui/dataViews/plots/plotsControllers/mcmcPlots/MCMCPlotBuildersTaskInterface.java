package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots;

import org.cirdles.tripoli.plots.PlotBuilder;

public interface MCMCPlotBuildersTaskInterface {
    boolean healthyPlotbuilder();

    PlotBuilder[] getPeakShapesBuilder();

    PlotBuilder[] getRatiosHistogramBuilder();

    PlotBuilder[] getBaselineHistogramBuilder();

    PlotBuilder[] getDalyFaradayGainHistogramBuilder();

//    PlotBuilder[] getSignalNoiseHistogramBuilder();

    PlotBuilder[] getMeanIntensityVsKnotsMultiLineBuilder();

    PlotBuilder[] getConvergeRatioLineBuilder();

    PlotBuilder[] getObservedDataLineBuilder();

    PlotBuilder[] getResidualDataLineBuilder();

    PlotBuilder[] getConvergeBLFaradayLineBuilder();

    PlotBuilder[] getConvergeErrWeightedMisfitLineBuilder();

    PlotBuilder[] getConvergeErrRawMisfitLineBuilder();

    PlotBuilder[] getConvergeIntensityLinesBuilder();

//    PlotBuilder[] getConvergeNoiseFaradayLineBuilder();

    PlotBuilder[] getObservedDataWithSubsetsLineBuilder();

    String call() throws Exception;

    void receiveLoggingSnippet(String loggingSnippet);

    public int getBlockID();
}