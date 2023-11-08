package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.gui.AnalysisManagerCallbackI;
import org.cirdles.tripoli.gui.dataViews.plots.*;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots.BlockRatioCyclesAnalysisPlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots.SpeciesIntensityAnalysisPlot;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.analysisPlotBuilders.BlockAnalysisRatioCyclesBuilder;
import org.cirdles.tripoli.plots.analysisPlotBuilders.SpeciesIntensityAnalysisBuilder;
import org.cirdles.tripoli.plots.compoundPlotBuilders.BlockRatioCyclesBuilder;
import org.cirdles.tripoli.plots.compoundPlotBuilders.BlockRatioCyclesRecord;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.initializers.AllBlockInitForOGTripoli;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockModelRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockRawDataSetRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputSingleBlockRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.species.IsotopicRatio;

import java.util.*;

import static java.util.Arrays.binarySearch;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotHeight;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotWidth;
import static org.cirdles.tripoli.sessions.analysis.Analysis.SKIP;

public class OGTripoliViewController {
    public static AnalysisInterface analysis;
    public static AnalysisManagerCallbackI analysisManagerCallbackI;
    private AllBlockInitForOGTripoli.PlottingData plottingData;
    @FXML
    private VBox plotWindowVBox;
    @FXML
    private TabPane plotTabPane;
    @FXML
    private AnchorPane ogtSpeciesIntensitiesPlotAnchorPane;
    @FXML
    private AnchorPane ogtCycleRatioPlotsAnchorPane;
    private PlotWallPaneInterface plotsWallPaneRatios;
    private PlotWallPaneInterface plotsWallPaneIntensities;

    public void setPlottingData(AllBlockInitForOGTripoli.PlottingData plottingData) {
        this.plottingData = plottingData;
    }

    @FXML
    public void initialize() {
        plotWindowVBox.widthProperty().addListener((observable, oldValue, newValue) -> {
            plotTabPane.setMinWidth((Double) newValue);
            ogtCycleRatioPlotsAnchorPane.setMinWidth((Double) newValue);
            ogtSpeciesIntensitiesPlotAnchorPane.setMinWidth((Double) newValue);
        });

        plotWindowVBox.heightProperty().addListener((observable, oldValue, newValue) -> {
            plotTabPane.setMinHeight(((Double) newValue) - 30.0);
            ogtCycleRatioPlotsAnchorPane.setMinHeight(((Double) newValue) - plotsWallPaneRatios.getToolBarCount() * plotsWallPaneRatios.getToolBarHeight() - 30.0);
            ogtSpeciesIntensitiesPlotAnchorPane.setMinHeight(((Double) newValue) - plotsWallPaneIntensities.getToolBarCount() * plotsWallPaneIntensities.getToolBarHeight() - 30.0);
        });

        if (null != plottingData) {
            populatePlots();
        }
    }

    public void populatePlots() {
        plotRatios();
        plotOnPeakIntensities();
    }

