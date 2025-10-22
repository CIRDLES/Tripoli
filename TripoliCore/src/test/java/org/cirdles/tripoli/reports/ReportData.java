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

import jakarta.xml.bind.JAXBException;
import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.sessions.Session;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReportData {
    List<AnalysisInterface> analysisList;
    AnalysisInterface analysis;
    String analysisName;
    Session tripoliSession;
    String dataFilepath;
    File dataFile;

    public ReportData(List<AnalysisInterface> analysisList, AnalysisInterface analysis, String analysisName, Session tripoliSession, String dataFilepath, File dataFile) {
        this.analysisList = analysisList;
        this.analysis = analysis;
        this.analysisName = analysisName;
        this.tripoliSession = tripoliSession;
        this.dataFilepath = dataFilepath;
        this.dataFile = dataFile;
    }

    public ReportData() {}

    public List<AnalysisInterface> getAnalysisList() {
        return analysisList;
    }

    public AnalysisInterface getAnalysis() {
        return analysis;
    }

    public String getAnalysisName() {
        return analysisName;
    }

    public Session getTripoliSession() {
        return tripoliSession;
    }

    public String getDataFilepath() {
        return dataFilepath;
    }

    public File getDataFile() {
        return dataFile;
    }

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
        System.out.println("💾 Generating Report Data for " + dataFile.getName() + "...");

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
}
