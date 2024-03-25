package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.expressions.species.IsotopicRatio;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.gui.AnalysisManagerCallbackI;
import org.cirdles.tripoli.gui.dataViews.plots.*;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots.AnalysisBlockCyclesPlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots.AnalysisBlockCyclesPlotOG;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots.SpeciesIntensityAnalysisPlot;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.analysisPlotBuilders.BlockAnalysisRatioCyclesBuilder;
import org.cirdles.tripoli.plots.analysisPlotBuilders.SpeciesIntensityAnalysisBuilder;
import org.cirdles.tripoli.plots.compoundPlotBuilders.BlockCyclesBuilder;
import org.cirdles.tripoli.plots.compoundPlotBuilders.PlotBlockCyclesRecord;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.SingleBlockRawDataLiteSetRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockModelRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.SingleBlockRawDataSetRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.initializers.AllBlockInitForMCMC;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputBlockRecordFull;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;

import java.util.*;

import static java.util.Arrays.binarySearch;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotHeight;
import static org.cirdles.tripoli.gui.dataViews.plots.TripoliPlotPane.minPlotWidth;
import static org.cirdles.tripoli.gui.utilities.UIUtilities.showTab;
import static org.cirdles.tripoli.sessions.analysis.Analysis.SKIP;

public class OGTripoliViewController {
    public static AnalysisInterface analysis;
    public static AnalysisManagerCallbackI analysisManagerCallbackI;
    public Tab onPeakResidualsTab;
    public Tab onPeakIntensitiesTab;
    @FXML
    private AnchorPane ogtSpeciesResidualsPlotAnchorPane;
    private AllBlockInitForMCMC.PlottingData plottingData;
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
    private PlotWallPaneInterface plotsWallPaneResiduals;

    public void setPlottingData(AllBlockInitForMCMC.PlottingData plottingData) {
        this.plottingData = plottingData;
    }

    @FXML
    public void initialize() {
        plotWindowVBox.widthProperty().addListener((observable, oldValue, newValue) -> {
            plotTabPane.setMinWidth((Double) newValue);
            ogtCycleRatioPlotsAnchorPane.setMinWidth((Double) newValue);
            if (analysis.getAnalysisCaseNumber() > 1) {
                ogtSpeciesIntensitiesPlotAnchorPane.setMinWidth((Double) newValue);
                ogtSpeciesResidualsPlotAnchorPane.setMinWidth((Double) newValue);
            }
        });

        plotWindowVBox.heightProperty().addListener((observable, oldValue, newValue) -> {
            plotTabPane.setMinHeight(((Double) newValue) - 30.0);
            ogtCycleRatioPlotsAnchorPane.setMinHeight(((Double) newValue) - plotsWallPaneRatios.getToolBarCount() * plotsWallPaneRatios.getToolBarHeight() - 30.0);
            if (analysis.getAnalysisCaseNumber() > 1) {
                ogtSpeciesIntensitiesPlotAnchorPane.setMinHeight(((Double) newValue) - plotsWallPaneIntensities.getToolBarCount() * plotsWallPaneIntensities.getToolBarHeight() - 30.0);
                ogtSpeciesResidualsPlotAnchorPane.setMinHeight(((Double) newValue) - plotsWallPaneResiduals.getToolBarCount() * plotsWallPaneResiduals.getToolBarHeight() - 30.0);
            }
        });

        if (analysis.getAnalysisCaseNumber() == 1) {
            plotTabPane.getTabs().remove(onPeakIntensitiesTab);
            plotTabPane.getTabs().remove(onPeakResidualsTab);
        } else {
            showTab(plotTabPane, 1, onPeakIntensitiesTab);
            showTab(plotTabPane, 1, onPeakResidualsTab);
        }

        if (null != plottingData) {
            populatePlots();
        }
    }

