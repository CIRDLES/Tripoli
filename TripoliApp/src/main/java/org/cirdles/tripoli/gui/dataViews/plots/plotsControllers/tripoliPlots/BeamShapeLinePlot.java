package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractDataView;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.visualizationUtilities.linePlots.BeamShapeLinePlotBuilder;


public class BeamShapeLinePlot extends AbstractDataView {

    private final BeamShapeLinePlotBuilder beamShapeLinePlotBuilder;
    private int leftBoundary;
    private int rightBoundary;

    /**
     * @param bounds
     * @param beamShapeLinePlotBuilder
     */
    public BeamShapeLinePlot(Rectangle bounds, BeamShapeLinePlotBuilder beamShapeLinePlotBuilder) {
        super(bounds, 50, 35);
        this.beamShapeLinePlotBuilder = beamShapeLinePlotBuilder;
    }

    @Override
    public void preparePanel() {
        xAxisData = beamShapeLinePlotBuilder.getxData();
        yAxisData = beamShapeLinePlotBuilder.getyData();
        leftBoundary = beamShapeLinePlotBuilder.getLeftBoundary();
        rightBoundary = beamShapeLinePlotBuilder.getRightBoundary();

        minX = xAxisData[0];
        maxX = xAxisData[xAxisData.length - 1];

        ticsX = TicGeneratorForAxes.generateTics(minX, maxX, (int) (graphWidth / 35.0));
        double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.05);
        minX -= xMarginStretch;
        maxX += xMarginStretch;

        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (double yAxisDatum : yAxisData) {
            minY = StrictMath.min(minY, yAxisDatum);
            maxY = StrictMath.max(maxY, yAxisDatum);
        }
        ticsY = TicGeneratorForAxes.generateTics(minY, maxY, (int) (graphHeight / 20.0));
        if ((ticsY != null) && (ticsY.length > 1)) {
            // force y to tics
            minY = ticsY[0].doubleValue();
            maxY = ticsY[ticsY.length - 1].doubleValue();
            // adjust margins
            double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.05);
            minY -= yMarginStretch;
            maxY += yMarginStretch;
        }

        setDisplayOffsetY(0.0);
        setDisplayOffsetX(0.0);

        this.repaint();
    }

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);

        Text text = new Text();
        g2d.setFont(Font.font("SansSerif", FontWeight.SEMI_BOLD, 12));
        int textWidth = 0;

        g2d.setFill(Paint.valueOf("RED"));
        g2d.fillText(beamShapeLinePlotBuilder.getTitle(), 20, 20);

        g2d.setLineWidth(2.0);
        // new line graph
        g2d.setStroke(Paint.valueOf("Black"));
        g2d.beginPath();
        g2d.moveTo(mapX(xAxisData[0]), mapY(yAxisData[0]));
        for (int i = 0; i < xAxisData.length; i++) {
            // line tracing through points
            g2d.lineTo(mapX(xAxisData[i]), mapY(yAxisData[i]));
        }

        g2d.stroke();
        g2d.beginPath();
        g2d.setLineDashes(8);
        g2d.setStroke(Paint.valueOf("Blue"));
        for (int i = leftBoundary; i <= rightBoundary; i++) {
            // line tracing through points

            g2d.lineTo(mapX(xAxisData[i]), mapY(yAxisData[leftBoundary]));
        }
        g2d.stroke();

        g2d.setFill(Paint.valueOf("Red"));
        g2d.fillOval(mapX(xAxisData[leftBoundary]) - 3.5, mapY(yAxisData[leftBoundary]) - 3.5, 7, 7);
        g2d.fillOval(mapX(xAxisData[rightBoundary]) - 3.5, mapY(yAxisData[rightBoundary]) - 3.5, 7, 7);

        g2d.beginPath();
        g2d.setLineDashes(0);

        g2d.stroke();
        if (ticsY.length > 1) {
            // border and fill
            g2d.setLineWidth(0.5);
            g2d.setStroke(Paint.valueOf("BLACK"));
            g2d.strokeRect(
                    mapX(minX),
                    mapY(ticsY[ticsY.length - 1].doubleValue()),
                    graphWidth,
                    StrictMath.abs(mapY(ticsY[ticsY.length - 1].doubleValue()) - mapY(ticsY[0].doubleValue())));

            g2d.setFill(Paint.valueOf("BLACK"));

            // ticsY
            float verticalTextShift = 3.2f;
            g2d.setFont(Font.font("SansSerif", 10));
            if (ticsY != null) {
                for (java.math.BigDecimal bigDecimal : ticsY) {
                    g2d.strokeLine(
                            mapX(minX), mapY(bigDecimal.doubleValue()), mapX(maxX), mapY(bigDecimal.doubleValue()));

                    // left side
                    text.setText(bigDecimal.toString());
                    textWidth = (int) text.getLayoutBounds().getWidth();
                    g2d.fillText(text.getText(),//
                            (float) mapX(minX) - textWidth - 5f,
                            (float) mapY(bigDecimal.doubleValue()) + verticalTextShift);

                }
                // ticsX
                if (ticsX != null) {
                    for (int i = 0; i < ticsX.length - 1; i++) {
                        try {
                            g2d.strokeLine(
                                    mapX(ticsX[i].doubleValue()),
                                    mapY(ticsY[0].doubleValue()),
                                    mapX(ticsX[i].doubleValue()),
                                    mapY(ticsY[0].doubleValue()) + 5);

                            // bottom
                            String xText = ticsX[i].toPlainString();
                            g2d.fillText(xText,
                                    (float) mapX(ticsX[i].doubleValue()) - 5f,
                                    (float) mapY(ticsY[0].doubleValue()) + 15);

                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }
}