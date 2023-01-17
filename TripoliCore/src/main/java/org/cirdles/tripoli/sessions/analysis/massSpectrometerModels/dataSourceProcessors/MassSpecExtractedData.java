package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors;

import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.DetectorSetup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MassSpecExtractedData implements Serializable {
    private MassSpectrometerContextEnum massSpectrometerContext;
    private MassSpecExtractedHeader header;
    private String[] columnHeaders;
    private DetectorSetup detectorSetup;
    private Map<Integer, MassSpecOutputSingleBlockRecord> blocksData;

    public MassSpecExtractedData() {
        massSpectrometerContext = MassSpectrometerContextEnum.UNKNOWN;
        populateHeader(new ArrayList<>());
        populateColumnNamesList(new ArrayList<>());
        populateDetectors(new ArrayList<>());
        blocksData = new TreeMap<>();
    }

    public void addBlockRecord(MassSpecOutputSingleBlockRecord massSpecOutputSingleBlockRecord) {
        blocksData.put(massSpecOutputSingleBlockRecord.blockNumber(), massSpecOutputSingleBlockRecord);
    }

    public void populateHeader(List<String[]> headerData) {
        String softwareVersion = "";
        String filename = "";
        String methodName = "";
        boolean isCorrected = false;
        boolean hasBChannels = false;
        String localDateTimeZero = "LocalDateTime.MIN";
        for (String[] ss : headerData) {
            switch (ss[0].trim().toUpperCase()) {
                case "VERSION" -> softwareVersion = ss[1].trim();
                case "FILENAME" -> filename = ss[1].trim();
                case "METHODNAME" -> methodName = ss[1].trim();
                case "METHOD NAME" -> methodName = ss[1].trim();
                case "CORRECTED" ->
                        isCorrected = Boolean.parseBoolean(ss[1].trim().toUpperCase().replace("YES", "TRUE"));
                case "BCHANNELS" ->
                        hasBChannels = Boolean.parseBoolean(ss[1].trim().toUpperCase().replace("YES", "TRUE"));
                case "TIMEZERO" -> localDateTimeZero = ss[1].trim();
            }
        }
        header = new MassSpecExtractedHeader(
                softwareVersion,
                filename,
                methodName,
                isCorrected,
                hasBChannels,
                localDateTimeZero
        );
    }

    public void populateColumnNamesList(List<String[]> columnNames) {
        if (columnNames.isEmpty()) {
            columnHeaders = new String[0];
        } else {
            columnHeaders = columnNames.get(0);
        }
    }

    public void populateDetectors(List<String[]> detectorData) {
        // Phoenix: first row contains headers: Name,Type,Resistor,Gain,Efficiency,DT
        detectorSetup = DetectorSetup.initializeDetectorSetup();
        for (int detectorIndex = 1; detectorIndex < detectorData.size(); detectorIndex++) {
            Detector detector = Detector.initializeDetector(
                    Detector.DetectorTypeEnum.mapOfNamesToDetectorType.get(detectorData.get(detectorIndex)[1]),
                    detectorData.get(detectorIndex)[0],
                    detectorIndex - 1,
                    Detector.AmplifierTypeEnum.mapOfDetectorTypetoAmplifierType.get(detectorData.get(detectorIndex)[1]),
                    Double.parseDouble(detectorData.get(detectorIndex)[2]),
                    Double.parseDouble(detectorData.get(detectorIndex)[3]),
                    Double.parseDouble(detectorData.get(detectorIndex)[4]),
                    Double.parseDouble(detectorData.get(detectorIndex)[5]));
            detectorSetup.addDetector(detector);
        }
    }

    public MassSpectrometerContextEnum getMassSpectrometerContext() {
        return massSpectrometerContext;
    }

    public void setMassSpectrometerContext(MassSpectrometerContextEnum massSpectrometerContext) {
        this.massSpectrometerContext = massSpectrometerContext;
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

    public DetectorSetup getDetectorSetup() {
        return detectorSetup;
    }

    public void setDetectorSetup(DetectorSetup detectorSetup) {
        this.detectorSetup = detectorSetup;
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