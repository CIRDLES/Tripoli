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

package org.cirdles.tripoli.plots.sessionPlots;

import org.cirdles.tripoli.plots.PlotBuilder;

public class SpeciesIntensitySessionBuilder extends PlotBuilder {

    //    @Serial
//    private static final long serialVersionUID = 5549376854790308330L;
    private final double[] xData;
    private final double[][] yData;
    private final double[][] ampResistance;
    private final double[][] baseLine;
    private final double[][] dfGain;

    protected SpeciesIntensitySessionBuilder(double[] xData, double[][] yData, double[][] ampResistance, double[][] baseLine, double[][] dfGain, String[] title, String xAxisLabel, String yAxisLabel) {
        super(title, xAxisLabel, yAxisLabel, true);
        this.xData = xData;
        this.yData = yData;
        this.ampResistance = ampResistance;
        this.baseLine = baseLine;
        this.dfGain = dfGain;
    }

    public static SpeciesIntensitySessionBuilder initializeSpeciesIntensitySessionPlot(double[] xData, double[][] yData, double[][] ampResistance, double[][] baseLine, double[][] dfGain, String[] title, String xAxisLabel, String yAxisLabel) {
        return new SpeciesIntensitySessionBuilder(xData, yData, ampResistance, baseLine, dfGain, title, xAxisLabel, yAxisLabel);
    }

    public double[][] getyData() {
        return yData;
    }

    public double[] getxData() {
        return xData;
    }

    public double[][] getAmpResistance() {
        return ampResistance;
    }

    public double[][] getBaseLine() {
        return baseLine;
    }

    public double[][] getDfGain() {
        return dfGain;
    }
}