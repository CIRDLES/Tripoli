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

package org.cirdles.tripoli.reports;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import jakarta.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.sessions.Session;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.initializers.AllBlockInitForDataLiteOne;
import org.cirdles.tripoli.sessions.analysis.outputs.etRedux.ETReduxFraction;
import org.cirdles.tripoli.sessions.analysis.outputs.etRedux.MeasuredUserFunction;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.xml.ETReduxFractionXMLConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class AllReportsTest {

    /**
     * Generates and returns the data required for a report to be generated.
     *
     * @param dataFilepath
     * @return
     * @throws URISyntaxException
     * @throws JAXBException
     * @throws TripoliException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     */
    public ReportData generateReportData(String dataFilepath) throws URISyntaxException, JAXBException, TripoliException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        File dataFile = new File(Objects.requireNonNull(getClass().getResource(dataFilepath)).toURI());
        System.out.println("💾 Generating Report Data for " + dataFile.getName());

        Session tripoliSession = Session.initializeDefaultSession();

        AnalysisInterface analysisProposed;
        AnalysisInterface analysis = null;
        String analysisName = "";
        try {
            analysisProposed = AnalysisInterface.initializeNewAnalysis(0);
            analysisName = analysisProposed.extractMassSpecDataFromPath(Path.of(dataFile.toURI()));

            if (analysisProposed.getMassSpecExtractedData().getMassSpectrometerContext().compareTo(MassSpectrometerContextEnum.UNKNOWN) != 0) {

                analysisProposed.setAnalysisName(analysisName);
                analysisProposed.setAnalysisStartTime(analysisProposed.getMassSpecExtractedData().getHeader().analysisStartTime());
                tripoliSession.getMapOfAnalyses().put(analysisProposed.getAnalysisName(), analysisProposed);
                analysis = analysisProposed;
            } else {
                analysis = null;
            }

        } catch (IOException | JAXBException | TripoliException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException e) {
        }

        assertNotNull(analysis);
        analysis.getUserFunctions().sort(null);

        ReportData reportData = new ReportData(List.of(analysis), analysis, analysisName, tripoliSession, dataFilepath, dataFile);
        System.out.println("✅ Report Data generated successfully!\n");

        return reportData;
    }

    /**
     * Uses a filepath to generate a full report and then asserts it to a premade Oracle made with the same analysis name
     *
     * @param dataFilepath
     * @param reportData
     * @throws JAXBException
     * @throws TripoliException
     * @throws URISyntaxException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     */
    public String[] fullReportTest(String dataFilepath, ReportData reportData) throws JAXBException, TripoliException, URISyntaxException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        List<AnalysisInterface> analysisList = reportData.getAnalysisList();
        AnalysisInterface analysis = reportData.getAnalysis();
        String analysisName = reportData.getAnalysisName();
        Session tripoliSession = reportData.getTripoliSession();
        File dataFile = reportData.getDataFile();

        System.out.println("📝 Generating Full Report for " + dataFile.getName());
        // Create a Full Report to test against the Oracle
        Report.supressContents = true; // Suppresses the Data File Path column and Created On: line because they would always be different
        String actualReport = null;
        String expectedReport = null;
        String actualReportPath = dataFilepath.substring(0, dataFilepath.lastIndexOf('/') + 1) + "New Session-" + analysisName + "-report.csv";
        String expectedReportPath = dataFilepath.substring(0, dataFilepath.lastIndexOf('/') + 1).replace("dataFiles", "fullReports") + "Oracle-" + analysisName + "-report.csv";
        Report fullReport;

        try {
            fullReport = Report.createFullReport("Full Report", analysis);
            fullReport.generateCSVFile(analysisList, tripoliSession.getSessionName());

            // Deserialize the report to test against the Oracle and the Oracle itself
            actualReport = FileUtils.readFileToString(new File(Objects.requireNonNull(getClass().getResource(actualReportPath)).toURI()), "UTF-8");

            expectedReport = FileUtils.readFileToString(new File(Objects.requireNonNull(getClass().getResource(expectedReportPath)).toURI()), "UTF-8");
        } catch (NullPointerException | IOException e) {
            assertNotNull(actualReport,
                    "Report to test not found for file " + dataFile.getName() + " at: " + actualReportPath);
            assertNotNull(expectedReport,
                    "Oracle not found for file " + dataFile.getName() + " at: " + expectedReportPath);
        } catch (ArrayIndexOutOfBoundsException e2) {
            try {
                // Deserialize the report to test against the Oracle and the Oracle itself
                actualReport = FileUtils.readFileToString(new File(Objects.requireNonNull(getClass().getResource(actualReportPath)).toURI()), "UTF-8");

                expectedReport = FileUtils.readFileToString(new File(Objects.requireNonNull(getClass().getResource(expectedReportPath)).toURI()), "UTF-8");
            } catch (NullPointerException | IOException e3) {
                assertNotNull(actualReport,
                        "Report to test not found for file " + dataFile.getName() + " at: " + actualReportPath);
                assertNotNull(expectedReport,
                        "Oracle not found for file " + dataFile.getName() + " at: " + expectedReportPath);

            }
        }

        return new String[]{expectedReport, actualReport};
    }

    /**
     * Uses a filepath to generate a redux report and then asserts it to a premade Oracle made with the same analysis name
     *
     * @param dataFilepath
     * @param reportData
     * @throws JAXBException
     * @throws TripoliException
     * @throws URISyntaxException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     */
    public ETReduxFraction[] reduxReportTest(String dataFilepath, ReportData reportData) throws JAXBException, TripoliException, URISyntaxException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        AnalysisInterface analysis = reportData.getAnalysis();
        File dataFile = reportData.getDataFile();

        System.out.println("📝 Generating Redux Report for " + dataFile.getName());

        // Create the report to test against the Oracle
        AllBlockInitForDataLiteOne.initBlockModels(analysis);
        ETReduxFraction actualReport = analysis.prepareFractionForETReduxExport();
        String actualFileName = actualReport.getSampleName() + "_" + actualReport.getFractionID() + "_" + actualReport.getEtReduxExportType() + ".xml";

        // Convert the Oracle into an InputStream for XStream to deserialize
        String expectedReportPath = dataFilepath.substring(0, dataFilepath.lastIndexOf('/') + 1).replace("dataFiles", "reduxReports") + "Oracle-" + actualFileName;
        InputStream expectedReportXML = getClass().getResourceAsStream(expectedReportPath);
        assertNotNull(expectedReportXML,
                "Oracle not found for file " + dataFile.getName() + " at: " + expectedReportPath);

        // Set up XStream to deserialize the Oracle
        XStream xstream = new XStream(new DomDriver());
        xstream.addPermission(AnyTypePermission.ANY);

        // Custom settings found from ETReduxFraction customizeXstream(XStream xstream)
        xstream.registerConverter(new ETReduxFractionXMLConverter());
        xstream.alias("UPbReduxFraction", ETReduxFraction.class);
        xstream.alias("MeasuredUserFunctionModel", MeasuredUserFunction.class);

        // Deserialize Oracle report from resources
        ETReduxFraction expectedReport = (ETReduxFraction) xstream.fromXML(expectedReportXML);

        return new ETReduxFraction[]{expectedReport, actualReport};
    }

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

        System.out.println("📝 Generating Short Report for " + dataFile.getName());
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

    //####################################################################################################################//

    // Parameterized Test Source
    public static Stream<String> generate_filepaths() throws URISyntaxException {
        System.out.println("🗃️ Generating file paths...");

        String dataFilesDir = "/org/cirdles/tripoli/core/reporting/dataFiles/";
        Path dataFilesDirPath = Paths.get(Objects.requireNonNull(Tripoli.class.getResource(dataFilesDir)).toURI());

        try {
            Stream<Path> pathStream = Files.walk(dataFilesDirPath);
            System.out.println("✅ File paths generated successfully!");
            return pathStream
                    .filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().startsWith("New Session-"))
                    .map(Path::toString)
                    .map(p -> p.replace("\\", "/"))
                    .map(p -> p.substring(p.indexOf("/org/")));
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            return Stream.empty();
        }
    }

    @ParameterizedTest
    @MethodSource("generate_filepaths")
    public void generate_filepaths_test(String dataFilePath) {
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        try {
            ReportData reportData = generateReportData(dataFilePath);

            String[] fullReportTestResults = fullReportTest(dataFilePath, reportData);
            assertEquals(fullReportTestResults[0], fullReportTestResults[1], "❌ Full Report generation failed!");
            System.out.println("✅ Full Report generated successfully!\n");

//            ETReduxFraction[] reduxReportTestResults = reduxReportTest(dataFilePath, reportData);
//            assertEquals(reduxReportTestResults[0], reduxReportTestResults[1], "❌ Redux Report generation failed!");
//            System.out.println("✅ Redux Report generated successfully!\n");

            String[] shortReportTestResults = shortReportTest(dataFilePath, reportData);
            assertEquals(shortReportTestResults[0], shortReportTestResults[1], "❌ Short Report generation failed!");
            System.out.println("✅ Short Report generated successfully!\n");
        } catch (JAXBException | TripoliException | URISyntaxException | InvocationTargetException |
                 NoSuchMethodException | IllegalAccessException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}