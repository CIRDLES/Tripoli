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

    /**
     * Uses a filepath to generate a test report and then asserts it to a premade Oracle made with the same analysis name
     */
    @Test
    public void fullReportTest() throws URISyntaxException, JAXBException, IOException {
        // This is the absolute path of the file that is tested
        String filepath = "C:/Users/redfl/Desktop/CIRDLES/Test Data/TripoliTestData/IsotopxPhoenixTIMS/KU_IGL/IsolinxVersion2/NBS981 230024a.RAW/NBS981 230024a-145.TIMSDP";

        File dataFile = new File(filepath);

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

        } catch (IOException | JAXBException | TripoliException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {

        }

        assertNotNull(analysis);
        analysis.getUserFunctions().sort(null);
        Report fullReport = Report.createFullReport("Full Report", analysis);
        List<AnalysisInterface> analysisList = List.of(analysis);
        fullReport.generateCSVFile(analysisList, tripoliSession.getSessionName());

        String actualReport = "";
        String expectedReport = "Oracle not found for file " + dataFile.getName() + " at: TripoliCore/src/test/resources/org/cirdles/tripoli/core/fullReports";

        try {
            actualReport = FileUtils.readFileToString(new File(filepath.substring(0, filepath.lastIndexOf('/') + 1) + "New Session-" + analysisName + "-report.csv"), "UTF-8");

            /**
             * To make an Oracle for an analysis:
             * 1. Manually create a report with Tripoli.
             * 2. Name it Oracle-{analysis name}-report.csv
             * 3. Move it into TripoliCore/src/test/resources/org/cirdles/tripoli/core/fullReports
             * 4. Replace the values for "Data File Path" and "Created On:" to DATA_FILE_PATH and TIME_CREATED, respectively
             */
            expectedReport = FileUtils.readFileToString(new File(Objects.requireNonNull(getClass().getResource("/org/cirdles/tripoli/core/reporting/dataFiles/fullReports/Oracle-" + analysisName + "-report.csv")).toURI()), "UTF-8")
                    .replace("DATA_FILE_PATH", dataFile.toPath().toString())
                    .replace("TIME_CREATED", fullReport.getTimeCreated());
        }
        catch (NullPointerException e) {
            System.out.println(expectedReport);
        }

        assertEquals(expectedReport, actualReport);
    }

    @Test
    public void clipboardReportTest() {

    }

}
