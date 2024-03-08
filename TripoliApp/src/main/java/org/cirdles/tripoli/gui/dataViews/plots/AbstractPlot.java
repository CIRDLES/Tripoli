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
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.ogTripoliPlots.analysisPlots.AnalysisBlockCyclesPlotI;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.HistogramSinglePlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.LinePlot;
import org.cirdles.tripoli.gui.dataViews.plots.plotsControllers.tripoliPlots.MultiLineIntensityPlot;
import org.cirdles.tripoli.gui.utilities.TripoliColor;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.linePlots.LinePlotBuilder;
import org.cirdles.tripoli.plots.linePlots.MultiLinePlotBuilder;

import java.math.BigDecimal;
import java.text.DecimalFormat;

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
    protected MenuItem plotContextMenuItemSculpt;
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
    protected boolean showXaxis;

    protected EventHandler<MouseEvent> mouseDraggedEventHandler;
    protected EventHandler<ScrollEvent> scrollEventEventHandler;

    protected PlotBuilder plotBuilder;
    protected double yAxisTickSpread = 15.0;

    AbstractPlot() {
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
        showXaxis = true;

        updatePlotSize();

        setupPlotContextMenu();

        scrollEventEventHandler = new EventHandler<>() {
            @Override
            public void handle(ScrollEvent event) {
                if (mouseInHouse(event.getX(), event.getY())) {
                    // converting scroll as Y event since scroller button works on Y
                    zoomChunkX = Math.abs(zoomChunkX) * Math.signum(event.getDeltaY());
                    zoomChunkY = Math.abs(zoomChunkY) * Math.signum(event.getDeltaY());
                    if (getDisplayRangeX() >= zoomChunkX) {
                        if (event.getSource() instanceof AnalysisBlockCyclesPlotI) {
                            AnalysisBlockCyclesPlotI sourceAnalysisBlockCyclesPlot = (AnalysisBlockCyclesPlotI) event.getSource();
                            if (event.isControlDown()) {
                                ((PlotWallPane) sourceAnalysisBlockCyclesPlot.getParentWallPane()).synchronizeRatioPlotsScroll(sourceAnalysisBlockCyclesPlot, zoomChunkX, zoomChunkY);
                            } else {
                                sourceAnalysisBlockCyclesPlot.adjustZoomSelf();
                            }
                        } else {
                            adjustZoom();
                        }
                    }
                }
            }
        };
        addEventFilter(ScrollEvent.SCROLL, scrollEventEventHandler);

        // Feb 2024 moving pan action to right mouse
        mouseDraggedEventHandler = event -> {
            if (mouseInHouse(event.getX(), event.getY()) && event.isSecondaryButtonDown()) {
                if (event.getSource() instanceof AnalysisBlockCyclesPlotI) {
                    AnalysisBlockCyclesPlotI sourceAnalysisBlockCyclesPlot = (AnalysisBlockCyclesPlotI) event.getSource();
                    if (event.isControlDown()) {
                        ((PlotWallPane) sourceAnalysisBlockCyclesPlot.getParentWallPane()).synchronizeRatioPlotsDrag(event.getX(), event.getY());
                    } else {
                        sourceAnalysisBlockCyclesPlot.adjustOffsetsForDrag(event.getX(), event.getY());
                    }
                } else if (event.getSource() instanceof LinePlot) {
                    LinePlot sourceLinePlot = (LinePlot) event.getSource();
                    if (mouseInShadeHandle(plotBuilder.getShadeWidthForModelConvergence(), event.getX(), event.getY())) {
                        plotBuilder.setShadeWidthForModelConvergence(convertMouseXToValue(event.getX()));
                        sourceLinePlot.getParentWallPane().synchronizeConvergencePlotsShade(((LinePlotBuilder) plotBuilder).getBlockID(), convertMouseXToValue(event.getX()));
                    }
                } else if (event.getSource() instanceof MultiLineIntensityPlot) {
                    MultiLineIntensityPlot sourceLinePlot = (MultiLineIntensityPlot) event.getSource();
                    if (mouseInShadeHandle(plotBuilder.getShadeWidthForModelConvergence(), event.getX(), event.getY())) {
                        plotBuilder.setShadeWidthForModelConvergence(convertMouseXToValue(event.getX()));
                        sourceLinePlot.getParentWallPane().synchronizeConvergencePlotsShade(((MultiLinePlotBuilder) plotBuilder).getBlockID(), convertMouseXToValue(event.getX()));
                    }
                } else {
                    displayOffsetX = displayOffsetX + (convertMouseXToValue(mouseStartX) - convertMouseXToValue(event.getX()));

                    if (this instanceof HistogramSinglePlot) {
                        displayOffsetY = Math.max(0.0, displayOffsetY + (convertMouseYToValue(mouseStartY) - convertMouseYToValue(event.getY())));
                    } else {
                        displayOffsetY = displayOffsetY + (convertMouseYToValue(mouseStartY) - convertMouseYToValue(event.getY()));
                    }

                    adjustMouseStartsForPress(event.getX(), event.getY());
                    calculateTics();
                    repaint();
                }
            }
        };
        addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedEventHandler);

        EventHandler<MouseEvent> mousePressedEventHandler = e -> {
            if (mouseInHouse(e.getX(), e.getY())) {// && e.isPrimaryButtonDown()) {
                if (e.getSource() instanceof AnalysisBlockCyclesPlotI) {
                    AnalysisBlockCyclesPlotI sourceAnalysisBlockCyclesPlot = (AnalysisBlockCyclesPlotI) e.getSource();
                    ((PlotWallPane) sourceAnalysisBlockCyclesPlot.getParentWallPane()).synchronizeMouseStartsOnPress(e.getX(), e.getY());
                } else {
                    adjustMouseStartsForPress(e.getX(), e.getY());
                }
            }
        };
        addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedEventHandler);
