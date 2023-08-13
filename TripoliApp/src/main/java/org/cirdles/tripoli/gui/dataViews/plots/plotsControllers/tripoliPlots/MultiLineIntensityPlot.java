package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.linePlots.MultiLinePlotBuilder;


public class MultiLineIntensityPlot extends AbstractPlot {

    private final MultiLinePlotBuilder multiLinePlotBuilder;
    private double[][] xData;
    private double[][] yData;

    /**
     * @param bounds
     * @param multiLinePlotBuilder
     */
    private MultiLineIntensityPlot(Rectangle bounds, MultiLinePlotBuilder multiLinePlotBuilder) {
        super(bounds, 75, 25,
                multiLinePlotBuilder.getTitle(),
                multiLinePlotBuilder.getxAxisLabel(),
                multiLinePlotBuilder.getyAxisLabel());
        this.multiLinePlotBuilder = multiLinePlotBuilder;
    }

    public static AbstractPlot generatePlot(Rectangle bounds, MultiLinePlotBuilder multiLinePlotBuilder) {
        return new MultiLineIntensityPlot(bounds, multiLinePlotBuilder);
    }

    @Override
    public void preparePanel(boolean reScaleX, boolean reScaleY) {
        xData = multiLinePlotBuilder.getxData();
        xAxisData = xData[0];
        minX = (xData[0][0]);
        maxX = (xData[0][xData[0].length - 1]);

        yData = multiLinePlotBuilder.getyData();
        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (int i = 0; i < yData.length; i++) {
            for (int j = 0; j < yData[i].length; j++) {
                minY = StrictMath.min(minY, yData[i][j]);
                maxY = StrictMath.max(maxY, yData[i][j]);
            }
        }

        displayOffsetX = 0.0;
        displayOffsetY = 0.0;

        prepareExtents(true, true);
        calculateTics();
        repaint();
    }

    public void prepareExtents(boolean reScaleX, boolean reScaleY) {
        double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.01);
        if (0.0 == xMarginStretch) {
            xMarginStretch = maxX * 0.01;
        }
        minX -= xMarginStretch;
        maxX += xMarginStretch;

        double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.1);
        maxY += yMarginStretch;
        minY -= yMarginStretch;
    }

//    @Override
//    public void calculateTics() {
//        // logarithmic ticsX
//        List<Double> xTicsList = new ArrayList<>();
//        int limitLog = (int) xData[0][xData[0].length - 1];
//        for (int logIndex = 1; logIndex <= limitLog; logIndex = logIndex * 10) {
//            xTicsList.add(Math.log(logIndex));
//        }
//        ticsX = new BigDecimal[xTicsList.size()];
//        for (int i = 0; i < xTicsList.size(); i++) {
//            ticsX[i] = new BigDecimal(Double.toString(xTicsList.get(i)));
//        }
//
//        if (0 == ticsX.length) {
//            ticsX = new BigDecimal[2];
//            ticsX[0] = new BigDecimal(Double.toString(minX));
//            ticsX[ticsX.length - 1] = new BigDecimal(Double.toString(maxX));
//        }
//
//        ticsY = TicGeneratorForAxes.generateTics(getDisplayMinY(), getDisplayMaxY(), (int) (plotHeight / 15.0));
//        if (0 == ticsY.length) {
//            ticsY = new BigDecimal[2];
//            ticsY[0] = new BigDecimal(Double.toString(minY));
//            ticsY[ticsY.length - 1] = new BigDecimal(Double.toString(maxY));
//        }
//
//        zoomChunkX = getDisplayRangeX() / 100.0;
//        zoomChunkY = getDisplayRangeY() / 100.0;
//    }

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);
    }

    @Override
    public void plotData(GraphicsContext g2d) {
        Text text = new Text();
        text.setFont(Font.font("SansSerif", 12));
        int textWidth = 0;

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
            boolean startedPlot = false;
            for (int i = 0; i < xAxisData.length; i++) {
                if (pointInPlot((xAxisData[i]), yData[y][i])) {
                    if (!startedPlot) {
                        g2d.moveTo(mapX((xAxisData[i])), mapY(yData[y][i]));
                        startedPlot = true;
                    }
                    // line tracing through points
                    g2d.lineTo(mapX((xAxisData[i])), mapY(yData[y][i]));
                } else {
                    // out of bounds
                    g2d.moveTo(mapX((xAxisData[i])), mapY(yData[y][i]) < topMargin ? topMargin : topMargin + plotHeight);
                }
            }
            g2d.stroke();
        }

//        if (1 < ticsY.length) {
//            // border and fill
//            g2d.setLineWidth(0.5);
//            g2d.setStroke(Paint.valueOf("BLACK"));
//            g2d.strokeRect(
//                    mapX(minX),
//                    mapY(ticsY[ticsY.length - 1].doubleValue()),
//                    graphWidth,
//                    StrictMath.abs(mapY(ticsY[ticsY.length - 1].doubleValue()) - mapY(ticsY[0].doubleValue())));
//
//            g2d.setFill(Paint.valueOf("BLACK"));
//
//            // ticsY
//            float verticalTextShift = 3.2f;
//            g2d.setFont(Font.font("SansSerif", 10));
//            if (null != ticsY) {
//                for (BigDecimal bigDecimal : ticsY) {
//                    g2d.strokeLine(
//                            mapX(minX), mapY(bigDecimal.doubleValue()), mapX(maxX), mapY(bigDecimal.doubleValue()));
//
//                    // left side
//                    text.setText(bigDecimal.toString());
//                    textWidth = (int) text.getLayoutBounds().getWidth();
//                    g2d.fillText(text.getText(),//
//                            (float) mapX(minX) - textWidth - 5.0f,
//                            (float) mapY(bigDecimal.doubleValue()) + verticalTextShift);
//
//                }
//                // ticsX
//                if (null != ticsX) {
//                    for (BigDecimal bigDecimal : ticsX) {
//                        try {
//                            g2d.strokeLine(
//                                    mapX(bigDecimal.doubleValue()),
//                                    mapY(ticsY[0].doubleValue()),
//                                    mapX(bigDecimal.doubleValue()),
//                                    mapY(ticsY[0].doubleValue()) + 5);
//
//                            // bottom
//                            String xText = (new BigDecimal(Double.toString(Math.exp(bigDecimal.doubleValue())))).setScale(-1, RoundingMode.HALF_UP).toPlainString();
//                            g2d.fillText(xText,
//                                    (float) mapX(bigDecimal.doubleValue()) - 5.0f,
//                                    (float) mapY(ticsY[0].doubleValue()) + 15);
//
//                        } catch (Exception ignored) {
//                        }
//                    }
//                }
//            }
//        }
    }

    /**
     * @param g2d
     */
    @Override
    public void plotStats(GraphicsContext g2d) {

    }
}