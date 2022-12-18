package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MassSpecExtractedData implements Serializable {

    private MassSpecExtractedHeader header;
    private String[] columnHeaders;

    private Map<Integer, MassSpecOutputSingleBlockRecord> blocksData;

    public MassSpecExtractedData() {
        populateHeader(new ArrayList<>());
        populateColumnNamesList(new ArrayList<>());
        this.blocksData = new TreeMap<>();
    }

    public void addBlockRecord(MassSpecOutputSingleBlockRecord massSpecOutputSingleBlockRecord) {
        blocksData.put(massSpecOutputSingleBlockRecord.blockNumber(), massSpecOutputSingleBlockRecord);
    }

    public void populateHeader(List<String[]> headerData){
        String softwareVersion = "";
        String filename = "";
        String methodName = "";
        boolean isCorrected = false;
        boolean hasBChannels = false;
        String localDateTimeZero = "LocalDateTime.MIN";
        for (String[] ss : headerData){
                switch (ss[0].trim().toUpperCase()){
                    case "VERSION" -> softwareVersion = ss[1].trim();
                    case "FILENAME" -> filename = ss[1].trim();
                    case "METHODNAME" -> methodName = ss[1].trim();
                    case "METHOD NAME" -> methodName = ss[1].trim();
                    case "CORRECTED" -> isCorrected = Boolean.parseBoolean(ss[1].trim().toUpperCase().replace("YES", "TRUE"));
                    case "BCHANNELS" -> hasBChannels = Boolean.parseBoolean(ss[1].trim().toUpperCase().replace("YES", "TRUE"));
                    case "TIMEZERO" -> localDateTimeZero = ss[1].trim();
                }
        }
        this.header = new MassSpecExtractedHeader(
                softwareVersion,
                filename,
                methodName,
                isCorrected,
                hasBChannels,
                localDateTimeZero
        );
    }

    public void populateColumnNamesList(List<String[]> columnNames){
        if (columnNames.isEmpty()) {
            columnHeaders = new String[0];
        } else {
            columnHeaders = columnNames.get(0);
        }
    }

    public MassSpecExtractedHeader getHeader() {
        return header;
    }

    public void setHeader(MassSpecExtractedHeader header) {
        this.header = header;
    }

    public String[] getColumnHeaders() {
        return columnHeaders;
    }

    public void setColumnHeaders(String[] columnHeaders) {
        this.columnHeaders = columnHeaders;
    }

    public Map<Integer, MassSpecOutputSingleBlockRecord> getBlocksData() {
        return blocksData;
    }

    public void setBlocksData(Map<Integer, MassSpecOutputSingleBlockRecord> blocksData) {
        this.blocksData = blocksData;
    }

    public record MassSpecExtractedHeader(
            String softwareVersion,
            String filename,
            String methodName,
            boolean isCorrected,
            boolean hasBChannels,
            String localDateTimeZero
    ) implements Serializable {
    }
}