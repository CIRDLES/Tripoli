package org.cirdles.tripoli.gui.dataViews.plots;

import org.cirdles.tripoli.utilities.DelegateActionInterface;

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

    void repeatLayoutStyle();

    void updateStatusOfCycleCheckBox();

    void toggleSculptingMode();

    void addRepaintDelegateAction(DelegateActionInterface delegateAction);
    void removeRepaintDelegateAction(DelegateActionInterface delegateAction);
}