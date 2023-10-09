package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors;

import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.DetectorSetup;

import java.io.Serializable;
import java.util.*;

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
        blocksData.put(massSpecOutputSingleBlockRecord.blockID(), massSpecOutputSingleBlockRecord);
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

    public String printHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("Software Version: " + header.softwareVersion() + "\n");
        sb.append("Sample: " + "unknown" + "\n");
        sb.append("Fraction: " + "unknown" + "\n");
        sb.append("Method Name: " + header.methodName() + "\n");
        sb.append("Time Zero: " + header.localDateTimeZero() + "\n\n");
        return sb.toString();
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
                    //detectorType
                    Detector.DetectorTypeEnum.mapOfNamesToDetectorType.get(detectorData.get(detectorIndex)[1]),
                    //detectorName
                    detectorData.get(detectorIndex)[0],
                    //ordinalIndex
                    detectorIndex - 1,
                    //amplifierType
                    Detector.AmplifierTypeEnum.mapOfDetectorTypetoAmplifierType.get(detectorData.get(detectorIndex)[1]),
                    //amplifierResistanceInOhms
                    Double.parseDouble(detectorData.get(detectorIndex)[2]),
                    //amplifierGain
                    Double.parseDouble(detectorData.get(detectorIndex)[3]),
                    //amplifierEfficiency
                    Double.parseDouble(detectorData.get(detectorIndex)[4]),
                    //detectorDeadTime
                    Double.parseDouble(detectorData.get(detectorIndex)[5]));
            detectorSetup.addDetector(detector);
        }
    }

    public double[] calculateSessionTimes() {
        int totalSize = 0;
        for (MassSpecOutputSingleBlockRecord blockRecord : blocksData.values()) {
            totalSize += blockRecord.onPeakTimeStamps().length;
        }
        double[] times = new double[totalSize];
        totalSize = 0;
        for (MassSpecOutputSingleBlockRecord blockRecord : blocksData.values()) {
            double[] blockTimes = blockRecord.onPeakTimeStamps();

            System.arraycopy(blockTimes, 0, times, totalSize, blockTimes.length);
            totalSize += blockRecord.onPeakTimeStamps().length;
        }
        return times;
    }

    public int[] assignBlockIdToSessionTime() {
        int totalSize = 0;
        for (MassSpecOutputSingleBlockRecord blockRecord : blocksData.values()) {
            totalSize += blockRecord.onPeakTimeStamps().length;
        }
        int[] blockIDs = new int[totalSize];
        totalSize = 0;
        for (MassSpecOutputSingleBlockRecord blockRecord : blocksData.values()) {
            double[] blockTimes = blockRecord.onPeakTimeStamps();
            Arrays.fill(blockIDs, totalSize, totalSize + blockTimes.length, blockRecord.blockID());
            totalSize += blockRecord.onPeakTimeStamps().length;
        }
        return blockIDs;
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