package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractDataView;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


public class BasicLinePlotLogX extends AbstractDataView {

    private final LinePlotBuilder linePlotBuilder;

    /**
     * @param bounds
     * @param linePlotBuilder
     */
    public BasicLinePlotLogX(Rectangle bounds, LinePlotBuilder linePlotBuilder) {
        super(bounds, 50, 5);
        this.linePlotBuilder = linePlotBuilder;
    }

    @Override
    public void preparePanel() {
        xAxisData = linePlotBuilder.getxData();
        yAxisData = linePlotBuilder.getyData();

        minX = Math.log(xAxisData[0]);
        maxX = Math.log(xAxisData[xAxisData.length - 1]);

        // logarithmic ticsX
        List<Double> xTicsList = new ArrayList<>();
        int limitLog = (int) xAxisData[xAxisData.length - 1];
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

        labelXAxis("Log of Saved Iteration Count");
        showTitle(linePlotBuilder.getTitle());

        g2d.setLineWidth(2.0);
        // new line plot
        g2d.setStroke(Paint.valueOf("Black"));
        g2d.beginPath();
        g2d.moveTo(mapX(Math.log(xAxisData[0])), mapY(yAxisData[0]));
        for (int i = 0; i < xAxisData.length; i++) {
            // line tracing through points
            g2d.lineTo(mapX(Math.log(xAxisData[i])), mapY(yAxisData[i]));
        }
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
                for (BigDecimal bigDecimal : ticsY) {
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
                                    (float) mapX(bigDecimal.doubleValue()) - 5f,
                                    (float) mapY(ticsY[0].doubleValue()) + 15);

                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }
}