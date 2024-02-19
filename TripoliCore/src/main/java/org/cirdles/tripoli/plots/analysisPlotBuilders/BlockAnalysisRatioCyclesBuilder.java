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
import org.cirdles.tripoli.plots.compoundPlotBuilders.BlockCyclesRecord;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author James F. Bowring
 */
public class BlockAnalysisRatioCyclesBuilder extends PlotBuilder {
    private AnalysisBlockCyclesRecord analysisBlockCyclesRecord;


    public BlockAnalysisRatioCyclesBuilder() {
    }

    private BlockAnalysisRatioCyclesBuilder(String plotTitle, List<BlockCyclesRecord> blockCyclesRecords, boolean isRatio) {
        super(new String[]{plotTitle}, "NONE", "NONE", true);
        analysisBlockCyclesRecord = generateAnalysisBlockCyclesRecord(blockCyclesRecords, isRatio);
    }

    public static BlockAnalysisRatioCyclesBuilder initializeBlockAnalysisRatioCycles(
            String plotTitle, List<BlockCyclesRecord> blockCyclesRecordsList, boolean isRatio) {
        BlockAnalysisRatioCyclesBuilder blockAnalysisRatioCyclesBuilder = new BlockAnalysisRatioCyclesBuilder(plotTitle, blockCyclesRecordsList, isRatio);

        return blockAnalysisRatioCyclesBuilder;
    }

    private AnalysisBlockCyclesRecord generateAnalysisBlockCyclesRecord(List<BlockCyclesRecord> blockCyclesRecordsList, boolean isRatio) {
        Map<Integer, BlockCyclesRecord> mapBlockIdToBlockCyclesRecord = new TreeMap<>();
        int blockIndex = 0;
        for (BlockCyclesRecord blockCyclesRecord : blockCyclesRecordsList) {
            if (blockCyclesRecord != null) {
                mapBlockIdToBlockCyclesRecord.put(blockCyclesRecord.blockID(), blockCyclesRecord);
            } else {
                mapBlockIdToBlockCyclesRecord.put(blockIndex + 1, null);
            }
            blockIndex++;
        }

        return new AnalysisBlockCyclesRecord(
                mapBlockIdToBlockCyclesRecord,
                blockCyclesRecordsList.get(0).cyclesIncluded().length,
                title,
                isRatio);
    }

    public AnalysisBlockCyclesRecord getBlockAnalysisRatioCyclesRecord() {
        return analysisBlockCyclesRecord;
    }
}