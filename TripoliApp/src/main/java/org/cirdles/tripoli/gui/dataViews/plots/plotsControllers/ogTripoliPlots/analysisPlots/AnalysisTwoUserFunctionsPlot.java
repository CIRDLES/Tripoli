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

import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
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
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.gui.dataViews.plots.*;
import org.cirdles.tripoli.gui.utilities.ViridisColorPalette;
import org.cirdles.tripoli.plots.analysisPlotBuilders.AnalysisBlockCyclesRecord;
import org.cirdles.tripoli.plots.compoundPlotBuilders.PlotBlockCyclesRecord;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.AnalysisStatsRecord;
import org.cirdles.tripoli.sessions.analysis.BlockStatsRecord;
import org.cirdles.tripoli.sessions.analysis.GeometricMeanStatsRecord;
import org.cirdles.tripoli.utilities.mathUtilities.FormatterForSigFigN;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.StrictMath.*;
import static java.util.Map.entry;
import static org.cirdles.tripoli.utilities.mathUtilities.FormatterForSigFigN.countOfTrailingDigitsForSigFig;
import static org.cirdles.tripoli.utilities.mathUtilities.MathUtilities.applyChauvenetsCriterion;

/**
 * @author James F. Bowring
 */
public class AnalysisTwoUserFunctionsPlot extends AbstractPlot implements AnalysisBlockCyclesPlotI {
    private final Tooltip tooltip;
    private final String tooltipTextSculpt = "Left mouse: cntrl click toggles block, Dbl-click to Sculpt data. Right mouse: cntrl click zooms one block, Dbl-click toggles full view.";
    private final String tooltipTextExitSculpt = "Left mouse: cntrl click toggles block, Dbl-click Exits Sculpting. Right mouse: cntrl click zooms one block, Dbl-click toggles full view.";
    private AnalysisInterface analysis;
    private Map<Integer, PlotBlockCyclesRecord> mapBlockIdToBlockCyclesRecord;
    private Map<Integer, PlotBlockCyclesRecord> mapBlockIdToBlockCyclesRecordX;
    private Map<Integer, PlotBlockCyclesRecord> mapBlockIdToBlockCyclesRecordIntensity;
    private UserFunction userFunction;
    private UserFunction xAxisUserFunction;
    private UserFunction intensityUserFunction;

    private double[] oneSigmaForCycles;
    private double[] intensityData;
    private double minIntensity;
    private double maxIntensity;
    private boolean logScale;
    private boolean[] zoomFlagsXY;
    private PlotWallPaneInterface parentWallPane;
    private boolean isRatio;
    private boolean blockMode;
    private AnalysisStatsRecord analysisStatsRecord;
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
    private boolean ignoreRejects;
    
    private AnalysisTwoUserFunctionsPlot(
            AnalysisInterface analysis,
            Rectangle bounds,
            UserFunction userFunction,
            UserFunction xAxisUserFunction,
            UserFunction intensityUserFunction,
            PlotWallPane parentWallPane) {
        super(bounds,
                185, 25, // Copy PlotOG stats margins
                new String[]{userFunction.getName()
                        + "  " + "x\u0304" + "= 0"
                        , "\u00B1" + " 0"},
                xAxisUserFunction.getName() + " vs " + userFunction.getName(),
                userFunction.isTreatAsIsotopicRatio() ? "Ratio" : "Function");
        this.analysis = analysis;
        this.userFunction = userFunction;
        this.xAxisUserFunction = xAxisUserFunction;
        this.intensityUserFunction = intensityUserFunction;
        this.mapBlockIdToBlockCyclesRecord = userFunction.getMapBlockIdToBlockCyclesRecord();
        this.mapBlockIdToBlockCyclesRecordX = xAxisUserFunction.getMapBlockIdToBlockCyclesRecord();
        if (intensityUserFunction != null) {
            this.mapBlockIdToBlockCyclesRecordIntensity = intensityUserFunction.getMapBlockIdToBlockCyclesRecord();
        } else {
            this.mapBlockIdToBlockCyclesRecordIntensity = null;
        }
        this.logScale = false;
        this.zoomFlagsXY = new boolean[]{true, true};
        this.parentWallPane = parentWallPane;
        this.blockMode = userFunction.getReductionMode().equals(TripoliConstants.ReductionModeEnum.BLOCK);
        this.isRatio = userFunction.isTreatAsIsotopicRatio();
        this.ignoreRejects = false;
        this.intensityData = null;
        this.minIntensity = 0.0;
        this.maxIntensity = 0.0;

        tooltip = new Tooltip(tooltipTextSculpt);
        Tooltip.install(this, tooltip);

        setOnMouseClicked(new MouseClickEventHandler());
    }

    public static AbstractPlot generatePlot(
            Rectangle bounds, AnalysisInterface analysis, UserFunction userFunction,
            UserFunction xAxisUserFunction, UserFunction intensityUserFunction, PlotWallPane parentWallPane) {
        return new AnalysisTwoUserFunctionsPlot(analysis, bounds, userFunction, xAxisUserFunction, intensityUserFunction, parentWallPane);
    }

    public PlotWallPaneInterface getParentWallPane() {
        return parentWallPane;
    }

    public void setLogScale(boolean logScale) {
        this.logScale = logScale;
    }

    /**
     * @return
     */
    @Override
    public boolean getBlockMode() {
        return blockMode;
    }

    public void setBlockMode(boolean blockMode) {
        this.blockMode = blockMode;
    }

    public void setZoomFlagsXY(boolean[] zoomFlagsXY) {
        this.zoomFlagsXY = zoomFlagsXY;
    }

    public void resetBlockMode() {
        blockMode = userFunction.isTreatAsIsotopicRatio();
    }

