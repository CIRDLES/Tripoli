package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.sessionPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.sessionPlots.SpeciesIntensitySessionBuilder;

import static java.lang.StrictMath.*;

public class SpeciesIntensitySessionPlot extends AbstractPlot {
    private final SpeciesIntensitySessionBuilder speciesIntensitySessionBuilder;
    private final double[][] dfGain;
    TripoliConstants.IntensityUnits intensityUnits = TripoliConstants.IntensityUnits.COUNTS;
    private double[][] yDataCounts;
    private double[][] yData;
    private double[][] ampResistance;
    private double[][] baseLine;
    private boolean[] speciesChecked;
    private boolean showFaradays;
    private boolean showPMs;
    private boolean showModels;
    private boolean baselineCorr;
    private boolean gainCorr;
    private boolean logScale;
    private boolean[] zoomFlagsXY;

    private SpeciesIntensitySessionPlot(Rectangle bounds, SpeciesIntensitySessionBuilder speciesIntensitySessionBuilder) {
        super(bounds, 100, 25,
                speciesIntensitySessionBuilder.getTitle(),
                speciesIntensitySessionBuilder.getxAxisLabel(),
                speciesIntensitySessionBuilder.getyAxisLabel());
        this.speciesIntensitySessionBuilder = speciesIntensitySessionBuilder;
        this.yDataCounts = speciesIntensitySessionBuilder.getyData();
        this.ampResistance = speciesIntensitySessionBuilder.getAmpResistance();
        this.baseLine = speciesIntensitySessionBuilder.getBaseLine();
        this.dfGain = speciesIntensitySessionBuilder.getDfGain();
        this.speciesChecked = new boolean[yDataCounts.length / 4];
        this.speciesChecked[0] = true;
        this.showFaradays = true;
        this.showPMs = true;
        this.showModels = true;
        this.baselineCorr = false;
        this.gainCorr = false;
        this.logScale = false;
        this.zoomFlagsXY = new boolean[]{true, true};

    }

    public static AbstractPlot generatePlot(Rectangle bounds, SpeciesIntensitySessionBuilder speciesIntensitySessionBuilder) {
        return new SpeciesIntensitySessionPlot(bounds, speciesIntensitySessionBuilder);
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
        xAxisData = speciesIntensitySessionBuilder.getxData();
        if (reScaleX) {
            minX = xAxisData[0];
            maxX = xAxisData[xAxisData.length - 1];

            displayOffsetX = 0.0;
        }

        yData = new double[yDataCounts.length][yDataCounts[0].length];

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
                            yData[row][col] -= baseLine[row][col];
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
            double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.01);
            if (0.0 == xMarginStretch) {
                xMarginStretch = maxX * 0.01;
            }
            minX -= 50;//xMarginStretch;
            maxX += 50;//xMarginStretch;
        }

        if (reScaleY) {
            double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.01);
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
                    g2d.setLineDashes(0);
                    boolean startedPlot = false;
                    g2d.setFill(isotopeColors[isotopePlotSetIndex]);
                    g2d.setStroke(isotopeColors[isotopePlotSetIndex]);
                    for (int i = 0; i < xAxisData.length; i++) {
                        if ((yData[isotopePlotSetIndex * 4 + 2][i] != 0.0) && pointInPlot(xAxisData[i], yData[isotopePlotSetIndex * 4 + 2][i])) {
                            double dataX = mapX(xAxisData[i]);
                            double dataY = mapY(yData[isotopePlotSetIndex * 4 + 2][i]);
                            g2d.fillOval(dataX - 1.5, dataY - 1.5, 3, 3);
                        }

                        if (showModels && !gainCorr) {
                            if ((i < xAxisData.length - 1) && (xAxisData[i + 1] - xAxisData[i] < 10.0)) {
                                if ((yData[isotopePlotSetIndex * 4 + 3][i] != 0.0) && pointInPlot(xAxisData[i], yData[isotopePlotSetIndex * 4 + 3][i])) {
                                    if (!startedPlot) {
                                        g2d.beginPath();
                                        g2d.moveTo(mapX(xAxisData[i]), mapY(yData[isotopePlotSetIndex * 4 + 3][i]));
                                        startedPlot = true;
                                    }
                                    g2d.lineTo(mapX(xAxisData[i]), mapY(yData[isotopePlotSetIndex * 4 + 3][i]));
                                }
                            } else {
                                startedPlot = false;
                                g2d.setStroke(Color.AQUAMARINE);
                                g2d.stroke();
                            }
                        }
                    }
                    g2d.setStroke(isotopeColors[isotopePlotSetIndex]);
                }
                // plot Faraday
                if (showFaradays) {
                    g2d.setLineDashes(0);
                    boolean startedPlot = false;
                    g2d.setFill(isotopeColors[isotopePlotSetIndex]);
                    g2d.setStroke(isotopeColors[isotopePlotSetIndex]);
                    for (int i = 0; i < xAxisData.length; i++) {
                        if ((yData[isotopePlotSetIndex * 4][i] != 0.0) && pointInPlot(xAxisData[i], yData[isotopePlotSetIndex * 4][i])) {
                            double dataX = mapX(xAxisData[i]);
                            double dataY = mapY(yData[isotopePlotSetIndex * 4][i]);
                            g2d.fillOval(dataX - 1.5, dataY - 1.5, 3, 3);
                        }

                        if (showModels) {
                            // TODO: make this 10.0 more robust for finding block separations
                            if ((i < xAxisData.length - 1) && (xAxisData[i + 1] - xAxisData[i] < 10.0)) {
                                if ((yData[isotopePlotSetIndex * 4 + 1][i] != 0.0) && pointInPlot(xAxisData[i], yData[isotopePlotSetIndex * 4 + 1][i])) {
                                    if (!startedPlot) {
                                        g2d.beginPath();
                                        g2d.moveTo(mapX(xAxisData[i]), mapY(yData[isotopePlotSetIndex * 4 + 1][i]));
                                        startedPlot = true;
                                    }
                                    g2d.lineTo(mapX(xAxisData[i]), mapY(yData[isotopePlotSetIndex * 4 + 1][i]));
                                }
                            } else {
                                startedPlot = false;
                                g2d.setStroke(Color.RED);
                                g2d.stroke();
                            }
                        }
                    }
                    g2d.setStroke(isotopeColors[isotopePlotSetIndex]);
                }
            }
        }
    }

    @Override
    public void plotStats(GraphicsContext g2d) {

    }

    @Override
    public void setupPlotContextMenu() {
        plotContextMenu = new ContextMenu();
        MenuItem plotContextMenuItem1 = new MenuItem("Restore plot");
        plotContextMenuItem1.setOnAction((mouseEvent) -> {
            refreshPanel(true, true);
        });

        plotContextMenu.getItems().addAll(plotContextMenuItem1);
    }
}