package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots;

import com.google.common.primitives.Booleans;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.analysisPlotBuilders.SpeciesIntensityAnalysisBuilder;
import org.cirdles.tripoli.sessions.analysis.Analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.StrictMath.*;

public class SpeciesIntensityAnalysisPlot extends AbstractPlot {
    private final SpeciesIntensityAnalysisBuilder speciesIntensityAnalysisBuilder;
    private final double[][] dfGain;
    private final double[][] yDataCounts;
    private final double[][] ampResistance;
    private final double[][] baseLine;
    TripoliConstants.IntensityUnits intensityUnits = TripoliConstants.IntensityUnits.COUNTS;
    EventHandler<MouseEvent> mousePressedEventHandler = e -> {
        if (mouseInHouse(e.getX(), e.getY()) && e.isPrimaryButtonDown()) {
            if (e.getSource() instanceof BlockRatioCyclesAnalysisPlot sourceBlockRatioCyclesAnalysisPlot) {
                sourceBlockRatioCyclesAnalysisPlot.getParentWallPane().synchronizeMouseStartsOnPress(e.getX(), e.getY());
            } else {
                adjustMouseStartsForPress(e.getX(), e.getY());
            }
        }
    };
    private double[][] yData;
    private boolean[][] onPeakDataIncludedAllBlocks;
    private boolean[] speciesChecked;
    private boolean showFaradays;
    private boolean showPMs;
    private boolean showModels;
    private boolean baselineCorr;
    private boolean gainCorr;
    private boolean logScale;
    private boolean[] zoomFlagsXY;

    private double selectorBoxX = 0.0;
    private double selectorBoxY = 0.0;

    private boolean inSculptorMode = false;
    private int sculptBlockID = 0;
    private int countOfPreviousBlockIncludedData;

    private SpeciesIntensityAnalysisPlot(Rectangle bounds, SpeciesIntensityAnalysisBuilder speciesIntensityAnalysisBuilder) {
        super(bounds, 100, 25,
                speciesIntensityAnalysisBuilder.getTitle(),
                speciesIntensityAnalysisBuilder.getxAxisLabel(),
                speciesIntensityAnalysisBuilder.getyAxisLabel());
        this.speciesIntensityAnalysisBuilder = speciesIntensityAnalysisBuilder;
        yDataCounts = speciesIntensityAnalysisBuilder.getyData();
        ampResistance = speciesIntensityAnalysisBuilder.getAmpResistance();
        baseLine = speciesIntensityAnalysisBuilder.getBaseLine();
        dfGain = speciesIntensityAnalysisBuilder.getDfGain();
        speciesChecked = new boolean[yDataCounts.length / 4];
        speciesChecked[0] = true;
        showFaradays = true;
        showPMs = true;
        showModels = true;
        baselineCorr = true;
        gainCorr = true;
        logScale = false;
        zoomFlagsXY = new boolean[]{true, true};


        setOnMouseClicked(new MouseClickEventHandler());
//
//        removeEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedEventHandler);
//        setOnMouseDragged(new MouseDraggedEventHandler());
//
//        removeEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
//        setOnMousePressed(new MousePressedEventHandler());
//
//        setOnMouseReleased(new MouseReleasedEventHandler());

    }

