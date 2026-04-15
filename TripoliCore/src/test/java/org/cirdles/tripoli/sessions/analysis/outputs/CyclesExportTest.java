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

import static org.cirdles.tripoli.sessions.analysis.Analysis.suppressContents;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CyclesExportTest {

    /**
     * Uses a filepath to generate a cycles export report and then asserts it to a premade Oracle made with the same analysis name
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
    public String[] cyclesExportTest(String dataFilepath, ReportData reportData) throws JAXBException, TripoliException, URISyntaxException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        suppressContents = true;
        AnalysisInterface analysis = reportData.getAnalysis();
        String analysisName = reportData.getAnalysisName();
        File dataFile = reportData.getDataFile();

        // Sort UserFunctions
        List<UserFunction> userFunctions = new ArrayList<>(analysis.getUserFunctions());
        userFunctions.sort(null);
        analysis.setUserFunctions(userFunctions);

        System.out.println("📝 Generating Cycles Export Report for " + dataFile.getName() + "...");
        // Create the report to test against the Oracle
        String actualReport = "";
        String expectedReport = null;
        String expectedReportPath = null;
        try {
            // Deserialize the Oracle report
            expectedReportPath = dataFilepath.substring(0, dataFilepath.lastIndexOf('/') + 1).replace("dataFiles", "cycleReports") + "Oracle-" + analysisName + "-report.tsv";
            expectedReport = FileUtils.readFileToString(new File(Objects.requireNonNull(getClass().getResource(expectedReportPath)).toURI()), "UTF-8");

            AllBlockInitForDataLiteOne.initBlockModels(analysis);
            actualReport = String.join("", analysis.prepareFractionForCyclesExport(reportData.getTripoliSession()));
        } catch (NullPointerException | IOException e) {
            assertNotNull(expectedReport,
                    "Oracle not found for file " + dataFile.getName() + " at: " + expectedReportPath);
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        return new String[]{expectedReport, actualReport};
    }

    @ParameterizedTest
    @MethodSource("org.cirdles.tripoli.reports.ReportData#generateFilepaths")
    public void cyclesExportTestResults(String dataFilepath) {
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        try {
            ReportData reportData = new ReportData();
            reportData = reportData.generateReportData(dataFilepath);

            System.out.println("Cycles Export Test Results for " + dataFilepath + ":");
            String[] cyclesExportTestResults = cyclesExportTest(dataFilepath, reportData);
            assertEquals(cyclesExportTestResults[0], cyclesExportTestResults[1], "❌ Cycles Export Report generation failed!\n");
            System.out.println("✅ Cycles Export Report generated successfully!\n");
        } catch (JAXBException | TripoliException | URISyntaxException | InvocationTargetException |
                 NoSuchMethodException | IllegalAccessException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
