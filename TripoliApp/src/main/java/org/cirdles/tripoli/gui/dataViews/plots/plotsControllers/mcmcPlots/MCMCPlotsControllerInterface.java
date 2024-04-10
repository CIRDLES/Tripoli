package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots;

import org.cirdles.tripoli.plots.PlotBuilder;

public interface MCMCPlotsControllerInterface {
    void plotEnsemblesEngine(PlotBuilder[][] plotBuilders);

    void plotAnalysisRatioEngine();

    int getCurrentBlockID();
}