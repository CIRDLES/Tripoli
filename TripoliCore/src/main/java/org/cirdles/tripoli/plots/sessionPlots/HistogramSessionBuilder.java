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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.histograms.HistogramRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author James F. Bowring
 */
public class HistogramSessionBuilder extends PlotBuilder {
    //    @Serial
//    private static final long serialVersionUID = 9180059676626735662L;
    private HistogramSessionRecord histogramSessionRecord;

    public HistogramSessionBuilder() {
    }

    public HistogramSessionBuilder(List<HistogramRecord> histogramRecords, String[] title, String xAxisLabel, String yAxisLabel) {
        super(title, xAxisLabel, yAxisLabel);
        histogramSessionRecord = generateHistogramSession(histogramRecords);
    }

    public static HistogramSessionBuilder initializeHistogramSession(List<HistogramRecord> histogramRecords, String[] title, String xAxisLabel, String yAxisLabel) {
        HistogramSessionBuilder histogramSessionBuilder = new HistogramSessionBuilder(histogramRecords, title, xAxisLabel, yAxisLabel);
        histogramSessionBuilder.histogramSessionRecord = histogramSessionBuilder.generateHistogramSession(histogramRecords);
        return histogramSessionBuilder;
    }

    private HistogramSessionRecord generateHistogramSession(List<HistogramRecord> histogramRecords) {
        // calculate mean, error, etc across all

        List<Double> blockIdList = new ArrayList<>();
        List<Double> histogramMeans = new ArrayList<>();
        List<Double> histogramOneSigma = new ArrayList<>();
        DescriptiveStatistics descriptiveStatisticsRatiosByBlock = new DescriptiveStatistics();

        Map<Integer, HistogramRecord> mapBlockIdToHistogramRecord = new TreeMap<>();
        for (HistogramRecord histogramRecord : histogramRecords) {
            mapBlockIdToHistogramRecord.put(histogramRecord.blockID(), histogramRecord);
            blockIdList.add((double) histogramRecord.blockID());
            histogramMeans.add(histogramRecord.mean());
            descriptiveStatisticsRatiosByBlock.addValue(histogramRecord.mean());
            histogramOneSigma.add(histogramRecord.standardDeviation());
        }
        double[] blockIds = blockIdList.stream().mapToDouble(d -> d).toArray();
        double[] blockMeans = histogramMeans.stream().mapToDouble(d -> d).toArray();
        double[] blockOneSigmas = histogramOneSigma.stream().mapToDouble(d -> d).toArray();

        return new HistogramSessionRecord(
                mapBlockIdToHistogramRecord,
                blockIds,
                blockMeans,
                blockOneSigmas,
                descriptiveStatisticsRatiosByBlock.getMean(),
                descriptiveStatisticsRatiosByBlock.getStandardDeviation(),
                title,
                "Block Number",
                "Ratio"
        );
    }

    public HistogramSessionRecord getHistogramSessionRecord() {
        return histogramSessionRecord;
    }
}