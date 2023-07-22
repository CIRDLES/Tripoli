package org.cirdles.tripoli.gui;

//import com.google.common.collect.BiMap;
//import com.google.common.collect.HashBiMap;
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
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.AllBlockInitForOGTripoli;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockModelRecord;
import org.cirdles.tripoli.species.IsotopicRatio;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotHeight;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotWidth;
import static org.cirdles.tripoli.sessions.analysis.Analysis.SKIP;

public class OGTripoliViewController {
    public static AnalysisInterface analysis;
    @FXML
    public AnchorPane ogTripoliIntensitiesPlotPane;

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

        SingleBlockModelRecord[] singleBlockModelRecords = AllBlockInitForOGTripoli.initBlockModels(analysis);
        int countOfOnPeakCycles = singleBlockModelRecords[0].cycleCount();
        List<IsotopicRatio> isotopicRatioList = analysis.getAnalysisMethod().getIsotopicRatiosList();

        boolean[] DUMMY_CYCLES_INCLUDED = new boolean[countOfOnPeakCycles];
        Arrays.fill(DUMMY_CYCLES_INCLUDED, true);


        // build list of ratios to plot
        List<IsotopicRatio> ratiosToPlot = new ArrayList<>();
        for (IsotopicRatio isotopicRatio : analysis.getAnalysisMethod().getIsotopicRatiosList()){
            if (isotopicRatio.isDisplayed()){
                ratiosToPlot.add(isotopicRatio);
            }
        }
        for (IsotopicRatio isotopicRatio : analysis.getAnalysisMethod().getDerivedIsotopicRatiosList()){
            if (isotopicRatio.isDisplayed()){
                ratiosToPlot.add(isotopicRatio);
                // need to calculate cycle values

            }
        }

        for (IsotopicRatio isotopicRatio : ratiosToPlot){
            TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotsWallPane);
            List<BlockRatioCyclesRecord> blockRatioCyclesRecords = new ArrayList<>();
            for (int blockIndex = 0; blockIndex < singleBlockModelRecords.length; blockIndex++) {
                int blockStatus = analysis.getMapOfBlockIdToProcessStatus().get(blockIndex + 1);
                blockRatioCyclesRecords.add(BlockRatioCyclesBuilder.initializeBlockCycles(
                        blockIndex + 1,
                        singleBlockModelRecords[blockIndex].assembleCycleMeansForRatio(isotopicRatio),
                        singleBlockModelRecords[blockIndex].assembleCycleStdDevForRatio(isotopicRatio),
                        DUMMY_CYCLES_INCLUDED,
                        new String[]{isotopicRatio.prettyPrint()},
                        "Blocks & Cycles by Time",
                        "Ratio",
                        true,
                        blockStatus != SKIP).getBlockCyclesRecord());
            }

            analysis.getAnalysisMethod().getBiMapOfRatiosAndInverses();//temp

            BlockRatioCyclesSessionBuilder blockRatioCyclesSessionBuilder =
                    BlockRatioCyclesSessionBuilder.initializeBlockRatioCyclesSession(
                            blockRatioCyclesRecords, new String[]{isotopicRatio.prettyPrint()},
                            "Blocks & Cycles by Time", "Ratio");
            AbstractPlot plot = BlockRatioCyclesSessionPlot.generatePlot(
                    new Rectangle(minPlotWidth, minPlotHeight), blockRatioCyclesSessionBuilder.getBlockRatioCyclesSessionRecord());
            tripoliPlotPane.addPlot(plot);
        }
        plotsWallPane.stackPlots();
    }
}