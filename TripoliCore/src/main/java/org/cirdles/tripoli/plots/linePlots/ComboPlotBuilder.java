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

public class ComboPlotBuilder extends PlotBuilder {

    private final double[] xData;
    private final double[] yData;
    private final double[] yData2;
    private final boolean yData2OneSigma;

    protected ComboPlotBuilder(double[] xData, double[] yData, double[] yData2, boolean yData2OneSigma, String[] title) {
        super(title, "", "");
        this.xData = xData;
        this.yData = yData;
        this.yData2 = yData2;
        this.yData2OneSigma = yData2OneSigma;
    }

    public static ComboPlotBuilder initializeLinePlot(double[] xData, double[] yData, double[] yData2, String[] title) {
        return new ComboPlotBuilder(xData, yData, yData2, false, title);
    }

    public static ComboPlotBuilder initializeLinePlotWithOneSigma(double[] xData, double[] yData, double[] yData2, String[] title) {
        return new ComboPlotBuilder(xData, yData, yData2, true, title);
    }

    public double[] getyData() {
        return yData;
    }

    public double[] getxData() {
        return xData;
    }

    public double[] getyData2() {
        return yData2;
    }

    public boolean isyData2OneSigma() {
        return yData2OneSigma;
    }
}