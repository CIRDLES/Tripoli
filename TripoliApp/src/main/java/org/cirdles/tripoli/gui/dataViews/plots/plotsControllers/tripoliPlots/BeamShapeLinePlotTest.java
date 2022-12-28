package org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots;

import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.cirdles.tripoli.gui.dataViews.plots.AbstractPlot;
import org.cirdles.tripoli.gui.dataViews.plots.TicGeneratorForAxes;
import org.cirdles.tripoli.plots.linePlots.BeamShapeLinePlotBuilder;

public class BeamShapeLinePlotTest extends AbstractPlot {
    private final BeamShapeLinePlotBuilder beamShapeLinePlotBuilder;
    private final Tooltip tooltip;
    private int leftBoundary;
    private int rightBoundary;

    /**
     * @param bounds
     * @param beamShapeLinePlotBuilder
     */
    public BeamShapeLinePlotTest(Rectangle bounds, BeamShapeLinePlotBuilder beamShapeLinePlotBuilder) {
        super(bounds, 50, 35, beamShapeLinePlotBuilder.getTitle(), beamShapeLinePlotBuilder.getxAxisLabel(), beamShapeLinePlotBuilder.getyAxisLabel());
        this.beamShapeLinePlotBuilder = beamShapeLinePlotBuilder;

        this.setOnMouseMoved(new MouseMovedHandler());
        this.setOnMouseClicked(new MouseClickEventHandler());
        setupPlotContextMenu();
        tooltip = new Tooltip();
        Tooltip.install(this, tooltip);
    }

    @Override
    public void preparePanel() {
        xAxisData = beamShapeLinePlotBuilder.getxData();
        minX = xAxisData[0];
        maxX = xAxisData[xAxisData.length - 1];

        leftBoundary = beamShapeLinePlotBuilder.getLeftBoundary();
        rightBoundary = beamShapeLinePlotBuilder.getRightBoundary();

        yAxisData = beamShapeLinePlotBuilder.getyData();
        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;


        for (int i = 0; i < yAxisData.length; i++) {
            minY = StrictMath.min(minY, yAxisData[i]);
            maxY = StrictMath.max(maxY, yAxisData[i]);
        }


        displayOffsetX = 0.0;
        displayOffsetY = 0.0;

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
        g2d.setLineWidth(2.2);
        // new line graph
        g2d.setStroke(Paint.valueOf("Black"));
        g2d.beginPath();
        g2d.moveTo(mapX(xAxisData[0]), mapY(yAxisData[0]));
        for (int i = 0; i < xAxisData.length; i++) {
            // line tracing through points
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
        g2d.setLineDashes(8);
        g2d.setStroke(Paint.valueOf("Blue"));
        for (int i = leftBoundary; i <= rightBoundary; i++) {
            // line tracing through points
            if (pointInPlot(xAxisData[i], yAxisData[leftBoundary])) {
                // line tracing through points
                g2d.lineTo(mapX(xAxisData[i]), mapY(yAxisData[leftBoundary]));
            } else {
                // out of bounds
                g2d.moveTo(mapX(xAxisData[i]), mapY(yAxisData[leftBoundary]));
            }

        }
        g2d.stroke();

        g2d.setFill(Paint.valueOf("Red"));
        if (pointInPlot(xAxisData[leftBoundary], yAxisData[leftBoundary])) {
            // line tracing through points

            g2d.fillOval(mapX(xAxisData[leftBoundary]) - 3.5, mapY(yAxisData[leftBoundary]) - 3.5, 7, 7);
            g2d.fillOval(mapX(xAxisData[rightBoundary]) - 3.5, mapY(yAxisData[rightBoundary]) - 3.5, 7, 7);
        } else if (pointInPlot(xAxisData[rightBoundary], yAxisData[rightBoundary])) {
            // line tracing through points
            g2d.fillOval(mapX(xAxisData[rightBoundary]) - 3.5, mapY(yAxisData[rightBoundary]) - 3.5, 7, 7);
        } else {
            // out of bounds
            g2d.moveTo(mapX(xAxisData[leftBoundary]) - 3.5, mapY(yAxisData[leftBoundary]));
            g2d.moveTo(mapX(xAxisData[rightBoundary]) - 3.5, mapY(yAxisData[rightBoundary]));
        }


        g2d.beginPath();
        g2d.setLineDashes(0);

        g2d.stroke();
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

    private class MouseClickEventHandler implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent mouseEvent) {
            plotContextMenu.hide();
            boolean isPrimary = mouseEvent.getButton().compareTo(MouseButton.PRIMARY) == 0;

            if (mouseInHouse(mouseEvent.getX(), mouseEvent.getY())) {
                if (isPrimary) {
                } else {
                    plotContextMenu.show((Node) mouseEvent.getSource(), Side.LEFT, mouseEvent.getSceneX() - getLayoutX(), mouseEvent.getSceneY() - getLayoutY());
                }
            }

        }
    }


    private class MouseMovedHandler implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent event) {

            Node potNode;

            if (mouseInHouse(event.getX(), event.getY())) {
                ((Canvas) event.getSource()).getParent().getScene().setCursor(Cursor.CROSSHAIR);
                potNode = ((Canvas) event.getSource()).getParent();

                // currently only works with x value
                for (int i = 0; i < getxAxisData().length; i++) {
                    if ((getxAxisData()[i] >= convertMouseXToValue(event.getX()) - 0.00005 && getxAxisData()[i] <= convertMouseXToValue(event.getX()) + 0.00005)) {
                        String x = String.format("%.3f", getxAxisData()[i]);
                        String y = String.format("%.2f", getyAxisData()[i]);
                        tooltip.setText("(x=" + x + ", y=" + y + ")");
                        tooltip.setAnchorX(event.getSceneX());
                        tooltip.show(potNode, event.getScreenX() + 15, event.getScreenY() + 15);
                    }
                }


            } else {
                ((Canvas) event.getSource()).getParent().getScene().setCursor(Cursor.DEFAULT);
                tooltip.hide();
            }
        }
    }
}


