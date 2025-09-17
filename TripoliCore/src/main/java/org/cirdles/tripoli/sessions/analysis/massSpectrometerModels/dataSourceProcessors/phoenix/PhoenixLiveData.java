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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.phoenix;

import org.apache.commons.lang3.time.DateUtils;
import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputBlockRecordLite;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliPersistentState;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod.createAnalysisMethodFromCase1;

public class PhoenixLiveData {
    AnalysisInterface liveDataAnalysis;
    boolean initMetaData = true;
    MassSpecOutputBlockRecordLite blockRecordLite;
    MassSpecExtractedData massSpecExtractedData;
    double [][] cycleData;
    int numOfFunctions = 0;
    int cycleIndex = 0;
    int blockIndex = 0;
    String analysisNumber;
    int cyclesPerBlock = 0;

    public PhoenixLiveData() throws TripoliException {
        liveDataAnalysis = AnalysisInterface.initializeNewAnalysis(0);
        massSpecExtractedData = new MassSpecExtractedData();
        massSpecExtractedData.setColumnHeaders(new String[] { "Cycle", "Time" });
        MassSpectrometerContextEnum massSpectrometerContext = liveDataAnalysis.getParameters().getMassSpectrometerContext();
        massSpecExtractedData.setMassSpectrometerContext(massSpectrometerContext);
        liveDataAnalysis.setMassSpecExtractedData(massSpecExtractedData);
    }

    private File getAnalysisTxtFile() {
        Path liveDataPath = Path.of(liveDataAnalysis.getDataFilePathString()).getParent();
        String analysisName = liveDataAnalysis.getAnalysisSampleName() + " " + liveDataAnalysis.getAnalysisFractionName();
        analysisName = analysisName + "-" + analysisNumber;
        if (new File(liveDataPath.resolve(analysisName + ".txt").toString()).exists()) {
            return liveDataPath.resolve(analysisName + ".txt").toFile();
        }
        return null;
    }

    public AnalysisInterface getLiveDataAnalysis(){
        return liveDataAnalysis;
    }

