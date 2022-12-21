package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractDataView;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.linePlots.GBeamLinePlotBuilder;

public class GBeamLinePlot extends AbstractDataView {

    private final GBeamLinePlotBuilder gBeamLinePlotBuilder;
    private double[] xMass;
    private double[] yIntensity;

    private Tooltip tooltip;

    /**
     * @param bounds
     * @param gBeamLinePlotBuilder
     */
    public GBeamLinePlot(Rectangle bounds, GBeamLinePlotBuilder gBeamLinePlotBuilder) {
        super(bounds, 50, 35);
        this.gBeamLinePlotBuilder = gBeamLinePlotBuilder;

        this.setOnMouseMoved(new MouseMovedHandler());
        tooltip = new Tooltip();
        Tooltip.install(this, tooltip);
    }

    @Override
    public void preparePanel() {
        xAxisData = gBeamLinePlotBuilder.getxData();
        yAxisData = gBeamLinePlotBuilder.getyData();
        xMass = gBeamLinePlotBuilder.getMassData();
        yIntensity = gBeamLinePlotBuilder.getIntensityData();

        minX = xAxisData[0];
        maxX = xAxisData[xAxisData.length - 1];

        ticsX = TicGeneratorForAxes.generateTics(minX, maxX, (int) (graphWidth / 50.0));
        double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.05);
        minX -= xMarginStretch;
        maxX += xMarginStretch;


        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (double v : yIntensity) {
            minY = StrictMath.min(minY, v);
            maxY = StrictMath.max(maxY, v);
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
        g2d.fillText(gBeamLinePlotBuilder.getTitle(), 20, 20);

        g2d.setLineWidth(2.5);

        g2d.beginPath();
        g2d.setStroke(Paint.valueOf("Blue"));
        g2d.setLineDashes(0);
        // x = magnetMass y = blockIntensities

        for (int i = 0; i < xMass.length; i++) {
            g2d.lineTo(mapX(xMass[i]), mapY(yIntensity[i]));
        }

        g2d.stroke();
        g2d.beginPath();
        g2d.setLineWidth(2.5);
        g2d.setLineDashes(4);
        g2d.setStroke(Paint.valueOf("Red"));

        // x = magnetMass y = G-Beam
        g2d.moveTo(mapX(xAxisData[0]), mapY(yAxisData[0]));
        for (int i = 0; i < xAxisData.length; i++) {
            // line tracing through points
            g2d.lineTo(mapX(xAxisData[i]), mapY(yAxisData[i]));
        }
        g2d.stroke();
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

    private class MouseMovedHandler implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent event) {

            Node potNode;

            if (mouseInHouse(event)) {
                ((Canvas) event.getSource()).getParent().getScene().setCursor(Cursor.CROSSHAIR);
                potNode = ((Canvas) event.getSource()).getParent();
                // setToolTips(potNode);

                // currently only works with x value
                for (int i = 0; i < getxAxisData().length; i++) {
                    if ((getxAxisData()[i] >= convertMouseXToValue(event.getX()) - 0.0005 && getxAxisData()[i] <= convertMouseXToValue(event.getX()) + 0.0005)) {
                        String x = String.format("%.2f", getxAxisData()[i]) ;
                        String y = String.format("%.2f", getyAxisData()[i]) ;
                        tooltip.setText(x + ", " + y);
                        tooltip.setAnchorX(event.getSceneX());
                        tooltip.show(potNode, event.getScreenX(), event.getScreenY());
                    }
                }


            } else {
                ((Canvas) event.getSource()).getParent().getScene().setCursor(Cursor.DEFAULT);
                tooltip.hide();
            }
        }
    }
}