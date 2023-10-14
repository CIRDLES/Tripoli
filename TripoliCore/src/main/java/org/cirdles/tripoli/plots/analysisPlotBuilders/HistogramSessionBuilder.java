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

package org.cirdles.tripoli.plots.analysisPlotBuilders;

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
    private HistogramAnalysisRecord histogramAnalysisRecord;
    private int blockCount;

    public HistogramSessionBuilder() {
    }

    public HistogramSessionBuilder(int blockCount, List<HistogramRecord> histogramRecords, String[] title, String xAxisLabel, String yAxisLabel) {
        super(title, xAxisLabel, yAxisLabel, true);
        this.blockCount = blockCount;
        histogramAnalysisRecord = generateHistogramSession(histogramRecords);
    }

    public static HistogramSessionBuilder initializeHistogramSession(
            int blockCount, List<HistogramRecord> histogramRecords, String[] title, String xAxisLabel, String yAxisLabel) {
        HistogramSessionBuilder histogramSessionBuilder = new HistogramSessionBuilder(blockCount, histogramRecords, title, xAxisLabel, yAxisLabel);
        histogramSessionBuilder.histogramAnalysisRecord = histogramSessionBuilder.generateHistogramSession(histogramRecords);
        return histogramSessionBuilder;
    }

    private HistogramAnalysisRecord generateHistogramSession(List<HistogramRecord> histogramRecords) {
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

        return new HistogramAnalysisRecord(
                blockCount,
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

    public HistogramAnalysisRecord getHistogramSessionRecord() {
        return histogramAnalysisRecord;
    }
}