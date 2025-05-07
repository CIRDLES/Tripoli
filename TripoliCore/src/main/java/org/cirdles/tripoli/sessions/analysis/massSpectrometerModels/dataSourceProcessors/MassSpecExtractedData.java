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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors;

import org.apache.commons.lang3.time.DateUtils;
import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.expressions.expressionTrees.ExpressionTreeInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.DetectorSetup;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliPersistentState;

import java.io.Serial;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;


public class MassSpecExtractedData implements Serializable {
    @Serial
    private static final long serialVersionUID = -3958694419007139380L;
    private MassSpectrometerContextEnum massSpectrometerContext;
    private MassSpecExtractedHeader header;
    private String[] columnHeaders;
    private DetectorSetup detectorSetup;
    private Map<Integer, MassSpecOutputBlockRecordFull> blocksDataFull;
    private Map<Integer, MassSpecOutputBlockRecordLite> blocksDataLite;

    public MassSpecExtractedData() throws TripoliException {
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

    public void populateHeader(List<String[]> headerData) throws TripoliException {
        String softwareVersion = "";
        String filename = "";
        String sampleName = "";
        String methodName = "";
        boolean isCorrected = false;
        boolean hasBChannels = false;
        // for Lite version
        int cyclesPerBlock = 0;
        StringBuilder analysisStartTime = new StringBuilder(LocalDateTime.now().toLocalDate().toString());
        for (String[] headerStrings : headerData) {
            switch (headerStrings[0].trim().toUpperCase()) {
                // All
                case "METHODNAME" -> methodName = headerStrings[1].trim().substring(0, headerStrings[1].lastIndexOf('.'));
                case "METHOD NAME" -> methodName = headerStrings[1].trim().substring(0, headerStrings[1].lastIndexOf('.'));

                // Phoenix
                case "VERSION" -> softwareVersion = headerStrings[1].trim();
                case "FILENAME" -> filename = headerStrings[1].trim();
                case "CORRECTED" ->
                        isCorrected = Boolean.parseBoolean(headerStrings[1].trim().toUpperCase().replace("YES", "TRUE"));
                case "BCHANNELS" ->
                        hasBChannels = Boolean.parseBoolean(headerStrings[1].trim().toUpperCase().replace("YES", "TRUE"));
                case "TIMEZERO" -> analysisStartTime = new StringBuilder(headerStrings[1].trim());
                case "ANALYSISSTART" -> analysisStartTime = new StringBuilder(headerStrings[1].trim());
                case "CYCLESTOMEASURE" -> cyclesPerBlock = Integer.parseInt(headerStrings[1].trim());
                case "SAMPLEID" -> {
                    sampleName = headerStrings[1].trim();
                }
                // Neptune
                case "ANALYSIS DATE" -> analysisStartTime = new StringBuilder(headerStrings[1].trim());
                case "ANALYSIS TIME" -> analysisStartTime.append(" ").append(headerStrings[1].trim());

                // Triton
                case "DATA VERSION" -> softwareVersion = headerStrings[1].trim();
                case "DATE" -> analysisStartTime = new StringBuilder(headerStrings[1].trim());

                // Nu
                case "VERSION NUMBER" -> softwareVersion = headerStrings[1].trim();
                case "SAMPLE NAME" -> sampleName = headerStrings[1].trim();
                case "ANALYSIS FILE NAME" -> {
                    filename = headerStrings[1].trim();
                    if (methodName.isEmpty()) {methodName = headerStrings[1].trim();}
                }
                case "NUMBER OF MEASUREMENTS PER BLOCK" -> cyclesPerBlock = Integer.parseInt(headerStrings[1].trim());
            }
        }

        String[] methodLocNameArray = methodName.split("\\\\");
        methodName = methodLocNameArray[methodLocNameArray.length - 1];

        Date date = null;
        try {
            date = DateUtils.parseDate(analysisStartTime.toString(),
                    "yyyy-MM-dd hh:mm:ss", "yyyy-MM-dd h:mm:ss a", "dd/MM-yyyy", "E d MMMM yyyy hh:mm:ss", "MM/dd/yyyy hh:mm:ss", "MM/dd/yyyy h:mm:ss a", "dd.MM.yyyy", "dd.MM.yyyy hh:mm:ss", "MM/dd/yyyy", "yyyy-MM-dd", "y/m/d");
        } catch (Exception e) {
            //
        } finally {
            if (date != null) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                analysisStartTime = new StringBuilder(df.format(date));
            }
        }

        //  if (cyclesPerBlock == 0){
        if (TripoliPersistentState.getExistingPersistentState().getMapMethodNamesToDefaults().containsKey(methodName)) {
            cyclesPerBlock = TripoliPersistentState.getExistingPersistentState().getMapMethodNamesToDefaults().get(methodName).getCyclesPerBlock();
        } //else cyclesPerBlock = 10;
        //    }
        if (cyclesPerBlock == 0) {
            cyclesPerBlock = 10;
        }

        int totalUsedCycles = 0;

        header = new MassSpecExtractedHeader(
                softwareVersion,
                filename,
                sampleName,
                methodName,
                isCorrected,
                hasBChannels,
                analysisStartTime.toString(),
                cyclesPerBlock
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

    public void expandCycleDataForCustomExpression(ExpressionTreeInterface customExpressionTree){
        Double[][] expressionData = customExpressionTree.eval(columnHeaders, blocksDataLite);
        for (Integer blockID : blocksDataLite.keySet()) {
            blocksDataLite.put(blockID, blocksDataLite.get(blockID).expandForCustomExpression(expressionData[blockID-1]));
        }
        String[] columnHeadersExpanded = new String[columnHeaders.length+1];
        System.arraycopy(columnHeaders, 0, columnHeadersExpanded, 0, columnHeaders.length);
        columnHeadersExpanded[columnHeaders.length] = customExpressionTree.getName();
        columnHeaders = columnHeadersExpanded;
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
    )
            implements Serializable {
    }
}