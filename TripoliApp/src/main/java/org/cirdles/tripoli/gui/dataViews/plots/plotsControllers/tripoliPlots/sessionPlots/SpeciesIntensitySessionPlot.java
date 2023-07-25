package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.sessionPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;
import org.cirdles.tripoli.plots.sessionPlots.SpeciesIntensitySessionBuilder;

public class SpeciesIntensitySessionPlot extends AbstractPlot {
    private final SpeciesIntensitySessionBuilder speciesIntensitySessionBuilder;

    private double[][] yData;


    private SpeciesIntensitySessionPlot(Rectangle bounds, SpeciesIntensitySessionBuilder speciesIntensitySessionBuilder) {
        super(bounds, 75, 25,
                speciesIntensitySessionBuilder.getTitle(),
                speciesIntensitySessionBuilder.getxAxisLabel(),
                speciesIntensitySessionBuilder.getyAxisLabel());
        this.speciesIntensitySessionBuilder = speciesIntensitySessionBuilder;
    }

    public static AbstractPlot generatePlot(Rectangle bounds, SpeciesIntensitySessionBuilder speciesIntensitySessionBuilder) {
        return new SpeciesIntensitySessionPlot(bounds, speciesIntensitySessionBuilder);
    }

    @Override
    public void preparePanel() {
        xAxisData = speciesIntensitySessionBuilder.getxData();
        minX = xAxisData[0];
        maxX = xAxisData[xAxisData.length - 1];

        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

//        for (int i = 0; i < yAxisData.length; i++) {
//            if (yAxisData[i] != 0.0) {
//                minY = StrictMath.min(minY, yAxisData[i]);
//                maxY = StrictMath.max(maxY, yAxisData[i]);
//            }
//        }

        yData = speciesIntensitySessionBuilder.getyData();
        for (int row = 0; row < 2; row++){//yData.length
            for (int col = 0; col < yData[row].length; col++){
                if (yData[row][col] != 0.0) {
                    minY = StrictMath.min(minY, yData[row][col]);
                    maxY = StrictMath.max(maxY, yData[row][col]);
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
        g2d.setLineWidth(1.0);

        for (int i = 0; i < xAxisData.length; i++) {
            if ((yData[0][i] != 0.0) && pointInPlot(xAxisData[i], yData[0][i])) {
                double dataX = mapX(xAxisData[i]);
                double dataY = mapY(yData[0][i]);
                g2d.fillOval(dataX - 2.0, dataY - 2.0, 4, 4);
            }
        }

//
//        for (int i = 0; i < xAxisData.length; i++) {
//            if ((yData[1][i] != 0.0) && pointInPlot(xAxisData[i], yData[1][i])) {
//                double dataX = mapX(xAxisData[i]);
//                double dataY = mapY(yData[1][i]);
//                g2d.fillOval(dataX - 2.0, dataY - 2.0, 4, 4);
//            }
//        }

        g2d.setStroke(Color.GREEN);
        g2d.beginPath();
        boolean startedPlot = false;
        for (int i = 0; i < xAxisData.length; i++) {
            if ((yData[1][i] != 0.0) && pointInPlot(xAxisData[i], yData[1][i])) {
                if (!startedPlot) {
                    g2d.moveTo(mapX(xAxisData[i]), mapY(yData[1][i]));
                    startedPlot = true;
                }
                // line tracing through points
                g2d.lineTo(mapX(xAxisData[i]), mapY(yData[1][i]));
            } else {
                // out of bounds
//                g2d.moveTo(mapX(xAxisData[i]), mapY(yData[1][i]) < topMargin ? topMargin : topMargin + plotHeight);
            }
        }
        g2d.stroke();

//        g2d.setFill(Color.ORANGE);
//        for (int i = 0; i < xAxisData.length; i++) {
//            if ((yData[2][i] != 0.0) && pointInPlot(xAxisData[i], yData[2][i])) {
//                double dataX = mapX(xAxisData[i]);
//                double dataY = mapY(yData[2][i]);
//                g2d.fillOval(dataX - 2.0, dataY - 2.0, 4, 4);
//            }
//        }
//
//        g2d.setFill(Color.RED);
//        for (int i = 0; i < xAxisData.length; i++) {
//            if ((yData[3][i] != 0.0) && pointInPlot(xAxisData[i], yData[3][i])) {
//                double dataX = mapX(xAxisData[i]);
//                double dataY = mapY(yData[3][i]);
//                g2d.fillOval(dataX - 2.0, dataY - 2.0, 4, 4);
//            }
//        }
    }

    @Override
    public void plotStats(GraphicsContext g2d) {

    }
}