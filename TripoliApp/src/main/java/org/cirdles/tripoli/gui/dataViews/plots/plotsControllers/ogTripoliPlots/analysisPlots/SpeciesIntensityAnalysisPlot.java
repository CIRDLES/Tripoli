package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots;

import com.google.common.primitives.Booleans;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.PlotWallPaneIntensities;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.analysisPlotBuilders.SpeciesIntensityAnalysisBuilder;
import org.cirdles.tripoli.sessions.analysis.Analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.StrictMath.*;
import static java.util.Arrays.binarySearch;
import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.TRIPOLI_PALLETTE_FIVE;

public class SpeciesIntensityAnalysisPlot extends AbstractPlot {
    private final SpeciesIntensityAnalysisBuilder speciesIntensityAnalysisBuilder;
    private final double[][] dfGain;
    private final double[][] yDataCounts;
    private final double[][] ampResistance;
    private final double[][] baseLine;
    private final Tooltip tooltip;
    private final String tooltipTextSculpt = "Double click to Sculpt selected Block.";
    private final String tooltipTextExitSculpt = "Right Mouse to PAN, Shift-click toggles block, Dbl-click to EXIT Sculpting.";
    TripoliConstants.IntensityUnits intensityUnits = TripoliConstants.IntensityUnits.COUNTS;
    private double[][] yData;
    private double[][] residuals;
    private boolean showResiduals;
    private boolean showUncertainties = false;
    private boolean[][] onPeakDataIncludedAllBlocks;
    private boolean[] speciesChecked;
    private boolean showFaradays;
    private boolean showPMs;
    private boolean showModels;
    private boolean baselineCorr;
    private boolean gainCorr;
    private boolean logScale;
    private boolean[] zoomFlagsXY;
    private double selectorBoxX;
    private double selectorBoxY;
    private boolean inSculptorMode;
    private int sculptBlockID;
    private boolean showSelectionBox;
    private int countOfPreviousBlockIncludedData;
    private boolean inZoomBoxMode;
    private boolean showZoomBox;
    private double zoomBoxX;
    private double zoomBoxY;
    private double[][] oneSigmaResiduals;


    private SpeciesIntensityAnalysisPlot(Rectangle bounds, SpeciesIntensityAnalysisBuilder speciesIntensityAnalysisBuilder) {
        super(bounds, 100, 35,
                speciesIntensityAnalysisBuilder.getTitle(),
                speciesIntensityAnalysisBuilder.getxAxisLabel(),
                speciesIntensityAnalysisBuilder.getyAxisLabel());
        this.speciesIntensityAnalysisBuilder = speciesIntensityAnalysisBuilder;
        yDataCounts = speciesIntensityAnalysisBuilder.getyData();
        residuals = new double[yDataCounts.length / 2][yDataCounts[0].length];
        ampResistance = speciesIntensityAnalysisBuilder.getAmpResistance();
        baseLine = speciesIntensityAnalysisBuilder.getBaseLine();
        dfGain = speciesIntensityAnalysisBuilder.getDfGain();
        speciesChecked = new boolean[yDataCounts.length / 4];
        speciesChecked[speciesChecked.length - 1] = true;
        zoomFlagsXY = new boolean[]{true, true};
        // TODO: make this a user pref
        yAxisTickSpread = 45.0;

        setOnMouseClicked(new MouseClickEventHandler());

        tooltip = new Tooltip(tooltipTextSculpt);
        Tooltip.install(this, tooltip);


//        SpeciesIntensityAnalysisBuilder.PlotSpecsSpeciesIntensityAnalysis plotSpecs = ((Analysis)speciesIntensityAnalysisBuilder.getAnalysis()).getPlotSpecsSpeciesIntensityAnalysis();
//        speciesChecked = plotSpecs.speciesChecked();//new boolean[yDataCounts.length / 4];
//        showFaradays = plotSpecs.showFaradays();
//        showPMs = plotSpecs.showPMs();
//        showModels = plotSpecs.showModels();
//        baselineCorr = plotSpecs.baselineCorr();
//        gainCorr = plotSpecs.gainCorr();
//        logScale = plotSpecs.logScale();
//
//        speciesChecked[speciesChecked.length - 1] = true;
    }

    public static AbstractPlot generatePlot(Rectangle bounds, SpeciesIntensityAnalysisBuilder speciesIntensityAnalysisBuilder) {
        return new SpeciesIntensityAnalysisPlot(bounds, speciesIntensityAnalysisBuilder);
    }

    public boolean isInSculptorMode() {
        return inSculptorMode;
    }

    public void setInSculptorMode(boolean inSculptorMode) {
        this.inSculptorMode = inSculptorMode;
    }

    public void setSpeciesChecked(boolean[] speciesChecked) {
        this.speciesChecked = speciesChecked;
    }

    public void setShowFaradays(boolean showFaradays) {
        this.showFaradays = showFaradays;
    }

    public void setShowPMs(boolean showPMs) {
        this.showPMs = showPMs;
    }

    public void setShowModels(boolean showModels) {
        this.showModels = showModels;
    }

    public void setIntensityUnits(TripoliConstants.IntensityUnits intensityUnits) {
        this.intensityUnits = intensityUnits;
    }

    public void setBaselineCorr(boolean baselineCorr) {
        this.baselineCorr = baselineCorr;
    }

    public void setGainCorr(boolean gainCorr) {
        this.gainCorr = gainCorr;
    }

    public void setLogScale(boolean logScale) {
        this.logScale = logScale;
    }

