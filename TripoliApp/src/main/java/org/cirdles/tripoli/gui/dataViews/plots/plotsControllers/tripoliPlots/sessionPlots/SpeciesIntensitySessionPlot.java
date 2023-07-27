package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.sessionPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.constants.TripoliConstants;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.sessionPlots.SpeciesIntensitySessionBuilder;

public class SpeciesIntensitySessionPlot extends AbstractPlot {
    private final SpeciesIntensitySessionBuilder speciesIntensitySessionBuilder;

    private double[][] yDataCounts;
    private double[][] yData;
    private double[][] ampResistance;

    private boolean[] speciesChecked;
    private boolean showFaradays;
    private boolean showPMs;
    private boolean showModels;

    private SpeciesIntensitySessionPlot(Rectangle bounds, SpeciesIntensitySessionBuilder speciesIntensitySessionBuilder) {
        super(bounds, 75, 25,
                speciesIntensitySessionBuilder.getTitle(),
                speciesIntensitySessionBuilder.getxAxisLabel(),
                speciesIntensitySessionBuilder.getyAxisLabel());
        this.speciesIntensitySessionBuilder = speciesIntensitySessionBuilder;
        this.yDataCounts = speciesIntensitySessionBuilder.getyData();
        this.ampResistance = speciesIntensitySessionBuilder.getAmpResistance();
        this.speciesChecked = new boolean[yDataCounts.length / 4];
        this.speciesChecked[0] = true;
        this.showFaradays = true;
        this.showPMs = true;
        this.showModels = true;
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

    TripoliConstants.IntensityUnits intensityUnits = TripoliConstants.IntensityUnits.COUNTS;

    @Override
    public void preparePanel() {
        xAxisData = speciesIntensitySessionBuilder.getxData();
        minX = xAxisData[0];
        maxX = xAxisData[xAxisData.length - 1];

        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        yData = new double[yDataCounts.length][yDataCounts[0].length];
        switch (intensityUnits){
            case VOLTS -> {
                for (int row = 0; row < yDataCounts.length; row++) {
                    yData[row] = TripoliConstants.IntensityUnits.convertFromCountsToVolts(yDataCounts[row], ampResistance[row / 4]);
                }
            }
            case AMPS -> {
                for (int row = 0; row < yDataCounts.length; row++) {
                    yData[row] = TripoliConstants.IntensityUnits.convertFromCountsToAmps( yDataCounts[row]);
                }
            }
            case COUNTS ->{
                for (int row = 0; row < yDataCounts.length; row++){
                    yData[row] = yDataCounts[row];
                }
            }
        }

        for (int row = 0; row < yData.length; row++) {
            int speciesIndex = (row / 4);
            if (speciesChecked[speciesIndex]) {
                boolean plotFaradays = (showFaradays && (row >= speciesIndex * 4) && (row <= speciesIndex * 4 + 1));
                boolean plotPMs = (showPMs && (row >= speciesIndex * 4 + 2) && (row <= speciesIndex * 4 + 3));
                for (int col = 0; col < yData[row].length; col++) {
                    if ((yData[row][col] != 0.0) && (plotFaradays || plotPMs)) {
                        minY = StrictMath.min(minY, yData[row][col]);
                        maxY = StrictMath.max(maxY, yData[row][col]);
                    }
                }
            }
        }

        displayOffsetX = 0.0;
        displayOffsetY = 0.0;

        prepareExtents();
        calculateTics();
        repaint();
    }

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);
    }

    public void prepareExtents() {
        double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.01);
        if (0.0 == xMarginStretch) {
            xMarginStretch = maxX * 0.01;
        }
        minX -= 50;//xMarginStretch;
        maxX += 50;//xMarginStretch;

        double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.01);
        maxY += yMarginStretch;
        minY -= yMarginStretch;
    }

    @Override
    public void plotData(GraphicsContext g2d) {

        g2d.setFill(dataColor.color());
        g2d.setStroke(dataColor.color());
        g2d.setLineWidth(1.5);

        Color[] isotopeColors = {Color.BLUE, Color.GREEN, Color.BLACK, Color.PURPLE, Color.ORANGE};
        for (int isotopePlotSetIndex = 0; isotopePlotSetIndex < yData.length / 4; isotopePlotSetIndex++) {
            if (speciesChecked[isotopePlotSetIndex]) {
                // plot Faraday
                if (showFaradays) {
                    g2d.beginPath();
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
                            if ((yData[isotopePlotSetIndex * 4 + 1][i] != 0.0) && pointInPlot(xAxisData[i], yData[isotopePlotSetIndex * 4 + 1][i])) {
                                if (!startedPlot) {
                                    g2d.moveTo(mapX(xAxisData[i]), mapY(yData[isotopePlotSetIndex * 4 + 1][i]));
                                    startedPlot = true;
                                }
                                // line tracing through points
                                g2d.lineTo(mapX(xAxisData[i]), mapY(yData[isotopePlotSetIndex * 4 + 1][i]));
                            }
                        }
                    }
                    g2d.setStroke(Color.RED);
                    g2d.stroke();
                    g2d.setStroke(isotopeColors[isotopePlotSetIndex]);
                }

                // plot PM
                if (showPMs) {
                    g2d.beginPath();
                    g2d.setLineDashes(4);
                    boolean startedPlot = false;
                    g2d.setFill(isotopeColors[isotopePlotSetIndex]);
                    g2d.setStroke(isotopeColors[isotopePlotSetIndex]);
                    for (int i = 0; i < xAxisData.length; i++) {
                        if ((yData[isotopePlotSetIndex * 4 + 2][i] != 0.0) && pointInPlot(xAxisData[i], yData[isotopePlotSetIndex * 4 + 2][i])) {
                            double dataX = mapX(xAxisData[i]);
                            double dataY = mapY(yData[isotopePlotSetIndex * 4 + 2][i]);
                            g2d.fillRect(dataX - 1.5, dataY - 1.5, 3, 3);
                        }

                        if (showModels) {
                            if ((yData[isotopePlotSetIndex * 4 + 3][i] != 0.0) && pointInPlot(xAxisData[i], yData[isotopePlotSetIndex * 4 + 3][i])) {
                                if (!startedPlot) {
                                    g2d.moveTo(mapX(xAxisData[i]), mapY(yData[isotopePlotSetIndex * 4 + 3][i]));
                                    startedPlot = true;
                                }
                                // line tracing through points
                                g2d.lineTo(mapX(xAxisData[i]), mapY(yData[isotopePlotSetIndex * 4 + 3][i]));
                            }
                        }
                    }
                    g2d.setStroke(Color.RED);
                    g2d.stroke();
                    g2d.setStroke(isotopeColors[isotopePlotSetIndex]);

                }
            }
        }


    }

    @Override
    public void plotStats(GraphicsContext g2d) {

    }
}