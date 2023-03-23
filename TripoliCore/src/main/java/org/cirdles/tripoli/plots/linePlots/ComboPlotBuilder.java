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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ComboPlotBuilder extends PlotBuilder {

    private final double[] xData;
    private final double[] yData;
    private final double[] yData2;
    private final boolean yData2OneSigma;
    private final Map<String, List<Double>> blockMapOfIdsToData;

    protected ComboPlotBuilder(
            double[] xData, double[] yData, double[] yData2, Map<String, List<Double>> blockMapOfIdsToData, boolean yData2OneSigma, String[] title, String xAxisLabel, String yAxisLabel) {
        super(title, xAxisLabel, yAxisLabel);
        this.xData = xData;
        this.yData = yData;
        this.yData2 = yData2;
        this.yData2OneSigma = yData2OneSigma;
        this.blockMapOfIdsToData = blockMapOfIdsToData;
    }

    public static ComboPlotBuilder initializeLinePlot(double[] xData, double[] yData, double[] yData2, String[] title, String xAxisLabel, String yAxisLabel) {
        return new ComboPlotBuilder(xData, yData, yData2, new TreeMap<>(), false, title, xAxisLabel, yAxisLabel);
    }

    public static ComboPlotBuilder initializeLinePlotWithOneSigma(double[] xData, double[] yData, double[] yData2, String[] title, String xAxisLabel, String yAxisLabel) {
        return new ComboPlotBuilder(xData, yData, yData2, new TreeMap<>(), true, title, xAxisLabel, yAxisLabel);
    }

    public static ComboPlotBuilder initializeLinePlotWithSubsets(
            double[] xData, double[] yData, double[] yData2, Map<String, List<Double>> sequenceIds, String[] title, String xAxisLabel, String yAxisLabel) {
        return new ComboPlotBuilder(xData, yData, yData2, sequenceIds, false, title, xAxisLabel, yAxisLabel);
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

    public Map<String, List<Double>> getBlockMapOfIdsToData() {
        return blockMapOfIdsToData;
    }
}