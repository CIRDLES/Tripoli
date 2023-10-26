package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.mcmcPlots;

import org.cirdles.tripoli.plots.PlotBuilder;

public interface MCMCPlotsControllerInterface {
    public void plotEnsemblesEngine(PlotBuilder[][] plotBuilders);

    public void plotRatioSessionEngine();

    public int getCurrentBlockID();
}