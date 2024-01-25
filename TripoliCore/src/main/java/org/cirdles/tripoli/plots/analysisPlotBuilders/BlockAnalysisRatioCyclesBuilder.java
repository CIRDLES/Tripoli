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

import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.plots.compoundPlotBuilders.BlockRatioCyclesRecord;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author James F. Bowring
 */
public class BlockAnalysisRatioCyclesBuilder extends PlotBuilder {
    private BlockAnalysisRatioCyclesRecord blockAnalysisRatioCyclesRecord;


    public BlockAnalysisRatioCyclesBuilder() {
    }

    private BlockAnalysisRatioCyclesBuilder(String plotTitle, List<BlockRatioCyclesRecord> blockRatioCyclesRecords, String xAxisLabel, String yAxisLabel) {
        super(new String[]{plotTitle}, xAxisLabel, yAxisLabel, true);
        blockAnalysisRatioCyclesRecord = generateBlockAnalysisRatioCycles(blockRatioCyclesRecords);
    }

    public static BlockAnalysisRatioCyclesBuilder initializeBlockAnalysisRatioCycles(
            String plotTitle, List<BlockRatioCyclesRecord> blockRatioCyclesRecordsList, String xAxisLabel, String yAxisLabel) {
        BlockAnalysisRatioCyclesBuilder blockAnalysisRatioCyclesBuilder = new BlockAnalysisRatioCyclesBuilder(plotTitle, blockRatioCyclesRecordsList, xAxisLabel, yAxisLabel);

        return blockAnalysisRatioCyclesBuilder;
    }

    private BlockAnalysisRatioCyclesRecord generateBlockAnalysisRatioCycles(List<BlockRatioCyclesRecord> blockRatioCyclesRecordsList) {
//        List<Double> histogramMeans = new ArrayList<>();
//        List<Double> histogramOneSigma = new ArrayList<>();
//        DescriptiveStatistics descriptiveStatisticsRatiosByBlock = new DescriptiveStatistics();

        Map<Integer, BlockRatioCyclesRecord> mapBlockIdToBlockRatioCyclesRecord = new TreeMap<>();
        int blockIndex = 0;
        for (BlockRatioCyclesRecord blockRatioCyclesRecord : blockRatioCyclesRecordsList) {
            if (blockRatioCyclesRecord != null) {
                mapBlockIdToBlockRatioCyclesRecord.put(blockRatioCyclesRecord.blockID(), blockRatioCyclesRecord);
//            histogramMeans.add(histogramRecord.mean());
//            descriptiveStatisticsRatiosByBlock.addValue(histogramRecord.mean());
//            histogramOneSigma.add(histogramRecord.standardDeviation());
            } else {
                mapBlockIdToBlockRatioCyclesRecord.put(blockIndex + 1, null);
            }
            blockIndex++;
        }
//        double[] blockIds = blockIdList.stream().mapToDouble(d -> d).toArray();
//        double[] blockLogRatioMeans = histogramMeans.stream().mapToDouble(d -> d).toArray();
//        double[] blockLogRatioOneSigmas = histogramOneSigma.stream().mapToDouble(d -> d).toArray();

        return new BlockAnalysisRatioCyclesRecord(
                mapBlockIdToBlockRatioCyclesRecord,
                blockRatioCyclesRecordsList.get(0).cyclesIncluded().length,
                0.0,
                0.0,
                1,
                1,
                title,
                "Blocks & Cycles by Time",
                "Ratio"
        );
    }

    public BlockAnalysisRatioCyclesRecord getBlockAnalysisRatioCyclesRecord() {
        return blockAnalysisRatioCyclesRecord;
    }
}