    @Override
    public void preparePanel(boolean reScaleX, boolean reScaleY) {
        selectorBoxX = mouseStartX;
        selectorBoxY = mouseStartY;
        zoomBoxX = mouseStartX;
        zoomBoxY = mouseStartY;

        // process blocks - ensure both user functions have compatible structures
        int cyclesPerBlock = mapBlockIdToBlockCyclesRecord.get(1).cyclesIncluded().length;

        if (reScaleX) {
            // Calculate total data length from y-axis user function (both should match)
            int xDataLength = 0;
            for (Map.Entry<Integer, PlotBlockCyclesRecord> entry : mapBlockIdToBlockCyclesRecord.entrySet()) {
                xDataLength += entry.getValue().cycleMeansData().length;
            }
            xAxisData = new double[xDataLength];
            
            // Populate x-axis data from xAxisUserFunction
            boolean doInvertX = xAxisUserFunction.isInverted() && xAxisUserFunction.isTreatAsIsotopicRatio();
            for (Map.Entry<Integer, PlotBlockCyclesRecord> entry : mapBlockIdToBlockCyclesRecordX.entrySet()) {
                PlotBlockCyclesRecord plotBlockCyclesRecordX = entry.getValue();
                if (plotBlockCyclesRecordX != null) {
                    int availableCyclesPerBlock = plotBlockCyclesRecordX.cycleMeansData().length;
                    if (doInvertX) {
                        System.arraycopy(plotBlockCyclesRecordX.invertedCycleMeansData(), 0, xAxisData, (plotBlockCyclesRecordX.blockID() - 1) * cyclesPerBlock, availableCyclesPerBlock);
                    } else {
                        System.arraycopy(plotBlockCyclesRecordX.cycleMeansData(), 0, xAxisData, (plotBlockCyclesRecordX.blockID() - 1) * cyclesPerBlock, availableCyclesPerBlock);
                    }
                }
            }

            // Apply log scale to x-axis if enabled
            if (logScale && xAxisUserFunction.isTreatAsIsotopicRatio()) {
                for (int i = 0; i < xAxisData.length; i++) {
                    xAxisData[i] = (xAxisData[i] > 0.0) ? log(xAxisData[i]) : 0.0;
                }
            }

            // Initialize mouse event handlers
            displayOffsetX = 0.0;
            inSculptorMode = false;
            sculptBlockID = 0;
            showSelectionBox = false;
            removeEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedEventHandler);
            setOnMouseDragged(new AnalysisTwoUserFunctionsPlot.MouseDraggedEventHandler());
            setOnMousePressed(new AnalysisTwoUserFunctionsPlot.MousePressedEventHandler());
            setOnMouseReleased(new AnalysisTwoUserFunctionsPlot.MouseReleasedEventHandler());
            addEventFilter(ScrollEvent.SCROLL, scrollEventEventHandler);

            // Calculate min/max for x-axis
            minX = Double.MAX_VALUE;
            maxX = -Double.MAX_VALUE;
            for (double xAxisDatum : xAxisData) {
                if (xAxisDatum != 0.0) {
                    minX = min(minX, xAxisDatum);
                    maxX = max(maxX, xAxisDatum);
                }
            }
        }

        // Populate y-axis data
        yAxisData = new double[xAxisData.length];
        oneSigmaForCycles = new double[xAxisData.length];
        boolean doInvertY = userFunction.isInverted() && userFunction.isTreatAsIsotopicRatio();
        for (Map.Entry<Integer, PlotBlockCyclesRecord> entry : mapBlockIdToBlockCyclesRecord.entrySet()) {
            PlotBlockCyclesRecord plotBlockCyclesRecord = entry.getValue();
            if (plotBlockCyclesRecord != null) {
                int availableCyclesPerBlock = plotBlockCyclesRecord.cycleMeansData().length;
                if (doInvertY) {
                    System.arraycopy(plotBlockCyclesRecord.invertedCycleMeansData(), 0, yAxisData, (plotBlockCyclesRecord.blockID() - 1) * cyclesPerBlock, availableCyclesPerBlock);
                } else {
                    System.arraycopy(plotBlockCyclesRecord.cycleMeansData(), 0, yAxisData, (plotBlockCyclesRecord.blockID() - 1) * cyclesPerBlock, availableCyclesPerBlock);
                }
                System.arraycopy(plotBlockCyclesRecord.cycleOneSigmaData(), 0, oneSigmaForCycles, (plotBlockCyclesRecord.blockID() - 1) * cyclesPerBlock, availableCyclesPerBlock);
            }
        }

        // Populate intensity data if intensity user function is provided
        if (intensityUserFunction != null && mapBlockIdToBlockCyclesRecordIntensity != null) {
            intensityData = new double[xAxisData.length];
            boolean doInvertIntensity = intensityUserFunction.isInverted() && intensityUserFunction.isTreatAsIsotopicRatio();
            for (Map.Entry<Integer, PlotBlockCyclesRecord> entry : mapBlockIdToBlockCyclesRecordIntensity.entrySet()) {
                PlotBlockCyclesRecord plotBlockCyclesRecordIntensity = entry.getValue();
                if (plotBlockCyclesRecordIntensity != null) {
                    int availableCyclesPerBlock = plotBlockCyclesRecordIntensity.cycleMeansData().length;
                    if (doInvertIntensity) {
                        System.arraycopy(plotBlockCyclesRecordIntensity.invertedCycleMeansData(), 0, intensityData, (plotBlockCyclesRecordIntensity.blockID() - 1) * cyclesPerBlock, availableCyclesPerBlock);
                    } else {
                        System.arraycopy(plotBlockCyclesRecordIntensity.cycleMeansData(), 0, intensityData, (plotBlockCyclesRecordIntensity.blockID() - 1) * cyclesPerBlock, availableCyclesPerBlock);
                    }
                }
            }

            // Calculate min/max intensity values for normalization
            minIntensity = Double.MAX_VALUE;
            maxIntensity = -Double.MAX_VALUE;
            for (double intensityDatum : intensityData) {
                if (intensityDatum != 0.0) {
                    minIntensity = min(minIntensity, intensityDatum);
                    maxIntensity = max(maxIntensity, intensityDatum);
                }
            }
            // Handle case where all values are the same or zero
            if (minIntensity == Double.MAX_VALUE || maxIntensity == -Double.MAX_VALUE || minIntensity == maxIntensity) {
                minIntensity = 0.0;
                maxIntensity = 1.0;
            }
        } else {
            intensityData = null;
            minIntensity = 0.0;
            maxIntensity = 0.0;
        }

        // Set axis labels with user function names
        String xAxisName = xAxisUserFunction.showCorrectName();
        if (xAxisUserFunction.isTreatAsCustomExpression()) {
            xAxisName = xAxisUserFunction.getCustomExpression().getName();
        }
        plotAxisLabelX = xAxisName;
        
        String yAxisName = userFunction.showCorrectName();
        if (userFunction.isTreatAsCustomExpression()) {
            yAxisName = userFunction.getCustomExpression().getName();
        }
        plotAxisLabelY = yAxisName;

