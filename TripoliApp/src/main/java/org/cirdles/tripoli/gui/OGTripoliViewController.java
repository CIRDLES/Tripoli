package org.cirdles.tripoli.gui;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPane;
import org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.sessionPlots.BlockRatioCyclesSessionPlot;
import org.cirdles.tripoli.plots.compoundPlots.BlockRatioCyclesBuilder;
import org.cirdles.tripoli.plots.compoundPlots.BlockRatioCyclesRecord;
import org.cirdles.tripoli.plots.sessionPlots.BlockRatioCyclesSessionBuilder;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.AllBlockInitForMCMC;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockModelRecord;
import org.cirdles.tripoli.species.IsotopicRatio;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotHeight;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotWidth;

public class OGTripoliViewController {
    public static AnalysisInterface analysis;

    @FXML
    private AnchorPane ogTripoliPlotsAnchorPane;

    @FXML
    void initialize() throws TripoliException {
        populatePlots();
    }

    private void populatePlots() throws TripoliException {
        ogTripoliPlotsAnchorPane.getChildren().clear();

        PlotWallPane plotsWallPane = PlotWallPane.createPlotWallPane("OGTripoli");
        PlotWallPane.menuOffset = 0.0;
        plotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
        plotsWallPane.setPrefSize(ogTripoliPlotsAnchorPane.getPrefWidth(), ogTripoliPlotsAnchorPane.getPrefHeight());
        ogTripoliPlotsAnchorPane.getChildren().add(plotsWallPane);

        SingleBlockModelRecord[] singleBlockModelRecords = AllBlockInitForMCMC.initBlockModels(analysis);
        int countOfBlocks = singleBlockModelRecords.length;
        int countOfOnPeakCycles = singleBlockModelRecords[0].mapLogRatiosToCycleStats().get(0).keySet().size();
        List<IsotopicRatio> isotopicRatioList = analysis.getAnalysisMethod().getIsotopicRatiosList();
        boolean[] DUMMY_CYCLES_INCLUDED = new boolean[countOfOnPeakCycles];
        Arrays.fill(DUMMY_CYCLES_INCLUDED, true);
        for (int logRatioIndex = 0; logRatioIndex < singleBlockModelRecords[0].logRatios().length; logRatioIndex++) {
            TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotsWallPane);
            List<BlockRatioCyclesRecord> blockRatioCyclesRecords = new ArrayList<>();
            for (int blockIndex = 0; blockIndex < singleBlockModelRecords.length; blockIndex++) {
                blockRatioCyclesRecords.add(BlockRatioCyclesBuilder.initializeBlockCycles(
                        blockIndex + 1,
                        singleBlockModelRecords[blockIndex].assembleCycleMeansForLogRatio(logRatioIndex),
                        singleBlockModelRecords[blockIndex].assembleCycleStdDevForLogRatio(logRatioIndex),
                        DUMMY_CYCLES_INCLUDED,
                        new String[]{isotopicRatioList.get(logRatioIndex).prettyPrint()},
                        "TIME",
                        "LOGRATIO",
                        true).getBlockCyclesRecord());
            }
            BlockRatioCyclesSessionBuilder blockRatioCyclesSessionBuilder =
                    BlockRatioCyclesSessionBuilder.initializeBlockRatioCyclesSession(
                            blockRatioCyclesRecords, new String[]{isotopicRatioList.get(logRatioIndex).prettyPrint()},
                            "TIME", "LOGRATIO");
            AbstractPlot plot = BlockRatioCyclesSessionPlot.generatePlot(
                    new Rectangle(minPlotWidth, minPlotHeight), blockRatioCyclesSessionBuilder.getBlockRatioCyclesSessionRecord());
            tripoliPlotPane.addPlot(plot);
        }
        plotsWallPane.stackPlots();
    }
}