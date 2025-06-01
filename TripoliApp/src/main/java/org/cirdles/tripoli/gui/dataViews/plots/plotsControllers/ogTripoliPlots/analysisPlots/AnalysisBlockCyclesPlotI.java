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

package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots;

import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.plots.analysisPlotBuilders.AnalysisBlockCyclesRecord;
import org.cirdles.tripoli.plots.compoundPlotBuilders.PlotBlockCyclesRecord;

import java.util.Map;

public interface AnalysisBlockCyclesPlotI {
    public boolean getBlockMode();

    public void setBlockMode(boolean blockMode);

    void setLogScale(boolean logScale);

    Object getParentWallPane();

    void resetData();

    void adjustMouseStartsForPress(double x, double y);

    Map<Integer, PlotBlockCyclesRecord> getMapBlockIdToBlockCyclesRecord();

    AnalysisBlockCyclesRecord getAnalysisBlockCyclesRecord();

    void repaint();

    void adjustZoomSelf();

    void setZoomChunkX(double zoomChunkX);

    void setZoomChunkY(double zoomChunkY);

    void adjustZoom();

    void adjustOffsetsForDrag(double x, double y);

    void setZoomFlagsXY(boolean[] zoomFlagsXY);

    void performChauvenets();

    boolean detectAllIncludedStatus();

    UserFunction getUserFunction();

    boolean isIgnoreRejects();

    void setIgnoreRejects(boolean b);

    void refreshPanel(boolean reScaleX, boolean reScaleY);
}
