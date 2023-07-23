package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.sessionPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;
import org.cirdles.tripoli.plots.sessionPlots.SpeciesIntensitySessionBuilder;

public class SpeciesIntensitySessionPlot extends AbstractPlot {
    private final SpeciesIntensitySessionBuilder speciesIntensitySessionBuilder;


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

        yAxisData = speciesIntensitySessionBuilder.getyData()[0];
        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (int i = 0; i < yAxisData.length; i++) {
            if (yAxisData[i] > 0.0) {
                minY = StrictMath.min(minY, yAxisData[i]);
                maxY = StrictMath.max(maxY, yAxisData[i]);
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
        minX -= xMarginStretch;
        maxX += xMarginStretch;

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
            if (pointInPlot(xAxisData[i], yAxisData[i])) {
                double dataX = mapX(xAxisData[i]);
                double dataY = mapY(yAxisData[i]);
                g2d.fillOval(dataX - 2.5, dataY - 2.5, 5, 5);
            }
        }
    }

    @Override
    public void plotStats(GraphicsContext g2d) {

    }
}