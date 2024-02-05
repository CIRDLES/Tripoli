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
import org.cirdles.tripoli.expressions.species.IsotopicRatio;
import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.histograms.HistogramRecord;
import org.cirdles.tripoli.utilities.mathUtilities.weightedMeans.WeighteMeanOfLogRatio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author James F. Bowring
 */
public class AnalysisRatioPlotBuilder extends PlotBuilder {
    private AnalysisRatioRecord analysisRatioRecord;
    private int blockCount;

    public AnalysisRatioPlotBuilder() {
    }

    private AnalysisRatioPlotBuilder(int blockCount, String[] title, String xAxisLabel, String yAxisLabel) {
        super(title, xAxisLabel, yAxisLabel, true);
        this.blockCount = blockCount;
    }

    public static AnalysisRatioPlotBuilder initializeAnalysisRatioPlotBuilder(
            int blockCount, IsotopicRatio ratio, List<HistogramRecord> histogramRecords, String[] title, String xAxisLabel, String yAxisLabel) {
        AnalysisRatioPlotBuilder analysisRatioPlotBuilder = new AnalysisRatioPlotBuilder(blockCount, title, xAxisLabel, yAxisLabel);
        analysisRatioPlotBuilder.analysisRatioRecord = analysisRatioPlotBuilder.generateAnalysisRatioRecord(ratio, histogramRecords);
        return analysisRatioPlotBuilder;
    }

    private AnalysisRatioRecord generateAnalysisRatioRecord(IsotopicRatio ratio, List<HistogramRecord> histogramRecords) {
        List<Integer> blockIdList = new ArrayList<>();
        List<Double> analysisLogRatioBlockMeans = new ArrayList<>();
        List<Double> analysisLogRatioBlockOneSigmas = new ArrayList<>();
        DescriptiveStatistics descriptiveStatisticsAnalysisLogRatio = new DescriptiveStatistics();

        Map<Integer, HistogramRecord> mapBlockIdToHistogramRecord = new TreeMap<>();
        for (HistogramRecord histogramRecord : histogramRecords) {
            mapBlockIdToHistogramRecord.put(histogramRecord.blockID(), histogramRecord);
            blockIdList.add(histogramRecord.blockID());

            analysisLogRatioBlockMeans.add(histogramRecord.mean());
            descriptiveStatisticsAnalysisLogRatio.addValue(histogramRecord.mean());
            analysisLogRatioBlockOneSigmas.add(histogramRecord.standardDeviation());
        }
        double[] blockIds = blockIdList.stream().mapToDouble(d -> d).toArray();
        double[] blockLogRatioMeans = analysisLogRatioBlockMeans.stream().mapToDouble(d -> d).toArray();
        double[] blockLogRatioOneSigmas = analysisLogRatioBlockOneSigmas.stream().mapToDouble(d -> d).toArray();

        double analysisLogRatiosMean = (descriptiveStatisticsAnalysisLogRatio.getMean());
        double analysisLogRatiosOneSigma = descriptiveStatisticsAnalysisLogRatio.getStandardDeviation();

        int countOfActiveBlocks = blockLogRatioMeans.length;
        double[][] logRatioCoVariances = new double[countOfActiveBlocks][countOfActiveBlocks];
        for (int i = 0; i < countOfActiveBlocks; i++) {
            logRatioCoVariances[i][i] = blockLogRatioOneSigmas[i] * blockLogRatioOneSigmas[i];
        }

        return new AnalysisRatioRecord(
                ratio,
                blockCount,
                mapBlockIdToHistogramRecord,
                blockIds,
                blockLogRatioMeans,
                blockLogRatioOneSigmas,
                analysisLogRatiosMean,
                analysisLogRatiosOneSigma,
                WeighteMeanOfLogRatio.calculateWeightedMean(blockLogRatioMeans, logRatioCoVariances),
                title,
                "Block Number",
                "LogRatio"
        );

    }

    public AnalysisRatioRecord getAnalysisRatioRecord() {
        return analysisRatioRecord;
    }
}