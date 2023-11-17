package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors;

import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.DetectorSetup;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.*;

public class MassSpecExtractedData implements Serializable {
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();

        ObjectStreamClass myObject = ObjectStreamClass.lookup(
                Class.forName(MassSpecExtractedData.class.getCanonicalName()));
        long theSUID = myObject.getSerialVersionUID();

        System.out.println("Customized De-serialization of MassSpecExtractedData "
                + theSUID);
    }

    private MassSpectrometerContextEnum massSpectrometerContext;
    private MassSpecExtractedHeader header;
    private String[] columnHeaders;
    private DetectorSetup detectorSetup;
    private Map<Integer, MassSpecOutputBlockRecordFull> blocksDataFull;
    private Map<Integer, MassSpecOutputBlockRecordLite> blocksDataLite;

    public MassSpecExtractedData() {
        massSpectrometerContext = MassSpectrometerContextEnum.UNKNOWN;
        populateHeader(new ArrayList<>());
        populateColumnNamesList(new ArrayList<>());
        populateDetectors(new ArrayList<>());
        blocksDataFull = new TreeMap<>();
        blocksDataLite = new TreeMap<>();
    }

    public void addBlockRecord(MassSpecOutputBlockRecordFull massSpecOutputBlockRecordFull) {
        blocksDataFull.put(massSpecOutputBlockRecordFull.blockID(), massSpecOutputBlockRecordFull);
    }

    public void addBlockLiteRecord(MassSpecOutputBlockRecordLite massSpecOutputBlockRecordLite) {
        blocksDataLite.put(massSpecOutputBlockRecordLite.blockID(), massSpecOutputBlockRecordLite);
    }

    public void populateHeader(List<String[]> headerData) {
        String softwareVersion = "";
        String filename = "";
        String methodName = "";
        boolean isCorrected = false;
        boolean hasBChannels = false;
        // for Lite version
        int cyclesPerBlock = 0;
        String localDateTimeZero = "LocalDateTime.MIN";
        for (String[] headerStrings : headerData) {
            switch (headerStrings[0].trim().toUpperCase()) {
                case "VERSION" -> softwareVersion = headerStrings[1].trim();
                case "FILENAME" -> filename = headerStrings[1].trim();
                case "METHODNAME" -> methodName = headerStrings[1].trim();
                case "METHOD NAME" -> methodName = headerStrings[1].trim();
                case "CORRECTED" ->
                        isCorrected = Boolean.parseBoolean(headerStrings[1].trim().toUpperCase().replace("YES", "TRUE"));
                case "BCHANNELS" ->
                        hasBChannels = Boolean.parseBoolean(headerStrings[1].trim().toUpperCase().replace("YES", "TRUE"));
                case "TIMEZERO" -> localDateTimeZero = headerStrings[1].trim();
                case "CYCLESTOMEASURE" -> cyclesPerBlock = Integer.parseInt(headerStrings[1].trim());
            }
        }
        header = new MassSpecExtractedHeader(
                softwareVersion,
                filename,
                methodName,
                isCorrected,
                hasBChannels,
                localDateTimeZero,
                cyclesPerBlock
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
        for (MassSpecOutputBlockRecordFull blockRecord : blocksDataFull.values()) {
            totalSize += blockRecord.onPeakTimeStamps().length;
        }
        double[] times = new double[totalSize];
        totalSize = 0;
        for (MassSpecOutputBlockRecordFull blockRecord : blocksDataFull.values()) {
            double[] blockTimes = blockRecord.onPeakTimeStamps();

            System.arraycopy(blockTimes, 0, times, totalSize, blockTimes.length);
            totalSize += blockRecord.onPeakTimeStamps().length;
        }
        return times;
    }

    public int[] assignBlockIdToSessionTime() {
        int totalSize = 0;
        for (MassSpecOutputBlockRecordFull blockRecord : blocksDataFull.values()) {
            totalSize += blockRecord.onPeakTimeStamps().length;
        }
        int[] blockIDs = new int[totalSize];
        totalSize = 0;
        for (MassSpecOutputBlockRecordFull blockRecord : blocksDataFull.values()) {
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

    public Map<Integer, MassSpecOutputBlockRecordFull> getBlocksDataFull() {
        return blocksDataFull;
    }

    public Map<Integer, MassSpecOutputBlockRecordLite> getBlocksDataLite() {
        return blocksDataLite;
    }

    public record MassSpecExtractedHeader(
            String softwareVersion,
            String filename,
            String methodName,
            boolean isCorrected,
            boolean hasBChannels,
            String localDateTimeZero,
            int cyclesPerBlock
    ) implements Serializable {
    }
}