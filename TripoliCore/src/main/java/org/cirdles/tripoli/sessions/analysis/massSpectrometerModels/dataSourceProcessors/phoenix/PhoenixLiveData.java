package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.phoenix;

import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputBlockRecordLite;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class PhoenixLiveData {
    AnalysisInterface liveDataAnalysis;
    boolean initMetaData = true;
    MassSpecOutputBlockRecordLite blockRecordLite;
    MassSpecExtractedData massSpecExtractedData;
    double [][] cycleData;
    int numOfFunctions = 0;
    int cycleIndex = 0;
    int blockIndex = 0;

    public PhoenixLiveData() throws TripoliException {
        liveDataAnalysis = AnalysisInterface.initializeNewAnalysis(0);
        massSpecExtractedData = new MassSpecExtractedData();
        liveDataAnalysis.setMassSpecExtractedData(massSpecExtractedData);
    }

    public AnalysisInterface readLiveDataFile(Path filePath){
        File liveDataFile = filePath.toFile();
        if (liveDataFile.exists() && liveDataFile.isFile()){
            try {
                String[] lines = Files.readAllLines(filePath).toArray(new String[0]);
                for (String line : lines){
                    readLiveDataLine(line);
                }
                initMetaData = false;

                blockRecordLite = new MassSpecOutputBlockRecordLite(blockIndex, cycleData);
                massSpecExtractedData.addBlockLiteRecord(blockRecordLite);
                massSpecExtractedData.setMassSpectrometerContext(MassSpectrometerContextEnum.PHOENIX_TIMSDP_CASE1);

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

    public void readLiveDataLine(String dataLine){
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
                    String analysisName = Path.of(dataLineSplit[1].substring(1, dataLineSplit[1].length()-1)).getFileName().toString();
                    liveDataAnalysis.setAnalysisName(analysisName);
                }
                break;
            case "Acquire Date":
                if (initMetaData){
                    liveDataAnalysis.setAnalysisStartTime(LocalDateTime.now().toLocalDate().toString());
                }
                break;
            case "Functions":
                numOfFunctions = Integer.parseInt(dataLineSplit[1]);
                break;
            case "Cycle":
                cycleIndex = Integer.parseInt(dataLineSplit[1]);
                if (cycleData == null || cycleData.length > cycleIndex) {
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
                    }
                    cycleData[cycleIndex-1][columnIndex] = userFunctionValue;
                } catch (Exception ignore) {}
        }

    }
}