        if (reScaleY || ignoreRejects) {
            // Calculate min/max for y-axis across all included blocks
            minY = Double.MAX_VALUE;
            maxY = -Double.MAX_VALUE;

            for (int i = 0; i < yAxisData.length; i++) {
                int blockIndex = i / cyclesPerBlock;
                if ((yAxisData[i] != 0.0) && (!ignoreRejects || mapBlockIdToBlockCyclesRecord.get(blockIndex + 1).cyclesIncluded()[i % cyclesPerBlock])) {
                    minY = min(minY, yAxisData[i] - oneSigmaForCycles[i]);
                    maxY = max(maxY, yAxisData[i] + oneSigmaForCycles[i]);
                }
            }

            displayOffsetY = 0.0;
        }
        prepareExtents(reScaleX, reScaleY);
        showXaxis = true;
        showStats = false;
        calcStats();
        calculateTics();
        repaint();
    }

    /**
     * @param g2d
     */
    @Override
    public void labelAxisY(GraphicsContext g2d) {
        Paint savedPaint = g2d.getFill();
        g2d.setFill(Paint.valueOf("BLACK"));
        g2d.setFont(Font.font("SansSerif", 14));
        Text text = new Text();
        text.setFont(Font.font("SansSerif", 14));
        text.setText(plotAxisLabelY);
        int textWidth = (int) text.getLayoutBounds().getWidth();
        g2d.rotate(-90.0);
        g2d.fillText(text.getText(), -(2.0 * topMargin + plotHeight) / 2.0 - textWidth / 2.0, leftMargin - 45);
        g2d.rotate(90.0);
        g2d.setFill(savedPaint);
    }

    @Override
    public void calculateTics() {
        super.calculateTics();

        zoomChunkX = zoomFlagsXY[0] ? zoomChunkX : 0.0;
        zoomChunkY = zoomFlagsXY[1] ? zoomChunkY : 0.0;
    }

    /**
     * Finds the actual min/max x-axis values in the data array.
     * Unlike the parent class which assumes xAxisData contains sorted indices,
     * this plot uses user function values that may not be sorted.
     */
    private double[] getDataMinMaxX() {
        double dataMinX = Double.MAX_VALUE;
        double dataMaxX = -Double.MAX_VALUE;
        for (double xAxisDatum : xAxisData) {
            if (xAxisDatum != 0.0) {
                dataMinX = min(dataMinX, xAxisDatum);
                dataMaxX = max(dataMaxX, xAxisDatum);
            }
        }
        return new double[]{dataMinX, dataMaxX};
    }

    protected void reCalcDisplayOffsetX() {
        // Find the actual min/max x-axis values in the data
        double[] minMax = getDataMinMaxX();
        double dataMinX = minMax[0];
        double dataMaxX = minMax[1];
        
        // Adjust displayOffsetX to keep display within data bounds
        if (getDisplayMaxX() > dataMaxX) {
            displayOffsetX -= (getDisplayMaxX() - dataMaxX);
        }
        if (getDisplayMinX() < dataMinX) {
            displayOffsetX -= (getDisplayMinX() - dataMinX);
        }
    }

    @Override
    public void adjustZoomSelf() {
        double effectiveZoomChunkX = zoomFlagsXY[0] ? zoomChunkX : 0.0;
        double effectiveZoomChunkY = zoomFlagsXY[1] ? zoomChunkY : 0.0;
        
        // Find the actual min/max x-axis values in the data
        double[] minMax = getDataMinMaxX();
        double dataMinX = minMax[0];
        double dataMaxX = minMax[1];
        
        // Apply zoom to x-axis
        minX = Math.max(dataMinX, minX - effectiveZoomChunkX);
        maxX = Math.min(dataMaxX, maxX + effectiveZoomChunkX);
        reCalcDisplayOffsetX();
        
        // Apply zoom to y-axis
        minY += -effectiveZoomChunkY;
        maxY -= -effectiveZoomChunkY;

        calculateTics();
        repaint();
    }

    public void calcStats() {
        analysisStatsRecord = userFunction.calculateAnalysisStatsRecord(analysis);
    }

    /**
     * @param g2d
     */
    @Override
    public void showLegend(GraphicsContext g2d) {
        Paint savedPaint = g2d.getFill();
        Paint savedStroke = g2d.getStroke();
        Font savedFont = g2d.getFont();

        // Overall legend column extents (to the left of the plot area)
        double legendX = 10.0;
        double legendRightEdge = leftMargin - 10.0;
        double legendWidth = Math.max(0.0, legendRightEdge - legendX);

        // Vertical cursor for stacking legend sections
        double legendTopY = 18.0;
        double sectionTopY = legendTopY;

        // --- Time index color gradient (top section) ---
        double timeBarHeight = 14.0;
        // Leave a small margin on the right for text like "Late"
        double timeBarWidth = Math.max(0.0, legendWidth - 50.0);
        double timeBarX = legendX;
        double timeBarY = sectionTopY + 4.0;

        // --- Intensity size legend (bottom section, if available) ---
        // Uses the same width and left edge as the Time Index gradient
        boolean showIntensityLegend = (intensityUserFunction != null && intensityData != null);
        double intensityLegendWidth = showIntensityLegend ? timeBarWidth : 0.0;

        // --- Time Index gradient section ---
        int gradientSteps = 100;
        for (int i = 0; i < gradientSteps; i++) {
            double normalizedValue = (double) i / (gradientSteps - 1);
            Color viridisColor = ViridisColorPalette.getViridisColor(normalizedValue);
            g2d.setFill(viridisColor);
            g2d.setStroke(viridisColor);
            double stepWidth = timeBarWidth / gradientSteps;
            g2d.fillRect(timeBarX + i * stepWidth, timeBarY, stepWidth + 0.5, timeBarHeight);
        }

        // Border and labels for gradient
        g2d.setStroke(Color.BLACK);
        g2d.setLineWidth(0.5);
        g2d.strokeRect(timeBarX, timeBarY, timeBarWidth, timeBarHeight);

        g2d.setFont(Font.font("SansSerif", 11));
        g2d.setFill(Color.BLACK);
        g2d.fillText("Time Index:", timeBarX, timeBarY - 2);
        g2d.setFont(Font.font("SansSerif", 10));
        g2d.fillText("Early", timeBarX, timeBarY + timeBarHeight + 12);
        g2d.fillText("Late", timeBarX + timeBarWidth - 25, timeBarY + timeBarHeight + 12);

        // Advance vertical cursor below Time Index section (extra padding before Rejection legend)
        sectionTopY = timeBarY + timeBarHeight + 30.0;

        // --- Rejection markers section (middle) ---
        // Align rejection label and icons within the same left-aligned section as Time Index
        double rejectionLabelX = legendX;
        double rejectionIconX = legendX + 8.0;

        // Label for rejection markers
        g2d.setFont(Font.font("SansSerif", 11));
        g2d.setFill(Color.BLACK);
        g2d.fillText("Rejection:", rejectionLabelX, sectionTopY);

        // Icons below label, stacked vertically (one per line)
        double rejectionIconY = sectionTopY + 8.0;
        double rejectionLineSpacing = 14.0;
        double rejectionIconSize = 6.0;

        // Both rejected - red square
        g2d.setFill(Color.RED);
        g2d.setStroke(Color.BLACK);
        g2d.setLineWidth(0.5);
        g2d.fillRect(rejectionIconX - rejectionIconSize / 2.0, rejectionIconY - rejectionIconSize / 2.0, rejectionIconSize, rejectionIconSize);
        g2d.strokeRect(rejectionIconX - rejectionIconSize / 2.0, rejectionIconY - rejectionIconSize / 2.0, rejectionIconSize, rejectionIconSize);
        g2d.setFont(Font.font("SansSerif", 10));
        g2d.setFill(Color.BLACK);
        g2d.fillText("Both", rejectionIconX + 12, rejectionIconY + 4);

        // X rejected - red downward triangle (next line)
        rejectionIconY += rejectionLineSpacing;
        g2d.setFill(Color.RED);
        g2d.setStroke(Color.BLACK);
        g2d.setLineWidth(0.5);
        double[] xPointsDown = {rejectionIconX, rejectionIconX - rejectionIconSize, rejectionIconX + rejectionIconSize};
        double[] yPointsDown = {rejectionIconY + rejectionIconSize, rejectionIconY - rejectionIconSize / 2.0, rejectionIconY - rejectionIconSize / 2.0};
        g2d.fillPolygon(xPointsDown, yPointsDown, 3);
        g2d.strokePolygon(xPointsDown, yPointsDown, 3);
        g2d.setFont(Font.font("SansSerif", 10));
        g2d.setFill(Color.BLACK);
        g2d.fillText("X only", rejectionIconX + 12, rejectionIconY + 4);

        // Y rejected - red left triangle (next line)
        rejectionIconY += rejectionLineSpacing;
        g2d.setFill(Color.RED);
        g2d.setStroke(Color.BLACK);
        g2d.setLineWidth(0.5);
        double[] xPointsLeft = {rejectionIconX - rejectionIconSize, rejectionIconX + rejectionIconSize / 2.0, rejectionIconX + rejectionIconSize / 2.0};
        double[] yPointsLeft = {rejectionIconY, rejectionIconY - rejectionIconSize, rejectionIconY + rejectionIconSize};
        g2d.fillPolygon(xPointsLeft, yPointsLeft, 3);
        g2d.strokePolygon(xPointsLeft, yPointsLeft, 3);
        g2d.setFont(Font.font("SansSerif", 10));
        g2d.setFill(Color.BLACK);
        g2d.fillText("Y only", rejectionIconX + 12, rejectionIconY + 4);

        // Advance vertical cursor below Rejection section
        sectionTopY = rejectionIconY + rejectionIconSize + 24.0;

        // --- Intensity legend section (bottom, optional) ---
        if (showIntensityLegend && intensityLegendWidth > 0.0) {
            double intensityLegendX = legendX;
            double intensityLegendY = sectionTopY + 4.0;

            // Label for intensity legend
            g2d.setFont(Font.font("SansSerif", 11));
            g2d.setFill(Color.BLACK);
            g2d.fillText("Intensity:", intensityLegendX, intensityLegendY - 2);

            // Show the attached user-function name used for this intensity scaling
            String intensityName = intensityUserFunction.showCorrectName();
            if (intensityUserFunction.isTreatAsCustomExpression() && intensityUserFunction.getCustomExpression() != null) {
                intensityName = intensityUserFunction.getCustomExpression().getName();
            }
            String intensityUFLabel = "(" + intensityName + "):";
            g2d.setFont(Font.font("SansSerif", 10));
            g2d.fillText(intensityUFLabel, intensityLegendX, intensityLegendY + 10);

            // Draw multiple circles showing size progression (7 steps - odd number for middle value)
            double circleY = intensityLegendY + 24; // Positioned below the UF label
            int numSteps = 7;
            double spacing = intensityLegendWidth / (numSteps - 1);

            g2d.setFill(Color.GRAY);
            g2d.setStroke(Color.BLACK);
            g2d.setLineWidth(0.5);

            // Get min/max sizes from parameters
            double minSize = 2.0; // Fallback default
            double maxSize = 50.0; // Fallback default
            if (analysis != null && analysis.getParameters() != null) {
                minSize = analysis.getParameters().getScalingDotMinSize();
                maxSize = analysis.getParameters().getScalingDotMaxSize();
            }

            for (int i = 0; i < numSteps; i++) {
                // Calculate size from min to max using parameters
                double normalized = (double) i / (numSteps - 1);
                double circleSize = minSize + (normalized * (maxSize - minSize));

                // Center circle vertically
                double circleX = intensityLegendX + (i * spacing) - circleSize / 2.0;
                double circleYPos = circleY - circleSize / 2.0;

                // Ensure circles stay within legend column
                if (circleX + circleSize <= legendRightEdge) {
                    g2d.fillOval(circleX, circleYPos, circleSize, circleSize);
                    g2d.strokeOval(circleX, circleYPos, circleSize, circleSize);
                }
            }

            // Labels below circles - moved down more to avoid overlap with largest circles
            g2d.setFont(Font.font("SansSerif", 11));
            if (minIntensity != maxIntensity) {
                // Format values to be more readable
                String minLabel = formatIntensityValue(minIntensity);
                String maxLabel = formatIntensityValue(maxIntensity);

                // Position labels further down to avoid overlap with largest circles
                double labelY = circleY + 22.0;

                // Calculate positions to align with circle centers
                double estimatedCharWidth = 5.5;

                // Draw min label aligned with first circle center
                g2d.fillText(minLabel, intensityLegendX, labelY);

                // Draw max label aligned with last circle center (index 6 in 7-step array)
                double maxLabelWidth = maxLabel.length() * estimatedCharWidth;
                double maxCircleX = intensityLegendX + (6 * spacing);
                if (maxCircleX + maxLabelWidth / 2.0 <= legendRightEdge) {
                    g2d.fillText(maxLabel, maxCircleX - (maxLabelWidth / 2.0), labelY);
                }
            }
        }

        // Restore saved graphics state
        g2d.setFill(savedPaint);
        g2d.setStroke(savedStroke);
        g2d.setFont(savedFont);
    }

    private String appendTrailingZeroIfNeeded(String valueString, int countOfTrailingDigits) {
        String retVal = valueString;
        if (!valueString.isBlank()) {
            String checkForTrailingZero = String.format("%,1." + countOfTrailingDigits + "f", Double.parseDouble(valueString));
            if (checkForTrailingZero.substring(checkForTrailingZero.length() - 1).compareTo("0") == 0) {
                retVal += "0";
            }
        }
        return retVal;
    }

    /**
     * Formats intensity value for display in legend.
     * Uses scientific notation for very large/small values, otherwise decimal.
     * @param value The intensity value to format
     * @return Formatted string representation
     */
    private String formatIntensityValue(double value) {
        if (abs(value) >= 1e4 || (abs(value) < 1e-2 && value != 0.0)) {
            return String.format("%.2e", value);
        } else if (abs(value) >= 1.0) {
            return String.format("%.2f", value);
        } else {
            return String.format("%.4f", value);
        }
    }

    /**
     * Calculates point size based on intensity value.
     * Normalizes intensity to a range defined by parameters (default 2-50 pixels radius).
     * @param intensityValue The intensity value for the point
     * @return The point size (radius) in pixels, or default size if intensity not available
     */
    private double calculatePointSize(double intensityValue) {
        if (intensityData == null || minIntensity == maxIntensity || intensityValue == 0.0) {
            // Return minimum size from parameters, or default if not available
            if (analysis != null && analysis.getParameters() != null) {
                return analysis.getParameters().getScalingDotMinSize();
            }
            return 2.0; // Fallback default
        }
        
        // Normalize intensity value to 0-1 range
        double normalized = (intensityValue - minIntensity) / (maxIntensity - minIntensity);
        
        // Clamp to [0, 1] range
        normalized = Math.max(0.0, Math.min(1.0, normalized));
        
        // Map to parameter-defined range
        double minSize = 2.0; // Fallback default
        double maxSize = 50.0; // Fallback default
        if (analysis != null && analysis.getParameters() != null) {
            minSize = analysis.getParameters().getScalingDotMinSize();
            maxSize = analysis.getParameters().getScalingDotMaxSize();
        }
        return minSize + (normalized * (maxSize - minSize));
    }

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);
    }

    @Override
    public void labelAxisX(GraphicsContext g2d) {
        Paint savedPaint = g2d.getFill();
        g2d.setFill(Paint.valueOf("BLACK"));
        g2d.setFont(Font.font("SansSerif", 14));
        Text text = new Text();
        text.setFont(Font.font("SansSerif", 14));
        text.setText(plotAxisLabelX);
        int textWidth = (int) text.getLayoutBounds().getWidth();
        g2d.fillText(text.getText(), leftMargin + (plotWidth - textWidth) / 2.0, plotHeight + 2.0 * topMargin - 4.0);
        g2d.setFill(savedPaint);
    }

    public void prepareExtents(boolean reScaleX, boolean reScaleY) {
        if (reScaleX) {
            double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.10);
            if (xMarginStretch == 0.0 && xAxisData.length > 0) {
                xMarginStretch = xAxisData[0] / 100.0;
            }
            minX -= xMarginStretch;
            maxX += xMarginStretch;
        }

        if (reScaleY || ignoreRejects) {
            double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.10);
            if (yMarginStretch == 0.0 && yAxisData.length > 0) {
                yMarginStretch = yAxisData[0] / 100.0;
            }
            minY -= yMarginStretch;
            maxY += yMarginStretch;
        }
    }

    @Override
    public void plotData(GraphicsContext g2d) {
        int cyclesPerBlock = mapBlockIdToBlockCyclesRecord.get(1).cyclesIncluded().length;
        int cycleCount = 0;
        
        // Use viridis color palette based on time index
        int totalDataPoints = xAxisData.length;

        for (int i = 0; i < xAxisData.length; i++) {
            // Calculate block ID from cycle index
            int blockID = (cycleCount / cyclesPerBlock) + 1;
            if (blockID > mapBlockIdToBlockCyclesRecord.size()) {
                blockID = mapBlockIdToBlockCyclesRecord.size();
            }
            
            if (pointInPlot(xAxisData[i], yAxisData[i]) && (xAxisData[i] != 0.0) && (yAxisData[i] != 0.0)) {
                // Check rejection status for both x and y user functions
                boolean yIncluded = true;
                boolean xIncluded = true;
                int cycleIndex = cycleCount % cyclesPerBlock;
                
                // Check y-axis user function (main user function)
                if (blockID <= mapBlockIdToBlockCyclesRecord.size() && 
                    mapBlockIdToBlockCyclesRecord.get(blockID) != null &&
                    cycleIndex < mapBlockIdToBlockCyclesRecord.get(blockID).cyclesIncluded().length) {
                    yIncluded = mapBlockIdToBlockCyclesRecord.get(blockID).cyclesIncluded()[cycleIndex];
                }
                
                // Check x-axis user function
                if (blockID <= mapBlockIdToBlockCyclesRecordX.size() && 
                    mapBlockIdToBlockCyclesRecordX.get(blockID) != null &&
                    cycleIndex < mapBlockIdToBlockCyclesRecordX.get(blockID).cyclesIncluded().length) {
                    xIncluded = mapBlockIdToBlockCyclesRecordX.get(blockID).cyclesIncluded()[cycleIndex];
                }
                
                double dataX = mapX(xAxisData[i]);
                double dataY = mapY(yAxisData[i]);
                
                // Calculate point size based on intensity if available
                // Get default size from parameters (minimum of range)
                double pointSize = 2.0; // Fallback default
                if (analysis != null && analysis.getParameters() != null) {
                    pointSize = analysis.getParameters().getScalingDotMinSize();
                }
                if (intensityData != null && i < intensityData.length && intensityData[i] != 0.0) {
                    pointSize = calculatePointSize(intensityData[i]);
                }
                double halfSize = pointSize / 2.0;
                
                // Determine marker type and color based on rejection status
                if (!xIncluded && !yIncluded) {
                    // Both rejected: red square
                    g2d.setFill(Color.RED);
                    g2d.setStroke(Color.BLACK);
                    g2d.setLineWidth(0.5);
                    g2d.fillRect(dataX - halfSize, dataY - halfSize, pointSize, pointSize);
                    g2d.strokeRect(dataX - halfSize, dataY - halfSize, pointSize, pointSize);
                } else if (!xIncluded && yIncluded) {
                    // X rejected only: red downward-pointing triangle ▼
                    g2d.setFill(Color.RED);
                    g2d.setStroke(Color.BLACK);
                    g2d.setLineWidth(0.5);
                    // Triangle pointing down: apex at bottom, base at top
                    double[] xPoints = {dataX, dataX - pointSize, dataX + pointSize};
                    double[] yPoints = {dataY + pointSize, dataY - halfSize, dataY - halfSize};
                    g2d.fillPolygon(xPoints, yPoints, 3);
                    g2d.strokePolygon(xPoints, yPoints, 3);
                } else if (xIncluded && !yIncluded) {
                    // Y rejected only: red left-pointing triangle ◀
                    g2d.setFill(Color.RED);
                    g2d.setStroke(Color.BLACK);
                    g2d.setLineWidth(0.5);
                    // Triangle pointing left: apex on left, base on right
                    double[] xPoints = {dataX - pointSize, dataX + halfSize, dataX + halfSize};
                    double[] yPoints = {dataY, dataY - pointSize, dataY + pointSize};
                    g2d.fillPolygon(xPoints, yPoints, 3);
                    g2d.strokePolygon(xPoints, yPoints, 3);
                } else {
                    // Neither rejected: viridis colored circle
                    Color pointColor = ViridisColorPalette.getViridisColorForIndex(i, totalDataPoints);
                    g2d.setFill(pointColor);
                    g2d.setStroke(Color.BLACK);
                    g2d.setLineWidth(0.5);
                    g2d.fillOval(dataX - halfSize, dataY - halfSize, pointSize, pointSize);
                    g2d.strokeOval(dataX - halfSize, dataY - halfSize, pointSize, pointSize);
                }
            }
            cycleCount++;
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

        // Block delimiters removed for heatmap visualization

    }

    private void showBlockID(GraphicsContext g2d, int blockID, double xPosition) {
        Paint savedPaint = g2d.getFill();
        g2d.setFill(Paint.valueOf("BLACK"));

        g2d.setFont(Font.font("SansSerif", FontWeight.EXTRA_BOLD, 8));

        g2d.fillText("" + blockID, xPosition - 4, topMargin + plotHeight + 10);
        g2d.setFill(savedPaint);
    }

    /**
     *
     *
     * @param mean
     * @param stdDev
     * @param stdErr
     * @return
     */
    public HashMap<String, Double> calcPlotStatsCM(double mean, double stdDev, double stdErr) {
        double meanPlusOneStandardDeviation = mean + stdDev;
        double meanPlusTwoStandardDeviation = mean + 2.0 * stdDev;
        double meanPlusTwoStandardError = mean + 2.0 * stdErr;
        double meanMinusOneStandardDeviation = mean - stdDev;
        double meanMinusTwoStandardDeviation = mean - 2.0 * stdDev;
        double meanMinusTwoStandardError = mean - 2.0 * stdErr;

        HashMap<String, Double> output = new HashMap<>(Map.ofEntries(
                entry("mean", mean),
                entry("stdDev", stdDev),
                entry("stdErr", stdErr),
                entry("meanPlusOneStandardDeviation", meanPlusOneStandardDeviation),
                entry("meanPlusTwoStandardDeviation", meanPlusTwoStandardDeviation),
                entry("meanPlusTwoStandardError", meanPlusTwoStandardError),
                entry("meanMinusOneStandardDeviation", meanMinusOneStandardDeviation),
                entry("meanMinusTwoStandardDeviation", meanMinusTwoStandardDeviation),
                entry("meanMinusTwoStandardError", meanMinusTwoStandardError)
        ));

        return output;
    }

    public void plotStats(GraphicsContext g2d) {
        // Intentionally left blank: AnalysisTwoUserFunctionsPlot does not render
        // the block/cycle statistical background (mean line, 1σ/2σ/2SE bands).
    }

    public void setupPlotContextMenu() {
        // no menu for now
        plotContextMenu = new ContextMenu();
    }

    public void resetData() {
        for (int i = 0; i < mapBlockIdToBlockCyclesRecord.size(); i++) {
            // Reset Y-axis user function
            mapBlockIdToBlockCyclesRecord.put(i + 1, mapBlockIdToBlockCyclesRecord.get(i + 1).resetAllDataIncluded());
            analysis.getMapOfBlockIdToRawDataLiteOne().put(i + 1, analysis.getMapOfBlockIdToRawDataLiteOne().get(i + 1).resetAllDataIncluded(userFunction));
            
            // Reset X-axis user function
            if (mapBlockIdToBlockCyclesRecordX != null && mapBlockIdToBlockCyclesRecordX.containsKey(i + 1)) {
                mapBlockIdToBlockCyclesRecordX.put(i + 1, mapBlockIdToBlockCyclesRecordX.get(i + 1).resetAllDataIncluded());
                analysis.getMapOfBlockIdToRawDataLiteOne().put(i + 1, analysis.getMapOfBlockIdToRawDataLiteOne().get(i + 1).resetAllDataIncluded(xAxisUserFunction));
            }
        }
        repaint();
    }

    @Override
    public void performChauvenets() {
        if (blockMode) {
            for (int i = 0; i < mapBlockIdToBlockCyclesRecord.size(); i++) {
                int blockID = i + 1;
                PlotBlockCyclesRecord plotBlockCyclesRecord = mapBlockIdToBlockCyclesRecord.get(blockID).performChauvenets(analysis.getParameters());
                mapBlockIdToBlockCyclesRecord.put(blockID, plotBlockCyclesRecord);
                analysis.getMapOfBlockIdToRawDataLiteOne().put(blockID,
                        analysis.getMapOfBlockIdToRawDataLiteOne().get(i + 1).recordChauvenets(userFunction, plotBlockCyclesRecord.cyclesIncluded()));

                boolean[] plotBlockCyclesIncluded = analysis.getMapOfBlockIdToRawDataLiteOne().get(blockID).assembleCyclesIncludedForUserFunction(userFunction);
                mapBlockIdToBlockCyclesRecord.put(
                        blockID,
                        getMapBlockIdToBlockCyclesRecord().get(blockID).updateCyclesIncluded(plotBlockCyclesIncluded));
            }
        } else {
            // cycle mode
            boolean[] cycleModeIncluded = analysisStatsRecord.cycleModeIncluded();
            double[] cycleModeData = analysisStatsRecord.cycleModeData();
//            if (Booleans.countTrue(cycleModeIncluded) == cycleModeIncluded.length) {
                boolean[] chauvenets = applyChauvenetsCriterion(
                        cycleModeData,
                        cycleModeIncluded,
                        analysis.getParameters());
                // reset included cycles for each block
                BlockStatsRecord[] blockStatsRecords = analysisStatsRecord.blockStatsRecords();
                int countOfProcessedCycles = 0;
                for (int i = 0; i < blockStatsRecords.length; i++) {
                    System.arraycopy(chauvenets, countOfProcessedCycles,
                            blockStatsRecords[i].cyclesIncluded(), 0, blockStatsRecords[i].cyclesIncluded().length);
                    countOfProcessedCycles += blockStatsRecords[i].cyclesIncluded().length;
                    int blockID = i + 1;
                    PlotBlockCyclesRecord plotBlockCyclesRecord = mapBlockIdToBlockCyclesRecord.get(blockID);
                    plotBlockCyclesRecord.updateCyclesIncluded(blockStatsRecords[i].cyclesIncluded());
                    mapBlockIdToBlockCyclesRecord.put(blockID, plotBlockCyclesRecord);
                    analysis.getMapOfBlockIdToRawDataLiteOne().put(blockID,
                            analysis.getMapOfBlockIdToRawDataLiteOne().get(i + 1).recordChauvenets(userFunction, plotBlockCyclesRecord.cyclesIncluded()));

                }
                analysisStatsRecord = AnalysisStatsRecord.generateAnalysisStatsRecord(blockStatsRecords);
//            }
        }

        repaint();
    }

    public boolean detectAllIncludedStatus() {
        boolean retVal = true;
        for (int i = 0; i < mapBlockIdToBlockCyclesRecord.size(); i++) {
            retVal = retVal && mapBlockIdToBlockCyclesRecord.get(i + 1).detectAllIncludedStatus();
        }
        return retVal;
    }

    private int determineSculptBlock(double mouseX) {
        // Convert mouse X coordinate to x-axis value (user function value)
        double mouseXValue = convertMouseXToValue(mouseX);
        
        // Find the closest data point index
        int closestIndex = 0;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < xAxisData.length; i++) {
            double distance = Math.abs(xAxisData[i] - mouseXValue);
            if (distance < minDistance) {
                minDistance = distance;
                closestIndex = i;
            }
        }
        
        // Calculate block ID from data index
        int cyclesPerBlock = mapBlockIdToBlockCyclesRecord.get(1).cyclesIncluded().length;
        int blockID = (closestIndex / cyclesPerBlock) + 1;
        
        // Ensure block ID is within valid range
        if (blockID > mapBlockIdToBlockCyclesRecord.size()) {
            blockID = mapBlockIdToBlockCyclesRecord.size();
        }
        if (blockID < 1) {
            blockID = 1;
        }
        
        return blockID;
    }

    public Map<Integer, PlotBlockCyclesRecord> getMapBlockIdToBlockCyclesRecord() {
        return mapBlockIdToBlockCyclesRecord;
    }

    /**
     * @return
     */
    @Override
    public AnalysisBlockCyclesRecord getAnalysisBlockCyclesRecord() {
        return null;
    }

    public UserFunction getUserFunction() {
        return userFunction;
    }

    private boolean mouseInBlockLabel(double sceneX, double sceneY) {
        return ((sceneX >= leftMargin)
                && (sceneY >= topMargin - 15)
                && (sceneY < topMargin)
                && (sceneX < (plotWidth + leftMargin - 2)));
    }

    public void sculptBlock(boolean zoomBlock) {
        if ((0 < sculptBlockID) && !inSculptorMode) {
            inSculptorMode = true;
            showSelectionBox = true;
            setOnMouseDragged(new AnalysisTwoUserFunctionsPlot.MouseDraggedEventHandlerSculpt());
            setOnMousePressed(new AnalysisTwoUserFunctionsPlot.MousePressedEventHandlerSculpt());
            setOnMouseReleased(new AnalysisTwoUserFunctionsPlot.MouseReleasedEventHandlerSculpt());
            selectorBoxX = mouseStartX;
            selectorBoxY = mouseStartY;

            // zoom into block
            countOfPreviousBlockIncludedData = (sculptBlockID - 1) * mapBlockIdToBlockCyclesRecord.get(1).cyclesIncluded().length;
            if (zoomBlock) {
                displayOffsetX = 0;
                int countOfCycles = mapBlockIdToBlockCyclesRecord.get(sculptBlockID).cyclesIncluded().length;
                int startIndex = countOfPreviousBlockIncludedData;
                int endIndex = Math.min(startIndex + countOfCycles - 1, xAxisData.length - 1);
                
                // Find min/max x-axis values for this block
                minX = Double.MAX_VALUE;
                maxX = -Double.MAX_VALUE;
                minY = Double.MAX_VALUE;
                maxY = -Double.MAX_VALUE;
                
                for (int i = startIndex; i <= endIndex; i++) {
                    if (i < xAxisData.length && xAxisData[i] != 0.0) {
                        minX = min(minX, xAxisData[i]);
                        maxX = max(maxX, xAxisData[i]);
                    }
                    if (i < yAxisData.length && yAxisData[i] != 0.0) {
                        minY = min(minY, yAxisData[i]);
                        maxY = max(maxY, yAxisData[i]);
                    }
                }
                
                // Add margins
                double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.05);
                if (xMarginStretch == 0.0 && (maxX - minX) > 0) {
                    xMarginStretch = (maxX - minX) / 100.0;
                }
                minX -= xMarginStretch;
                maxX += xMarginStretch;
                
                double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.05);
                if (yMarginStretch == 0.0 && (maxY - minY) > 0) {
                    yMarginStretch = (maxY - minY) / 100.0;
                }
                maxY += yMarginStretch;
                minY -= yMarginStretch;
                displayOffsetY = 0.0;

                refreshPanel(false, false);
            }
        } else {
            inSculptorMode = false;
            showSelectionBox = false;
            setOnMouseDragged(new AnalysisTwoUserFunctionsPlot.MouseDraggedEventHandler());
            setOnMousePressed(new AnalysisTwoUserFunctionsPlot.MousePressedEventHandler());
            setOnMouseReleased(new AnalysisTwoUserFunctionsPlot.MouseReleasedEventHandler());
        }
        repaint();
    }

    private void exitSculptingMode() {
        inSculptorMode = false;
        sculptBlockID = 0;
        inZoomBoxMode = true;
        showZoomBox = true;
        zoomBoxX = mouseStartX;
        zoomBoxY = mouseStartY;
        refreshPanel(true, true);
        ((TripoliPlotPane) getParent().getParent()).removeSculptingHBox();
        tooltip.setText(tooltipTextSculpt);
    }

    private void enterSculptingMode() {
        inSculptorMode = false;
        inZoomBoxMode = false;
        showZoomBox = false;
        ((TripoliPlotPane) getParent().getParent()).removeSculptingHBox();
        sculptBlockID = 1;//determineSculptBlock(mouseEvent.getX());
        ((TripoliPlotPane) getParent().getParent()).builtSculptingHBox(
                "Cycle Sculpting " + "  >> " + tooltipTextExitSculpt);
        sculptBlock(false);//mouseInBlockLabel(mouseEvent.getX(), mouseEvent.getY()));
        inSculptorMode = true;
        tooltip.setText(tooltipTextExitSculpt);
    }

    public boolean isIgnoreRejects() {
        return ignoreRejects;
    }

    public void setIgnoreRejects(boolean ignoreRejects) {
        this.ignoreRejects = ignoreRejects;
    }

    public void toggleSculptingMode() {
        if (inSculptorMode) {
            exitSculptingMode();
        } else {
            enterSculptingMode();
        }
    }

    class MouseClickEventHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent mouseEvent) {
            boolean isPrimary = (0 == mouseEvent.getButton().compareTo(MouseButton.PRIMARY));
            if (2 == mouseEvent.getClickCount() && !mouseEvent.isControlDown()) {
                if (isPrimary && (mouseInHouse(mouseEvent.getX(), mouseEvent.getY()) || mouseInBlockLabel(mouseEvent.getX(), mouseEvent.getY()))) {
                    if (inSculptorMode) {
                        exitSculptingMode();
                    } else {
                        enterSculptingMode();
                    }
                }
            } else {
                if (isPrimary && mouseEvent.isControlDown() && (mouseInHouse(mouseEvent.getX(), mouseEvent.getY()))) {
                    // turn off / on block - update both X and Y user functions
                    sculptBlockID = determineSculptBlock(mouseEvent.getX());
                    mapBlockIdToBlockCyclesRecord.put(sculptBlockID,
                            mapBlockIdToBlockCyclesRecord.get(sculptBlockID).toggleBlockIncluded());
                    analysis.getMapOfBlockIdToRawDataLiteOne().put(sculptBlockID,
                            analysis.getMapOfBlockIdToRawDataLiteOne().get(sculptBlockID).toggleAllDataIncludedUserFunction(userFunction));
                    
                    // Also toggle X-axis user function
                    if (mapBlockIdToBlockCyclesRecordX.get(sculptBlockID) != null) {
                        mapBlockIdToBlockCyclesRecordX.put(sculptBlockID,
                                mapBlockIdToBlockCyclesRecordX.get(sculptBlockID).toggleBlockIncluded());
                        analysis.getMapOfBlockIdToRawDataLiteOne().put(sculptBlockID,
                                analysis.getMapOfBlockIdToRawDataLiteOne().get(sculptBlockID).toggleAllDataIncludedUserFunction(xAxisUserFunction));
                    }

                    inZoomBoxMode = !inSculptorMode;
                    showZoomBox = !inSculptorMode;
                    refreshPanel(false, false);
                } else if (!isPrimary && mouseEvent.isControlDown() && (mouseInHouse(mouseEvent.getX(), mouseEvent.getY()))) {
                    // zoom block
                    sculptBlockID = determineSculptBlock(mouseEvent.getX());
                    ((TripoliPlotPane) getParent().getParent()).removeSculptingHBox();
                    ((TripoliPlotPane) getParent().getParent()).builtSculptingHBox(
                            "Cycle Sculpting " + "  >> " + tooltipTextExitSculpt);
                    sculptBlock(true);
                    inZoomBoxMode = !inSculptorMode;
                    showZoomBox = !inSculptorMode;

                    tooltip.setText(tooltipTextExitSculpt);
                    repaint();
                }
            }
        }
    }

    class MouseDraggedEventHandlerSculpt implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            if (e.isPrimaryButtonDown()) {
                if (mouseInHouse(e.getX(), e.getY())) {
                    selectorBoxX = e.getX();
                    selectorBoxY = e.getY();
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
                // Convert selection box to data coordinates
                double xValueLeft = convertMouseXToValue(Math.min(mouseStartX, selectorBoxX));
                double xValueRight = convertMouseXToValue(Math.max(mouseStartX, selectorBoxX));
                double yValueTop = convertMouseYToValue(Math.min(mouseStartY, selectorBoxY));
                double yValueBottom = convertMouseYToValue(Math.max(mouseStartY, selectorBoxY));

                int expectedCyclesCount = mapBlockIdToBlockCyclesRecord.get(1).cycleMeansData().length;

                // Find indices that fall within the selection box
                // We need to check each point individually to see if it's in the box
                List<Integer> selectedIndices = new ArrayList<>();
                for (int i = 0; i < xAxisData.length && i < yAxisData.length; i++) {
                    // Check if point is within selection box (both X and Y coordinates)
                    if (xAxisData[i] >= xValueLeft && xAxisData[i] <= xValueRight &&
                        yAxisData[i] >= yValueBottom && yAxisData[i] <= yValueTop &&
                        xAxisData[i] != 0.0 && yAxisData[i] != 0.0) {
                        selectedIndices.add(i);
                    }
                }

                if (selectedIndices.isEmpty()) {
                    showSelectionBox = false;
                    adjustMouseStartsForPress(e.getX(), e.getY());
                    selectorBoxX = mouseStartX;
                    selectorBoxY = mouseStartY;
                    refreshPanel(false, false);
                    return;
                }

                // Calculate majority status from selected points
                List<Boolean> statusListY = new ArrayList<>();
                List<Boolean> statusListX = new ArrayList<>();
                
                for (int idx : selectedIndices) {
                    int blockID = (idx / expectedCyclesCount) + 1;
                    if (blockID > mapBlockIdToBlockCyclesRecord.size()) {
                        blockID = mapBlockIdToBlockCyclesRecord.size();
                    }
                    if (blockID < 1) {
                        blockID = 1;
                    }
                    int cycleIndex = idx % expectedCyclesCount;
                    
                    // Get Y-axis status
                    if (mapBlockIdToBlockCyclesRecord.get(blockID) != null &&
                        cycleIndex < mapBlockIdToBlockCyclesRecord.get(blockID).cyclesIncluded().length) {
                        statusListY.add(mapBlockIdToBlockCyclesRecord.get(blockID).cyclesIncluded()[cycleIndex]);
                    }
                    
                    // Get X-axis status
                    if (blockID <= mapBlockIdToBlockCyclesRecordX.size() && 
                        mapBlockIdToBlockCyclesRecordX.get(blockID) != null &&
                        cycleIndex < mapBlockIdToBlockCyclesRecordX.get(blockID).cyclesIncluded().length) {
                        statusListX.add(mapBlockIdToBlockCyclesRecordX.get(blockID).cyclesIncluded()[cycleIndex]);
                    }
                }

                // Determine majority for each axis independently
                int countIncludedY = 0;
                for (Boolean b : statusListY) {
                    if (b) countIncludedY++;
                }
                boolean majorityIncludedY = countIncludedY > statusListY.size() / 2;
                
                int countIncludedX = 0;
                for (Boolean b : statusListX) {
                    if (b) countIncludedX++;
                }
                boolean majorityIncludedX = countIncludedX > statusListX.size() / 2;

                // Update both axes for selected points
                for (int idx : selectedIndices) {
                    int blockID = (idx / expectedCyclesCount) + 1;
                    if (blockID > mapBlockIdToBlockCyclesRecord.size()) {
                        blockID = mapBlockIdToBlockCyclesRecord.size();
                    }
                    if (blockID < 1) {
                        blockID = 1;
                    }
                    int cycleIndex = idx % expectedCyclesCount;
                    
                    // Update Y-axis user function
                    if (mapBlockIdToBlockCyclesRecord.get(blockID) != null &&
                        cycleIndex < mapBlockIdToBlockCyclesRecord.get(blockID).cyclesIncluded().length) {
                        boolean[] cyclesIncludedY = mapBlockIdToBlockCyclesRecord.get(blockID).cyclesIncluded().clone();
                        cyclesIncludedY[cycleIndex] = !majorityIncludedY;
                        mapBlockIdToBlockCyclesRecord.put(blockID,
                                mapBlockIdToBlockCyclesRecord.get(blockID).updateCyclesIncluded(cyclesIncludedY));
                        analysis.getMapOfBlockIdToRawDataLiteOne().put(blockID,
                                analysis.getMapOfBlockIdToRawDataLiteOne().get(blockID).updateIncludedCycles(userFunction, cyclesIncludedY));
                    }
                    
                    // Update X-axis user function
                    if (blockID <= mapBlockIdToBlockCyclesRecordX.size() && 
                        mapBlockIdToBlockCyclesRecordX.get(blockID) != null &&
                        cycleIndex < mapBlockIdToBlockCyclesRecordX.get(blockID).cyclesIncluded().length) {
                        boolean[] cyclesIncludedX = mapBlockIdToBlockCyclesRecordX.get(blockID).cyclesIncluded().clone();
                        cyclesIncludedX[cycleIndex] = !majorityIncludedX;
                        mapBlockIdToBlockCyclesRecordX.put(blockID,
                                mapBlockIdToBlockCyclesRecordX.get(blockID).updateCyclesIncluded(cyclesIncludedX));
                        analysis.getMapOfBlockIdToRawDataLiteOne().put(blockID,
                                analysis.getMapOfBlockIdToRawDataLiteOne().get(blockID).updateIncludedCycles(xAxisUserFunction, cyclesIncludedX));
                    }
                }

            } else {
                showSelectionBox = false;
            }
            adjustMouseStartsForPress(e.getX(), e.getY());
            selectorBoxX = mouseStartX;
            selectorBoxY = mouseStartY;

            refreshPanel(false, false);
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

    class MouseDraggedEventHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            if (inZoomBoxMode && mouseInHouse(e.getX(), e.getY()) && e.isPrimaryButtonDown()) {
                zoomBoxX = e.getX();
                zoomBoxY = e.getY();
                showZoomBox = true;

            } else {
                if (mouseInHouse(e.getX(), e.getY()) && !e.isPrimaryButtonDown() && !e.isControlDown()) {
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
                    // Convert mouse coordinates to x-axis values (user function values)
                    double xValueLeft = convertMouseXToValue(Math.min(mouseStartX, zoomBoxX));
                    double xValueRight = convertMouseXToValue(Math.max(mouseStartX, zoomBoxX));
                    double yValueTop = convertMouseYToValue(Math.min(mouseStartY, zoomBoxY));
                    double yValueBottom = convertMouseYToValue(Math.max(mouseStartY, zoomBoxY));

                    // Set zoom bounds directly from the converted values
                    minX = xValueLeft;
                    maxX = xValueRight;
                    minY = yValueBottom;
                    maxY = yValueTop;

                    // Add margins
                    double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.05);
                    if (xMarginStretch == 0.0 && (maxX - minX) > 0) {
                        xMarginStretch = (maxX - minX) / 100.0;
                    }
                    minX -= xMarginStretch;
                    maxX += xMarginStretch;

                    double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.05);
                    if (yMarginStretch == 0.0 && (maxY - minY) > 0) {
                        yMarginStretch = (maxY - minY) / 100.0;
                    }
                    maxY += yMarginStretch;
                    minY -= yMarginStretch;
                    
                    displayOffsetX = 0.0;
                    displayOffsetY = 0.0;

                    inZoomBoxMode = false;
                    showZoomBox = false;
                    ((TripoliPlotPane) getParent().getParent()).removeSculptingHBox();
                    ((TripoliPlotPane) getParent().getParent()).builtSculptingHBox(
                            "Cycle Sculpting " + "  >> " + tooltipTextExitSculpt);
                    inSculptorMode = true;
                    showSelectionBox = false;
                    setOnMouseDragged(new AnalysisTwoUserFunctionsPlot.MouseDraggedEventHandlerSculpt());
                    setOnMousePressed(new AnalysisTwoUserFunctionsPlot.MousePressedEventHandlerSculpt());
                    setOnMouseReleased(new AnalysisTwoUserFunctionsPlot.MouseReleasedEventHandlerSculpt());
                    selectorBoxX = mouseStartX;
                    selectorBoxY = mouseStartY;

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
}