    public static AbstractPlot generatePlot(Rectangle bounds, SpeciesIntensityAnalysisBuilder speciesIntensityAnalysisBuilder) {
        return new SpeciesIntensityAnalysisPlot(bounds, speciesIntensityAnalysisBuilder);
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

    @Override
    public void preparePanel(boolean reScaleX, boolean reScaleY) {
        xAxisData = speciesIntensityAnalysisBuilder.getxData();
        if (reScaleX) {
            minX = xAxisData[0];
            maxX = xAxisData[xAxisData.length - 1];

            displayOffsetX = 0.0;
            inSculptorMode = false;
            sculptBlockID = 0;
            addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedEventHandler);
            setOnMouseDragged(null);
            addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
            setOnMousePressed(null);
            setOnMouseReleased(null);
            addEventFilter(ScrollEvent.SCROLL, scrollEventEventHandler);

            plotContextMenuItemSculpt.setText("Sculpt this block");
        }


        yData = new double[yDataCounts.length][yDataCounts[0].length];
        onPeakDataIncludedAllBlocks = speciesIntensityAnalysisBuilder.getOnPeakDataIncludedAllBlocks();

        for (int row = 0; row < yData.length; row++) {
            int speciesIndex = (row / 4);
            if (speciesChecked[speciesIndex]) {
                for (int col = 0; col < yData[0].length; col++) {
                    yData[row][col] = yDataCounts[row][col];
                    if (yDataCounts[row][col] != 0.0) {
                        if (baselineCorr) {
                            yData[row][col] -= baseLine[row][col];
                        }

                        if ((gainCorr) && (dfGain[row][col] != 0.0)) {
                            yData[row][col] /= dfGain[row][col];
                        }

                        if (logScale) {
                            yData[row][col] = (yData[row][col] > 0.0) ? log(yData[row][col]) : 0.0;
                        }
                    }
                }
            }
        }

        switch (intensityUnits) {
            case VOLTS -> {
                plotAxisLabelY = "Intensity (volts)";
                for (int row = 0; row < yData.length; row++) {
                    yData[row] = TripoliConstants.IntensityUnits.convertFromCountsToVolts(yData[row], ampResistance[row / 4]);
                }
            }
            case AMPS -> {
                plotAxisLabelY = "Intensity (amps)";
                for (int row = 0; row < yData.length; row++) {
                    yData[row] = TripoliConstants.IntensityUnits.convertFromCountsToAmps(yData[row]);
                }
            }
            case COUNTS -> {
                plotAxisLabelY = "Intensity (counts)";
            }
        }

        if (reScaleY) {
            minY = Double.MAX_VALUE;
            maxY = -Double.MAX_VALUE;

            for (int row = 0; row < yData.length; row++) {
                int speciesIndex = (row / 4);
                if (speciesChecked[speciesIndex]) {
                    boolean plotFaradays = (showFaradays && (row >= speciesIndex * 4) && (row <= speciesIndex * 4 + 1));
                    boolean plotPMs = (showPMs && (row >= speciesIndex * 4 + 2) && (row <= speciesIndex * 4 + 3));
                    for (int col = 0; col < yData[row].length; col++) {
                        if ((yData[row][col] != 0.0) && (plotFaradays || plotPMs)) {
                            minY = min(minY, yData[row][col]);
                            maxY = max(maxY, yData[row][col]);
                        }
                    }
                }
            }

            displayOffsetY = 0.0;
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
            minX -= xMarginStretch;
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

        Color[] isotopeColors = {Color.BLUE, Color.GREEN, Color.BLACK, Color.PURPLE, Color.ORANGE};
        for (int isotopePlotSetIndex = 0; isotopePlotSetIndex < yData.length / 4; isotopePlotSetIndex++) {
            if (speciesChecked[isotopePlotSetIndex]) {
                // plot PM
                if (showPMs) {
                    g2d.closePath();
                    g2d.setLineDashes(0);
                    boolean startedPlot = false;
                    g2d.setFill(isotopeColors[isotopePlotSetIndex]);
                    g2d.setStroke(isotopeColors[isotopePlotSetIndex]);
                    for (int i = 0; i < xAxisData.length; i++) {
                        if ((yData[isotopePlotSetIndex * 4 + 2][i] != 0.0) && pointInPlot(xAxisData[i], yData[isotopePlotSetIndex * 4 + 2][i])) {
                            double dataX = mapX(xAxisData[i]);
                            double dataY = mapY(yData[isotopePlotSetIndex * 4 + 2][i]);
                            if (onPeakDataIncludedAllBlocks[isotopePlotSetIndex][i]) {
                                g2d.fillOval(dataX - 2.0, Math.abs(dataY) - 2.0, 4, 4);
                            } else {
                                g2d.setFill(Color.RED);
                                g2d.fillOval(dataX - 2.0, Math.abs(dataY) - 2.0, 4, 4);
                                g2d.setFill(isotopeColors[isotopePlotSetIndex]);
                            }
                        }

                        g2d.setLineWidth(2.0);
                        if (showModels && !gainCorr) {
                            if ((i < xAxisData.length - 1) && (10.0 > xAxisData[i + 1] - xAxisData[i])) {
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
                    g2d.setStroke(isotopeColors[isotopePlotSetIndex]);
                }
                // plot Faraday
                if (showFaradays) {
                    g2d.closePath();
                    g2d.setLineDashes(0);
                    boolean startedPlot = false;
                    g2d.setFill(isotopeColors[isotopePlotSetIndex]);
                    g2d.setStroke(isotopeColors[isotopePlotSetIndex]);
                    for (int i = 0; i < xAxisData.length; i++) {
                        if ((yData[isotopePlotSetIndex * 4][i] != 0.0) && pointInPlot(xAxisData[i], yData[isotopePlotSetIndex * 4][i])) {
                            double dataX = mapX(xAxisData[i]);
                            double dataY = mapY(yData[isotopePlotSetIndex * 4][i]);
                            if (onPeakDataIncludedAllBlocks[isotopePlotSetIndex][i]) {
                                g2d.fillOval(dataX - 2.0, Math.abs(dataY) - 2.0, 4, 4);
                            } else {
                                g2d.setFill(Color.RED);
                                g2d.fillOval(dataX - 2.0, Math.abs(dataY) - 2.0, 4, 4);
                                g2d.setFill(isotopeColors[isotopePlotSetIndex]);
                            }
                        }

                        if (showModels) {
                            // TODO: make this 10.0 more robust for finding block separations
                            if ((i < xAxisData.length - 1) && (10.0 > xAxisData[i + 1] - xAxisData[i])) {
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
                    g2d.setStroke(isotopeColors[isotopePlotSetIndex]);
                }
            }
        }

        if (inSculptorMode) {
            //plot selectorbox
            g2d.setStroke(Color.RED);
            g2d.setLineWidth(0.5);
            g2d.strokeRect(Math.min(mouseStartX, selectorBoxX), Math.min(mouseStartY, selectorBoxY), Math.abs(selectorBoxX - mouseStartX), Math.abs(selectorBoxY - mouseStartY));
        }


        // block delimiters
        int[] xAxisBlockIDs = speciesIntensityAnalysisBuilder.getxAxisBlockIDs();
        g2d.setStroke(Color.BLACK);
        g2d.setLineWidth(0.5);
        int blockID = 0;
        for (int i = 0; i < xAxisBlockIDs.length; i++) {
            double dataX = mapX(xAxisData[i]) - 5.0;
            if (!inSculptorMode && (xAxisBlockIDs[i] > blockID) && xInPlot(xAxisData[i])) {
                g2d.strokeLine(dataX, topMargin + plotHeight, dataX, topMargin);
            }
            if (xAxisBlockIDs[i] > blockID) {
                blockID++;
                if (xInPlot(xAxisData[i])) {
                    showBlockID(g2d, Integer.toString(blockID), mapX(xAxisData[i]));
                }
            }

        }
        if (!inSculptorMode) {
            double dataX = mapX(xAxisData[xAxisData.length - 1]) + 5;
            g2d.strokeLine(dataX, topMargin + plotHeight, dataX, topMargin);
        }
    }

    private void showBlockID(GraphicsContext g2d, String blockID, double xPosition) {
        Paint savedPaint = g2d.getFill();
        g2d.setFill(Paint.valueOf("BLACK"));
        g2d.setFont(Font.font("SansSerif", 10));

        g2d.fillText("BL#" + blockID, xPosition, topMargin);
        g2d.setFill(savedPaint);
    }

    @Override
    public void plotStats(GraphicsContext g2d) {

    }

    @Override
    public void setupPlotContextMenu() {
        plotContextMenu = new ContextMenu();
        plotContextMenuItemSculpt = new MenuItem("Sculpt this block");
        plotContextMenuItemSculpt.setOnAction((mouseEvent) -> {
            if (sculptBlockID > 0) {
                inSculptorMode = true;
                removeEventFilter(ScrollEvent.SCROLL, scrollEventEventHandler);
                removeEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedEventHandler);
                setOnMouseDragged(new MouseDraggedEventHandler());
                removeEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
                setOnMousePressed(new MousePressedEventHandler());
                setOnMouseReleased(new MouseReleasedEventHandler());
                selectorBoxX = mouseStartX;
                selectorBoxY = mouseStartY;
                // zoom into block
                countOfPreviousBlockIncludedData = 0;
                for (int prevBlockID = 1; prevBlockID < sculptBlockID; prevBlockID++) {
                    countOfPreviousBlockIncludedData += ((Analysis) speciesIntensityAnalysisBuilder.getAnalysis()).getMapOfBlockIdToIncludedPeakData().get(prevBlockID)[0].length;
                }
                displayOffsetX = xAxisData[countOfPreviousBlockIncludedData] - minX - 5;
                // find index of last intensity in block is found in photoMultiplier data sculptedSpeciesIndex * 4 + 2
                int sculptedSpeciesIndex = Booleans.indexOf(speciesChecked, true);
                int countOfIntensities = ((Analysis) speciesIntensityAnalysisBuilder.getAnalysis()).getMapOfBlockIdToIncludedPeakData().get(sculptBlockID)[0].length;
                for (int i = ((Analysis) speciesIntensityAnalysisBuilder.getAnalysis()).getMapOfBlockIdToIncludedPeakData().get(sculptBlockID)[0].length; i > 0; i--) {
                    if (yData[sculptedSpeciesIndex * 4 + 2][countOfPreviousBlockIncludedData + i - 1] == 0.0) {
                        countOfIntensities--;
                    } else
                        break;
                }
                maxX = xAxisData[countOfPreviousBlockIncludedData
                        + countOfIntensities - 1]
                        - displayOffsetX + 5;

                minY = Double.MAX_VALUE;
                maxY = -Double.MAX_VALUE;
                for (int i = 1; i < ((Analysis) speciesIntensityAnalysisBuilder.getAnalysis()).getMapOfBlockIdToIncludedPeakData().get(sculptBlockID)[0].length; i++) {
                    // faraday
                    if (yData[sculptedSpeciesIndex * 4][countOfPreviousBlockIncludedData + i - 1] != 0.0) {
                        minY = Math.min(minY, yData[sculptedSpeciesIndex * 4][countOfPreviousBlockIncludedData + i - 1]);
                        maxY = Math.max(maxY, yData[sculptedSpeciesIndex * 4][countOfPreviousBlockIncludedData + i - 1]);
                    }
                    // photoMultiplier
                    if (yData[sculptedSpeciesIndex * 4 + 2][countOfPreviousBlockIncludedData + i - 1] != 0.0) {
                        minY = Math.min(minY, yData[sculptedSpeciesIndex * 4 + 2][countOfPreviousBlockIncludedData + i - 1]);
                        maxY = Math.max(maxY, yData[sculptedSpeciesIndex * 4 + 2][countOfPreviousBlockIncludedData + i - 1]);
                    }
                }
                double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.05);
                maxY += yMarginStretch;
                minY -= yMarginStretch;
                displayOffsetY = 0.0;

                plotContextMenuItemSculpt.setText("Scale to fit this block");

                refreshPanel(false, false);
            } else {
                inSculptorMode = false;
                addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedEventHandler);
                setOnMouseDragged(null);
                addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
                setOnMousePressed(null);
                setOnMouseReleased(null);
                addEventFilter(ScrollEvent.SCROLL, scrollEventEventHandler);
                plotContextMenuItemSculpt.setText("Sculpt this block");
            }
            repaint();
        });
        plotContextMenu.getItems().addAll(plotContextMenuItemSculpt);
    }

    private int onlyOneSpeciesShown() {
        int isotopeIndex = -1;
        if (Booleans.countTrue(speciesChecked) == 1) {
            isotopeIndex = Booleans.indexOf(speciesChecked, true);
        }

        return isotopeIndex;
    }

    class MouseClickEventHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent mouseEvent) {
            plotContextMenu.hide();
            boolean isPrimary = (0 == mouseEvent.getButton().compareTo(MouseButton.PRIMARY));
            if (!isPrimary) {
                plotContextMenu.show((Node) mouseEvent.getSource(), Side.LEFT, mouseEvent.getX() - getLayoutX(), mouseEvent.getY() - getLayoutY());
                sculptBlockID = speciesIntensityAnalysisBuilder.getxAxisBlockIDs()
                        [Math.max(2, Math.abs(Arrays.binarySearch(xAxisData, convertMouseXToValue(mouseEvent.getX())))) - 2];
            }

            repaint();
        }
    }

    class MouseDraggedEventHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            if (mouseInHouse(e.getX(), e.getY()) && onlyOneSpeciesShown() >= 0) {
                selectorBoxX = e.getX();
                selectorBoxY = e.getY();
                repaint();
            }
        }
    }