    public void setZoomFlagsXY(boolean[] zoomFlagsXY) {
        this.zoomFlagsXY = zoomFlagsXY;
    }

    public void setShowUncertainties(boolean showUncertainties) {
        this.showUncertainties = showUncertainties;
    }

    @Override
    public void preparePanel(boolean reScaleXin, boolean reScaleYin) {
        boolean reScaleX = !inSculptorMode && reScaleXin;
        boolean reScaleY = !inSculptorMode && reScaleYin;
        showResiduals = speciesIntensityAnalysisBuilder.isShowResiduals();

        xAxisData = speciesIntensityAnalysisBuilder.getxData();
        if (reScaleX) {
            minX = xAxisData[0];
            maxX = xAxisData[xAxisData.length - 1];

            displayOffsetX = 0.0;
            inSculptorMode = false;
            sculptBlockID = 0;
            showSelectionBox = false;
            removeEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedEventHandler);
            setOnMouseDragged(new MouseDraggedEventHandler());
            setOnMousePressed(new MousePressedEventHandler());
            setOnMouseReleased(new MouseReleasedEventHandler());
            addEventFilter(ScrollEvent.SCROLL, scrollEventEventHandler);
        }


        yData = new double[yDataCounts.length][yDataCounts[0].length];
        onPeakDataIncludedAllBlocks = speciesIntensityAnalysisBuilder.getOnPeakDataIncludedAllBlocks();

        if (showResiduals) {
            double[][] onPeakDataSignalNoiseArray = speciesIntensityAnalysisBuilder.getOnPeakDataSignalNoiseArray();
            oneSigmaResiduals = new double[onPeakDataSignalNoiseArray.length][onPeakDataSignalNoiseArray[0].length];
            for (int speciesIndex = 0; speciesIndex < onPeakDataSignalNoiseArray.length; speciesIndex++) {
                for (int col = 0; col < onPeakDataSignalNoiseArray[0].length; col++) {
                    oneSigmaResiduals[speciesIndex][col] = 2.0 * sqrt(onPeakDataSignalNoiseArray[speciesIndex][col]);
                }
            }
        }


        for (int row = 0; row < yData.length; row++) {
            for (int col = 0; col < yData[0].length; col++) {
                yData[row][col] = yDataCounts[row][col];
                if (0.0 != yDataCounts[row][col]) {
                    if (baselineCorr) {
                        yData[row][col] -= baseLine[row][col];
                    }

                    if ((gainCorr) && (0.0 != dfGain[row][col])) {
                        yData[row][col] /= dfGain[row][col];
                    }

                    if (logScale) {
                        yData[row][col] = (0.0 < yData[row][col]) ? log(yData[row][col]) : 0.0;
                    }
                }
            }
        }

        switch (intensityUnits) {
            case VOLTS -> {
                plotAxisLabelY = showResiduals ? "Residuals (volts)" : "Intensity (volts)";
                for (int row = 0; row < yData.length; row++) {
                    yData[row] = TripoliConstants.IntensityUnits.convertFromCountsToVolts(yData[row], ampResistance[row / 4]);
                    if (showResiduals) {
                        oneSigmaResiduals[row] = TripoliConstants.IntensityUnits.convertFromCountsToVolts(oneSigmaResiduals[row], ampResistance[row / 4]);
                    }
                }
            }
            case AMPS -> {
                plotAxisLabelY = showResiduals ? "Residuals (amps)" : "Intensity (amps)";
                for (int row = 0; row < yData.length; row++) {
                    yData[row] = TripoliConstants.IntensityUnits.convertFromCountsToAmps(yData[row]);
                    if (showResiduals) {
                        oneSigmaResiduals[row] = TripoliConstants.IntensityUnits.convertFromCountsToAmps(oneSigmaResiduals[row]);
                    }
                }
            }
            case COUNTS -> {
                plotAxisLabelY = showResiduals ? "Residuals (counts)" : "Intensity (counts)";
            }
        }

        // calculate residuals for each faraday and pm == 2-tuples - yData has already been scaled, so residuals are too
        for (int speciesIndex = 0; speciesIndex < residuals.length / 2; speciesIndex++) {
            for (int col = 0; col < yData[0].length; col++) {
                residuals[speciesIndex * 2][col] = yData[speciesIndex * 4][col] - yData[speciesIndex * 4 + 1][col];
                residuals[speciesIndex * 2 + 1][col] = yData[speciesIndex * 4 + 2][col] - yData[speciesIndex * 4 + 3][col];
            }
        }


        if (reScaleY) {
            minY = Double.MAX_VALUE;
            maxY = -Double.MAX_VALUE;

            // todo: separate loops
            for (int row = 0; row < yData.length; row++) {
                int speciesIndex = (row / 4);
                if (speciesChecked[speciesIndex]) {
                    boolean plotFaradays = (showFaradays && (row >= speciesIndex * 4) && (row <= speciesIndex * 4 + 1));
                    boolean plotPMs = (showPMs && (row >= speciesIndex * 4 + 2) && (row <= speciesIndex * 4 + 3));
                    for (int col = 0; col < yData[row].length; col++) {
                        if (!Double.isNaN(yData[row][col]) && (0.0 != yData[row][col]) && (plotFaradays || plotPMs)) {
                            if (showResiduals) {
                                minY = min(minY, residuals[row / 2][col] - oneSigmaResiduals[speciesIndex][col]);
                                maxY = max(maxY, residuals[row / 2][col] + oneSigmaResiduals[speciesIndex][col]);
                            } else {
                                minY = min(minY, yData[row][col]);
                                maxY = max(maxY, yData[row][col]);
                            }
                        }
                    }
                }
            }

            displayOffsetY = 0.0;
        }