    public AnalysisInterface readLiveDataFile(Path filePath){
        File liveDataFile = filePath.toFile();
        analysisNumber = liveDataFile.getName().split("-")[0];

        if (liveDataFile.exists() && liveDataFile.isFile()){
            try {
                String[] lines = Files.readAllLines(filePath).toArray(new String[0]);
                for (String line : lines){
                    readLiveDataLine(line);
                }
                if (initMetaData) {
                    setAnalysisHeader();

                    // Have AnalysisMethod figure out ratios and then set them accordingly
                    liveDataAnalysis.setMethod(createAnalysisMethodFromCase1(massSpecExtractedData));
                    List<UserFunction> userFunctionModel = liveDataAnalysis.getMethod().getUserFunctionsModel();
                    for (UserFunction modelFunc : userFunctionModel) {
                        if (modelFunc.isTreatAsIsotopicRatio()) {
                            for (UserFunction func : liveDataAnalysis.getUserFunctions()) {
                                if (func.getName().equals(modelFunc.getName())) {
                                    func.setTreatAsIsotopicRatio(true);
                                }
                            }
                        }
                    }
                    liveDataAnalysis.setDataFilePathString(filePath.getParent().toString());
                }
                initMetaData = false;

                blockRecordLite = new MassSpecOutputBlockRecordLite(blockIndex, cycleData);
                massSpecExtractedData.addBlockLiteRecord(blockRecordLite);

                for (UserFunction userFunction : liveDataAnalysis.getUserFunctions()){
                    userFunction.getMapBlockIdToBlockCyclesRecord().clear();
                }

                return liveDataAnalysis;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void readLiveDataLine(String dataLine){
        String[] dataLineSplit = dataLine.split(",");

        switch (dataLineSplit[0]){
            case "Version":
            case "Serial No":
            case "Sample No":
            case "Beam Interp":
            case "Baseline Corrected":
                break;
            case "Method":
                if (initMetaData)
                {
                    String analysisName = dataLineSplit[1].substring(dataLineSplit[1].lastIndexOf("\\")+1, dataLineSplit[1].length()-1);
                    liveDataAnalysis.setAnalysisName(analysisName + (" (Live Data)"));
                    liveDataAnalysis.setAnalysisSampleName(analysisName.split(" ")[0]);
                    liveDataAnalysis.setAnalysisFractionName(analysisName.split(" ")[1].split("-")[0]);
                }
                break;
            case "Acquire Date":
                if (initMetaData){
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date;
                    try {
                        date = DateUtils.parseDate(dataLineSplit[1].trim(),
                                "dd/MM/yyyy HH:mm:ss",
                                "yyyy-MM-dd hh:mm:ss",
                                "yyyy-MM-dd h:mm:ss a",
                                "dd/MM-yyyy",
                                "E d MMMM yyyy hh:mm:ss",
                                "MM/dd/yyyy hh:mm:ss",
                                "MM/dd/yyyy h:mm:ss a",
                                "dd.MM.yyyy",
                                "dd.MM.yyyy hh:mm:ss",
                                "MM/dd/yyyy",
                                "yyyy-MM-dd",
                                "y/m/d");
                        liveDataAnalysis.setAnalysisStartTime(df.format(date));

                    } catch (ParseException ignored) {
                        liveDataAnalysis.setAnalysisStartTime("Unknown");
                    }
                }
                break;
            case "Functions":
                numOfFunctions = Integer.parseInt(dataLineSplit[1]);
                break;
            case "Cycle":
                cycleIndex = Integer.parseInt(dataLineSplit[1]);
                if (cycleData == null || cycleData.length > cycleIndex) {
                    // Starting a new block, set the CPB and redo the header
                    if (cycleData != null && cyclesPerBlock == 0){
                        cyclesPerBlock = cycleData.length;
                        setAnalysisHeader();
                    }
                    cycleData = new double[cycleIndex][numOfFunctions];
                } else { // Copy old data to new array
                    double[][] expandedCycleData = new double[cycleIndex][numOfFunctions];
                    for (int row = 0; row < cycleData.length; row++) {
                        System.arraycopy(cycleData[row], 0, expandedCycleData[row], 0, cycleData[row].length);
                    }
                    cycleData = expandedCycleData;

                }
                break;
            case "Block":
                blockIndex = Integer.parseInt(dataLineSplit[1]);
                break;
            default:
                try {
                    int columnIndex = Integer.parseInt(dataLineSplit[0])-1;
                    String userFunctionName = dataLineSplit[1].substring(1, dataLineSplit[1].length()-1);
                    double userFunctionValue = Double.parseDouble(dataLineSplit[2]);
                    if (initMetaData){
                        UserFunction userFunction = new UserFunction(userFunctionName, columnIndex);
                        liveDataAnalysis.getUserFunctions().add(userFunction);
                        String[] headersExpanded = Arrays.copyOf(massSpecExtractedData.getColumnHeaders(),
                                massSpecExtractedData.getColumnHeaders().length+1);
                        headersExpanded[columnIndex+2] = userFunctionName;
                        massSpecExtractedData.setColumnHeaders(headersExpanded);
                    }
                    cycleData[cycleIndex-1][columnIndex] = userFunctionValue;
                } catch (Exception ignore) {}
        }
    }
    private List<String> readTxtHeaderFromFile(File analysisTxtFile) {
        if (analysisTxtFile != null) {
            try {
                BufferedReader br = Files.newBufferedReader(analysisTxtFile.toPath());
                List<String> headerData = new ArrayList<>();

                String line;
                while (!Objects.equals(line = br.readLine(), "")) {
                    if (line.contains(",")){
                        headerData.add(line.split(",")[1]);
                    }
                }
                if (headerData.get(2).contains(".")){ // Strip off file extension
                    headerData.set(2, headerData.get(2).substring(0, headerData.get(2).lastIndexOf(".")));
                }
                return headerData;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    private void setAnalysisHeader() {
        MassSpecExtractedData.MassSpecExtractedHeader header;

        File analysisTxtFile = getAnalysisTxtFile();
        List<String> headerData = readTxtHeaderFromFile(analysisTxtFile);

        if (analysisTxtFile != null) {
            String fileName = headerData.get(1).split("\\.")[0];
            header = new MassSpecExtractedData.MassSpecExtractedHeader(
                    headerData.get(0),
                    fileName,
                    fileName.substring(0, fileName.lastIndexOf("-")),
                    headerData.get(2),
                    Boolean.parseBoolean(headerData.get(6)),
                    Boolean.parseBoolean(headerData.get(7)),
                    headerData.get(8).trim(),
                    cyclesPerBlock
            );
        } else {
            header = new MassSpecExtractedData.MassSpecExtractedHeader(
                    "Phoenix",
                    "LiveData",
                    "",
                    "Phoenix_Live_Data_Processing",
                    false,
                    false,
                    "",
                    cyclesPerBlock
            );
        }

        massSpecExtractedData.setHeader(header);
    }

    /**
     * Merges changes from LiveData analysis to finished analysis. Copies BlockIdToRawDataLiteOne included data,
     * and replaces UserFunctions in FinishedAnalysis with those from LiveDataAnalysis.
     * @param finishedAnalysis
     */
    public void mergeFinalFile(AnalysisInterface finishedAnalysis){
        liveDataAnalysis.getMapOfBlockIdToRawDataLiteOne().forEach((blockID, blockRawData) -> {
            boolean[][] liveArray = blockRawData.blockRawDataLiteIncludedArray();
            boolean[][] finishedArray = finishedAnalysis.getMapOfBlockIdToRawDataLiteOne().get(blockID).blockRawDataLiteIncludedArray();
            for (int row = 0; row < liveArray.length; row++) {
                System.arraycopy(liveArray[row], 0, finishedArray[row], 0, liveArray[row].length);
            }
        });

        List<UserFunction> liveUFs = liveDataAnalysis.getUserFunctions();
        List<UserFunction> finishedUFs = finishedAnalysis.getUserFunctions();
        finishedUFs.replaceAll(finishedUF ->
                liveUFs.stream()
                        .filter(liveUF -> liveUF.getName().equals(finishedUF.getName()))
                        .findFirst()
                        .orElse(finishedUF)
        );

    }
}
