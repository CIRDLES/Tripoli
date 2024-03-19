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
import org.cirdles.tripoli.plots.compoundPlotBuilders.PlotBlockCyclesRecord;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author James F. Bowring
 */
public class BlockAnalysisRatioCyclesBuilder extends PlotBuilder {
    private AnalysisBlockCyclesRecord analysisBlockCyclesRecord = null;

    private BlockAnalysisRatioCyclesBuilder(String plotTitle, Map<Integer, Integer> mapOfBlockIdToProcessStatus, List<PlotBlockCyclesRecord> plotBlockCyclesRecords, boolean isRatio, boolean isInverted, int[] xAxisBlockIDs) {
        super(new String[]{plotTitle}, "NONE", "NONE", true);
        analysisBlockCyclesRecord = generateAnalysisBlockCyclesRecord(plotBlockCyclesRecords, mapOfBlockIdToProcessStatus, xAxisBlockIDs, isRatio, isInverted);
    }

    public static BlockAnalysisRatioCyclesBuilder initializeBlockAnalysisRatioCycles(
            String plotTitle, List<PlotBlockCyclesRecord> plotBlockCyclesRecordsList, Map<Integer, Integer> mapOfBlockIdToProcessStatus, int[] xAxisBlockIDs, boolean isRatio, boolean isInverted) {
        BlockAnalysisRatioCyclesBuilder blockAnalysisRatioCyclesBuilder =
                new BlockAnalysisRatioCyclesBuilder(plotTitle, mapOfBlockIdToProcessStatus, plotBlockCyclesRecordsList, isRatio, isInverted, xAxisBlockIDs);

        return blockAnalysisRatioCyclesBuilder;
    }

    private AnalysisBlockCyclesRecord generateAnalysisBlockCyclesRecord(
            List<PlotBlockCyclesRecord> plotBlockCyclesRecordsList, Map<Integer, Integer> mapOfBlockIdToProcessStatus, int[] xAxisBlockIDs, boolean isRatio, boolean isInverted) {
        Map<Integer, PlotBlockCyclesRecord> mapBlockIdToBlockCyclesRecord = new TreeMap<>();
        int blockIndex = 0;
        for (PlotBlockCyclesRecord plotBlockCyclesRecord : plotBlockCyclesRecordsList) {
            if (plotBlockCyclesRecord != null) {
                mapBlockIdToBlockCyclesRecord.put(plotBlockCyclesRecord.blockID(), plotBlockCyclesRecord);
            } else {
                mapBlockIdToBlockCyclesRecord.put(blockIndex + 1, null);
            }
            blockIndex++;
        }

        return new AnalysisBlockCyclesRecord(
                mapBlockIdToBlockCyclesRecord,
                mapOfBlockIdToProcessStatus,
                plotBlockCyclesRecordsList.get(0).cyclesIncluded().length,
                xAxisBlockIDs,
                title,
                isRatio,
                isInverted);
    }

    public AnalysisBlockCyclesRecord getBlockAnalysisRatioCyclesRecord() {
        return analysisBlockCyclesRecord;
    }
}