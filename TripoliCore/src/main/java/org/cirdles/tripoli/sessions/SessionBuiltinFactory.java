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

import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethodBuiltinFactory;
import org.cirdles.tripoli.sessions.analysis.samples.Sample;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import static org.cirdles.tripoli.TripoliConstants.SYNTHETIC_DATA_FOLDER_2ISOTOPE;
import static org.cirdles.tripoli.TripoliConstants.SYNTHETIC_DATA_FOLDER_5ISOTOPE;
import static org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethodBuiltinFactory.BURDICK_BL_SYNTHETIC_DATA;
import static org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethodBuiltinFactory.KU_204_5_6_7_8_DALY_ALL_FARADAY_PB;

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

        AnalysisMethod twoIsotopeSyntheticAnalysisMethod = AnalysisMethodBuiltinFactory.analysisMethodsBuiltinMap.get(BURDICK_BL_SYNTHETIC_DATA);
        Sample twoIsotopeSample_01 = new Sample("Two Isotopes of Pb 01");
        Analysis twoIsotopes_01 = AnalysisInterface.initializeAnalysis("Two Isotope Demo_01", twoIsotopeSyntheticAnalysisMethod, twoIsotopeSample_01);
        Path dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_2ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_01.txt");
        twoIsotopes_01.setDataFilePath(dataFilePath.toString());
        tripoliDemonstrationSession.addAnalysis(twoIsotopes_01);

        AnalysisMethod fiveIsotopeSyntheticAnalysisMethod = AnalysisMethodBuiltinFactory.analysisMethodsBuiltinMap.get(KU_204_5_6_7_8_DALY_ALL_FARADAY_PB);
        Sample fiveIsotopeSample_01 = new Sample("Five Isotopes of Pb 01");
        Analysis fiveIsotopes_01 = AnalysisInterface.initializeAnalysis("Five Isotope Demo_01", fiveIsotopeSyntheticAnalysisMethod, fiveIsotopeSample_01);
        dataFilePath = Path.of(SYNTHETIC_DATA_FOLDER_5ISOTOPE.getAbsolutePath() + File.separator + "SyntheticDataset_01R.txt");
        fiveIsotopes_01.setDataFilePath(dataFilePath.toString());
        tripoliDemonstrationSession.addAnalysis(fiveIsotopes_01);
    }
}