    public void plotRatios() {
        ogtCycleRatioPlotsAnchorPane.getChildren().clear();

        plotsWallPaneRatios = PlotWallPane.createPlotWallPane("OGTripoliSession", analysis, null, analysisManagerCallbackI);
        plotsWallPaneRatios.setToolBarCount(1);
        plotsWallPaneRatios.setToolBarHeight(35.0);
        PlotWallPane.menuOffset = 0.0;
        ((Pane) plotsWallPaneRatios).setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));

        ((Pane) plotsWallPaneRatios).prefWidthProperty().bind(ogtCycleRatioPlotsAnchorPane.widthProperty());
        ((Pane) plotsWallPaneRatios).prefHeightProperty().bind(ogtCycleRatioPlotsAnchorPane.heightProperty());

        ogtCycleRatioPlotsAnchorPane.getChildren().add(((Pane) plotsWallPaneRatios));
        plotWindowVBox.widthProperty().addListener((observable, oldValue, newValue) -> plotsWallPaneRatios.stackPlots());
        plotWindowVBox.heightProperty().addListener((observable, oldValue, newValue) -> plotsWallPaneRatios.stackPlots());

        SingleBlockModelRecord[] singleBlockModelRecords = plottingData.singleBlockModelRecords();
        int countOfOnPeakCycles = plottingData.cycleCount();

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
                //TODO: need to calculate cycle values

            }
        }

        for (IsotopicRatio isotopicRatio : ratiosToPlot) {
            TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotsWallPaneRatios);

            List<BlockRatioCyclesRecord> blockRatioCyclesRecords = new ArrayList<>();
            for (int blockIndex = 0; blockIndex < singleBlockModelRecords.length; blockIndex++) {
                if (null != singleBlockModelRecords[blockIndex]) {
                    Integer blockID = singleBlockModelRecords[blockIndex].blockID();
                    int blockStatus = analysis.getMapOfBlockIdToProcessStatus().get(blockID);
                    boolean processed = (null != analysis.getMapOfBlockIdToPlots().get(blockID));
                    blockRatioCyclesRecords.add(BlockRatioCyclesBuilder.initializeBlockCycles(
                            blockID,
                            processed,
                            singleBlockModelRecords[blockIndex].detectorFaradayGain(),
                            singleBlockModelRecords[blockIndex].assembleCycleMeansForRatio(isotopicRatio),
                            singleBlockModelRecords[blockIndex].assembleCycleStdDevForRatio(isotopicRatio),
                            DUMMY_CYCLES_INCLUDED,
                            new String[]{isotopicRatio.prettyPrint()},
                            "Blocks & Cycles by Time",
                            "Ratio",
                            true,
                            SKIP != blockStatus).getBlockCyclesRecord());
                } else {
                    blockRatioCyclesRecords.add(null);
                }
            }
            BlockAnalysisRatioCyclesBuilder blockAnalysisRatioCyclesBuilder =
                    BlockAnalysisRatioCyclesBuilder.initializeBlockAnalysisRatioCycles(
                            isotopicRatio, blockRatioCyclesRecords,
                            "Blocks & Cycles by Time", "Ratio");
            AbstractPlot plot = BlockRatioCyclesAnalysisPlot.generatePlot(
                    new Rectangle(minPlotWidth, minPlotHeight), blockAnalysisRatioCyclesBuilder.getBlockAnalysisRatioCyclesRecord(), (PlotWallPane) plotsWallPaneRatios);

            tripoliPlotPane.addPlot(plot);
            plot.refreshPanel(false, false);

        }
        plotsWallPaneRatios.buildToolBar();
        plotsWallPaneRatios.buildScaleControlsToolbar();
        plotsWallPaneRatios.stackPlots();
    }

    private void plotOnPeakIntensities() {
        ogtSpeciesIntensitiesPlotAnchorPane.getChildren().clear();
        plotsWallPaneIntensities = PlotWallPaneOGTripoli.createPlotWallPane("OGTripoliSession");
        PlotWallPane.menuOffset = 0.0;
        plotsWallPaneIntensities.setToolBarCount(2);
        plotsWallPaneIntensities.setToolBarHeight(35.0);
        ((Pane) plotsWallPaneIntensities).setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));

        ((Pane) plotsWallPaneIntensities).prefWidthProperty().bind(ogtSpeciesIntensitiesPlotAnchorPane.widthProperty());
        ((Pane) plotsWallPaneIntensities).prefHeightProperty().bind(ogtSpeciesIntensitiesPlotAnchorPane.heightProperty());

        ogtSpeciesIntensitiesPlotAnchorPane.getChildren().add(((Pane) plotsWallPaneIntensities));

        plotWindowVBox.widthProperty().addListener((observable, oldValue, newValue) -> plotsWallPaneIntensities.stackPlots());
        plotWindowVBox.heightProperty().addListener((observable, oldValue, newValue) -> plotsWallPaneIntensities.stackPlots());

        SingleBlockRawDataSetRecord[] singleBlockRawDataSetRecords = plottingData.singleBlockRawDataSetRecords();
        SingleBlockModelRecord[] singleBlockModelRecords = plottingData.singleBlockModelRecords();
        // only plotting onPeaks
        int countOfBlocks = singleBlockRawDataSetRecords.length;
        int countOfSpecies = analysis.getAnalysisMethod().getSpeciesList().size();

        // x-axis = time
        double[] xAxis = analysis.getMassSpecExtractedData().calculateSessionTimes();
        int[] xAxisBlockIDs = analysis.getMassSpecExtractedData().assignBlockIdToSessionTime();

        // alternating faraday, model, pm, and model rows (4-tuple for each species)

        double[][] onPeakDataCounts = new double[countOfSpecies * 4][xAxis.length];
        boolean[][] onPeakDataIncludedAllBlocks = new boolean[countOfSpecies][xAxis.length];
        double[][] onPeakDataAmpResistance = new double[countOfSpecies][xAxis.length];
        double[][] onPeakBaseline = new double[countOfSpecies * 4][xAxis.length];
        double[][] onPeakGain = new double[countOfSpecies * 4][xAxis.length];

        Set<Detector> detectors = analysis.getAnalysisMethod().getSequenceTable().getMapOfDetectorsToSequenceCells().keySet();
        Map<Integer, Double> mapOfOrdinalDetectorsToResistance = new TreeMap<>();
        for (Detector detector : detectors) {
            mapOfOrdinalDetectorsToResistance.put(detector.getOrdinalIndex(), detector.getAmplifierResistanceInOhms());
        }

        Map<Integer, MassSpecOutputSingleBlockRecord> blocksData = analysis.getMassSpecExtractedData().getBlocksData();
        for (int blockIndex = 0; blockIndex < countOfBlocks; blockIndex++) {

            if (null != singleBlockModelRecords[blockIndex]) {
                Integer blockID = singleBlockModelRecords[blockIndex].blockID();
                double[] onPeakTimeStamps = blocksData.get(blockID).onPeakTimeStamps();

                SingleBlockModelRecord singleBlockModelRecord = singleBlockModelRecords[blockIndex];
                int countOfBaselineDataEntries = singleBlockRawDataSetRecords[blockIndex].getCountOfBaselineIntensities();
                int countOfFaradayDataEntries = singleBlockRawDataSetRecords[blockIndex].getCountOfOnPeakFaradayIntensities();
                boolean[][] intensityIncludedAccumulatorArray = ((Analysis) analysis).getMapOfBlockIdToIncludedPeakData().get(blockID);

                double[] onPeakModelFaradayData = singleBlockModelRecord.getOnPeakDataModelFaradayArray(countOfBaselineDataEntries, countOfFaradayDataEntries);
                double[] baseLineVector = singleBlockModelRecord.baselineMeansArray();
                double dfGain = singleBlockModelRecord.detectorFaradayGain();
                Map<Integer, Integer> mapDetectorOrdinalToFaradayIndex = singleBlockModelRecord.mapDetectorOrdinalToFaradayIndex();

                SingleBlockRawDataSetRecord.SingleBlockRawDataRecord onPeakFaradayDataSet = singleBlockRawDataSetRecords[blockIndex].onPeakFaradayDataSetMCMC();
                List<Double> intensityAccumulatorList = onPeakFaradayDataSet.intensityAccumulatorList();
                List<Integer> timeIndexAccumulatorList = onPeakFaradayDataSet.timeIndexAccumulatorList();
                List<Integer> isotopeOrdinalIndexAccumulatorList = onPeakFaradayDataSet.isotopeOrdinalIndicesAccumulatorList();
                List<Integer> detectorOrdinalIndicesAccumulatorList = onPeakFaradayDataSet.detectorOrdinalIndicesAccumulatorList();

                for (int onPeakDataIndex = 0; onPeakDataIndex < intensityAccumulatorList.size(); onPeakDataIndex++) {
                    int timeIndex = timeIndexAccumulatorList.get(onPeakDataIndex);
                    double time = onPeakTimeStamps[timeIndex];
                    int intensitySpeciesIndex = isotopeOrdinalIndexAccumulatorList.get(onPeakDataIndex) - 1;
                    int timeIndx = binarySearch(xAxis, time);
                    onPeakDataCounts[intensitySpeciesIndex * 4][timeIndx] = intensityAccumulatorList.get(onPeakDataIndex);
                    onPeakDataCounts[intensitySpeciesIndex * 4 + 1][timeIndx] = onPeakModelFaradayData[onPeakDataIndex];
                    onPeakDataAmpResistance[intensitySpeciesIndex][timeIndx] = mapOfOrdinalDetectorsToResistance.get(detectorOrdinalIndicesAccumulatorList.get(onPeakDataIndex));
                    onPeakBaseline[intensitySpeciesIndex * 4][timeIndx] = baseLineVector[mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndicesAccumulatorList.get(onPeakDataIndex))];
                    onPeakBaseline[intensitySpeciesIndex * 4 + 1][timeIndx] = baseLineVector[mapDetectorOrdinalToFaradayIndex.get(detectorOrdinalIndicesAccumulatorList.get(onPeakDataIndex))];
                    onPeakDataIncludedAllBlocks[intensitySpeciesIndex][timeIndx] = intensityIncludedAccumulatorArray[intensitySpeciesIndex][timeIndex];
                }

                double[] onPeakModelPhotoMultiplierData = singleBlockModelRecord.getOnPeakDataModelPhotoMultiplierArray(countOfBaselineDataEntries, countOfFaradayDataEntries);

                SingleBlockRawDataSetRecord.SingleBlockRawDataRecord onPeakPhotoMultiplierDataSet = singleBlockRawDataSetRecords[blockIndex].onPeakPhotoMultiplierDataSetMCMC();
                intensityAccumulatorList = onPeakPhotoMultiplierDataSet.intensityAccumulatorList();
                timeIndexAccumulatorList = onPeakPhotoMultiplierDataSet.timeIndexAccumulatorList();
                isotopeOrdinalIndexAccumulatorList = onPeakPhotoMultiplierDataSet.isotopeOrdinalIndicesAccumulatorList();
                detectorOrdinalIndicesAccumulatorList = onPeakPhotoMultiplierDataSet.detectorOrdinalIndicesAccumulatorList();

                for (int onPeakDataIndex = 0; onPeakDataIndex < intensityAccumulatorList.size(); onPeakDataIndex++) {
                    int timeIndex = timeIndexAccumulatorList.get(onPeakDataIndex);
                    double time = onPeakTimeStamps[timeIndex];
                    int intensitySpeciesIndex = isotopeOrdinalIndexAccumulatorList.get(onPeakDataIndex) - 1;
                    int timeIndx = binarySearch(xAxis, time);
                    onPeakDataCounts[intensitySpeciesIndex * 4 + 2][timeIndx] = intensityAccumulatorList.get(onPeakDataIndex);
                    onPeakDataCounts[intensitySpeciesIndex * 4 + 3][timeIndx] = onPeakModelPhotoMultiplierData[onPeakDataIndex];
                    onPeakDataAmpResistance[intensitySpeciesIndex][timeIndx] = mapOfOrdinalDetectorsToResistance.get(detectorOrdinalIndicesAccumulatorList.get(onPeakDataIndex));
                    //TODO: address this: onPeakBaseline is  zero for PM for now
                    onPeakGain[intensitySpeciesIndex * 4 + 2][timeIndx] = dfGain;
                    onPeakGain[intensitySpeciesIndex * 4 + 3][timeIndx] = dfGain;
                    onPeakDataIncludedAllBlocks[intensitySpeciesIndex][timeIndx] = intensityIncludedAccumulatorArray[intensitySpeciesIndex][timeIndex];
                }
            }
        }

        // accumulate intensity statistics
        DescriptiveStatistics[] intensityStatistics = new DescriptiveStatistics[countOfSpecies];
        for (int speciesIndex = 0; speciesIndex < countOfSpecies; speciesIndex++) {
            intensityStatistics[speciesIndex] = new DescriptiveStatistics();
        }
        for (int speciesIndex = 0; speciesIndex < countOfSpecies; speciesIndex++) {
            for (int xIndex = 0; xIndex < xAxis.length; xIndex++) {
                if (0.0 != onPeakDataCounts[speciesIndex * 4][xIndex]) {
                    intensityStatistics[speciesIndex].addValue(onPeakDataCounts[speciesIndex * 4][xIndex] - onPeakBaseline[speciesIndex * 4][xIndex]);
                }
                if (0.0 != onPeakDataCounts[speciesIndex * 4 + 2][xIndex]) {
                    intensityStatistics[speciesIndex].addValue(onPeakDataCounts[speciesIndex * 4 + 2][xIndex] / onPeakGain[speciesIndex * 4 + 2][xIndex]);
                }
            }
        }

        ((Analysis) analysis).setAnalysisSpeciesStats(intensityStatistics);

        PlotBuilder plotBuilder = SpeciesIntensityAnalysisBuilder.initializeSpeciesIntensityAnalysisPlot(
                analysis, xAxis, onPeakDataIncludedAllBlocks, xAxisBlockIDs, onPeakDataCounts, onPeakDataAmpResistance, onPeakBaseline, onPeakGain,
                new String[]{"Species Intensity by Analysis"}, "Time (secs)", "Intensity (counts)");

        TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotsWallPaneIntensities);
        AbstractPlot plot = SpeciesIntensityAnalysisPlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), (SpeciesIntensityAnalysisBuilder) plotBuilder);

        tripoliPlotPane.addPlot(plot);
        plot.refreshPanel(false, false);

        ((PlotWallPaneOGTripoli) plotsWallPaneIntensities).buildOGTripoliToolBar(analysis.getAnalysisMethod().getSpeciesList());
        plotsWallPaneIntensities.buildScaleControlsToolbar();
        plotsWallPaneIntensities.stackPlots();
    }
}