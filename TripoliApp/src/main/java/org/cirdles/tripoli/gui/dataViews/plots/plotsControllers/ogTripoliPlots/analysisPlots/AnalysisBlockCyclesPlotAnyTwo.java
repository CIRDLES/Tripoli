package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots;

import javafx.scene.control.Tooltip;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.OGTripoliPlotsWindow;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.OGTripoliViewController;
import org.cirdles.tripoli.plots.compoundPlotBuilders.PlotBlockCyclesRecord;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotHeight;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotWidth;

public class AnalysisBlockCyclesPlotAnyTwo {

    ArrayList<UserFunction> twoUserFunctions;

    private AnalysisBlockCyclesPlotAnyTwo(List<UserFunction> userFunctions){
        twoUserFunctions = new ArrayList<>(userFunctions);
    }

}