    class MousePressedEventHandler implements EventHandler<MouseEvent> {
        /**
         * @param e the event which occurred
         */
        @Override
        public void handle(MouseEvent e) {
            if (mouseInHouse(e.getX(), e.getY()) && e.isPrimaryButtonDown()) {
                adjustMouseStartsForPress(e.getX(), e.getY());
                selectorBoxX = mouseStartX;
                selectorBoxY = mouseStartY;
            }
        }
    }

    class MouseReleasedEventHandler implements EventHandler<MouseEvent> {
        /**
         * @param e the event which occurred
         */
        @Override
        public void handle(MouseEvent e) {
            boolean isPrimary = (0 == e.getButton().compareTo(MouseButton.PRIMARY));
            if (mouseInHouse(e.getX(), e.getY()) && isPrimary) {
                int isotopeIndex = onlyOneSpeciesShown();
                if (isotopeIndex >= 0) {
                    // process contained datapoints
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


                    List<Boolean> statusList = new ArrayList<>();
                    for (int index = indexLeft; index <= indexRight; index++) {
                        // faraday
                        if ((yData[isotopeIndex * 4][index] < intensityTop) && (yData[isotopeIndex * 4][index] > intensityBottom) && (yData[isotopeIndex * 4][index] != 0.0)) {
                            onPeakDataIncludedAllBlocks[isotopeIndex][index] = !onPeakDataIncludedAllBlocks[isotopeIndex][index];
                            statusList.add(onPeakDataIncludedAllBlocks[isotopeIndex][index]);

                            ((Analysis) speciesIntensityAnalysisBuilder.getAnalysis()).getMapOfBlockIdToIncludedPeakData().get(sculptBlockID)[isotopeIndex][index - countOfPreviousBlockIncludedData]
                                    = !((Analysis) speciesIntensityAnalysisBuilder.getAnalysis()).getMapOfBlockIdToIncludedPeakData().get(sculptBlockID)[isotopeIndex][index - countOfPreviousBlockIncludedData];
                        }
                        // photoMultiplier
                        if ((yData[isotopeIndex * 4 + 2][index] < intensityTop) && (yData[isotopeIndex * 4 + 2][index] > intensityBottom) && (yData[isotopeIndex * 4 + 2][index] != 0.0)) {
                            onPeakDataIncludedAllBlocks[isotopeIndex][index] = !onPeakDataIncludedAllBlocks[isotopeIndex][index];
                            statusList.add(onPeakDataIncludedAllBlocks[isotopeIndex][index]);

                            ((Analysis) speciesIntensityAnalysisBuilder.getAnalysis()).getMapOfBlockIdToIncludedPeakData().get(sculptBlockID)[isotopeIndex][index - countOfPreviousBlockIncludedData]
                                    = !((Analysis) speciesIntensityAnalysisBuilder.getAnalysis()).getMapOfBlockIdToIncludedPeakData().get(sculptBlockID)[isotopeIndex][index - countOfPreviousBlockIncludedData];
                        }
                    }
                    boolean majorityValue = false;
                    boolean[] status = Booleans.toArray(statusList);
                    int countIncluded = Booleans.countTrue(status);
                    if ((countIncluded > 0) && (countIncluded > status.length / 2)) {
                        majorityValue = countIncluded > status.length / 2;
                        for (int index = indexLeft; index <= indexRight; index++) {
                            // faraday
                            if ((yData[isotopeIndex * 4][index] < intensityTop) && (yData[isotopeIndex * 4][index] > intensityBottom) && (yData[isotopeIndex * 4][index] != 0.0)) {
                                onPeakDataIncludedAllBlocks[isotopeIndex][index] = majorityValue;

                                ((Analysis) speciesIntensityAnalysisBuilder.getAnalysis()).getMapOfBlockIdToIncludedPeakData().get(sculptBlockID)[isotopeIndex][index - countOfPreviousBlockIncludedData]
                                        = majorityValue;
                            }
                            // photomultiplier
                            if ((yData[isotopeIndex * 4 + 2][index] < intensityTop) && (yData[isotopeIndex * 4 + 2][index] > intensityBottom) && (yData[isotopeIndex * 4 + 2][index] != 0.0)) {
                                onPeakDataIncludedAllBlocks[isotopeIndex][index] = majorityValue;

                                ((Analysis) speciesIntensityAnalysisBuilder.getAnalysis()).getMapOfBlockIdToIncludedPeakData().get(sculptBlockID)[isotopeIndex][index - countOfPreviousBlockIncludedData]
                                        = majorityValue;
                            }
                        }
                    }
                }

                // And the onPeakDataIncludedAllBlocks arrays EXPERIMENT - needs to be separated by far and pm
                boolean[] summaryIncluded = new boolean[onPeakDataIncludedAllBlocks[0].length];
                Arrays.fill(summaryIncluded, true);
                for (int i = 0; i < onPeakDataIncludedAllBlocks.length; i++) {
                    for (int j = 0; j < onPeakDataIncludedAllBlocks[i].length; j++) {
                        summaryIncluded[j] = summaryIncluded[j] & onPeakDataIncludedAllBlocks[i][j];
                    }
                }

                repaint();

                adjustMouseStartsForPress(e.getX(), e.getY());
                selectorBoxX = mouseStartX;
                selectorBoxY = mouseStartY;
            }
        }
    }
}