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
    protected HistogramRecord histogramRecord;

    protected HistogramBuilder(int blockID, String[] title, String xAxisLabel, String yAxisLabel, boolean displayed) {
        super(title, xAxisLabel, yAxisLabel, displayed);
        histogramRecord = generateHistogram(blockID, new double[0], 0, new String[]{""}, xAxisLabel);
        this.displayed = displayed;
    }

    public static HistogramBuilder initializeHistogram(int blockID, double[] data, int binCount, String[] title, String xAxisLabel, String yAxisLabel, boolean displayed) {
        HistogramBuilder histogramBuilder = new HistogramBuilder(blockID, title, xAxisLabel, yAxisLabel, displayed);
        histogramBuilder.histogramRecord = histogramBuilder.generateHistogram(blockID, data, binCount, title, xAxisLabel);
        return histogramBuilder;
    }

    protected HistogramRecord generateHistogram(int blockID, double[] data, int binCount, String[] title, String xAxisLabel) {
        DescriptiveStatistics descriptiveStatisticsData = new DescriptiveStatistics();
        for (int index = 0; index < data.length; index++) {
            descriptiveStatisticsData.addValue(data[index]);
        }
        double dataMax = descriptiveStatisticsData.getMax();
        double dataMin = descriptiveStatisticsData.getMin();

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
                blockID,
                data,
                descriptiveStatisticsData.getMean(),
                descriptiveStatisticsData.getStandardDeviation(),
                binCount,
                binCounts,
                binWidth,
                binCenters,
                title,
                xAxisLabel,
                yAxisLabel
        );
    }

    public HistogramRecord getHistogramRecord() {
        return histogramRecord;
    }
}