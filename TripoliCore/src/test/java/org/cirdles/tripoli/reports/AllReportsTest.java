package org.cirdles.tripoli.reports;

import jakarta.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.sessions.Session;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AllReportsTest {

    public ReportData generateReportData(String dataFilepath) throws URISyntaxException, JAXBException, TripoliException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        File dataFile = new File(Objects.requireNonNull(getClass().getResource(dataFilepath)).toURI());
        System.out.println("📝 Generating Report Data for " + dataFile.getName());

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
            }
            else {
                analysis = null;
            }

        } catch (IOException | JAXBException | TripoliException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {}

        assertNotNull(analysis);
        analysis.getUserFunctions().sort(null);

        return new ReportData(List.of(analysis), analysis, analysisName, tripoliSession, dataFilepath, dataFile);
    }

    /**
     * Uses a filepath to generate a test report and then asserts it to a premade Oracle made with the same analysis name
     */
    public void fullReportTest(String dataFilepath) throws JAXBException, TripoliException, URISyntaxException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ReportData reportData = generateReportData(dataFilepath);
        List<AnalysisInterface> analysisList = reportData.getAnalysisList();
        AnalysisInterface analysis = reportData.getAnalysis();
        String analysisName = reportData.getAnalysisName();
        Session tripoliSession = reportData.getTripoliSession();
        File dataFile = reportData.getDataFile();
        System.out.println("✅ Report Data generated successfully!");

        System.out.println("📝 Generating Full Report for " + dataFile.getName());

        Report fullReport = Report.createFullReport("Full Report", analysis);
        fullReport.generateCSVFile(analysisList, tripoliSession.getSessionName());

        String actualReportPath = dataFilepath.substring(0, dataFilepath.lastIndexOf('/') + 1) + "New Session-" + analysisName + "-report.csv";
        String expectedReportPath = dataFilepath.substring(0, dataFilepath.lastIndexOf('/') + 1).replace("dataFiles", "fullReports") + "Oracle-" + analysisName + "-report.csv";

        String actualReport = "";
        String expectedReport = "Oracle not found for file " + dataFile.getName() + " at: " + expectedReportPath;
        try {

            actualReport = FileUtils.readFileToString(new File(Objects.requireNonNull(getClass().getResource(actualReportPath)).toURI()), "UTF-8");

            expectedReport = FileUtils.readFileToString(new File(Objects.requireNonNull(getClass().getResource(expectedReportPath)).toURI()), "UTF-8");
        }
        catch (NullPointerException | IOException e) {
            System.out.println(expectedReport);
        }

        assertEquals(expectedReport, actualReport);
    }

    @Test
    public void B998_F11_13223M02_iz1_Pb1_14973ReportTest() {
        // This is the absolute path of the file that is tested
        String dataFilepath = "/org/cirdles/tripoli/core/reporting/dataFiles/IsotopxPhoenixTIMS/BoiseState/B998_F11_13223M02 iz1 Pb1-14973.xls";
        try {
            fullReportTest(dataFilepath);
            System.out.println("✅ Full Report generated successfully!");
        } catch (JAXBException | TripoliException | URISyntaxException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
           System.out.println("Error: " + e.getMessage());
        }
    }
}
