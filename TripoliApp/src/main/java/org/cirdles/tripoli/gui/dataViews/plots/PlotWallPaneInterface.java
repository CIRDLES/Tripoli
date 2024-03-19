package org.cirdles.tripoli.gui.dataViews.plots;

public interface PlotWallPaneInterface {
    void buildToolBar();

    void buildScaleControlsToolbar();

    double getToolBarHeight();

    void setToolBarHeight(double toolBarHeight);

    int getToolBarCount();

    void setToolBarCount(int toolBarCount);

    void stackPlots();

    void tilePlots();

    void toggleShowStatsAllPlots();

}