        if (logScale) {
            plotAxisLabelY = "Log-" + plotAxisLabelY;
        }

        prepareExtents(reScaleX, reScaleY);

        calculateTics();

        repaint();
    }

    @Override
    public void calculateTics() {
        super.calculateTics();
        zoomChunkX = zoomFlagsXY[0] ? zoomChunkX : 0.0;
        zoomChunkY = zoomFlagsXY[1] ? zoomChunkY : 0.0;
    }

    /**
     *
     */
    @Override
    public void showLegend(GraphicsContext g2d) {
        Paint savedPaint = g2d.getFill();
        List<SpeciesRecordInterface> speciesList = speciesIntensityAnalysisBuilder.getAnalysis().getAnalysisMethod().getSpeciesList();
        for (int isotopePlotSetIndex = 0; isotopePlotSetIndex < yData.length / 4; isotopePlotSetIndex++) {
            if (speciesChecked[isotopePlotSetIndex]) {
                g2d.setFill(Color.web(TRIPOLI_PALLETTE_FIVE[isotopePlotSetIndex]));//.brighter());
                Text text = new Text(speciesList.get(isotopePlotSetIndex).prettyPrintShortForm());
                g2d.setFont(Font.font("Monospaced", FontWeight.BOLD, 20));
                g2d.fillText(text.getText(), 5, 150 - isotopePlotSetIndex * 22);
                g2d.setFill(savedPaint);
            }
        }
    }

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);
    }

    public void prepareExtents(boolean reScaleX, boolean reScaleY) {
        if (reScaleX) {
            double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.05);
            if (0.0 == xMarginStretch) {
                xMarginStretch = maxX * 0.01;
            }

            minX = Math.max(0, minX - xMarginStretch);
            maxX += xMarginStretch;
        }

        if (reScaleY) {
            double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.05);
            maxY += yMarginStretch;
            minY -= yMarginStretch;
        }
    }

    @Override
    public void plotData(GraphicsContext g2d) {

        g2d.setFill(dataColor.color());
        g2d.setStroke(dataColor.color());
        g2d.setLineWidth(2.0);
        for (int isotopePlotSetIndex = 0; isotopePlotSetIndex < yData.length / 4; isotopePlotSetIndex++) {
            if (speciesChecked[isotopePlotSetIndex]) {
                // plot PM
                if (showPMs) {
                    g2d.closePath();
                    g2d.setLineDashes(0);
                    boolean startedPlot = false;
                    g2d.setFill(Color.web(TRIPOLI_PALLETTE_FIVE[isotopePlotSetIndex]).darker());
                    g2d.setStroke(Color.web(TRIPOLI_PALLETTE_FIVE[isotopePlotSetIndex]).darker());
                    for (int i = 0; i < xAxisData.length; i++) {
                        if ((0.0 != yData[isotopePlotSetIndex * 4 + 2][i])
                                && (pointInPlot(xAxisData[i], yData[isotopePlotSetIndex * 4 + 2][i])
                                || pointInPlot(xAxisData[i], residuals[isotopePlotSetIndex * 2 + 1][i]))) {
                            double dataX = mapX(xAxisData[i]);
                            double dataY = 0.0;
                            if (showResiduals) {
                                dataY = mapY(residuals[isotopePlotSetIndex * 2 + 1][i]);
                            } else {
                                dataY = mapY(yData[isotopePlotSetIndex * 4 + 2][i]);
                            }
                            if (onPeakDataIncludedAllBlocks[isotopePlotSetIndex][i]) {
                                g2d.fillOval(dataX - 2.0, Math.abs(dataY) - 2.0, 4, 4);
                            } else {
                                g2d.setFill(Color.RED);
                                g2d.fillOval(dataX - 2.0, Math.abs(dataY) - 2.0, 4, 4);
                                g2d.setFill(Color.web(TRIPOLI_PALLETTE_FIVE[isotopePlotSetIndex]).darker());
                            }
                            if (showResiduals && showUncertainties) {
                                g2d.setLineWidth(0.5);
//                                g2d.strokeLine(dataX,
//                                        mapY(Math.max(residuals[isotopePlotSetIndex * 2 + 1][i] - oneSigmaResiduals[isotopePlotSetIndex][i], minY)),
//                                        dataX,
//                                        mapY(Math.min(residuals[isotopePlotSetIndex * 2 + 1][i] + oneSigmaResiduals[isotopePlotSetIndex][i], maxY)));

                                g2d.setStroke(Color.BLACK);
                                g2d.strokeLine(dataX,
                                        mapY(Math.max(0.0 - oneSigmaResiduals[isotopePlotSetIndex][i], minY)),
                                        dataX,
                                        mapY(Math.min(0.0 + oneSigmaResiduals[isotopePlotSetIndex][i], maxY)));
                            }
                        }

                        g2d.setLineWidth(2.0);
                        if (showModels && !showResiduals) {
                            if ((i < xAxisData.length - 1) && (yData[isotopePlotSetIndex * 4 + 3][i] != 0.0)) {
                                if ((0.0 != yData[isotopePlotSetIndex * 4 + 3][i]) && pointInPlot(xAxisData[i], yData[isotopePlotSetIndex * 4 + 3][i])) {
                                    if (!startedPlot) {
                                        g2d.beginPath();
                                        g2d.moveTo(mapX(xAxisData[i]), mapY(yData[isotopePlotSetIndex * 4 + 3][i]));
                                        startedPlot = true;
                                    }
                                    g2d.lineTo(mapX(xAxisData[i]), mapY(yData[isotopePlotSetIndex * 4 + 3][i]));
                                }
                            } else {
                                if (startedPlot) {
                                    startedPlot = false;
                                    g2d.setStroke(Color.AQUAMARINE);
                                    g2d.stroke();
                                }
                            }
                        }
                    }
                    g2d.setStroke(Color.web(TRIPOLI_PALLETTE_FIVE[isotopePlotSetIndex]).darker());
                }
                // plot Faraday
                if (showFaradays) {
                    g2d.closePath();
                    g2d.setLineDashes(0);
                    boolean startedPlot = false;
                    g2d.setFill(Color.web(TRIPOLI_PALLETTE_FIVE[isotopePlotSetIndex]).brighter());
                    g2d.setStroke(Color.web(TRIPOLI_PALLETTE_FIVE[isotopePlotSetIndex]).brighter());
                    for (int i = 0; i < xAxisData.length; i++) {
                        if ((0.0 != yData[isotopePlotSetIndex * 4][i])
                                && (pointInPlot(xAxisData[i], yData[isotopePlotSetIndex * 4][i])
                                || pointInPlot(xAxisData[i], residuals[isotopePlotSetIndex * 2][i]))) {
                            double dataX = mapX(xAxisData[i]);
                            double dataY = 0.0;
                            if (showResiduals) {
                                dataY = mapY(residuals[isotopePlotSetIndex * 2][i]);
                            } else {
                                dataY = mapY(yData[isotopePlotSetIndex * 4][i]);
                            }
                            if (onPeakDataIncludedAllBlocks[isotopePlotSetIndex][i]) {
                                g2d.fillOval(dataX - 2.0, Math.abs(dataY) - 2.0, 4, 4);
                            } else {
                                g2d.setFill(Color.RED);
                                g2d.fillOval(dataX - 2.0, Math.abs(dataY) - 2.0, 4, 4);
                                g2d.setFill(Color.web(TRIPOLI_PALLETTE_FIVE[isotopePlotSetIndex]).brighter());
                            }
                            if (showResiduals && showUncertainties) {
                                g2d.setLineWidth(0.5);
//                                g2d.strokeLine(dataX,
//                                        mapY(Math.max(residuals[isotopePlotSetIndex * 2][i] - oneSigmaResiduals[isotopePlotSetIndex][i], minY)),
//                                        dataX,
//                                        mapY(Math.min(residuals[isotopePlotSetIndex * 2][i] + oneSigmaResiduals[isotopePlotSetIndex][i], maxY)));

                                g2d.setStroke(Color.BLACK);
                                g2d.strokeLine(dataX,
                                        mapY(Math.max(0.0 - oneSigmaResiduals[isotopePlotSetIndex][i], minY)),
                                        dataX,
                                        mapY(Math.min(0.0 + oneSigmaResiduals[isotopePlotSetIndex][i], maxY)));
                            }
                        }

                        if (showModels && !showResiduals) {
                            if ((i < xAxisData.length - 1) && (yData[isotopePlotSetIndex * 4 + 1][i] != 0.0)) {
                                if ((0.0 != yData[isotopePlotSetIndex * 4 + 1][i]) && pointInPlot(xAxisData[i], yData[isotopePlotSetIndex * 4 + 1][i])) {
                                    if (!startedPlot) {
                                        g2d.beginPath();
                                        g2d.moveTo(mapX(xAxisData[i]), mapY(yData[isotopePlotSetIndex * 4 + 1][i]));
                                        startedPlot = true;
                                    }
                                    g2d.lineTo(mapX(xAxisData[i]), mapY(yData[isotopePlotSetIndex * 4 + 1][i]));
                                }
                            } else {
                                if (startedPlot) {
                                    startedPlot = false;
                                    g2d.setStroke(Color.RED);
                                    g2d.stroke();
                                }
                            }
                        }
                    }
                    g2d.setStroke(Color.web(TRIPOLI_PALLETTE_FIVE[isotopePlotSetIndex]).brighter());
                }
            }
        }

        if (inSculptorMode && showSelectionBox) {
            //plot selectorbox
            g2d.setStroke(Color.RED);
            g2d.setLineWidth(1.0);
            g2d.strokeRect(Math.min(mouseStartX, selectorBoxX), Math.min(mouseStartY, selectorBoxY), Math.abs(selectorBoxX - mouseStartX), Math.abs(selectorBoxY - mouseStartY));
        }

        if (inZoomBoxMode && showZoomBox) {
            g2d.setStroke(Color.BLUE);
            g2d.setLineWidth(1.5);
            g2d.strokeRect(Math.min(mouseStartX, zoomBoxX), Math.min(mouseStartY, zoomBoxY), Math.abs(zoomBoxX - mouseStartX), Math.abs(zoomBoxY - mouseStartY));
        }


        // block delimiters + labels
        int[] xAxisBlockIDs = speciesIntensityAnalysisBuilder.getxAxisBlockIDs();
        g2d.setStroke(Color.BLACK);
        g2d.setLineWidth(0.5);
        int blockID = 0;
        for (int i = 0; i < xAxisBlockIDs.length; i++) {
            double dataX = mapX(xAxisData[i]) - 5.0;
            if ((xAxisBlockIDs[i] > blockID) && xInPlot(xAxisData[i])) {
                g2d.strokeLine(dataX, topMargin + plotHeight, dataX, topMargin);
            }
            if (xAxisBlockIDs[i] > blockID) {
                blockID++;
                if (xInPlot(xAxisData[i])) {
                    showBlockID(g2d, Integer.toString(blockID), mapX(xAxisData[i]));
                }
            }

        }

        double dataX = mapX(xAxisData[xAxisData.length - 1]) + 5;
        g2d.strokeLine(dataX, topMargin + plotHeight, dataX, topMargin);
    }

    private void showBlockID(GraphicsContext g2d, String blockID, double xPosition) {
        Paint savedPaint = g2d.getFill();
        g2d.setFill(Paint.valueOf("GREEN"));
        g2d.setFont(Font.font("SansSerif", FontWeight.EXTRA_BOLD, 10));
        g2d.fillText("BL#" + blockID, xPosition, 30);
        g2d.setFill(savedPaint);
    }

    @Override
    public void plotStats(GraphicsContext g2d) {

    }

    @Override
    public void setupPlotContextMenu() {
    }

    public void sculptBlock(boolean zoomBlock) {
        if ((0 < sculptBlockID) && !inSculptorMode) {
            inSculptorMode = true;
            showSelectionBox = true;
            setOnMouseDragged(new MouseDraggedEventHandlerSculpt());
            setOnMousePressed(new MousePressedEventHandlerSculpt());
            setOnMouseReleased(new MouseReleasedEventHandlerSculpt());
            selectorBoxX = mouseStartX;
            selectorBoxY = mouseStartY;
            // zoom into block
            countOfPreviousBlockIncludedData = 0;
            for (int prevBlockID = 1; prevBlockID < sculptBlockID; prevBlockID++) {
                countOfPreviousBlockIncludedData +=
                        speciesIntensityAnalysisBuilder.getAnalysis().getMassSpecExtractedData().getBlocksDataFull().get(prevBlockID).onPeakIntensities().length;
            }

            if (zoomBlock) {
                displayOffsetX = xAxisData[countOfPreviousBlockIncludedData] - minX - 10;
                // find index of last intensity in block is found in photoMultiplier data first species * 4 + 2
                int sculptedSpeciesIndex = Booleans.indexOf(speciesChecked, true);
                int countOfIntensities = ((Analysis) speciesIntensityAnalysisBuilder.getAnalysis()).getMapOfBlockIdToIncludedPeakData().get(sculptBlockID)[0].length;
                for (int i = countOfIntensities; 0 < i; i--) {
                    if (0.0 == yData[sculptedSpeciesIndex * 4 + 2][countOfPreviousBlockIncludedData + i - 1]) {
                        countOfIntensities--;
                    } else
                        break;
                }
                maxX = xAxisData[countOfPreviousBlockIncludedData
                        + countOfIntensities - 1]
                        - displayOffsetX + 25;

                minY = Double.MAX_VALUE;
                maxY = -Double.MAX_VALUE;
                for (int i = 1; i < ((Analysis) speciesIntensityAnalysisBuilder.getAnalysis()).getMapOfBlockIdToIncludedPeakData().get(sculptBlockID)[0].length; i++) {
                    for (sculptedSpeciesIndex = 0; sculptedSpeciesIndex < speciesChecked.length; sculptedSpeciesIndex++) {
                        if (speciesChecked[sculptedSpeciesIndex]) {
                            // faraday
                            if (0.0 != yData[sculptedSpeciesIndex * 4][countOfPreviousBlockIncludedData + i - 1]) {
                                if (showResiduals) {
                                    minY = min(minY, residuals[sculptedSpeciesIndex * 2][countOfPreviousBlockIncludedData + i - 1]);
                                    maxY = max(maxY, residuals[sculptedSpeciesIndex * 2][countOfPreviousBlockIncludedData + i - 1]);
                                } else {
                                    minY = Math.min(minY, yData[sculptedSpeciesIndex * 4][countOfPreviousBlockIncludedData + i - 1]);
                                    maxY = Math.max(maxY, yData[sculptedSpeciesIndex * 4][countOfPreviousBlockIncludedData + i - 1]);
                                }
                            }
                            // photoMultiplier
                            if (0.0 != yData[sculptedSpeciesIndex * 4 + 2][countOfPreviousBlockIncludedData + i - 1]) {
                                if (showResiduals) {
                                    minY = min(minY, residuals[sculptedSpeciesIndex * 1][countOfPreviousBlockIncludedData + i - 1]);
                                    maxY = max(maxY, residuals[sculptedSpeciesIndex * 1][countOfPreviousBlockIncludedData + i - 1]);
                                } else {
                                    minY = Math.min(minY, yData[sculptedSpeciesIndex * 4 + 2][countOfPreviousBlockIncludedData + i - 1]);
                                    maxY = Math.max(maxY, yData[sculptedSpeciesIndex * 4 + 2][countOfPreviousBlockIncludedData + i - 1]);
                                }
                            }
                        }
                    }
                }
                double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.05);
                maxY += yMarginStretch;
                minY -= yMarginStretch;
                displayOffsetY = 0.0;

                refreshPanel(false, false);
            }
        } else {
            inSculptorMode = false;
            showSelectionBox = false;
            setOnMouseDragged(new MouseDraggedEventHandler());
            setOnMousePressed(new MousePressedEventHandler());
            setOnMouseReleased(new MouseReleasedEventHandler());
        }
        repaint();
    }


    private int onlyOneSpeciesShown() {
        int isotopeIndex = -1;
        if (1 == Booleans.countTrue(speciesChecked)) {
            isotopeIndex = Booleans.indexOf(speciesChecked, true);
        }

        return isotopeIndex;
    }

    private boolean mouseInBlockLabel(double sceneX, double sceneY) {
        return ((sceneX >= leftMargin)
                && (sceneY >= topMargin - 15)
                && (sceneY < topMargin)
                && (sceneX < (plotWidth + leftMargin - 2)));
    }

    private int determineSculptBlock(double mouseX) {
        double mouseTime = convertMouseXToValue(mouseX);
        int xAxisIndexOfMouse = Math.min(xAxisData.length - 1, Math.abs(Arrays.binarySearch(xAxisData, mouseTime)));
        double t0 = xAxisData[xAxisIndexOfMouse];
        double t2 = xAxisData[(xAxisIndexOfMouse >= 2) ? (xAxisIndexOfMouse - 2) : 0];
        int sculptBlockIDCalc = speciesIntensityAnalysisBuilder.getxAxisBlockIDs()[(xAxisIndexOfMouse >= 2) ? (xAxisIndexOfMouse - 2) : 0];
        if (((t0 - t2) > 5.0) && (Math.abs(mouseTime - t2) > Math.abs(mouseTime - t0))) {
            // in between blocks
            sculptBlockIDCalc = speciesIntensityAnalysisBuilder.getxAxisBlockIDs()[xAxisIndexOfMouse];
        }
        return sculptBlockIDCalc;
    }

    class MouseClickEventHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent mouseEvent) {
            boolean isPrimary = (0 == mouseEvent.getButton().compareTo(MouseButton.PRIMARY));
            if (2 == mouseEvent.getClickCount()) {
                if (isPrimary && (mouseInHouse(mouseEvent.getX(), mouseEvent.getY()) || mouseInBlockLabel(mouseEvent.getX(), mouseEvent.getY()))) {
                    if (inSculptorMode) {
                        inSculptorMode = false;
                        sculptBlockID = 0;
                        inZoomBoxMode = true;
                        showZoomBox = true;
                        zoomBoxX = mouseStartX;
                        zoomBoxY = mouseStartY;
                        refreshPanel(true, true);
                        ((PlotWallPaneIntensities) getParent().getParent().getParent()).removeSculptingHBox();
                        tooltip.setText(tooltipTextSculpt);
                    } else {
                        inZoomBoxMode = false;
                        showZoomBox = false;
                        ((PlotWallPaneIntensities) getParent().getParent().getParent()).removeSculptingHBox();
                        if (0 < Booleans.countTrue(speciesChecked)) {
                            sculptBlockID = determineSculptBlock(mouseEvent.getX());
                            ((PlotWallPaneIntensities) getParent().getParent().getParent()).builtSculptingHBox(
                                    "Intensity Sculpting " + "  >> " + tooltipTextExitSculpt);
                            sculptBlock(mouseInBlockLabel(mouseEvent.getX(), mouseEvent.getY()));
                            tooltip.setText(tooltipTextExitSculpt);
                        }
                    }
                }
            } else {
                if (isPrimary && mouseEvent.isShiftDown() && (mouseInHouse(mouseEvent.getX(), mouseEvent.getY()) || mouseInBlockLabel(mouseEvent.getX(), mouseEvent.getY()))) {
                    // turn off / on block
                    sculptBlockID = determineSculptBlock(mouseEvent.getX());
                    countOfPreviousBlockIncludedData = 0;
                    for (int prevBlockID = 1; prevBlockID < sculptBlockID; prevBlockID++) {
                        countOfPreviousBlockIncludedData += ((Analysis) speciesIntensityAnalysisBuilder.getAnalysis()).getMapOfBlockIdToIncludedPeakData().get(prevBlockID)[0].length;
                    }

                    boolean[][] included = ((Analysis) speciesIntensityAnalysisBuilder.getAnalysis()).getMapOfBlockIdToIncludedPeakData().get(sculptBlockID);
                    boolean allVal = true;
                    for (int speciesIndex = 0; speciesIndex < included.length; speciesIndex++) {
                        allVal = allVal && (Booleans.countTrue(included[speciesIndex]) == 0);
                    }
                    for (int speciesIndex = 0; speciesIndex < included.length; speciesIndex++) {
                        Arrays.fill(included[speciesIndex], allVal);
                        System.arraycopy(included[speciesIndex], 0, onPeakDataIncludedAllBlocks[speciesIndex], countOfPreviousBlockIncludedData, included[speciesIndex].length);
                    }

                    inZoomBoxMode = !inSculptorMode;
                    showZoomBox = !inSculptorMode;
                    repaint();
                }
            }
        }
    }

    class MouseDraggedEventHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            if (inZoomBoxMode && mouseInHouse(e.getX(), e.getY()) && e.isPrimaryButtonDown()) {
                zoomBoxX = e.getX();
                zoomBoxY = e.getY();
                showZoomBox = true;

            } else {
                if (mouseInHouse(e.getX(), e.getY()) && !e.isPrimaryButtonDown()) {
                    // right mouse PAN
                    showZoomBox = false;
                    displayOffsetX = displayOffsetX + (convertMouseXToValue(mouseStartX) - convertMouseXToValue(e.getX()));
                    displayOffsetY = displayOffsetY + (convertMouseYToValue(mouseStartY) - convertMouseYToValue(e.getY()));
                    adjustMouseStartsForPress(e.getX(), e.getY());
                    calculateTics();
                }
            }
            repaint();
        }
    }

    class MouseDraggedEventHandlerSculpt implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            if (e.isPrimaryButtonDown()) {
                if (mouseInHouse(e.getX(), e.getY())) {
                    int currentSculptBlockID = determineSculptBlock(e.getX());
                    if ((currentSculptBlockID == sculptBlockID)) {
                        selectorBoxX = e.getX();
                        selectorBoxY = e.getY();
                    }
                    showSelectionBox = true;
                }
            } else {
                showSelectionBox = false;
                displayOffsetX = displayOffsetX + (convertMouseXToValue(mouseStartX) - convertMouseXToValue(e.getX()));
                displayOffsetY = displayOffsetY + (convertMouseYToValue(mouseStartY) - convertMouseYToValue(e.getY()));
                adjustMouseStartsForPress(e.getX(), e.getY());
                calculateTics();
            }
            repaint();
        }
    }

    class MousePressedEventHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            if (mouseInHouse(e.getX(), e.getY()) && !e.isPrimaryButtonDown()) {
                adjustMouseStartsForPress(e.getX(), e.getY());
                inZoomBoxMode = false;
                showZoomBox = false;
            } else if (mouseInHouse(e.getX(), e.getY()) && e.isPrimaryButtonDown()) {
                inZoomBoxMode = true;
                showZoomBox = true;
                adjustMouseStartsForPress(e.getX(), e.getY());
                zoomBoxX = mouseStartX;
                zoomBoxY = mouseStartY;
            }
        }
    }


    class MousePressedEventHandlerSculpt implements EventHandler<MouseEvent> {
        /**
         * @param e the event which occurred
         */
        @Override
        public void handle(MouseEvent e) {
            if (mouseInHouse(e.getX(), e.getY()) && e.isPrimaryButtonDown()) {
                showSelectionBox = true;
                adjustMouseStartsForPress(e.getX(), e.getY());
                selectorBoxX = mouseStartX;
                selectorBoxY = mouseStartY;
                sculptBlockID = determineSculptBlock(e.getX());
                inSculptorMode = false;
                sculptBlock(false);
            } else {
                showSelectionBox = false;
                adjustMouseStartsForPress(e.getX(), e.getY());
            }
        }
    }

    class MouseReleasedEventHandler implements EventHandler<MouseEvent> {
        /**
         * @param e the event which occurred
         */
        @Override
        public void handle(MouseEvent e) {
            if (inZoomBoxMode && mouseInHouse(e.getX(), e.getY())) {
                showZoomBox = true;
                zoomBoxX = e.getX();
                zoomBoxY = e.getY();
                if ((zoomBoxX != mouseStartX) && (zoomBoxY != mouseStartY)) {
                    double timeLeft = convertMouseXToValue(Math.min(mouseStartX, zoomBoxX));
                    double timeRight = convertMouseXToValue(Math.max(mouseStartX, zoomBoxX));
                    int indexLeft = Math.max(1, Math.abs(Arrays.binarySearch(xAxisData, timeLeft))) - 1;
                    int indexRight = Math.max(2, Math.abs(Arrays.binarySearch(xAxisData, timeRight))) - 2;
                    if (indexRight < indexLeft) {
                        indexRight = indexLeft;
                    }
                    double intensityTop = convertMouseYToValue(Math.min(mouseStartY, zoomBoxY));
                    double intensityBottom = convertMouseYToValue(Math.max(mouseStartY, zoomBoxY));

                    displayOffsetX = xAxisData[indexLeft] - minX - 5;
                    maxX = xAxisData[indexRight] - displayOffsetX + 10;

                    minY = intensityBottom;
                    maxY = intensityTop;

                    double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.05);
                    maxY += yMarginStretch;
                    minY -= yMarginStretch;
                    displayOffsetY = 0.0;

                    refreshPanel(false, false);
                }
                adjustMouseStartsForPress(e.getX(), e.getY());
                zoomBoxX = mouseStartX;
                zoomBoxY = mouseStartY;

            } else {
                zoomBoxX = mouseStartX;
                zoomBoxY = mouseStartY;
            }
            repaint();
        }
    }

    class MouseReleasedEventHandlerSculpt implements EventHandler<MouseEvent> {
        /**
         * @param e the event which occurred
         */
        @Override
        public void handle(MouseEvent e) {
            boolean isPrimary = (0 == e.getButton().compareTo(MouseButton.PRIMARY));
            if (mouseInHouse(e.getX(), e.getY()) && isPrimary) {
                showSelectionBox = true;
                // process contained data points
                selectorBoxX = e.getX();
                selectorBoxY = e.getY();
                double timeLeft = convertMouseXToValue(Math.min(mouseStartX, selectorBoxX));
                double timeRight = convertMouseXToValue(Math.max(mouseStartX, selectorBoxX));
                int indexLeft = Math.max(1, Math.abs(Arrays.binarySearch(xAxisData, timeLeft))) - 1;
                int indexRight = Math.max(2, Math.abs(Arrays.binarySearch(xAxisData, timeRight))) - 2;
                if (indexRight < indexLeft) {
                    indexRight = indexLeft;
                }

                double intensityTop = convertMouseYToValue(Math.min(mouseStartY, selectorBoxY));
                double intensityBottom = convertMouseYToValue(Math.max(mouseStartY, selectorBoxY));

                for (int isotopeIndex = 0; isotopeIndex < speciesChecked.length; isotopeIndex++) {
                    if (speciesChecked[isotopeIndex]) {
                        List<Boolean> statusList = new ArrayList<>();
                        boolean[] includedPeakData = ((Analysis) speciesIntensityAnalysisBuilder.getAnalysis()).getMapOfBlockIdToIncludedPeakData().get(sculptBlockID)[isotopeIndex];
                        for (int index = indexLeft; index < indexRight; index++) {
                            if ((0 <= (index - countOfPreviousBlockIncludedData)) && ((index - countOfPreviousBlockIncludedData) < includedPeakData.length)) {
                                // faraday
                                if (showFaradays && (((yData[isotopeIndex * 4][index] < intensityTop) && (yData[isotopeIndex * 4][index] > intensityBottom) && !showResiduals)
                                        || ((residuals[isotopeIndex * 2][index] < intensityTop) && (residuals[isotopeIndex * 2][index] > intensityBottom) && showResiduals))
                                        && (0.0 != yData[isotopeIndex * 4][index])) {
                                    onPeakDataIncludedAllBlocks[isotopeIndex][index] = !onPeakDataIncludedAllBlocks[isotopeIndex][index];
                                    statusList.add(onPeakDataIncludedAllBlocks[isotopeIndex][index]);
                                    includedPeakData[index - countOfPreviousBlockIncludedData] = !includedPeakData[index - countOfPreviousBlockIncludedData];
                                }
                                // photoMultiplier
                                if (showPMs && (((yData[isotopeIndex * 4 + 2][index] < intensityTop) && (yData[isotopeIndex * 4 + 2][index] > intensityBottom) && !showResiduals)
                                        || ((residuals[isotopeIndex * 2 + 1][index] < intensityTop) && (residuals[isotopeIndex * 2 + 1][index] > intensityBottom) && showResiduals))
                                        && (0.0 != yData[isotopeIndex * 4 + 2][index])) {
                                    onPeakDataIncludedAllBlocks[isotopeIndex][index] = !onPeakDataIncludedAllBlocks[isotopeIndex][index];
                                    statusList.add(onPeakDataIncludedAllBlocks[isotopeIndex][index]);
                                    includedPeakData[index - countOfPreviousBlockIncludedData] = !includedPeakData[index - countOfPreviousBlockIncludedData];
                                }
                            }
                        }

                        boolean[] status = Booleans.toArray(statusList);
                        int countIncluded = Booleans.countTrue(status);
                        boolean majorityValue = countIncluded > status.length / 2;
                        for (int index = indexLeft; index <= indexRight; index++) {
                            if ((0 <= (index - countOfPreviousBlockIncludedData)) && ((index - countOfPreviousBlockIncludedData) < includedPeakData.length)) {
                                // faraday
                                if (showFaradays && (yData[isotopeIndex * 4][index] < intensityTop) && (yData[isotopeIndex * 4][index] > intensityBottom) && (0.0 != yData[isotopeIndex * 4][index])) {
                                    onPeakDataIncludedAllBlocks[isotopeIndex][index] = majorityValue;
                                    includedPeakData[index - countOfPreviousBlockIncludedData] = majorityValue;
                                    ((Analysis) speciesIntensityAnalysisBuilder.getAnalysis()).getMapOfBlockIdToIncludedPeakData().get(sculptBlockID)[isotopeIndex][index - countOfPreviousBlockIncludedData]
                                            = majorityValue;
                                }
                                // photomultiplier
                                if (showPMs && (yData[isotopeIndex * 4 + 2][index] < intensityTop) && (yData[isotopeIndex * 4 + 2][index] > intensityBottom) && (0.0 != yData[isotopeIndex * 4 + 2][index])) {
                                    onPeakDataIncludedAllBlocks[isotopeIndex][index] = majorityValue;
                                    includedPeakData[index - countOfPreviousBlockIncludedData] = majorityValue;
                                    ((Analysis) speciesIntensityAnalysisBuilder.getAnalysis()).getMapOfBlockIdToIncludedPeakData().get(sculptBlockID)[isotopeIndex][index - countOfPreviousBlockIncludedData]
                                            = majorityValue;
                                }
                            }
                        }
                    }
                }

                // update included vector per block
                Analysis analysis = ((Analysis) speciesIntensityAnalysisBuilder.getAnalysis());
                double[] xTimes = analysis.getMassSpecExtractedData().calculateSessionTimes();
                int[] blockIsotopeOrdinalIndicesArray = analysis.getMapOfBlockIdToRawData().get(sculptBlockID).blockIsotopeOrdinalIndicesArray();
                boolean[] includedIntensities = new boolean[blockIsotopeOrdinalIndicesArray.length];
                double[] blockTimeArray = analysis.getMapOfBlockIdToRawData().get(sculptBlockID).blockTimeArray();

                for (int index = 0; index < blockIsotopeOrdinalIndicesArray.length; index++) {
                    int isotopeIndex = blockIsotopeOrdinalIndicesArray[index] - 1;
                    if (isotopeIndex >= 0) {
                        double time = blockTimeArray[index];
                        int timeIndx = binarySearch(xTimes, time);
                        includedIntensities[index] = onPeakDataIncludedAllBlocks[isotopeIndex][timeIndx];
                    }
                }
                analysis.getMapOfBlockIdToIncludedIntensities().put(sculptBlockID, includedIntensities);

            } else {
                showSelectionBox = false;
            }
            adjustMouseStartsForPress(e.getX(), e.getY());
            selectorBoxX = mouseStartX;
            selectorBoxY = mouseStartY;

            repaint();
        }
    }
}