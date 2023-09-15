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

package org.cirdles.tripoli.plots.linePlots;

import org.cirdles.tripoli.plots.PlotBuilder;

import java.io.Serial;

public class MultiLinePlotBuilder extends PlotBuilder {

    @Serial
    private static final long serialVersionUID = 7957148103741755713L;
    private double[][] xData;
    private double[][] yData;
    private boolean markerInLastLine;
    private int blockID;

    private MultiLinePlotBuilder(double[][] xData, double[][] yData, String[] title, String xAxisLabel, String yAxisLabel, boolean markerInLastLine, int blockID) {
        super(title, xAxisLabel, yAxisLabel, true);
        this.xData = xData;
        this.yData = yData;
        this.markerInLastLine = markerInLastLine;
        this.blockID = blockID;
    }

//    public MultiLinePlotBuilder() {
//    }

    public static MultiLinePlotBuilder initializeLinePlot(double[][] xData, double[][] yData, String[] title, String xAxisLabel, String yAxisLabel, boolean markerInLastLine, int blockID) {
        return new MultiLinePlotBuilder(xData, yData, title, xAxisLabel, yAxisLabel, markerInLastLine, blockID);
    }

    public int getBlockID() {
        return blockID;
    }

    public double[][] getyData() {
        return yData;
    }

    public double[][] getxData() {
        return xData;
    }

    public boolean isMarkerInLastLine() {
        return markerInLastLine;
    }
}