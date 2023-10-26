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
import org.cirdles.tripoli.species.IsotopicRatio;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author James F. Bowring
 */
public class BlockAnalysisRatioCyclesBuilder extends PlotBuilder {
    //    @Serial
//    private static final long serialVersionUID = 9180059676626735662L;
    private BlockAnalysisRatioCyclesRecord blockAnalysisRatioCyclesRecord;


    public BlockAnalysisRatioCyclesBuilder() {
    }

    private BlockAnalysisRatioCyclesBuilder(IsotopicRatio isotopicRatio, List<BlockRatioCyclesRecord> blockRatioCyclesRecords, String xAxisLabel, String yAxisLabel) {
        super(new String[]{isotopicRatio.prettyPrint()}, xAxisLabel, yAxisLabel, true);
        blockAnalysisRatioCyclesRecord = generateBlockAnalysisRatioCycles(isotopicRatio, blockRatioCyclesRecords);
    }

    public static BlockAnalysisRatioCyclesBuilder initializeBlockAnalysisRatioCycles(
            IsotopicRatio isotopicRatio, List<BlockRatioCyclesRecord> blockRatioCyclesRecordsList, String xAxisLabel, String yAxisLabel) {
        BlockAnalysisRatioCyclesBuilder blockAnalysisRatioCyclesBuilder = new BlockAnalysisRatioCyclesBuilder(isotopicRatio, blockRatioCyclesRecordsList, xAxisLabel, yAxisLabel);

        return blockAnalysisRatioCyclesBuilder;
    }

    private BlockAnalysisRatioCyclesRecord generateBlockAnalysisRatioCycles(IsotopicRatio isotopicRatio, List<BlockRatioCyclesRecord> blockRatioCyclesRecordsList) {
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
//        double[] blockMeans = histogramMeans.stream().mapToDouble(d -> d).toArray();
//        double[] blockOneSigmas = histogramOneSigma.stream().mapToDouble(d -> d).toArray();

        return new BlockAnalysisRatioCyclesRecord(
                isotopicRatio,
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