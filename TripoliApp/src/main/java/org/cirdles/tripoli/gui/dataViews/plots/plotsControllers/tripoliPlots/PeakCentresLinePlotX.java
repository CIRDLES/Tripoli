package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots;

import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.peakShapePlots.PeakShapeDemoPlotsControllerTest;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;


public class PeakCentresLinePlotX extends AbstractPlot {
    private final LinePlotBuilder peakCentrePlotBuilder;
    public int indexOfSelectedSpot;


    /**
     * @param bounds
     * @param linePlotBuilder
     */
    private PeakCentresLinePlotX(Rectangle bounds, LinePlotBuilder linePlotBuilder) {
        super(bounds, 50, 30, linePlotBuilder.getTitle(), linePlotBuilder.getxAxisLabel(), linePlotBuilder.getyAxisLabel());
        this.peakCentrePlotBuilder = linePlotBuilder;

        setupPlotContextMenu();
        this.setOnMouseMoved(new MouseMovedHandler());
        this.setOnMouseClicked(new MouseClickedEventHandler());
        this.indexOfSelectedSpot = -1;

    }

    public static AbstractPlot generatePlot(Rectangle bounds, LinePlotBuilder linePlotBuilder) {
        return new PeakCentresLinePlotX(bounds, linePlotBuilder);
    }

    @Override
    public void preparePanel() {

        xAxisData = peakCentrePlotBuilder.getxData();
        minX = xAxisData[0];
        maxX = xAxisData[xAxisData.length - 1];

        yAxisData = peakCentrePlotBuilder.getyData();
        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (int i = 0; i < yAxisData.length; i++) {
            minY = StrictMath.min(minY, yAxisData[i]);
            maxY = StrictMath.max(maxY, yAxisData[i]);
        }

        setDisplayOffsetY(0.0);
        setDisplayOffsetX(0.0);

        prepareExtents();
        calculateTics();
        this.repaint();
    }

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);


    }


    public void prepareExtents() {
        double xMarginStretch = TicGeneratorForAxes.generateMarginAdjustment(minX, maxX, 0.01);
        if (xMarginStretch == 0.0) {
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
        if (yAxisData.length != 1) {
            g2d.setLineWidth(2.5);

            g2d.beginPath();
            g2d.setStroke(Paint.valueOf("Black"));
            g2d.setLineDashes(0);
            // x = magnetMass y = blockIntensities

            for (int i = 0; i < xAxisData.length; i++) {
                if (pointInPlot(xAxisData[i], yAxisData[i])) {
                    // line tracing through points
                    g2d.lineTo(mapX(xAxisData[i]), mapY(yAxisData[i]));

                } else {
                    // out of bounds
                    g2d.moveTo(mapX(xAxisData[i]), mapY(yAxisData[i]));
                }
            }

            g2d.stroke();
            g2d.beginPath();
            g2d.setLineWidth(2.5);
            g2d.setLineDashes(4);


            // x = Time y = Peak Widths
            g2d.moveTo(mapX(xAxisData[0]), mapY(yAxisData[0]));
            for (int i = 0; i < xAxisData.length; i++) {
                // line tracing through points
                if (pointInPlot(xAxisData[i], yAxisData[i])) {
                    // line tracing through points
                    if (yAxisData[i] == 0) {
                        g2d.setFill(Paint.valueOf("Black"));
                    } else {
                        g2d.setFill(Paint.valueOf("Red"));
                    }
                    g2d.fillOval(mapX(xAxisData[i]) - 4, mapY(yAxisData[i]) - 4, 8, 8);
                } else {
                    // out of bounds
                    g2d.moveTo(mapX(xAxisData[i]), mapY(yAxisData[i]));
                }
                //g2d.fillOval(mapX(xAxisData[i]) - 4, mapY(yAxisData[i]) - 4, 8, 8);
            }
            g2d.stroke();
            g2d.beginPath();
            g2d.setLineDashes(0);

            g2d.stroke();


        } else {
            g2d.setFont(Font.font("SansSerif", FontWeight.SEMI_BOLD, 20));

            g2d.setFill(Paint.valueOf("Black"));
            g2d.fillText("Only one file", 20, 20);
        }
    }

    @Override
    public void plotStats(GraphicsContext g2d) {

    }

    private void setupPlotContextMenu() {
        plotContextMenu = new ContextMenu();
        MenuItem plotContextMenuItem1 = new MenuItem("Restore plot");
        plotContextMenuItem1.setOnAction((mouseEvent) -> {
            refreshPanel(true, true);
        });

        plotContextMenu.getItems().addAll(plotContextMenuItem1);

    }


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

            if (mouseInHouse(event.getX(), event.getY())) {
                ((Canvas) event.getSource()).getParent().getScene().setCursor(Cursor.CROSSHAIR);
            } else {
                ((Canvas) event.getSource()).getParent().getScene().setCursor(Cursor.DEFAULT);
            }
        }
    }

    private class MouseClickedEventHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent mouseEvent) {
            plotContextMenu.hide();
            boolean isPrimary = mouseEvent.getButton().compareTo(MouseButton.PRIMARY) == 0;
            if (mouseInHouse(mouseEvent.getX(), mouseEvent.getY())) {
                if (isPrimary) {
                    indexOfSelectedSpot = indexOfSpotFromMouseX(mouseEvent.getX());
                    PeakShapeDemoPlotsControllerTest.currentGroupIndex = indexOfSelectedSpot;
                    PeakShapeDemoPlotsControllerTest.resourceBrowserTarget = PeakShapeDemoPlotsControllerTest.getResourceGroups(PeakShapeDemoPlotsControllerTest.getCurrentGroup()).get(indexOfSelectedSpot);
                    repaint();
                    getGraphicsContext2D().setLineWidth(1.0);
                    getGraphicsContext2D().strokeOval(mapX(xAxisData[indexOfSelectedSpot]) - 6, mapY(yAxisData[indexOfSelectedSpot]) - 6, 12, 12);

                } else {
                    plotContextMenu.show((Node) mouseEvent.getSource(), Side.LEFT, mouseEvent.getSceneX() - getLayoutX(), mouseEvent.getSceneY() - getLayoutY());
                }


            } else {
                System.out.println(mouseEvent.getClickCount());
            }
        }
    }

}