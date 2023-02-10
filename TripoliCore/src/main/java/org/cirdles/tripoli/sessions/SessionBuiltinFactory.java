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

package org.cirdles.tripoli.sessions;

import jakarta.xml.bind.JAXBException;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import static org.cirdles.tripoli.TripoliConstants.SYNTHETIC_DATA_FOLDER_2ISOTOPE;

/**
 * @author James F. Bowring
 */
public final class SessionBuiltinFactory {

    public static final Map<String, Session> sessionsBuiltinMap = new TreeMap<>();
    public static String TRIPOLI_DEMONSTRATION_SESSION = "Tripoli Demonstration Session";

    private static ResourceExtractor RESOURCE_EXTRACTOR = new ResourceExtractor(Tripoli.class);

    static {
        Session tripoliDemonstrationSession = Session.initializeSession(TRIPOLI_DEMONSTRATION_SESSION);
        tripoliDemonstrationSession.setAnalystName("Team Tripoli");
        tripoliDemonstrationSession.setMutable(false);
        sessionsBuiltinMap.put(tripoliDemonstrationSession.getSessionName(), tripoliDemonstrationSession);

        Analysis twoIsotopes_01 = AnalysisInterface.initializeAnalysis("Two Isotope Demo_01", null, "Two Isotopes of Pb 01");
        Path dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_01.txt");
        try {
            twoIsotopes_01.extractMassSpecDataFromPath(dataFilePath);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }
        tripoliDemonstrationSession.addAnalysis(twoIsotopes_01);

        Analysis twoIsotopes_02 = AnalysisInterface.initializeAnalysis("Two Isotope Demo_02", null, "Two Isotopes of Pb 02");
        dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_02.txt");
        try {
            twoIsotopes_02.extractMassSpecDataFromPath(dataFilePath);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }
        tripoliDemonstrationSession.addAnalysis(twoIsotopes_02);

        Analysis twoIsotopes_03 = AnalysisInterface.initializeAnalysis("Two Isotope Demo_03", null, "Two Isotopes of Pb 03");
        dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_03.txt");
        try {
            twoIsotopes_03.extractMassSpecDataFromPath(dataFilePath);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }
        tripoliDemonstrationSession.addAnalysis(twoIsotopes_03);

        Analysis twoIsotopes_04 = AnalysisInterface.initializeAnalysis("Two Isotope Demo_04", null, "Two Isotopes of Pb 04");
        dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_04.txt");
        try {
            twoIsotopes_04.extractMassSpecDataFromPath(dataFilePath);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }
        tripoliDemonstrationSession.addAnalysis(twoIsotopes_04);

        Analysis twoIsotopes_05 = AnalysisInterface.initializeAnalysis("Two Isotope Demo_05", null, "Two Isotopes of Pb 05");
        dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_05.txt");
        try {
            twoIsotopes_05.extractMassSpecDataFromPath(dataFilePath);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }
        tripoliDemonstrationSession.addAnalysis(twoIsotopes_05);

        Analysis twoIsotopes_06 = AnalysisInterface.initializeAnalysis("Two Isotope Demo_06", null, "Two Isotopes of Pb 06");
        dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_06.txt");
        try {
            twoIsotopes_06.extractMassSpecDataFromPath(dataFilePath);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }
        tripoliDemonstrationSession.addAnalysis(twoIsotopes_06);

        Analysis twoIsotopes_07 = AnalysisInterface.initializeAnalysis("Two Isotope Demo_07", null, "Two Isotopes of Pb 07");
        dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_07.txt");
        try {
            twoIsotopes_07.extractMassSpecDataFromPath(dataFilePath);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }
        tripoliDemonstrationSession.addAnalysis(twoIsotopes_07);

        Analysis twoIsotopes_08 = AnalysisInterface.initializeAnalysis("Two Isotope Demo_08", null, "Two Isotopes of Pb 08");
        dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_08.txt");
        try {
            twoIsotopes_08.extractMassSpecDataFromPath(dataFilePath);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }
        tripoliDemonstrationSession.addAnalysis(twoIsotopes_08);




    }
}