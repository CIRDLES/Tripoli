package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.cirdles.tripoli.visualizationUtilities.linePlots.ComboPlotBuilder;


public class BasicScatterAndLinePlot extends AbstractDataView {

    private final ComboPlotBuilder comboPlotBuilder;
    private double[] yAxisData2;

    /**
     * @param bounds
     * @param comboPlotBuilder
     */
    public BasicScatterAndLinePlot(Rectangle bounds, ComboPlotBuilder comboPlotBuilder) {
        super(bounds, 50, 5);
        this.comboPlotBuilder = comboPlotBuilder;
    }

    @Override
    public void preparePanel() {
        xAxisData = comboPlotBuilder.getxData();
        yAxisData = comboPlotBuilder.getyData();
        yAxisData2 = comboPlotBuilder.getyData2();

        minX = xAxisData[0];
        maxX = xAxisData[xAxisData.length - 1];

        ticsX = TicGeneratorForAxes.generateTics(minX, maxX, (int) (graphWidth / 40.0));
        double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.01);
        minX -= xMarginStretch;
        maxX += xMarginStretch;

        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (int i = 0; i < yAxisData.length; i++) {
            minY = StrictMath.min(minY, yAxisData[i]);
            maxY = StrictMath.max(maxY, yAxisData[i]);
            minY = StrictMath.min(minY, yAxisData2[i]);
            maxY = StrictMath.max(maxY, yAxisData2[i]);
            if (comboPlotBuilder.isyData2OneSigma()) {
                minY = StrictMath.min(minY, -yAxisData2[i]);
            }
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

        showTitle(comboPlotBuilder.getTitle());

        // scatter plot
        g2d.setLineWidth(0.75);
        g2d.setStroke(Paint.valueOf("Black"));
        for (int i = 0; i < xAxisData.length; i++) {
            g2d.strokeOval(mapX(xAxisData[i]) - 2f, mapY(yAxisData[i]) - 2f, 4f, 4f);
        }

        // new line plot from yAxisData2
        g2d.setStroke(Paint.valueOf("red"));
        g2d.beginPath();
        g2d.moveTo(mapX(xAxisData[0]), mapY(yAxisData2[0]));
        for (int i = 0; i < xAxisData.length; i++) {
            // line tracing through points
            g2d.lineTo(mapX(xAxisData[i]), mapY(yAxisData2[i]));
        }
        g2d.stroke();

        if (comboPlotBuilder.isyData2OneSigma()) {
            g2d.beginPath();
            g2d.moveTo(mapX(xAxisData[0]), mapY(-yAxisData2[0]));
            for (int i = 0; i < xAxisData.length; i++) {
                // line tracing through points
                g2d.lineTo(mapX(xAxisData[i]), mapY(-yAxisData2[i]));
            }
            g2d.stroke();
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