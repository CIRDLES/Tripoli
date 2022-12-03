package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class MassSpecExtractedData implements Serializable {

    private MassSpecExtractedHeader header;

    private Map<Integer, MassSpecOutputSingleBlockRecord> blocksData;

    public MassSpecExtractedData() {
        this.header = new MassSpecExtractedHeader();
        this.blocksData = new TreeMap<>();
    }

    public void addBlockRecord(MassSpecOutputSingleBlockRecord massSpecOutputSingleBlockRecord){
        blocksData.put(massSpecOutputSingleBlockRecord.blockNumber(), massSpecOutputSingleBlockRecord);
    }
    public record MassSpecExtractedHeader() {

    }

    public MassSpecExtractedHeader getHeader() {
        return header;
    }

    public void setHeader(MassSpecExtractedHeader header) {
        this.header = header;
    }

    public Map<Integer, MassSpecOutputSingleBlockRecord> getBlocksData() {
        return blocksData;
    }

    public void setBlocksData(Map<Integer, MassSpecOutputSingleBlockRecord> blocksData) {
        this.blocksData = blocksData;
    }
}