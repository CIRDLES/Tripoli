package org.cirdles.tripoli.sessions.analysis.imports;

import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecOutputBlockRecordLite;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class OgTripoliImporter {

    public static AnalysisInterface importTripolizedData(File ogTripoliFile) {
        try {
            double[][] cycleData = new double[0][];
            int cycleIndex = 0;
            int blockIndex = 1;
            MassSpecExtractedData massSpecExtractedData = new MassSpecExtractedData();
            AnalysisInterface tripoliAnalysis = AnalysisInterface.initializeNewAnalysis(0);
            List<Double> timeValues = new ArrayList<>();

            Files.readAllLines(ogTripoliFile.toPath()).forEach(line -> {}); //todo this is it maybe?
            FileReader fileReader = new FileReader(ogTripoliFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            line = bufferedReader.readLine(); // Skip the first line

            tripoliAnalysis.setAnalysisName(line.split("\\.")[0]);

            // Skip 8 lines to get to headers
            for (int i = 0; i < 8; i++) {
                line = bufferedReader.readLine();
            }

            // Create UserFunctions based on headers (skipping Time)
            String[] headers = line.split("\t");
            List<UserFunction> ufList = tripoliAnalysis.getUserFunctions();
            for (int i = 1; i < headers.length; i++) {
                ufList.add(new UserFunction(headers[i], i-1));
            }

            // Skip 3 lines
            for (int i = 0; i < 3; i++) {
                line = bufferedReader.readLine();
            }

                String[] cycleValues = line.split("\t");
                if (cycleValues.length == 0) { // Start a new block
                    massSpecExtractedData.addBlockLiteRecord(new MassSpecOutputBlockRecordLite(blockIndex, cycleData));
                    cycleData = new double[0][];
                    blockIndex++;
                    cycleIndex = 0;
                } else { // gather cycle data
                    double[][] expandedCycleData = new double[cycleIndex + 1][cycleValues.length];
                    for (int row = 0; row < cycleData.length; row++) {
                        System.arraycopy(cycleData[row], 0, expandedCycleData[row], 0, cycleData[row].length);
                    }
                    cycleData = expandedCycleData;

                    for (int i = 1; i < cycleValues.length; i++) {
                        cycleData[cycleIndex][i-1] = Double.parseDouble(cycleValues[i]);
                    }
                    timeValues.add(Double.parseDouble(cycleValues[0]));
                    cycleIndex++;
                }
                line =  bufferedReader.readLine();

            tripoliAnalysis.setMassSpecExtractedData(massSpecExtractedData);
            return tripoliAnalysis;


        } catch (IOException | TripoliException e) {
            throw new RuntimeException(e);
        }
    }
}
