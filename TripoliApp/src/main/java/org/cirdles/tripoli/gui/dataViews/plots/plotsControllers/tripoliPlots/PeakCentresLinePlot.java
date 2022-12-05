package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractDataView;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.peakShapePlots.PeakShapePlotsController;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;

import java.util.HashMap;
import java.util.Map;

public class PeakCentresLinePlot extends AbstractDataView {
    private final LinePlotBuilder peakCentrePlotBuilder;
    public int indexOfSelectedSpot;
    Map<Integer, String> tooltips = new HashMap<>();

    /**
     * @param bounds
     * @param linePlotBuilder
     */
    public PeakCentresLinePlot(Rectangle bounds, LinePlotBuilder linePlotBuilder) {
        super(bounds, 50, 30);
        this.peakCentrePlotBuilder = linePlotBuilder;


        this.setOnMouseMoved(new MouseMovedHandler());
        this.setOnMouseClicked(new MouseClickedEventHandler());
        this.indexOfSelectedSpot = -1;

    }

    @Override
    public void preparePanel() {

        xAxisData = peakCentrePlotBuilder.getxData();
        yAxisData = peakCentrePlotBuilder.getyData();

        minX = xAxisData[0];
        maxX = xAxisData[xAxisData.length - 1];

        ticsX = TicGeneratorForAxes.generateTics(minX, maxX + 1, (int) (graphWidth / 50.0));
        double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.05);
        minX -= xMarginStretch;
        maxX += xMarginStretch;

        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (int i = 0; i < yAxisData.length; i++) {
            minY = StrictMath.min(minY, yAxisData[i]);
            maxY = StrictMath.max(maxY, yAxisData[i]);
        }

        ticsY = TicGeneratorForAxes.generateTics(minY, maxY + 0.0005, (int) (graphHeight / 25.0));
        if ((ticsY != null) && (ticsY.length > 1)) {
            // force y to tics
            minY = ticsY[0].doubleValue();
            maxY = ticsY[ticsY.length - 1].doubleValue();
            // adjust margins
            double yMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minY, maxY, 0.1);
            minY -= yMarginStretch;
            maxY += yMarginStretch;
        }

        setDisplayOffsetY(0.0);
        setDisplayOffsetX(0.0);

        for (int i = 0; i < xAxisData.length; i++) {
            tooltips.put((int) xAxisData[i] - 1, (xAxisData[i]) + "");
        }


        this.repaint();
    }

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);

        if (yAxisData.length != 1) {
            Text text = new Text();
            g2d.setFont(Font.font("SansSerif", FontWeight.SEMI_BOLD, 12));
            int textWidth = 0;

            g2d.setFill(Paint.valueOf("RED"));
            g2d.fillText(peakCentrePlotBuilder.getTitle(), 20, 12);

            g2d.setLineWidth(2.5);

            g2d.beginPath();
            g2d.setStroke(Paint.valueOf("Black"));
            g2d.setLineDashes(0);
            // x = magnetMass y = blockIntensities

            for (int i = 0; i < xAxisData.length; i++) {
                g2d.lineTo(mapX(xAxisData[i]), mapY(yAxisData[i]));
            }

            g2d.stroke();
            g2d.beginPath();
            g2d.setLineWidth(2.5);
            g2d.setLineDashes(4);
            g2d.setStroke(Paint.valueOf("Red"));

            // x = Time y = Peak Widths
            g2d.moveTo(mapX(xAxisData[0]), mapY(yAxisData[0]));
            for (int i = 0; i < xAxisData.length; i++) {
                // line tracing through points
                g2d.fillOval(mapX(xAxisData[i]) - 4, mapY(yAxisData[i]) - 4, 8, 8);
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
                    for (int i = 0; i < ticsY.length; i++) {
                        g2d.strokeLine(
                                mapX(minX), mapY(ticsY[i].doubleValue()), mapX(maxX), mapY(ticsY[i].doubleValue()));

                        // left side
                        text.setText(ticsY[i].toString());
                        textWidth = (int) text.getLayoutBounds().getWidth();
                        g2d.fillText(text.getText(),//
                                (float) mapX(minX) - textWidth - 3f,
                                (float) mapY(ticsY[i].doubleValue()) + verticalTextShift);

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

                            } catch (Exception e) {
                            }
                        }
                    }
                }
            }

        } else {
            g2d.setFont(Font.font("SansSerif", FontWeight.SEMI_BOLD, 20));

            g2d.setFill(Paint.valueOf("Black"));
            g2d.fillText("Only one file", 20, 20);
        }
    }

//    private void setToolTips(Node node, Map<Integer, String> toolTips) {
//        Tooltip tooltip = new Tooltip();
//        Tooltip.install(node, tooltip);
//        node.setOnMouseMoved(e -> toolTips.forEach((integer, String) -> {
//            if (integer.equals(indexOfSpotFromMouseX(e.getX()))) {
//                tooltip.setText(String);
//            }
//        }));
//        node.setOnMouseExited(e -> tooltip.hide());
//    }

    private int indexOfSpotFromMouseX(double x) {
        double convertedX = convertMouseXToValue(x);
        int index = -1;
        for (int i = 0; i < xAxisData.length - 1; i++) {
            if ((convertedX >= xAxisData[i] - 0.5) && convertedX < xAxisData[i + 1] - 0.5) {
                index = i;
                break;
            }


            if (index == -1 && ((StrictMath.abs(convertedX - xAxisData[xAxisData.length - 1]) < 0.5)))
                index = xAxisData.length - 1;
        }

        return index;
    }


    private class MouseMovedHandler implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent event) {

            if (mouseInHouse(event)) {
                ((Canvas) event.getSource()).getParent().getScene().setCursor(Cursor.CROSSHAIR);
            } else {
                ((Canvas) event.getSource()).getParent().getScene().setCursor(Cursor.DEFAULT);
            }
        }
    }

    private class MouseClickedEventHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent mouseEvent) {
            if (mouseInHouse(mouseEvent)) {

                indexOfSelectedSpot = indexOfSpotFromMouseX(mouseEvent.getX());
                PeakShapePlotsController.currentGroupIndex = indexOfSelectedSpot;
                PeakShapePlotsController.resourceBrowserTarget = PeakShapePlotsController.getResourceGroups(PeakShapePlotsController.getCurrentGroup()).get(indexOfSelectedSpot);
                repaint();
                getGraphicsContext2D().setLineWidth(1.0);
                getGraphicsContext2D().strokeOval(mapX(xAxisData[indexOfSelectedSpot]) - 6, mapY(yAxisData[indexOfSelectedSpot]) - 6, 12, 12);


            } else {
                System.out.println(mouseEvent.getClickCount());
            }
        }
    }

}