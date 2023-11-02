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
import org.cirdles.tripoli.plots.linePlots.GBeamLinePlotBuilder;

public class GBeamLinePlot extends AbstractPlot {
    private final GBeamLinePlotBuilder gBeamLinePlotBuilder;
    private final Tooltip tooltip;
    private double[] yIntensity;

    /**
     * @param bounds
     * @param gBeamLinePlotBuilder
     */
    private GBeamLinePlot(Rectangle bounds, GBeamLinePlotBuilder gBeamLinePlotBuilder) {
        super(bounds, 50, 35, gBeamLinePlotBuilder.getTitle(), gBeamLinePlotBuilder.getxAxisLabel(), gBeamLinePlotBuilder.getyAxisLabel());
        this.gBeamLinePlotBuilder = gBeamLinePlotBuilder;


        setupPlotContextMenu();
        tooltip = new Tooltip();
        Tooltip.install(this, tooltip);
        this.setOnMouseMoved(new MouseMovedHandler());
        this.setOnMouseClicked(new MouseClickEventHandler());
    }

    public static AbstractPlot generatePlot(Rectangle bounds, GBeamLinePlotBuilder gBeamLinePlotBuilder) {
        return new GBeamLinePlot(bounds, gBeamLinePlotBuilder);
    }

    @Override
    public void preparePanel(boolean reScaleX, boolean reScaleY) {
        xAxisData = gBeamLinePlotBuilder.getxData();
        minX = xAxisData[0];
        maxX = xAxisData[xAxisData.length - 1];

        yAxisData = gBeamLinePlotBuilder.getyData();
        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        yIntensity = gBeamLinePlotBuilder.getIntensityData();

        for (int i = 0; i < yAxisData.length; i++) {
            minY = StrictMath.min(minY, yIntensity[i]);
            maxY = StrictMath.max(maxY, yIntensity[i]);
        }


        displayOffsetX = 0.0;
        displayOffsetY = 0.0;

        prepareExtents(true, true);
        calculateTics();

        repaint();
    }

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);
    }


    public void prepareExtents(boolean reScaleX, boolean reScaleY) {
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

    /**
     * @param g2d
     */
    @Override
    public void showLegend(GraphicsContext g2d) {

    }

    @Override
    public void plotData(GraphicsContext g2d) {
        g2d.setLineWidth(2.2);


        g2d.setStroke(Paint.valueOf("Blue"));
        g2d.setLineDashes(0);
        g2d.beginPath();
        // x = magnetMass y = blockIntensities

        for (int i = 0; i < xAxisData.length; i++) {
            if (pointInPlot(xAxisData[i], yIntensity[i])) {
                // line tracing through points
                g2d.lineTo(mapX(xAxisData[i]), mapY(yIntensity[i]));
            } else {
                // out of bounds
                g2d.moveTo(mapX(xAxisData[i]), mapY(yIntensity[i]));
            }
        }
        g2d.stroke();
        g2d.beginPath();
        g2d.setLineWidth(2.2);
        g2d.setLineDashes(4);
        g2d.setStroke(Paint.valueOf("Red"));


        // x = magnetMass y = G-Beam
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
        g2d.setLineDashes(0);


    }

    @Override
    public void plotStats(GraphicsContext g2d) {

    }

    @Override
    public void setupPlotContextMenu() {
        plotContextMenu = new ContextMenu();
        MenuItem plotContextMenuItem1 = new MenuItem("Restore plot");
        plotContextMenuItem1.setOnAction((mouseEvent) -> {
            refreshPanel(true, true);
        });

        plotContextMenu.getItems().addAll(plotContextMenuItem1);

    }

    private void showToolTip(Node node, MouseEvent event, double xPos, double yPos) {
        String x = String.format("%.3f", xPos);
        String y = String.format("%.2f", yPos);
        tooltip.setText("(x=" + x + ", y=" + y + ")");
        tooltip.setAnchorX(event.getSceneX());
        tooltip.show(node, event.getScreenX() + 15, event.getScreenY() + 15);
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

                potNode = ((Canvas) event.getSource()).getParent();
                int maxIndex = 0;
                double maxVal = 0;

                // Displays toolTip of x and y positions on the G-Beam line plot
                for (int i = 0; i < getxAxisData().length - 1; i++) {
                    double diffX = Math.abs(getxAxisData()[i] - getxAxisData()[i + 1]);

                    if (getyAxisData()[i] > maxVal) {
                        maxVal = getyAxisData()[i];
                        maxIndex = i;
                    }

                    if ((getxAxisData()[i] >= convertMouseXToValue(event.getX()) - diffX && getxAxisData()[i] <= convertMouseXToValue(event.getX()) + diffX)) {
                        double diffY = Math.abs(getyAxisData()[i] - getyAxisData()[i + 1]);
                        if ((getyAxisData()[i] >= convertMouseYToValue(event.getY()) - diffY && getyAxisData()[i] <= convertMouseYToValue(event.getY()) + diffY)) {
                            showToolTip(potNode, event, getxAxisData()[i], getyAxisData()[i]);
                            ((Canvas) event.getSource()).setCursor(Cursor.CROSSHAIR);
                        } else if (convertMouseYToValue(event.getY()) - maxY / 100 <= getyAxisData()[maxIndex] && convertMouseYToValue(event.getY()) + maxY / 100 >= getyAxisData()[maxIndex]) {
                            showToolTip(potNode, event, getxAxisData()[i], getyAxisData()[i]);
                            ((Canvas) event.getSource()).setCursor(Cursor.CROSSHAIR);
                        } else if (convertMouseYToValue(event.getY()) - maxY / 100 <= getyAxisData()[0] && convertMouseYToValue(event.getY()) + maxY / 100 >= getyAxisData()[0]) {
                            showToolTip(potNode, event, getxAxisData()[i], getyAxisData()[i]);
                            ((Canvas) event.getSource()).setCursor(Cursor.CROSSHAIR);
                        } else {
                            ((Canvas) event.getSource()).setCursor(Cursor.DEFAULT);
                        }
                    }
                }


            } else {
                ((Canvas) event.getSource()).getParent().getScene().setCursor(Cursor.DEFAULT);
                tooltip.hide();
            }
        }
    }
}