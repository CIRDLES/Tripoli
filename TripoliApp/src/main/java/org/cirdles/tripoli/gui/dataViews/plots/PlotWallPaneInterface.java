package org.cirdles.tripoli.gui.dataViews.plots;

public interface PlotWallPaneInterface {
    public void buildToolBar();

    public void buildScaleControlsToolbar();

    public double getToolBarHeight();

    public void setToolBarHeight(double toolBarHeight);

    public int getToolBarCount();

    public void setToolBarCount(int toolBarCount);

    public void stackPlots();

    public void tilePlots();

    public void toggleShowStatsAllPlots();

    void repeatLayoutStyle();
}