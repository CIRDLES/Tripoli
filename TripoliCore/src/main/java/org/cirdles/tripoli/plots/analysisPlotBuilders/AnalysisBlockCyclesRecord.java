package org.cirdles.tripoli.plots.analysisPlotBuilders;

import org.cirdles.tripoli.plots.compoundPlotBuilders.PlotBlockCyclesRecord;

import java.io.Serializable;
import java.util.Map;

public record AnalysisBlockCyclesRecord(
        Map<Integer, PlotBlockCyclesRecord> mapBlockIdToBlockCyclesRecord,
        Map<Integer, Integer> mapOfBlockIdToProcessStatus,
        int cyclesPerBlock,
        int[] xAxisBlockIDs,
        String[] title,
        boolean isRatio,
        boolean isInverted) implements Serializable {

    public String[] updatedTitle() {
        String[] retVal = title.clone();
        if (isInverted && isRatio) {
            String[] nameSplit = retVal[0].split("/");
            retVal[0] = nameSplit[1] + "/" + nameSplit[0];
        }
        return retVal;
    }
}