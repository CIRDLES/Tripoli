/*
 * AbstractRawDataView.java
 *
 * Created Jul 6, 2011
 *
 * Copyright 2006 James F. Bowring and Earth-Time.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.HistogramSinglePlot;
import org.cirdles.tripoli.gui.utilities.TripoliColor;

import java.math.BigDecimal;
import java.util.Formatter;

/**
 * @author James F. Bowring
 */
public abstract class AbstractPlot extends Canvas {

    protected double x;
    protected double y;
    protected double width;
    protected double height;
    protected double[] yAxisData;
    protected double[] xAxisData;
    protected double plotWidth;
    protected double plotHeight;
    protected double topMargin;
    protected double leftMargin;
    protected double minX;
    protected double maxX;
    protected double minY;
    protected double maxY;

    protected ContextMenu plotContextMenu;
    protected double mouseStartX;
    protected double mouseStartY;
    protected BigDecimal[] ticsX;
    protected BigDecimal[] ticsY;
    protected double displayOffsetX;
    protected double displayOffsetY;
    protected double zoomChunkX;
    protected double zoomChunkY;
    protected String[] plotTitle;
    protected String plotAxisLabelX;
    protected String plotAxisLabelY;
    protected boolean showStats;
    protected TripoliColor dataColor;
    protected boolean showYaxis;

    private AbstractPlot() {
    }


