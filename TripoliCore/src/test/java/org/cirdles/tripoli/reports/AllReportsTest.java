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

    public ReportData generateReportData() throws URISyntaxException, IOException, JAXBException, TripoliException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        // This is the absolute path of the file that is tested
        String dataFilepath = "/org/cirdles/tripoli/core/reporting/dataFiles/IsotopxPhoenixTIMS/BoiseState/B998_F11_13223M02 iz1 Pb1-14973.xls";

        File dataFile = new File(Objects.requireNonNull(getClass().getResource(dataFilepath)).toURI());

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

        return new ReportData(List.of(analysis), analysis, analysisName, tripoliSession, dataFilepath, dataFile) ;
    }

    /**
     * Uses a filepath to generate a test report and then asserts it to a premade Oracle made with the same analysis name
     */
    @Test
    public void fullReportTest() throws URISyntaxException, JAXBException, IOException, TripoliException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ReportData reportData = generateReportData();
        List<AnalysisInterface> analysisList = reportData.getAnalysisList();
        AnalysisInterface analysis = reportData.getAnalysis();
        String analysisName = reportData.getAnalysisName();
        Session tripoliSession = reportData.getTripoliSession();
        String dataFilepath = reportData.getDataFilepath();
        File dataFile = reportData.getDataFile();

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
        catch (NullPointerException e) {
            System.out.println(expectedReport);
        }

        assertEquals(expectedReport, actualReport);
    }
}
