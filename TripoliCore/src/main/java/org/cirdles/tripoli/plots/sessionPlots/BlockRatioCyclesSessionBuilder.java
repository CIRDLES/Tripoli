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
import org.cirdles.tripoli.plots.compoundPlots.BlockRatioCyclesRecord;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author James F. Bowring
 */
public class BlockRatioCyclesSessionBuilder extends PlotBuilder {
    //    @Serial
//    private static final long serialVersionUID = 9180059676626735662L;
    private BlockRatioCyclesSessionRecord blockRatioCyclesSessionRecord;

    public BlockRatioCyclesSessionBuilder() {
    }

    public BlockRatioCyclesSessionBuilder(List<BlockRatioCyclesRecord> blockRatioCyclesRecords, String[] title, String xAxisLabel, String yAxisLabel) {
        super(title, xAxisLabel, yAxisLabel, true);
        blockRatioCyclesSessionRecord = generateBlockRatioCyclesSession(blockRatioCyclesRecords);
    }

    public static BlockRatioCyclesSessionBuilder initializeBlockRatioCyclesSession(
            List<BlockRatioCyclesRecord> blockRatioCyclesRecordsList, String[] title, String xAxisLabel, String yAxisLabel) {
        BlockRatioCyclesSessionBuilder blockRatioCyclesSessionBuilder = new BlockRatioCyclesSessionBuilder(blockRatioCyclesRecordsList, title, xAxisLabel, yAxisLabel);
        blockRatioCyclesSessionBuilder.blockRatioCyclesSessionRecord = blockRatioCyclesSessionBuilder.generateBlockRatioCyclesSession(blockRatioCyclesRecordsList);
        return blockRatioCyclesSessionBuilder;
    }

    private BlockRatioCyclesSessionRecord generateBlockRatioCyclesSession(List<BlockRatioCyclesRecord> blockRatioCyclesRecordsList) {
//        List<Double> histogramMeans = new ArrayList<>();
//        List<Double> histogramOneSigma = new ArrayList<>();
//        DescriptiveStatistics descriptiveStatisticsRatiosByBlock = new DescriptiveStatistics();

        Map<Integer, BlockRatioCyclesRecord> mapBlockIdToBlockRatioCyclesRecord = new TreeMap<>();
        for (BlockRatioCyclesRecord blockRatioCyclesRecord : blockRatioCyclesRecordsList) {
            mapBlockIdToBlockRatioCyclesRecord.put(blockRatioCyclesRecord.blockID(), blockRatioCyclesRecord);
//            histogramMeans.add(histogramRecord.mean());
//            descriptiveStatisticsRatiosByBlock.addValue(histogramRecord.mean());
//            histogramOneSigma.add(histogramRecord.standardDeviation());
        }
//        double[] blockIds = blockIdList.stream().mapToDouble(d -> d).toArray();
//        double[] blockMeans = histogramMeans.stream().mapToDouble(d -> d).toArray();
//        double[] blockOneSigmas = histogramOneSigma.stream().mapToDouble(d -> d).toArray();

        return new BlockRatioCyclesSessionRecord(
                mapBlockIdToBlockRatioCyclesRecord,
                blockRatioCyclesRecordsList.get(0).cyclesIncluded().length,
                1,
                1,
                title,
                "Blocks & Cycles by Time",
                "Ratio"
        );
    }

    public BlockRatioCyclesSessionRecord getBlockRatioCyclesSessionRecord() {
        return blockRatioCyclesSessionRecord;
    }
}