    /**
     * @param bounds
     */
    protected AbstractPlot(Rectangle bounds, int leftMargin, int topMargin, String[] plotTitle, String plotAxisLabelX, String plotAxisLabelY) {
        super(bounds.getWidth(), bounds.getHeight());
        x = bounds.getX();
        y = bounds.getY();
        width = bounds.getWidth();
        height = bounds.getHeight();

        this.leftMargin = leftMargin;
        this.topMargin = topMargin;
        this.plotTitle = plotTitle;
        this.plotAxisLabelX = plotAxisLabelX;
        this.plotAxisLabelY = plotAxisLabelY;
        dataColor = TripoliColor.create(Color.BLUE);

        xAxisData = new double[0];
        yAxisData = new double[0];
        ticsX = new BigDecimal[0];
        ticsY = new BigDecimal[0];
        showStats = false;
        showYaxis = true;

        updatePlotSize();

        setupPlotContextMenu();

        EventHandler<ScrollEvent> scrollEventEventHandler = new EventHandler<>() {
            @Override
            public void handle(ScrollEvent event) {
                if (mouseInHouse(event.getX(), event.getY())) {
                    zoomChunkX = Math.abs(zoomChunkX) * Math.signum(event.getDeltaY());
                    zoomChunkY = Math.abs(zoomChunkY) * Math.signum(event.getDeltaY());
                    if (getDisplayRangeX() >= zoomChunkX) {
                        minX += zoomChunkX;
                        maxX -= zoomChunkX;
                        minY += zoomChunkY;
                        maxY -= zoomChunkY;

                        calculateTics();
                        repaint();
                    }
                }
            }
        };
        addEventFilter(ScrollEvent.SCROLL, scrollEventEventHandler);
        EventHandler<MouseEvent> mouseDraggedEventHandler = e -> {
            if (mouseInHouse(e.getX(), e.getY())) {
                displayOffsetX = displayOffsetX + (convertMouseXToValue(mouseStartX) - convertMouseXToValue(e.getX()));
                mouseStartX = e.getX();

                if (this instanceof HistogramSinglePlot) {
                    displayOffsetY = Math.max(0.0, displayOffsetY + (convertMouseYToValue(mouseStartY) - convertMouseYToValue(e.getY())));
                } else {
                    displayOffsetY = displayOffsetY + (convertMouseYToValue(mouseStartY) - convertMouseYToValue(e.getY()));
                }
                mouseStartY = e.getY();

                calculateTics();
                repaint();
            }
        };
        addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedEventHandler);
        EventHandler<MouseEvent> mousePressedEventHandler = e -> {
            if (mouseInHouse(e.getX(), e.getY()) && e.isPrimaryButtonDown()) {
                mouseStartX = e.getX();
                mouseStartY = e.getY();
            }
        };
        addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
        setOnMouseClicked(new MouseClickEventHandler());
    }

    public TripoliColor getDataColor() {
        return dataColor;
    }

    public void setDataColor(TripoliColor dataColor) {
        this.dataColor = dataColor;
    }

    private void setupPlotContextMenu() {
        plotContextMenu = new ContextMenu();
        MenuItem plotContextMenuItem1 = new MenuItem("Restore plot");
        plotContextMenuItem1.setOnAction((mouseEvent) -> {
            refreshPanel(true, true);
        });

        MenuItem plotContextMenuItem2 = new MenuItem("Bring to front");
        plotContextMenuItem2.setOnAction((mouseEvent) -> {
            getParent().toFront();
        });

        MenuItem plotContextMenuItem3 = new MenuItem("Set data color");
        plotContextMenuItem3.setOnAction((mouseEvent) -> {
            ((TripoliPlotPane) getParent()).changeDataColor(this);
        });

        MenuItem plotContextMenuItem4 = new MenuItem("Toggle stats");
        plotContextMenuItem4.setOnAction((mouseEvent) -> {
            ((TripoliPlotPane) getParent()).toggleShowStats();
        });

        plotContextMenu.getItems().addAll(plotContextMenuItem1, plotContextMenuItem2, plotContextMenuItem3, plotContextMenuItem4);
    }

    /**
     * @param g2d
     */
    protected void paintInit(GraphicsContext g2d) {
        relocate(x, y);
        g2d.clearRect(0, 0, width, height);
    }

    /**
     * @param g2d
     */
    public void paint(GraphicsContext g2d) {
        if (!showYaxis) {
            leftMargin = 15;
        }
        paintInit(g2d);

        drawBorder(g2d);
        drawPlotLimits(g2d);

        plotData(g2d);

        if (showStats) {
            plotStats(g2d);
        }

        drawAxes(g2d);
        labelAxisX(g2d);
        labelAxisY(g2d);
        showTitle(g2d);
    }

    public void repaint() {
        paint(getGraphicsContext2D());
    }

    public abstract void plotData(GraphicsContext g2d);

    public abstract void plotStats(GraphicsContext g2d);

    public void prepareExtents() {
    }

    public void calculateTics() {
        ticsX = TicGeneratorForAxes.generateTics(getDisplayMinX(), getDisplayMaxX(), (int) (plotWidth / 50.0));
        if (0 == ticsX.length) {
            ticsX = new BigDecimal[2];
            ticsX[0] = new BigDecimal(Double.toString(minX));
            ticsX[ticsX.length - 1] = new BigDecimal(Double.toString(maxX));
        }

        ticsY = TicGeneratorForAxes.generateTics(getDisplayMinY(), getDisplayMaxY(), (int) (plotHeight / 15.0));
        if (0 == ticsY.length) {
            ticsY = new BigDecimal[2];
            ticsY[0] = new BigDecimal(Double.toString(minY));
            ticsY[ticsY.length - 1] = new BigDecimal(Double.toString(maxY));
        }

        zoomChunkX = getDisplayRangeX() / 100.0;
        zoomChunkY = getDisplayRangeY() / 100.0;
    }

    private void drawAxes(GraphicsContext g2d) {
        g2d.setFill(Paint.valueOf("BLACK"));
        g2d.setStroke(Paint.valueOf("BLACK"));
        g2d.setLineWidth(0.75);
        Text text = new Text();
        text.setFont(Font.font("SansSerif", 11));
        int textWidth;

        if (1 < ticsY.length) {
            if (showYaxis) {
                // ticsY
                float verticalTextShift = 3.2f;
                g2d.setFont(Font.font("SansSerif", 10));
                if (null != ticsY) {
                    for (BigDecimal bigDecimalTicY : ticsY) {
                        if ((mapY(bigDecimalTicY.doubleValue()) >= topMargin) && (mapY(bigDecimalTicY.doubleValue()) <= (topMargin + plotHeight))) {
                            g2d.strokeLine(
                                    leftMargin, mapY(bigDecimalTicY.doubleValue()), leftMargin + plotWidth, mapY(bigDecimalTicY.doubleValue()));
                            // left side
                            Formatter fmt = new Formatter();
                            fmt.format("%8.3g", bigDecimalTicY.doubleValue());
                            String yText = fmt.toString().trim();
                            text.setText(yText);
                            textWidth = (int) text.getLayoutBounds().getWidth();
                            g2d.fillText(text.getText(),//
                                    leftMargin - textWidth - 2.5f,
                                    (float) mapY(bigDecimalTicY.doubleValue()) + verticalTextShift);
                        }
                    }
                }
            }
            // ticsX
            if (null != ticsX) {
                for (int i = 1; i < ticsX.length - 1; i++) {
                    try {
                        g2d.strokeLine(
                                mapX(ticsX[i].doubleValue()),
                                topMargin + plotHeight,
                                mapX(ticsX[i].doubleValue()),
                                topMargin + plotHeight + 3);
                        // bottom
                        // http://www.java2s.com/Tutorials/Java/String/How_to_use_Java_Formatter_to_format_value_in_scientific_notation.htm#:~:text=%25e%20is%20for%20scientific%20notation,scientific%20notation%2C%20use%20%25e.
                        Formatter fmt = new Formatter();
                        fmt.format("%8.5g", ticsX[i].doubleValue());
                        String xText = fmt.toString().trim();
                        g2d.fillText(xText,
                                (float) mapX(ticsX[i].doubleValue()) - 7.0f,
                                (float) topMargin + plotHeight + 10);

                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    public void showTitle(GraphicsContext g2d) {
        Paint savedPaint = g2d.getFill();
        Font titleFont = Font.font("Monospaced Bold", 12);
        g2d.setFont(titleFont);
        g2d.setFill(Paint.valueOf("RED"));
        g2d.fillText(plotTitle[0], leftMargin, topMargin - 12);
        if (2 == plotTitle.length) {
            Text textTitle1 = new Text(plotTitle[0].split("\\.")[0]);
            textTitle1.setFont(titleFont);
            Text textTitle2 = new Text(plotTitle[1].split("\\.")[0]);
            textTitle2.setFont(titleFont);
            double offset = textTitle1.getLayoutBounds().getWidth() - textTitle2.getLayoutBounds().getWidth();
            g2d.fillText(plotTitle[1], leftMargin + offset, topMargin - 2);
        }
        g2d.setFill(savedPaint);
    }

    private void labelAxisX(GraphicsContext g2d) {
        Paint savedPaint = g2d.getFill();
        g2d.setFill(Paint.valueOf("BLACK"));
        g2d.setFont(Font.font("SansSerif", 11));
        Text text = new Text();
        text.setFont(Font.font("SansSerif", 11));
        text.setText(plotAxisLabelX);
        int textWidth = (int) text.getLayoutBounds().getWidth();
        g2d.fillText(text.getText(), leftMargin + (plotWidth - textWidth) / 2.0, plotHeight + 2.0 * topMargin - 2.0);
        g2d.setFill(savedPaint);
    }

    private void labelAxisY(GraphicsContext g2d) {
        Paint savedPaint = g2d.getFill();
        g2d.setFill(Paint.valueOf("BLACK"));
        g2d.setFont(Font.font("SansSerif", 11));
        Text text = new Text();
        text.setFont(Font.font("SansSerif", 11));
        text.setText(plotAxisLabelY);
        int textWidth = (int) text.getLayoutBounds().getWidth();
        g2d.rotate(-90.0);
        g2d.fillText(text.getText(), -(2.0 * topMargin + plotHeight) / 2.0 - textWidth / 2.0, 12);
        g2d.rotate(90.0);
        g2d.setFill(savedPaint);
    }

    private void drawBorder(GraphicsContext g2d) {
        // fill it in
        g2d.setFill(Paint.valueOf("WHITE"));
        g2d.fillRect(0, 0, width + 1, height + 1);

        // draw border
        g2d.setStroke(Paint.valueOf("BLACK"));
        g2d.setLineWidth(2);
        g2d.strokeRect(1, 1, width - 1, height - 1);
    }

    private void drawPlotLimits(GraphicsContext g2d) {
        // border and fill
        g2d.setLineWidth(0.5);
        g2d.setStroke(Paint.valueOf("BLACK"));
        g2d.strokeRect(
                leftMargin,
                topMargin,
                plotWidth,
                plotHeight);
        g2d.setFill(Paint.valueOf("BLACK"));
    }

    /**
     * @param x
     * @return mapped x
     */
    public double mapX(double x) {
        return (((x - getDisplayMinX()) / getDisplayRangeX()) * plotWidth) + leftMargin;
    }

    /**
     * @param y
     * @return mapped y
     */
    public double mapY(double y) {
        return (((getDisplayMaxY() - y) / getDisplayRangeY()) * plotHeight) + topMargin;
    }

    public boolean pointInPlot(double x, double y) {
        return ((mapX(x) >= leftMargin) && (mapX(x) <= (leftMargin + plotWidth)) && (mapY(y) >= topMargin) && (mapY(y) <= (topMargin + plotHeight)));
    }

    /**
     * @param doReScale  the value of doReScale
     * @param inLiveMode the value of inLiveMode
     */
    public void refreshPanel(boolean doReScale, boolean inLiveMode) {
        try {
            preparePanel();
            repaint();
        } catch (Exception ignored) {
        }
    }

    /**
     *
     */
    public abstract void preparePanel();

    /**
     * @return the displayOffsetY
     */
    public double getDisplayOffsetY() {
        return displayOffsetY;
    }

    /**
     * @param displayOffsetY the displayOffsetY to set
     */
    public void setDisplayOffsetY(double displayOffsetY) {
        this.displayOffsetY = displayOffsetY;
    }

    /**
     * @return the displayOffsetX
     */
    public double getDisplayOffsetX() {
        return displayOffsetX;
    }

    /**
     * @param displayOffsetX the displayOffsetX to set
     */
    public void setDisplayOffsetX(double displayOffsetX) {
        this.displayOffsetX = displayOffsetX;
    }

    /**
     * @return minimum displayed x
     */
    public double getDisplayMinX() {
        return minX + displayOffsetX;
    }

    /**
     * @return maximum displayed x
     */
    public double getDisplayMaxX() {
        return maxX + displayOffsetX;
    }

    /**
     * @return minimum displayed y
     */
    public double getDisplayMinY() {
        return minY + displayOffsetY;
    }

    /**
     * @return maximum displayed y
     */
    public double getDisplayMaxY() {
        return maxY + displayOffsetY;
    }

    /**
     * @return
     */
    public double getDisplayRangeX() {
        return (getDisplayMaxX() - getDisplayMinX());
    }

    /**
     * @return
     */
    public double getDisplayRangeY() {
        return (getDisplayMaxY() - getDisplayMinY());
    }

    /**
     * @return the yAxisData
     */
    public double[] getyAxisData() {
        return yAxisData.clone();
    }

    /**
     * @return the xAxisData
     */
    public double[] getxAxisData() {
        return xAxisData.clone();
    }

    public void toggleShowStats() {
        showStats = !showStats;
    }

    /**
     * @param x
     * @return
     */
    protected double convertMouseXToValue(double x) {
        return ((x - leftMargin + 2) / plotWidth) //
                * getDisplayRangeX()//
                + getDisplayMinX();
    }

    /**
     * @param y
     * @return
     */
    protected double convertMouseYToValue(double y) {
        return -1 * (((y - topMargin - 1) * getDisplayRangeY() / plotHeight)
                - getDisplayMaxY());
    }

    protected boolean mouseInHouse(double sceneX, double sceneY) {
        return ((sceneX >= leftMargin)
                && (sceneY >= topMargin)
                && (sceneY < plotHeight + topMargin - 2)
                && (sceneX < (plotWidth + leftMargin - 2)));
    }

    public void updatePlotSize(double width, double height) {
        this.width = width;
        this.height = height;
        updatePlotSize();
    }

    public void updatePlotSize() {
        plotWidth = (int) (width - leftMargin - 10.0);
        plotHeight = (int) (height - 2 * topMargin);
    }

    public void setWidthF(double width) {
        this.width = width;
    }

    public void setHeightF(double height) {
        this.height = height;
    }

    private class MouseClickEventHandler implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent mouseEvent) {
            plotContextMenu.hide();
            boolean isPrimary = 0 == mouseEvent.getButton().compareTo(MouseButton.PRIMARY);

            if (mouseInHouse(mouseEvent.getX(), mouseEvent.getY())) {
                if (isPrimary) {
                } else {
                    plotContextMenu.show((Node) mouseEvent.getSource(), Side.LEFT, mouseEvent.getX() - getLayoutX(), mouseEvent.getY() - getLayoutY());
                }
            }

        }
    }


}