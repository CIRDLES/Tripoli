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

package org.cirdles.tripoli.visualizationUtilities.histograms;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.visualizationUtilities.AbstractPlotBuilder;

/**
 * @author James F. Bowring
 */
public class HistogramBuilder extends AbstractPlotBuilder {

    private HistogramRecord[] histograms;

    private HistogramBuilder(String title) {
        super(title);
        histograms = new HistogramRecord[0];
    }

    public static HistogramBuilder initializeHistogram(double[] data, int binCount, String title) {
        HistogramBuilder histogramBuilder = new HistogramBuilder(title);
        histogramBuilder.histograms = new HistogramRecord[1];
        histogramBuilder.histograms[0] = histogramBuilder.generateHistogram(data, binCount);
        return histogramBuilder;
    }

    public static HistogramBuilder initializeHistogram(boolean histogramPerRow, double[][] data, int binCount, String title) {
        HistogramBuilder histogramBuilder = new HistogramBuilder(title);
        if (histogramPerRow){
            histogramBuilder.histograms = new HistogramRecord[data.length];
            for (int row = 0; row < data.length; row ++){
                histogramBuilder.histograms[row] = histogramBuilder.generateHistogram(data[row], binCount);
            }
        } else {
            double[] allData = new double[data[0].length * data.length];
            histogramBuilder.histograms = new HistogramRecord[1];
            for (int row = 0; row < data.length; row++) {
                System.arraycopy(data[row], 0, allData, row * data[0].length, data[0].length);
            }
            histogramBuilder.histograms[0] = histogramBuilder.generateHistogram(allData, binCount);
        }

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
        double binWidth = (dataMax - dataMin) / (double) binCount;

        for (int index = 0; index < data.length; index++) {
            double datum = data[index];
            if (datum != 0.0) { //ignore 0s here
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
                binCount,
                binCounts,
                binWidth,
                binCenters
        );
    }

    public HistogramRecord[] getHistograms() {
        return histograms;
    }
}