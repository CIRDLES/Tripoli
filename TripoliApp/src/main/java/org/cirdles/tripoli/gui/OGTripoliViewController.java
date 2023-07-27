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
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.sessionPlots.SpeciesIntensitySessionPlot;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.compoundPlots.BlockRatioCyclesBuilder;
import org.cirdles.tripoli.plots.compoundPlots.BlockRatioCyclesRecord;
import org.cirdles.tripoli.plots.sessionPlots.BlockRatioCyclesSessionBuilder;
import org.cirdles.tripoli.plots.sessionPlots.SpeciesIntensitySessionBuilder;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.AllBlockInitForOGTripoli;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockDataSetRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockModelRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputSingleBlockRecord;
import org.cirdles.tripoli.species.IsotopicRatio;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.binarySearch;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotHeight;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotWidth;
import static org.cirdles.tripoli.sessions.analysis.Analysis.SKIP;

public class OGTripoliViewController {
    public static AnalysisInterface analysis;
    @FXML
    public AnchorPane ogtSpeciesIntensitiesPlotAnchorPane;

    @FXML
    private AnchorPane ogtCycleRatioPlotsAnchorPane;

    private AllBlockInitForOGTripoli.PlottingData plottingData;

    @FXML
    void initialize() throws TripoliException {
        populatePlots();
    }

    private void populatePlots() throws TripoliException {
        plottingData = AllBlockInitForOGTripoli.initBlockModels(analysis);
        plotRatios();
        plotOnPeakIntensities();
    }

