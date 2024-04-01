package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots;

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
}
