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

/**
 * @author James F. Bowring
 */
public class HistogramPlot extends AbstractDataView {

    /**
     * @param bounds
     */
    public HistogramPlot(Rectangle bounds) {
        super(bounds, 100, 200);
    }

    @Override
    public void preparePanel() {

        minX = 100.0;
        maxX = 750;
        xAxisData = new double[]{100, 200, 300, 333, 400, 444, 500, 600, 700, 750};

        minY = 2.0;
        maxY = 7.0;
        yAxisData = new double[]{2, 4, 5, 6, 7, 8, 7, 6, 5, 4};

        setDisplayOffsetY(0.0);
        setDisplayOffsetX(0.0);

        this.repaint();
    }

    @Override
    public void paint(GraphicsContext g2d) {
        super.paint(g2d);

        g2d.setFont(Font.font("SansSerif", FontWeight.SEMI_BOLD, 15));
        g2d.setStroke(Paint.valueOf("BLACK"));
        g2d.setLineWidth(0.5);

        g2d.setFill(Paint.valueOf("RED"));
        g2d.fillText("HELLO", 20, 20);

        // plot data
        g2d.setLineWidth(2.0);
        for (int i = 0; i < xAxisData.length; i++) {
            g2d.setStroke(Paint.valueOf("RED"));

            g2d.strokeLine(
                    mapX(xAxisData[i]),
                    mapY(yAxisData[i] - 1),
                    mapX(xAxisData[i]),
                    mapY(yAxisData[i] + 1));
        }

        g2d.beginPath();
        g2d.moveTo(mapX(xAxisData[0]), mapY(yAxisData[0]));
        for (int i = 0; i < xAxisData.length; i++) {
            // line tracing through points
            g2d.lineTo(mapX(xAxisData[i]), mapY(yAxisData[i]));
        }
        g2d.stroke();
    }
}