    private void plotRatios() {
        ogtCycleRatioPlotsAnchorPane.getChildren().clear();

        PlotWallPane plotsWallPane = PlotWallPane.createPlotWallPane("OGTripoliSession");
        PlotWallPane.menuOffset = 0.0;
        plotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
        plotsWallPane.setPrefSize(ogtCycleRatioPlotsAnchorPane.getPrefWidth(), ogtCycleRatioPlotsAnchorPane.getPrefHeight());
        ogtCycleRatioPlotsAnchorPane.getChildren().add(plotsWallPane);

        SingleBlockModelRecord[] singleBlockModelRecords = plottingData.singleBlockModelRecords();
        int countOfOnPeakCycles = singleBlockModelRecords[0].cycleCount();

        boolean[] DUMMY_CYCLES_INCLUDED = new boolean[countOfOnPeakCycles];
        Arrays.fill(DUMMY_CYCLES_INCLUDED, true);

        // build list of ratios to plot
        List<IsotopicRatio> ratiosToPlot = new ArrayList<>();
        for (IsotopicRatio isotopicRatio : analysis.getAnalysisMethod().getIsotopicRatiosList()) {
            if (isotopicRatio.isDisplayed()) {
                ratiosToPlot.add(isotopicRatio);
            }
        }
        for (IsotopicRatio isotopicRatio : analysis.getAnalysisMethod().getDerivedIsotopicRatiosList()) {
            if (isotopicRatio.isDisplayed()) {
                ratiosToPlot.add(isotopicRatio);
                // need to calculate cycle values

            }
        }

        for (IsotopicRatio isotopicRatio : ratiosToPlot) {
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

    private void plotOnPeakIntensities() {
        ogtSpeciesIntensitiesPlotAnchorPane.getChildren().clear();
        PlotWallPane plotsWallPane = PlotWallPane.createPlotWallPane("OGTripoliSession");
        PlotWallPane.menuOffset = 0.0;
        plotsWallPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));
        plotsWallPane.setPrefSize(ogtSpeciesIntensitiesPlotAnchorPane.getPrefWidth(), ogtSpeciesIntensitiesPlotAnchorPane.getPrefHeight());
        ogtSpeciesIntensitiesPlotAnchorPane.getChildren().add(plotsWallPane);

        SingleBlockDataSetRecord[] singleBlockDataSetRecords = plottingData.singleBlockDataSetRecords();
        SingleBlockModelRecord[] singleBlockModelRecords = plottingData.singleBlockModelRecords();
        // only plotting onPeaks
        int countOfBlocks = singleBlockDataSetRecords.length;
        int countOfSpecies = analysis.getAnalysisMethod().getSpeciesList().size();

        // x-axis = time
        double[] xAxis = analysis.getMassSpecExtractedData().calculateSessionTimes();

        // alternating faraday, model, pm, and model rows (4-tuple for each species)
        // last line will carry block boundaries
        double[][] onPeakData = new double[countOfSpecies * 4][xAxis.length];
        Map<Integer, MassSpecOutputSingleBlockRecord> blocksData = analysis.getMassSpecExtractedData().getBlocksData();
        for (int blockIndex = 0; blockIndex < countOfBlocks; blockIndex++) {

            double[] onPeakTimeStamps = blocksData.get(blockIndex + 1).onPeakTimeStamps();

            SingleBlockModelRecord singleBlockModelRecord = singleBlockModelRecords[blockIndex];
            int countOfBaselineDataEntries = singleBlockDataSetRecords[blockIndex].getCountOfBaselineIntensities();
            int countOfFaradayDataEntries = singleBlockDataSetRecords[blockIndex].getCountOfOnPeakFaradayIntensities();
            double[] onPeakModelFaradayData = singleBlockModelRecord.getOnPeakDataModelFaradayArray(countOfBaselineDataEntries, countOfFaradayDataEntries);

            SingleBlockDataSetRecord.SingleBlockDataRecord onPeakFaradayDataSet = singleBlockDataSetRecords[blockIndex].onPeakFaradayDataSetMCMC();
            List<Double> intensityAccumulatorList = onPeakFaradayDataSet.intensityAccumulatorList();
            List<Integer> timeIndexAccumulatorList = onPeakFaradayDataSet.timeIndexAccumulatorList();
            List<Integer> isotopeOrdinalIndexAccumulatorList = onPeakFaradayDataSet.isotopeOrdinalIndicesAccumulatorList();

            for (int onPeakDataIndex = 0; onPeakDataIndex < intensityAccumulatorList.size(); onPeakDataIndex++) {
                int timeIndex = timeIndexAccumulatorList.get(onPeakDataIndex);
                double time = onPeakTimeStamps[timeIndex];
                int intensitySpeciesIndex = isotopeOrdinalIndexAccumulatorList.get(onPeakDataIndex) - 1;
                int timeIndx = binarySearch(xAxis, time);
                onPeakData[intensitySpeciesIndex * 4][timeIndx] = intensityAccumulatorList.get(onPeakDataIndex);
                onPeakData[intensitySpeciesIndex * 4 + 1][timeIndx] = onPeakModelFaradayData[onPeakDataIndex];
            }

            double[] onPeakModelPhotoMultiplierData = singleBlockModelRecord.getOnPeakDataModelPhotoMultiplierArray(countOfBaselineDataEntries, countOfFaradayDataEntries);

            SingleBlockDataSetRecord.SingleBlockDataRecord onPeakPhotoMultiplierDataSet = singleBlockDataSetRecords[blockIndex].onPeakPhotoMultiplierDataSetMCMC();
            intensityAccumulatorList = onPeakPhotoMultiplierDataSet.intensityAccumulatorList();
            timeIndexAccumulatorList = onPeakPhotoMultiplierDataSet.timeIndexAccumulatorList();
            isotopeOrdinalIndexAccumulatorList = onPeakPhotoMultiplierDataSet.isotopeOrdinalIndicesAccumulatorList();

            for (int onPeakDataIndex = 0; onPeakDataIndex < intensityAccumulatorList.size(); onPeakDataIndex++) {
                int timeIndex = timeIndexAccumulatorList.get(onPeakDataIndex);
                double time = onPeakTimeStamps[timeIndex];
                int intensitySpeciesIndex = isotopeOrdinalIndexAccumulatorList.get(onPeakDataIndex) - 1;
                int timeIndx = binarySearch(xAxis, time);
                onPeakData[intensitySpeciesIndex * 4 + 2][timeIndx] = intensityAccumulatorList.get(onPeakDataIndex);
                onPeakData[intensitySpeciesIndex * 4 + 3][timeIndx] = onPeakModelPhotoMultiplierData[onPeakDataIndex];
            }
        }

        PlotBuilder plotBuilder = SpeciesIntensitySessionBuilder.initializeSpeciesIntensitySessionPlot(
                xAxis, onPeakData, new String[]{"Species Intensity by Session"}, "Time", "Intensity (counts)");

        TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotsWallPane);
        AbstractPlot plot = SpeciesIntensitySessionPlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), (SpeciesIntensitySessionBuilder) plotBuilder);
        tripoliPlotPane.addPlot(plot);
        plotsWallPane.buildOGTripoliToolBar(analysis.getAnalysisMethod().getSpeciesList());
        plotsWallPane.stackPlots();
    }
}