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
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import static org.cirdles.tripoli.constants.TripoliConstants.SYNTHETIC_DATA_FOLDER_2ISOTOPE;

/**
 * @author James F. Bowring
 */
public enum SessionBuiltinFactory {
    ;

    public static final Map<String, Session> sessionsBuiltinMap = new TreeMap<>();
    public static final String TRIPOLI_DEMONSTRATION_SESSION = "Tripoli Demonstration Session";

    static {
        Session tripoliDemonstrationSession = null;
        try {
            tripoliDemonstrationSession = Session.initializeSession(TRIPOLI_DEMONSTRATION_SESSION);
        } catch (TripoliException e) {
            // throw new RuntimeException(e);
        }
        tripoliDemonstrationSession.setAnalystName("Team Tripoli");
        tripoliDemonstrationSession.setMutable(false);
        sessionsBuiltinMap.put(tripoliDemonstrationSession.getSessionName(), tripoliDemonstrationSession);

        Analysis fiveIsotopes_12 = null;
        try {
            fiveIsotopes_12 = AnalysisInterface.initializeAnalysis("5 Isotope Demo_12", null, "5 Isotopes of Pb 12");
        } catch (TripoliException e) {
//            throw new RuntimeException(e);
        }
        Path dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_12.txt");
        try {
            if (fiveIsotopes_12 != null) {
                fiveIsotopes_12.extractMassSpecDataFromPath(dataFilePath);
                tripoliDemonstrationSession.addAnalysis(fiveIsotopes_12);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }

        Analysis twoIsotopes_01 = null;
        try {
            twoIsotopes_01 = AnalysisInterface.initializeAnalysis("Two Isotope Demo_01", null, "Two Isotopes of Pb 01");
        } catch (TripoliException e) {
//            throw new RuntimeException(e);
        }
        dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_01.txt");
        try {
            if (twoIsotopes_01 != null) {
                twoIsotopes_01.extractMassSpecDataFromPath(dataFilePath);
                tripoliDemonstrationSession.addAnalysis(twoIsotopes_01);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }

        Analysis twoIsotopes_02 = null;
        try {
            twoIsotopes_02 = AnalysisInterface.initializeAnalysis("Two Isotope Demo_02", null, "Two Isotopes of Pb 02");
        } catch (TripoliException e) {
//            throw new RuntimeException(e);
        }
        dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_02.txt");
        try {
            if (twoIsotopes_02 != null) {
                twoIsotopes_02.extractMassSpecDataFromPath(dataFilePath);
                tripoliDemonstrationSession.addAnalysis(twoIsotopes_02);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }

        Analysis twoIsotopes_03 = null;
        try {
            twoIsotopes_03 = AnalysisInterface.initializeAnalysis("Two Isotope Demo_03", null, "Two Isotopes of Pb 03");
        } catch (TripoliException e) {
//            throw new RuntimeException(e);
        }
        dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_03.txt");
        try {
            if (twoIsotopes_03 != null) {
                twoIsotopes_03.extractMassSpecDataFromPath(dataFilePath);
                tripoliDemonstrationSession.addAnalysis(twoIsotopes_03);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }

        Analysis twoIsotopes_04 = null;
        try {
            twoIsotopes_04 = AnalysisInterface.initializeAnalysis("Two Isotope Demo_04", null, "Two Isotopes of Pb 04");
        } catch (TripoliException e) {
//            throw new RuntimeException(e);
        }
        dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_04.txt");
        try {
            if (twoIsotopes_04 != null) {
                twoIsotopes_04.extractMassSpecDataFromPath(dataFilePath);
                tripoliDemonstrationSession.addAnalysis(twoIsotopes_04);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }

        Analysis twoIsotopes_05 = null;
        try {
            twoIsotopes_05 = AnalysisInterface.initializeAnalysis("Two Isotope Demo_05", null, "Two Isotopes of Pb 05");
        } catch (TripoliException e) {
//            throw new RuntimeException(e);
        }
        dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_05.txt");
        try {
            if (twoIsotopes_05 != null) {
                twoIsotopes_05.extractMassSpecDataFromPath(dataFilePath);
                tripoliDemonstrationSession.addAnalysis(twoIsotopes_05);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }

        Analysis twoIsotopes_06 = null;
        try {
            twoIsotopes_06 = AnalysisInterface.initializeAnalysis("Two Isotope Demo_06", null, "Two Isotopes of Pb 06");
        } catch (TripoliException e) {
//            throw new RuntimeException(e);
        }
        dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_06.txt");
        try {
            if (twoIsotopes_06 != null) {
                twoIsotopes_06.extractMassSpecDataFromPath(dataFilePath);
                tripoliDemonstrationSession.addAnalysis(twoIsotopes_06);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }

        Analysis twoIsotopes_07 = null;
        try {
            twoIsotopes_07 = AnalysisInterface.initializeAnalysis("Two Isotope Demo_07", null, "Two Isotopes of Pb 07");
        } catch (TripoliException e) {
//            throw new RuntimeException(e);
        }
        dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_07.txt");
        try {
            if (twoIsotopes_07 != null) {
                twoIsotopes_07.extractMassSpecDataFromPath(dataFilePath);
                tripoliDemonstrationSession.addAnalysis(twoIsotopes_07);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }

        Analysis twoIsotopes_08 = null;
        try {
            twoIsotopes_08 = AnalysisInterface.initializeAnalysis("Two Isotope Demo_08", null, "Two Isotopes of Pb 08");
        } catch (TripoliException e) {
//            throw new RuntimeException(e);
        }
        dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_08.txt");
        try {
            if (twoIsotopes_08 != null) {
                twoIsotopes_08.extractMassSpecDataFromPath(dataFilePath);
                tripoliDemonstrationSession.addAnalysis(twoIsotopes_08);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }
    }
}