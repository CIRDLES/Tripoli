/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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