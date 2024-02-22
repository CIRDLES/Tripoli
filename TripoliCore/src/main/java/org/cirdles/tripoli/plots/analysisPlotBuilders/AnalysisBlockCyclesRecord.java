package org.cirdles.tripoli.plots.analysisPlotBuilders;

import org.cirdles.tripoli.plots.compoundPlotBuilders.BlockCyclesRecord;

import java.io.Serializable;
import java.util.Map;

public record AnalysisBlockCyclesRecord(
        Map<Integer, BlockCyclesRecord> mapBlockIdToBlockCyclesRecord,
        int cyclesPerBlock,
        String[] title,
        boolean isRatio,
        boolean isInverted) implements Serializable {

    public String[] updatedTitle(){
        String[] retVal = title.clone();
        if (isInverted && isRatio){
            String[] nameSplit = retVal[0].split("/");
            retVal[0] = nameSplit[1] + "/" + nameSplit[0];
        }
        return retVal;
    }
}