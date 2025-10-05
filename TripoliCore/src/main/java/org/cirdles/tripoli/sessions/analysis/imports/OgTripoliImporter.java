package org.cirdles.tripoli.sessions.analysis.imports;

import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class OgTripoliImporter {
    public static AnalysisInterface importTripolizedData(File ogTripoliFile) {
        try {
            AnalysisInterface tripoliAnalysis = AnalysisInterface.initializeNewAnalysis(0);
            List<Double> timeValues = new ArrayList<>();

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

            // Generate Cycle Data
            String[] cycleValues = line.split("\t");
            for (int i = 1; i < cycleValues.length; i++) {
                // todo: Populate cycles here
            }

        } catch (IOException | TripoliException e) {
            throw new RuntimeException(e);
        }
        return null; //todo: return tripoliAnalysis;
    }
}
