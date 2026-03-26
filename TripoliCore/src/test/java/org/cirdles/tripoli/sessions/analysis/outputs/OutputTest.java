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

package org.cirdles.tripoli.sessions.analysis.outputs;

import jakarta.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.reports.ReportData;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.initializers.AllBlockInitForDataLiteOne;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Compares clipboard output for MassSpec data against known formatting
 */

public class OutputTest {
    /**
     * Uses a filepath to generate a short report and then asserts it to a premade Oracle made with the same analysis name
     *
     * @param dataFilepath
     * @param reportData
     * @return
     * @throws JAXBException
     * @throws TripoliException
     * @throws URISyntaxException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     */
    public String[] shortReportTest(String dataFilepath, ReportData reportData) throws JAXBException, TripoliException, URISyntaxException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        AnalysisInterface analysis = reportData.getAnalysis();
        String analysisName = reportData.getAnalysisName();
        File dataFile = reportData.getDataFile();

        // Sort UserFunctions
        List<UserFunction> userFunctions = new ArrayList<>(analysis.getUserFunctions());
        userFunctions.sort(null);
        analysis.setUserFunctions(userFunctions);

        System.out.println("📝 Generating Short Report for " + dataFile.getName() + "...");
        // Create the report to test against the Oracle
        String actualReport = "";
        String expectedReport = null;
        String expectedReportPath = null;
        try {
            // Deserialize the Oracle report
            expectedReportPath = dataFilepath.substring(0, dataFilepath.lastIndexOf('/') + 1).replace("dataFiles", "shortReports") + "Oracle-" + analysisName + ".txt";
            expectedReport = FileUtils.readFileToString(new File(Objects.requireNonNull(getClass().getResource(expectedReportPath)).toURI()), "UTF-8");

            AllBlockInitForDataLiteOne.initBlockModels(analysis);
            actualReport = analysis.prepareFractionForClipboardExport();
        } catch (NullPointerException | IOException e) {
            assertNotNull(expectedReport,
                    "Oracle not found for file " + dataFile.getName() + " at: " + expectedReportPath);
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        return new String[]{expectedReport, actualReport};
    }

    @ParameterizedTest
    @MethodSource("org.cirdles.tripoli.reports.ReportData#generateFilepaths")
    public void outputTest(String dataFilePath) {
        System.out.println("Output Test for " + dataFilePath + "");
        try {
            ReportData reportData = new ReportData();
            reportData = reportData.generateReportData(dataFilePath);

            String[] shortReportTestResults = shortReportTest(dataFilePath, reportData);
            assertEquals(shortReportTestResults[0], shortReportTestResults[1], "❌ Short Report generation failed!\n");
            System.out.println("✅ Short Report generated successfully!\n");
        } catch (JAXBException | TripoliException | URISyntaxException | InvocationTargetException |
                 NoSuchMethodException | IllegalAccessException e) {
            System.out.println("Error: " + e.getMessage());
        }

    }
}
