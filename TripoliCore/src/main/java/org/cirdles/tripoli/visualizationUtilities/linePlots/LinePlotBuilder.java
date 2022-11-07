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

package org.cirdles.tripoli.visualizationUtilities.linePlots;

import javafx.scene.paint.Color;
import org.cirdles.tripoli.visualizationUtilities.AbstractPlotBuilder;

public class LinePlotBuilder extends AbstractPlotBuilder {

    private final double[] xData;
    private final double[] yData;

    protected LinePlotBuilder(double[] xData, double[] yData, String title, String xAxisLabel, String yAxisLabel, Color dataColor) {
        super(title, xAxisLabel, yAxisLabel, dataColor);
        this.xData = xData;
        this.yData = yData;
    }

    public static LinePlotBuilder initializeLinePlot(double[] xData, double[] yData, String title) {
        return new LinePlotBuilder(xData, yData, title, "X", "Y", Color.BLUE);
    }

    public double[] getyData() {
        return yData;
    }

    public double[] getxData() {
        return xData;
    }
}