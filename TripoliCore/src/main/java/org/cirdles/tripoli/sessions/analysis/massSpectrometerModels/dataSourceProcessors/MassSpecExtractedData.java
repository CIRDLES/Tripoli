package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors;

import org.apache.commons.lang3.time.DateUtils;
import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.DetectorSetup;

import java.io.Serial;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class MassSpecExtractedData implements Serializable {
    @Serial
    private static final long serialVersionUID = -3958694419007139380L;
    private MassSpectrometerContextEnum massSpectrometerContext;
    private MassSpecExtractedHeader header;
    private String[] columnHeaders;
    private DetectorSetup detectorSetup;
    private final Map<Integer, MassSpecOutputBlockRecordFull> blocksDataFull;
    private final Map<Integer, MassSpecOutputBlockRecordLite> blocksDataLite;

    public MassSpecExtractedData() {
        massSpectrometerContext = MassSpectrometerContextEnum.UNKNOWN;
        populateHeader(new ArrayList<>());
        populateColumnNamesList(new ArrayList<>());
        populateDetectors(new ArrayList<>());
        blocksDataFull = new TreeMap<>();
        blocksDataLite = new TreeMap<>();
    }

    public static Map<Integer, MassSpecOutputBlockRecordLite> blocksDataLiteConcatenate(
            Map<Integer, MassSpecOutputBlockRecordLite> blocksDataOne, Map<Integer, MassSpecOutputBlockRecordLite> blocksDataTwo) {
        Map<Integer, MassSpecOutputBlockRecordLite> blocksDataLiteConcatenated = new TreeMap<>();

        for (Integer blockID : blocksDataOne.keySet()) {
            blocksDataLiteConcatenated.put(blockID, blocksDataOne.get(blockID));
        }
        int blockIDOffset = blocksDataLiteConcatenated.size();
        for (Integer blockID : blocksDataTwo.keySet()) {
            blocksDataLiteConcatenated.put(blockID + blockIDOffset, blocksDataTwo.get(blockID).copyWithNewBlockID(blockID + blockIDOffset));
        }

        return blocksDataLiteConcatenated;
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
        String sampleName = "";
        String methodName = "";
        boolean isCorrected = false;
        boolean hasBChannels = false;
        // for Lite version
        int cyclesPerBlock = 0;
        String analysisStartTime = java.time.LocalDateTime.now().toLocalDate().toString();
        for (String[] headerStrings : headerData) {
            switch (headerStrings[0].trim().toUpperCase()) {
                // Phoenix
                case "VERSION" -> softwareVersion = headerStrings[1].trim();
                case "FILENAME" -> filename = headerStrings[1].trim();
                case "METHODNAME" -> methodName = headerStrings[1].trim();
                case "METHOD NAME" -> methodName = headerStrings[1].trim();
                case "CORRECTED" ->
                        isCorrected = Boolean.parseBoolean(headerStrings[1].trim().toUpperCase().replace("YES", "TRUE"));
                case "BCHANNELS" ->
                        hasBChannels = Boolean.parseBoolean(headerStrings[1].trim().toUpperCase().replace("YES", "TRUE"));
                case "TIMEZERO" -> analysisStartTime = headerStrings[1].trim();
                case "ANALYSISSTART" -> analysisStartTime = headerStrings[1].trim();
                case "CYCLESTOMEASURE" -> cyclesPerBlock = Integer.parseInt(headerStrings[1].trim());
                case "SAMPLEID" -> {
                    sampleName = headerStrings[1].trim();
                }

                // Triton
                case "DATA VERSION" -> softwareVersion = headerStrings[1].trim();
                case "DATE" -> analysisStartTime = headerStrings[1].trim();

                // Nu
                case "VERSION NUMBER" -> softwareVersion = headerStrings[1].trim();
                case "SAMPLE NAME" -> sampleName = headerStrings[1].trim();
                case "ANALYSIS FILE NAME" -> filename = headerStrings[1].trim();
                case "NUMBER OF MEASUREMENTS PER BLOCK" -> cyclesPerBlock = Integer.parseInt(headerStrings[1].trim());
            }
        }

        Date date = null;
        try {
            date = DateUtils.parseDate(analysisStartTime,
                    "yyyy-MM-dd hh:mm:ss", "dd/MM-yyyy", "E d MMMM yyyy hh:mm:ss", "MM/dd/yyyy hh:mm:ss", "dd.MM.yyyy", "MM/dd/yyyy", "yyyy-MM-dd", "y/m/d");
        } catch (Exception e) {
            //
        } finally {
            if (date != null) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                analysisStartTime = df.format(date);
            }
        }

        header = new MassSpecExtractedHeader(
                softwareVersion,
                filename,
                sampleName,
                methodName,
                isCorrected,
                hasBChannels,
                analysisStartTime,
                (cyclesPerBlock == 0) ? 10 : cyclesPerBlock //TODO: fix this hack for triton
        );
    }

    public String printHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("Software Version: " + header.softwareVersion() + "\n");
        sb.append("Sample: " + header.sampleName + "\n");
        sb.append("Fraction: " + header.sampleName + "\n");
        sb.append("Method Name: " + header.methodName() + "\n");
        sb.append("Start Time: " + header.analysisStartTime() + "\n\n");
        return sb.toString();
    }

    public void populateColumnNamesList(List<String[]> columnNames) {
        if (columnNames.isEmpty()) {
            columnHeaders = new String[0];
        } else {
            columnHeaders = columnNames.get(0);
        }
    }

    public void populateColumnNamesListNu(List<String> columnNames) {
        if (columnNames.isEmpty()) {
            columnHeaders = new String[0];
        } else {
            columnHeaders = columnNames.toArray(new String[0]);
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

    public int[] assignBlockIdToSessionTimeFull() {
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

    public int[] assignBlockIdToSessionTimeLite() {
//        int totalSize = 0;
//        for (MassSpecOutputBlockRecordLite blockRecord : blocksDataLite.values()) {
//            totalSize += blockRecord.cycleData().length;
//        }
        int expectedCyclesPerBlock = blocksDataLite.get(1).cycleData().length;
        int totalSize = blocksDataLite.keySet().size() * expectedCyclesPerBlock;
        int[] blockIDs = new int[totalSize];
        totalSize = 0;
        for (MassSpecOutputBlockRecordLite blockRecord : blocksDataLite.values()) {
            Arrays.fill(blockIDs, totalSize, totalSize + expectedCyclesPerBlock, blockRecord.blockID());
            totalSize += expectedCyclesPerBlock; //blockRecord.cycleData().length;
        }
        return blockIDs;
    }

    public void expandCycleDataForUraniumOxideCorrection(int r270_267ColumnIndex, int r265_267ColumnIndex, double r18O_16O) {
        for (Integer blockID : blocksDataLite.keySet()) {
            blocksDataLite.put(blockID, blocksDataLite.get(blockID).expandForUraniumOxideCorrection(r270_267ColumnIndex, r265_267ColumnIndex, r18O_16O));
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

    public String[] getUsedColumnHeaders() {
        List<String> usedColumnHeadersList = new ArrayList<>();
        for (String ch : columnHeaders) {
            if (ch != null) {
                usedColumnHeadersList.add(ch);
            }
        }
        return usedColumnHeadersList.toArray(new String[0]);
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

    public void setBlocksDataLite(Map<Integer, MassSpecOutputBlockRecordLite> blocksDataLite) {
        this.blocksDataLite = blocksDataLite;
    }

    public record MassSpecExtractedHeader(
            String softwareVersion,
            String filename,
            String sampleName,
            String methodName,
            boolean isCorrected,
            boolean hasBChannels,
            String analysisStartTime,
            int cyclesPerBlock
    ) implements Serializable {
    }
}