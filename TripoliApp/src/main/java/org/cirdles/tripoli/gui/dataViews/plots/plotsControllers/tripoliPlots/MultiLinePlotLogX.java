package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractDataView;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.linePlots.MultiLinePlotBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


public class MultiLinePlotLogX extends AbstractDataView {

    private final MultiLinePlotBuilder multiLinePlotBuilder;
    private double[][] xData;
    private double[][] yData;

    /**
     * @param bounds
     * @param multiLinePlotBuilder
     */
    public MultiLinePlotLogX(Rectangle bounds, MultiLinePlotBuilder multiLinePlotBuilder) {
        super(bounds, 50, 5);
        this.multiLinePlotBuilder = multiLinePlotBuilder;
    }

    @Override
    public void preparePanel() {
        xAxisData = xData[0];
        yData = multiLinePlotBuilder.getyData();

        minX = Math.log(xData[0][0]);
        maxX = Math.log(xData[0][xData[0].length - 1]);

        // logarithmic ticsX
        List<Double> xTicsList = new ArrayList<>();
        int limitLog = (int) xData[0][xData[0].length - 1];
        for (int logIndex = 1; logIndex <= limitLog; logIndex = logIndex * 10) {
            xTicsList.add(Math.log(logIndex));
        }
        ticsX = new BigDecimal[xTicsList.size()];
        for (int i = 0; i < xTicsList.size(); i++) {
            ticsX[i] = new BigDecimal(Double.toString(xTicsList.get(i)));
        }
        double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.01);
        minX -= xMarginStretch;
        maxX += xMarginStretch;

        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (double[] yDatum : yData) {
            for (double v : yDatum) {
                minY = StrictMath.min(minY, v);
                maxY = StrictMath.max(maxY, v);
            }
        }
        ticsY = TicGeneratorForAxes.generateTics(minY, maxY, (int) (graphHeight / 15.0));
        if ((null != ticsY) && (1 < ticsY.length)) {
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

        repaint();
    }

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);

        Text text = new Text();
        text.setFont(Font.font("SansSerif", 12));
        int textWidth = 0;

        labelXAxis("Log of Saved Iteration Count");
        showTitle(multiLinePlotBuilder.getTitle()[0]);

        g2d.setLineWidth(2.0);
        // new line plots
        // specify cool heatmap
        double rgbStartRed = 151.0 / 255.0;
        double rgbStartGreen = 248.0 / 255.0;
        double rgbStartBlue = 253.0 / 255.0;
        double rgbEndRed = 231.0 / 255.0;
        double rgbEndGreen = 56.0 / 255.0;
        double rgbEndBlue = 244.0 / 255.0;
        double redDelta = (rgbEndRed - rgbStartRed) / yData.length;
        double greenDelta = (rgbEndGreen - rgbStartGreen) / yData.length;
        double blueDelta = (rgbEndBlue - rgbStartBlue) / yData.length;

        for (int y = 0; y < yData.length; y++) {
            g2d.setStroke(Color.color(rgbStartRed + redDelta * y, rgbStartGreen + greenDelta * y, rgbStartBlue + blueDelta * y));
            g2d.beginPath();
            g2d.moveTo(mapX(Math.log(xAxisData[0])), mapY(yData[y][0]));
            for (int i = 0; i < xAxisData.length; i++) {
                // line tracing through points
                g2d.lineTo(mapX(Math.log(xAxisData[i])), mapY(yData[y][i]));
            }
            g2d.stroke();
        }

        if (1 < ticsY.length) {
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
            if (null != ticsY) {
                for (BigDecimal bigDecimal : ticsY) {
                    g2d.strokeLine(
                            mapX(minX), mapY(bigDecimal.doubleValue()), mapX(maxX), mapY(bigDecimal.doubleValue()));

                    // left side
                    text.setText(bigDecimal.toString());
                    textWidth = (int) text.getLayoutBounds().getWidth();
                    g2d.fillText(text.getText(),//
                            (float) mapX(minX) - textWidth - 5.0f,
                            (float) mapY(bigDecimal.doubleValue()) + verticalTextShift);

                }
                // ticsX
                if (null != ticsX) {
                    for (BigDecimal bigDecimal : ticsX) {
                        try {
                            g2d.strokeLine(
                                    mapX(bigDecimal.doubleValue()),
                                    mapY(ticsY[0].doubleValue()),
                                    mapX(bigDecimal.doubleValue()),
                                    mapY(ticsY[0].doubleValue()) + 5);

                            // bottom
                            String xText = (new BigDecimal(Double.toString(Math.exp(bigDecimal.doubleValue())))).setScale(-1, RoundingMode.HALF_UP).toPlainString();
                            g2d.fillText(xText,
                                    (float) mapX(bigDecimal.doubleValue()) - 5.0f,
                                    (float) mapY(ticsY[0].doubleValue()) + 15);

                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }
}