    public void populatePlots() {
        plotRatios();
        if (plottingData.analysisCaseNumber() == 4) {
            plotOnPeakIntensitiesAndResiduals();
        }
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
        plotWindowVBox.widthProperty().addListener((observable, oldValue, newValue) -> plotsWallPaneRatios.repeatLayoutStyle());
        plotWindowVBox.heightProperty().addListener((observable, oldValue, newValue) -> plotsWallPaneRatios.repeatLayoutStyle());

        boolean[] DUMMY_CYCLES_INCLUDED;

        switch (plottingData.analysisCaseNumber()) {
            case 1 -> {
                SingleBlockRawDataLiteSetRecord[] singleBlockRawDataLiteSetRecords = plottingData.singleBlockRawDataLiteSetRecords();
                int countOfOnPeakCycles = plottingData.cycleCount();
                int[] blockIDsPerTimeSlot = analysis.getMassSpecExtractedData().assignBlockIdToSessionTimeLite();

                DUMMY_CYCLES_INCLUDED = new boolean[countOfOnPeakCycles];
                Arrays.fill(DUMMY_CYCLES_INCLUDED, true);

                // build list of userFunctions to plot
                List<UserFunction> ratiosToPlot = new ArrayList<>();
                for (UserFunction userFunction : analysis.getAnalysisMethod().getUserFunctions()) {
                    if (userFunction.isDisplayed()) {
                        ratiosToPlot.add(userFunction);
                    }
                }

                for (UserFunction userFunction : ratiosToPlot) {
                    TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotsWallPaneRatios);

                    // todo: simplify since analysis carries most of the info
                    Map<Integer, PlotBlockCyclesRecord> mapOfBlocksToCyclesRecords = new TreeMap<>();
                    for (int blockIndex = 0; blockIndex < singleBlockRawDataLiteSetRecords.length; blockIndex++) {
                        if (null != singleBlockRawDataLiteSetRecords[blockIndex]) {
                            Integer blockID = singleBlockRawDataLiteSetRecords[blockIndex].blockID();

                            mapOfBlocksToCyclesRecords.put(blockID, (BlockCyclesBuilder.initializeBlockCycles(
                                    blockID,
                                    true,
                                    true, // TODO: not needed here
                                    singleBlockRawDataLiteSetRecords[blockIndex].assembleCyclesIncludedForUserFunction(userFunction),
                                    singleBlockRawDataLiteSetRecords[blockIndex].assembleCycleMeansForUserFunction(userFunction),
                                    singleBlockRawDataLiteSetRecords[blockIndex].assembleCycleStdDevForUserFunction(userFunction),
                                    new String[]{userFunction.getName()},
                                    true,
                                    userFunction.isTreatAsIsotopicRatio()).getBlockCyclesRecord()));
                        } else {
                            mapOfBlocksToCyclesRecords.put(blockIndex - 1, null);
                        }
                    }

                    AbstractPlot plot = AnalysisBlockCyclesPlotOG.generatePlot(
                            new Rectangle(minPlotWidth, minPlotHeight),
                            analysis,
                            userFunction,
                            mapOfBlocksToCyclesRecords,
                            blockIDsPerTimeSlot,
                            (PlotWallPane) plotsWallPaneRatios);

                    tripoliPlotPane.addPlot(plot);
                    plot.refreshPanel(false, false);
                }
            }

            case 4 -> {
                SingleBlockModelRecord[] singleBlockModelRecords = plottingData.singleBlockModelRecords();
                int countOfOnPeakCycles = plottingData.cycleCount();

                DUMMY_CYCLES_INCLUDED = new boolean[countOfOnPeakCycles];
                Arrays.fill(DUMMY_CYCLES_INCLUDED, true);

                // build list of ratios to plot
                List<IsotopicRatio> isotopicRatiosToPlot = new ArrayList<>();
                for (IsotopicRatio isotopicRatio : analysis.getAnalysisMethod().getIsotopicRatiosList()) {
                    if (isotopicRatio.isDisplayed()) {
                        isotopicRatiosToPlot.add(isotopicRatio);
                    }
                }
                for (IsotopicRatio isotopicRatio : analysis.getAnalysisMethod().getDerivedIsotopicRatiosList()) {
                    if (isotopicRatio.isDisplayed()) {
                        isotopicRatiosToPlot.add(isotopicRatio);
                        //TODO: need to calculate cycle values

                    }
                }

                for (IsotopicRatio isotopicRatio : isotopicRatiosToPlot) {
                    TripoliPlotPane tripoliPlotPane = TripoliPlotPane.makePlotPane(plotsWallPaneRatios);

                    List<PlotBlockCyclesRecord> plotBlockCyclesRecords = new ArrayList<>();
                    for (int blockIndex = 0; blockIndex < singleBlockModelRecords.length; blockIndex++) {
                        if (null != singleBlockModelRecords[blockIndex]) {
                            Integer blockID = singleBlockModelRecords[blockIndex].blockID();
                            int blockStatus = analysis.getMapOfBlockIdToProcessStatus().get(blockID);
                            boolean processed = (null != analysis.getMapOfBlockIdToPlots().get(blockID));
                            plotBlockCyclesRecords.add(BlockCyclesBuilder.initializeBlockCycles(
                                    blockID,
                                    SKIP != blockStatus,
                                    processed,
                                    DUMMY_CYCLES_INCLUDED,
                                    singleBlockModelRecords[blockIndex].assembleCycleMeansForRatio(isotopicRatio),
                                    singleBlockModelRecords[blockIndex].assembleCycleStdDevForRatio(isotopicRatio),
                                    new String[]{isotopicRatio.prettyPrint()},
                                    true,
                                    true).getBlockCyclesRecord());
                        } else {
                            plotBlockCyclesRecords.add(null);
                        }
                    }
                    BlockAnalysisRatioCyclesBuilder blockAnalysisRatioCyclesBuilder =
                            BlockAnalysisRatioCyclesBuilder.initializeBlockAnalysisRatioCycles(
                                    isotopicRatio.prettyPrint(), plotBlockCyclesRecords,
                                    analysis.getMapOfBlockIdToProcessStatus(),
                                    analysis.getMassSpecExtractedData().assignBlockIdToSessionTimeLite(),
                                    true,
                                    false);
                    AbstractPlot plot = AnalysisBlockCyclesPlot.generatePlot(
                            new Rectangle(minPlotWidth, minPlotHeight), blockAnalysisRatioCyclesBuilder.getBlockAnalysisRatioCyclesRecord(), (PlotWallPane) plotsWallPaneRatios);

                    tripoliPlotPane.addPlot(plot);
                    plot.refreshPanel(false, false);
                }
            }
        }

