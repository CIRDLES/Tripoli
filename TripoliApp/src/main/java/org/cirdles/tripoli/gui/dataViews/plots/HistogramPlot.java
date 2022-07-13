/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.tripoli.gui.dataViews.plots;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.cirdles.tripoli.visualizationUtilities.Histogram;

/**
 * @author James F. Bowring
 */
public class HistogramPlot extends AbstractDataView {

    private Histogram histogram;

    /**
     * @param bounds
     */
    public HistogramPlot(Rectangle bounds, Histogram histogram) {
        super(bounds, 100, 200);
        this.histogram = histogram;
    }

    @Override
    public void preparePanel() {

        xAxisData = histogram.getBinCenters();
        minX = xAxisData[0];
        maxX = xAxisData[xAxisData.length - 1];

        yAxisData = histogram.getBinCounts();
        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;

        for (int i = 0; i < yAxisData.length; i++) {
            minY = StrictMath.min(minY, yAxisData[i]);
            maxY = StrictMath.max(maxY, yAxisData[i]);
        }

        minY = -5.0;
        maxY += 5.0;

        setDisplayOffsetY(0.0);
        setDisplayOffsetX(0.0);

        this.repaint();
    }

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);

        g2d.setFont(Font.font("SansSerif", FontWeight.SEMI_BOLD, 15));
        g2d.setFill(Paint.valueOf("RED"));
        g2d.fillText("Histogram", 20, 20);

        // plot bins
        g2d.setLineWidth(2.0);
        for (int i = 0; i < xAxisData.length; i++) {
            System.err.println(mapX(xAxisData[i] - histogram.getBinWidth() / 2.0) + "    " + mapY(yAxisData[i]) + "   " + mapX(xAxisData[i] + histogram.getBinWidth()) + "   " + mapY(yAxisData[i]));
            g2d.fillRect(
                    mapX(xAxisData[i] - histogram.getBinWidth() / 2.0),
                    mapY(yAxisData[i]),
                    mapX(xAxisData[1]) - mapX(xAxisData[0]),
                    mapY(0.0) - mapY(yAxisData[i]));
        }

        g2d.setStroke(Paint.valueOf("BLACK"));
        for (int i = 0; i < xAxisData.length; i++) {
            g2d.strokeLine(mapX(xAxisData[i]), mapY(0.0), mapX(xAxisData[i]), mapY(yAxisData[i]));
        }

        // plot line for giggles
        g2d.setStroke(Paint.valueOf("BLACK"));
        g2d.beginPath();
        g2d.moveTo(mapX(xAxisData[0]), mapY(yAxisData[0]));
        for (int i = 0; i < xAxisData.length; i++) {
            // line tracing through points
            g2d.lineTo(mapX(xAxisData[i]), mapY(yAxisData[i]));
        }
        g2d.stroke();
    }
}