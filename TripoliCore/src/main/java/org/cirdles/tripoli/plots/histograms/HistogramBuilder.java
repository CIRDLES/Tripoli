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

package org.cirdles.tripoli.plots.histograms;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.plots.PlotBuilder;

import java.io.Serial;

/**
 * @author James F. Bowring
 */
public class HistogramBuilder extends PlotBuilder {

    @Serial
    private static final long serialVersionUID = 9180059676626735662L;
    private HistogramRecord histogram;

    public HistogramBuilder() {
    }

    public HistogramBuilder(String[] title, String xAxisLabel, String yAxisLabel) {
        super(title, xAxisLabel, yAxisLabel);
        histogram = generateHistogram(new double[0], 0);
    }

    public static HistogramBuilder initializeHistogram(double[] data, int binCount, String[] title, String xAxisLabel, String yAxisLabel) {
        HistogramBuilder histogramBuilder = new HistogramBuilder(title, xAxisLabel, yAxisLabel);
        histogramBuilder.histogram = histogramBuilder.generateHistogram(data, binCount);
        return histogramBuilder;
    }

    private HistogramRecord generateHistogram(double[] data, int binCount) {
        DescriptiveStatistics descriptiveStatisticsRatios = new DescriptiveStatistics();
        for (int index = 0; index < data.length; index++) {
            descriptiveStatisticsRatios.addValue(data[index]);
        }
        double dataMax = descriptiveStatisticsRatios.getMax();
        double dataMin = descriptiveStatisticsRatios.getMin();

        double[] binCounts = new double[binCount];
        double binWidth = (dataMax - dataMin) / binCount;

        for (int index = 0; index < data.length; index++) {
            double datum = data[index];
            if (0.0 != datum) { //ignore 0s here
                int binNum = Math.min((int) Math.floor(Math.abs((datum - dataMin * 1.000000001) / binWidth)), binCount - 1);
                try {
                    binCounts[binNum]++;
                } catch (Exception eHist) {
                    System.err.println(eHist.getMessage());
                }
            }
        }

        double[] binCenters = new double[binCount];
        for (int binIndex = 0; binIndex < binCount; binIndex++) {
            binCenters[binIndex] = dataMin + (binIndex + 0.5) * binWidth;
        }

        return new HistogramRecord(
                data,
                descriptiveStatisticsRatios.getMean(),
                descriptiveStatisticsRatios.getStandardDeviation(),
                binCount,
                binCounts,
                binWidth,
                binCenters,
                title,
                xAxisLabel,
                yAxisLabel
        );
    }

    public HistogramRecord getHistogram() {
        return histogram;
    }
}