        plotsWallPaneRatios.buildToolBar();
        plotsWallPaneRatios.buildScaleControlsToolbar();
        plotsWallPaneRatios.tilePlots();
    }

    private void plotOnPeakIntensitiesAndResiduals() {
        PlotWallPane.menuOffset = 0.0;
        ogtSpeciesIntensitiesPlotAnchorPane.getChildren().clear();
        plotsWallPaneIntensities = PlotWallPaneIntensities.createPlotWallPane("OGTripoliSession");
        plotsWallPaneIntensities.setToolBarCount(2);
        plotsWallPaneIntensities.setToolBarHeight(35.0);
        ((Pane) plotsWallPaneIntensities).setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));

        ((Pane) plotsWallPaneIntensities).prefWidthProperty().bind(ogtSpeciesIntensitiesPlotAnchorPane.widthProperty());
        ((Pane) plotsWallPaneIntensities).prefHeightProperty().bind(ogtSpeciesIntensitiesPlotAnchorPane.heightProperty());

        ogtSpeciesIntensitiesPlotAnchorPane.getChildren().add(((Pane) plotsWallPaneIntensities));

        plotWindowVBox.widthProperty().addListener((observable, oldValue, newValue) -> plotsWallPaneIntensities.stackPlots());
        plotWindowVBox.heightProperty().addListener((observable, oldValue, newValue) -> plotsWallPaneIntensities.stackPlots());


        ogtSpeciesResidualsPlotAnchorPane.getChildren().clear();
        plotsWallPaneResiduals = PlotWallPaneIntensities.createPlotWallPane("OGTripoliSession");
        plotsWallPaneResiduals.setToolBarCount(2);
        plotsWallPaneResiduals.setToolBarHeight(35.0);
        ((Pane) plotsWallPaneResiduals).setBackground(new Background(new BackgroundFill(Paint.valueOf("LINEN"), null, null)));

        ((Pane) plotsWallPaneResiduals).prefWidthProperty().bind(ogtSpeciesResidualsPlotAnchorPane.widthProperty());
        ((Pane) plotsWallPaneResiduals).prefHeightProperty().bind(ogtSpeciesResidualsPlotAnchorPane.heightProperty());

        ogtSpeciesResidualsPlotAnchorPane.getChildren().add(((Pane) plotsWallPaneResiduals));

        plotWindowVBox.widthProperty().addListener((observable, oldValue, newValue) -> plotsWallPaneResiduals.stackPlots());
        plotWindowVBox.heightProperty().addListener((observable, oldValue, newValue) -> plotsWallPaneResiduals.stackPlots());


        SingleBlockRawDataSetRecord[] singleBlockRawDataSetRecords = plottingData.singleBlockRawDataSetRecords();
        SingleBlockModelRecord[] singleBlockModelRecords = plottingData.singleBlockModelRecords();
        // only plotting onPeaks
        int countOfBlocks = singleBlockRawDataSetRecords.length;
        int countOfSpecies = analysis.getAnalysisMethod().getSpeciesList().size();

        // x-axis = time
        double[] xAxis = analysis.getMassSpecExtractedData().calculateSessionTimes();
        int[] xAxisBlockIDs = analysis.getMassSpecExtractedData().assignBlockIdToSessionTimeFull();

        // alternating faraday, model, pm, and model rows (4-tuple for each species)

        double[][] onPeakDataCounts = new double[countOfSpecies * 4][xAxis.length];
        boolean[][] onPeakDataIncludedAllBlocks = new boolean[countOfSpecies][xAxis.length];
        double[][] onPeakDataAmpResistance = new double[countOfSpecies][xAxis.length];
        double[][] onPeakBaseline = new double[countOfSpecies * 4][xAxis.length];
        double[][] onPeakGain = new double[countOfSpecies * 4][xAxis.length];
        double[][] onPeakDataSignalNoiseArray = new double[countOfSpecies][xAxis.length];

        Set<Detector> detectors = analysis.getAnalysisMethod().getSequenceTable().getMapOfDetectorsToSequenceCells().keySet();
        Map<Integer, Double> mapOfOrdinalDetectorsToResistance = new TreeMap<>();
        for (Detector detector : detectors) {
            mapOfOrdinalDetectorsToResistance.put(detector.getOrdinalIndex(), detector.getAmplifierResistanceInOhms());
        }

        Map<Integer, MassSpecOutputBlockRecordFull> blocksData = analysis.getMassSpecExtractedData().getBlocksDataFull();
        for (int blockIndex = 0; blockIndex < countOfBlocks; blockIndex++) {
            double[][] blockIncluedIntensities = new double[countOfSpecies][];
            if (null != singleBlockModelRecords[blockIndex]) {
                int countOfBaselineIntensities = singleBlockRawDataSetRecords[blockIndex].getCountOfBaselineIntensities();
                int countOfFaradayIntensities = singleBlockRawDataSetRecords[blockIndex].getCountOfOnPeakFaradayIntensities();
                Integer blockID = singleBlockModelRecords[blockIndex].blockID();
                if (!plottingData.preview()) MCMCVectorExporter.exportData(analysis, blockID);
                double[] onPeakTimeStamps = blocksData.get(blockID).onPeakTimeStamps();

                SingleBlockModelRecord singleBlockModelRecord = singleBlockModelRecords[blockIndex];
                int countOfBaselineDataEntries = singleBlockRawDataSetRecords[blockIndex].getCountOfBaselineIntensities();
                int countOfFaradayDataEntries = singleBlockRawDataSetRecords[blockIndex].getCountOfOnPeakFaradayIntensities();
                boolean[] intensityIncludedArray = ((Analysis) analysis).getMapOfBlockIdToIncludedIntensities().get(blockID);

                double[] onPeakModelFaradayData = singleBlockModelRecord.getOnPeakDataModelFaradayArray(countOfBaselineDataEntries, countOfFaradayDataEntries);
                double[] onPeakFaradayDataSignalNoise = singleBlockModelRecord.getOnPeakFaradayDataSignalNoiseArray(countOfBaselineDataEntries, countOfFaradayDataEntries);
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
                    onPeakDataIncludedAllBlocks[intensitySpeciesIndex][timeIndx] = intensityIncludedArray[countOfBaselineIntensities + onPeakDataIndex];
                    onPeakDataSignalNoiseArray[intensitySpeciesIndex][timeIndx] = onPeakFaradayDataSignalNoise[onPeakDataIndex];
                }

                double[] onPeakModelPhotoMultiplierData = singleBlockModelRecord.getOnPeakDataModelPhotoMultiplierArray(countOfBaselineDataEntries, countOfFaradayDataEntries);
                double[] onPeakPhotoMultiplierDataSignalNoise = singleBlockModelRecord.getOnPeakPhotoMultiplierDataSignalNoiseArray(countOfBaselineDataEntries, countOfFaradayDataEntries);

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
                    onPeakDataIncludedAllBlocks[intensitySpeciesIndex][timeIndx] = intensityIncludedArray[countOfBaselineIntensities + countOfFaradayIntensities + onPeakDataIndex];
                    onPeakDataSignalNoiseArray[intensitySpeciesIndex][timeIndx] = onPeakPhotoMultiplierDataSignalNoise[onPeakDataIndex];
                }
            }
            // block specific

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

        PlotBuilder plotBuilderIntensities = SpeciesIntensityAnalysisBuilder.initializeSpeciesIntensityAnalysisPlot(
                false, analysis, xAxis, onPeakDataIncludedAllBlocks, xAxisBlockIDs, onPeakDataCounts, onPeakDataAmpResistance, onPeakBaseline, onPeakGain,
                onPeakDataSignalNoiseArray, new String[]{"Species Intensity by Analysis"}, "Time (secs)", "Intensity (counts)");

        TripoliPlotPane tripoliPlotPaneIntensities = TripoliPlotPane.makePlotPane(plotsWallPaneIntensities);
        AbstractPlot plotIntensities = SpeciesIntensityAnalysisPlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), (SpeciesIntensityAnalysisBuilder) plotBuilderIntensities);

        tripoliPlotPaneIntensities.addPlot(plotIntensities);
        plotIntensities.refreshPanel(false, false);

        ((PlotWallPaneIntensities) plotsWallPaneIntensities).buildIntensitiesPlotToolBar(false, analysis.getAnalysisMethod().getSpeciesList());
        plotsWallPaneIntensities.buildScaleControlsToolbar();
        plotsWallPaneIntensities.stackPlots();


        PlotBuilder plotBuilderResiduals = SpeciesIntensityAnalysisBuilder.initializeSpeciesIntensityAnalysisPlot(
                true, analysis, xAxis, onPeakDataIncludedAllBlocks, xAxisBlockIDs, onPeakDataCounts, onPeakDataAmpResistance, onPeakBaseline, onPeakGain,
                onPeakDataSignalNoiseArray, new String[]{"Species Residuals by Analysis"}, "Time (secs)", "Intensity (counts)");

        TripoliPlotPane tripoliPlotPaneResiduals = TripoliPlotPane.makePlotPane(plotsWallPaneResiduals);
        AbstractPlot plotResiduals = SpeciesIntensityAnalysisPlot.generatePlot(new Rectangle(minPlotWidth, minPlotHeight), (SpeciesIntensityAnalysisBuilder) plotBuilderResiduals);

        tripoliPlotPaneResiduals.addPlot(plotResiduals);
        plotResiduals.refreshPanel(false, false);

        ((PlotWallPaneIntensities) plotsWallPaneResiduals).buildIntensitiesPlotToolBar(true, analysis.getAnalysisMethod().getSpeciesList());
        plotsWallPaneResiduals.buildScaleControlsToolbar();
        plotsWallPaneResiduals.stackPlots();


    }
}