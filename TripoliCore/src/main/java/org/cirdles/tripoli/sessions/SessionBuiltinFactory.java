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
import static org.cirdles.tripoli.TripoliConstants.SYNTHETIC_DATA_FOLDER_5ISOTOPE;

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

        Analysis fiveIsotopes_01 = AnalysisInterface.initializeAnalysis("Five Isotope Demo_01", null, "Five Isotopes of Pb 01");
        dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_5ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_01R.txt");
        try {
            fiveIsotopes_01.extractMassSpecDataFromPath(dataFilePath);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException |
                 JAXBException | TripoliException e) {
            // do nothing
        }
        tripoliDemonstrationSession.addAnalysis(fiveIsotopes_01);
    }
}