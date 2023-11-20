package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots;

import org.cirdles.tripoli.plots.PlotBuilder;

public interface MCMCPlotBuildersTaskInterface {
    boolean healthyPlotbuilder();

    PlotBuilder[] getPeakShapesBuilder();

    PlotBuilder[] getConvergeRatioLineBuilder();

    PlotBuilder[] getConvergeBLFaradayLineBuilder();

    PlotBuilder[] getConvergeErrWeightedMisfitLineBuilder();

    PlotBuilder[] getConvergeErrRawMisfitLineBuilder();

    PlotBuilder[] getConvergeIntensityLinesBuilder();

    String call() throws Exception;

    void receiveLoggingSnippet(String loggingSnippet);

    public int getBlockID();
}