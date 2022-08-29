package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.cirdles.tripoli.visualizationUtilities.linePlots.LinePlotBuilder;


public class BasicScatterPlot extends AbstractDataView {

    private final LinePlotBuilder intensityLinePlotBuilder;

    /**
     * @param bounds
     * @param intensityLinePlotBuilder
     */
    public BasicScatterPlot(Rectangle bounds, LinePlotBuilder intensityLinePlotBuilder) {
        super(bounds, 50, 5);
        this.intensityLinePlotBuilder = intensityLinePlotBuilder;
    }

    @Override
    public void preparePanel() {
        xAxisData = intensityLinePlotBuilder.getxData();
        yAxisData = intensityLinePlotBuilder.getyData();

        minX = xAxisData[0];
        maxX = xAxisData[xAxisData.length - 1];

        ticsX = TicGeneratorForAxes.generateTics(minX, maxX, (int) (graphWidth / 40.0));
        double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.01);
        minX -= xMarginStretch;
        maxX += xMarginStretch;

        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (double yAxisDatum : yAxisData) {
            minY = StrictMath.min(minY, yAxisDatum);
            maxY = StrictMath.max(maxY, yAxisDatum);
        }
        ticsY = TicGeneratorForAxes.generateTics(minY, maxY, (int) (graphHeight / 15.0));
        if ((ticsY != null) && (ticsY.length > 1)) {
            // force y to tics
            minY = ticsY[0].doubleValue();
            maxY = ticsY[ticsY.length - 1].doubleValue();
            // adjust margins
            double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.1);
            minY -= yMarginStretch * 2.0;
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
        text.setFont(Font.font("SansSerif", 12));
        int textWidth = 0;

        showTitle(intensityLinePlotBuilder.getTitle());

        g2d.setLineWidth(0.5);
        // scatter plot
        g2d.setStroke(Paint.valueOf("Black"));
        for (int i = 0; i < xAxisData.length; i++) {
            g2d.strokeOval(mapX(xAxisData[i]) - 2f, mapY(yAxisData[i]) - 2f, 4f, 4f);
        }


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
                            (float) mapX(minX) - textWidth + 5f,
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