//        setOnMouseClicked(new MouseClickEventHandler());

    }

    public PlotBuilder getPlotBuilder() {
        return plotBuilder;
    }

    public TripoliColor getDataColor() {
        return dataColor;
    }

    public void setDataColor(TripoliColor dataColor) {
        this.dataColor = dataColor;
    }

    public void setupPlotContextMenu() {
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

//        plotData(g2d);

        if (showStats) {
            plotStats(g2d);
        }
        plotData(g2d);

        if (this instanceof LinePlot) {
            ((LinePlot) this).plotLeftShade(g2d);
        }
        if (this instanceof MultiLineIntensityPlot) {
            ((MultiLineIntensityPlot) this).plotLeftShade(g2d);
        }

        drawAxes(g2d);
        labelAxisX(g2d);
        labelAxisY(g2d);
        if (!(this instanceof AnalysisBlockCyclesPlotI)) {
            showTitle(g2d);
        }
        showLegend(g2d);
    }

    public void repaint() {
        paint(getGraphicsContext2D());
    }

    public abstract void plotData(GraphicsContext g2d);

    public abstract void plotStats(GraphicsContext g2d);

    public void prepareExtents(boolean reScaleX, boolean reScaleY) {
    }

    public void calculateTics() {
        ticsX = TicGeneratorForAxes.generateTics(getDisplayMinX(), getDisplayMaxX(), Math.max(4, (int) (plotWidth / 50.0)));
        if (0 == ticsX.length) {
            ticsX = new BigDecimal[2];
            ticsX[0] = new BigDecimal(Double.toString(minX));
            ticsX[ticsX.length - 1] = new BigDecimal(Double.toString(maxX));
        }

        ticsY = TicGeneratorForAxes.generateTics(getDisplayMinY(), getDisplayMaxY(), Math.max(4, (int) (plotHeight / yAxisTickSpread)));
        if ((0 == ticsY.length) && !Double.isInfinite(minY)) {
            ticsY = new BigDecimal[2];
            ticsY[0] = new BigDecimal(Double.toString(minY));
            ticsY[ticsY.length - 1] = new BigDecimal(Double.toString(maxY));
        }

        zoomChunkX = getDisplayRangeX() / 100.0;
        zoomChunkY = getDisplayRangeY() / 100.0;
    }

    private void drawAxes(GraphicsContext g2d) {
        g2d.setLineDashes(0);
        g2d.setFill(Paint.valueOf("BLACK"));
        g2d.setStroke(Paint.valueOf("BLACK"));
        g2d.setLineWidth(0.75);
        Text text = new Text();
        text.setFont(Font.font("SansSerif", 11));
        int textWidth;

        if ((1 < ticsY.length) && showYaxis) {
            // ticsY
            float verticalTextShift = 3.2f;
            g2d.setFont(Font.font("SansSerif", 10));
            if (null != ticsY) {
                for (BigDecimal bigDecimalTicY : ticsY) {
                    if ((mapY(bigDecimalTicY.doubleValue()) >= topMargin) && (mapY(bigDecimalTicY.doubleValue()) <= (topMargin + plotHeight))) {
                        g2d.strokeLine(
                                leftMargin, mapY(bigDecimalTicY.doubleValue()), leftMargin + plotWidth, mapY(bigDecimalTicY.doubleValue()));
                        // left side
                        double ticValue = bigDecimalTicY.doubleValue();
                        DecimalFormat df = new DecimalFormat((99999 < Math.abs(ticValue) || 1.0e-5 > Math.abs(ticValue)) ? "0.0####E0" : "#####0.#####");
                        String yText = (ticValue == 0.0) ? "0" : df.format(ticValue);

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
        if ((null != ticsX) && showXaxis) {
            for (int i = 1; i < ticsX.length - 1; i++) {
                try {
                    g2d.strokeLine(
                            mapX(ticsX[i].doubleValue()),
                            topMargin + plotHeight,
                            mapX(ticsX[i].doubleValue()),
                            topMargin + plotHeight + 3);
                    // bottom
                    double ticValue = ticsX[i].doubleValue();
                    DecimalFormat df = new DecimalFormat((99999 < Math.abs(ticValue) || 1.0e-5 > Math.abs(ticValue)) ? "0.0####E0" : "#####0.#####");
                    String xText = (ticValue == 0.0) ? "0" : df.format(ticValue);

                    g2d.fillText(xText,
                            (float) mapX(ticsX[i].doubleValue()) - 7.0f,
                            (float) topMargin + plotHeight + 10);

                } catch (Exception ignored) {
                }
            }
        }
    }

    public void showTitle(GraphicsContext g2d) {
        Paint savedPaint = g2d.getFill();
        Font titleFont = Font.font("Courier Bold", 12);
        g2d.setFont(titleFont);
        g2d.setFill(Paint.valueOf("RED"));
        double titleLeftX = 15;
        g2d.fillText(plotTitle[0], titleLeftX, 12);
        if (2 == plotTitle.length) {
            Text textTitle1 = new Text(plotTitle[0].split("\\.")[0]);
            textTitle1.setFont(titleFont);
            Text textTitle2 = new Text(plotTitle[1].split("\\.")[0]);
            textTitle2.setFont(titleFont);
            double offset = textTitle1.getLayoutBounds().getWidth() - textTitle2.getLayoutBounds().getWidth();
            g2d.fillText(plotTitle[1], titleLeftX + offset, 22);
        }
        g2d.setFill(savedPaint);
    }

    public abstract void showLegend(GraphicsContext g2d);

    private void labelAxisX(GraphicsContext g2d) {
        Paint savedPaint = g2d.getFill();
        g2d.setFill(Paint.valueOf("BLACK"));
        g2d.setFont(Font.font("SansSerif", 14));
        Text text = new Text();
        text.setFont(Font.font("SansSerif", 14));
        text.setText(plotAxisLabelX);
        int textWidth = (int) text.getLayoutBounds().getWidth();
        g2d.fillText(text.getText(), leftMargin + (plotWidth - textWidth) / 2.0, plotHeight + 2.0 * topMargin - 2.0);
        g2d.setFill(savedPaint);
    }

    public void labelAxisY(GraphicsContext g2d) {
        Paint savedPaint = g2d.getFill();
        g2d.setFill(Paint.valueOf("BLACK"));
        g2d.setFont(Font.font("SansSerif", 14));
        Text text = new Text();
        text.setFont(Font.font("SansSerif", 14));
        text.setText(plotAxisLabelY);
        int textWidth = (int) text.getLayoutBounds().getWidth();
        g2d.rotate(-90.0);
        g2d.fillText(text.getText(), -(2.0 * topMargin + plotHeight) / 2.0 - textWidth / 2.0, leftMargin - 40);
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

    public boolean xInPlot(double x) {
        return ((mapX(x) >= leftMargin) && (mapX(x) <= (leftMargin + plotWidth)));
    }

    /**
     * @param doReScale  the value of doReScale
     * @param inLiveMode the value of inLiveMode
     */
    public void refreshPanel(boolean reScaleX, boolean reScaleY) {
        try {
            preparePanel(reScaleX, reScaleY);
            repaint();
        } catch (Exception ignored) {
        }
    }

    /**
     *
     */
    public abstract void preparePanel(boolean reScaleX, boolean reScaleY);

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
        return ((x - leftMargin + 2) / plotWidth)
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

    public void setZoomChunkX(double zoomChunkX) {
        this.zoomChunkX = this.zoomChunkX * -Math.signum(zoomChunkX);
    }

    public void setZoomChunkY(double zoomChunkY) {
        this.zoomChunkY = this.zoomChunkY * -Math.signum(zoomChunkY);
    }

    public void adjustZoom() {
        minX = Math.max(xAxisData[0], minX + zoomChunkX);
        maxX = Math.min(xAxisData[xAxisData.length - 1], maxX - zoomChunkX);
        reCalcDisplayOffsetX();
        minY += zoomChunkY;
        maxY -= zoomChunkY;

        calculateTics();
        repaint();
    }

    public void adjustZoomSelf() {
        minX = Math.max(xAxisData[0], minX - zoomChunkX);
        maxX = Math.min(xAxisData[xAxisData.length - 1], maxX + zoomChunkX);
        reCalcDisplayOffsetX();
        minY += -zoomChunkY;
        maxY -= -zoomChunkY;

        calculateTics();
        repaint();
    }

    public void adjustOffsetsForDrag(double x, double y) {
        displayOffsetX = displayOffsetX + (convertMouseXToValue(mouseStartX) - convertMouseXToValue(x));
        reCalcDisplayOffsetX();
        mouseStartX = x;
        displayOffsetY = displayOffsetY + (convertMouseYToValue(mouseStartY) - convertMouseYToValue(y));
        mouseStartY = y;

        calculateTics();
        repaint();
    }

    private void reCalcDisplayOffsetX() {
        if (getDisplayMaxX() > xAxisData[xAxisData.length - 1]) {
            displayOffsetX -= (getDisplayMaxX() - xAxisData[xAxisData.length - 1]);
        }
        if (getDisplayMinX() < xAxisData[0]) {
            displayOffsetX -= (getDisplayMinX() - xAxisData[0]);
        }
    }

    public void adjustMouseStartsForPress(double x, double y) {
        mouseStartX = x;
        mouseStartY = y;
    }

    public boolean mouseInShadeHandle(double shadeWidthForModelConvergence, double x, double y) {
        boolean inWidth = (x >= mapX(shadeWidthForModelConvergence) - 20) && (x <= mapX(shadeWidthForModelConvergence) + 20);
        boolean inHeight = (y >= (mapY(minY) - mapY(maxY)) / 2 + mapY(maxY) - 20) && (y <= (mapY(minY) - mapY(maxY)) / 2 + mapY(maxY) + 20);

        return inWidth && inHeight;
    }

//    class MouseClickEventHandler implements EventHandler<MouseEvent> {
//        @Override
//        public void handle(MouseEvent mouseEvent) {
//            plotContextMenu.hide();
//            boolean isPrimary = (0 == mouseEvent.getButton().compareTo(MouseButton.PRIMARY));
//            boolean isBlockRatioCyclesSessionPlot = (mouseEvent.getSource() instanceof AnalysisBlockCyclesPlot);
//
//            if (mouseInHouse(mouseEvent.getX(), mouseEvent.getY())) {
////                if (!isPrimary && isBlockRatioCyclesSessionPlot) {
////                    // TODO:  remove duplicate code
////                    AnalysisBlockCyclesPlot analysisBlockCyclesPlot = (AnalysisBlockCyclesPlot) mouseEvent.getSource();
////                    // determine blockID
////                    double xValue = convertMouseXToValue(mouseEvent.getX());
////                    int blockID = (int) ((xValue - 0.7) / analysisBlockCyclesPlot.getBlockRatioCyclesSessionRecord().cyclesPerBlock()) + 1;
////                    AnalysisBlockCyclesPlot sourceAnalysisBlockCyclesPlot = (AnalysisBlockCyclesPlot) mouseEvent.getSource();
////                    ((PlotWallPane) sourceAnalysisBlockCyclesPlot.getParentWallPane()).synchronizeBlockToggle(blockID);
////                } else
//                if (!isPrimary) {
//                    plotContextMenu.show((Node) mouseEvent.getSource(), Side.LEFT, mouseEvent.getX() - getLayoutX(), mouseEvent.getY() - getLayoutY());
//                }
//            }
//        }
//    }
}