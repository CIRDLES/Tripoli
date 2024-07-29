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
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.utilities.collections.TripoliSessionAnalysisMap;
import org.cirdles.tripoli.utilities.collections.TripoliSpeciesColorMap;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliPersistentState;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import static org.cirdles.tripoli.constants.TripoliConstants.MISSING_STRING_FIELD;

/**
 * @author James F. Bowring
 */
public class Session implements Serializable {

    @Serial
    private static final long serialVersionUID = 6597752272434171800L;

    private static boolean sessionChanged;
    private String sessionName;
    private String analystName;
    private String sessionFilePathAsString;
    private String sessionNotes;
//    private Map<String, AnalysisInterface> mapOfAnalyses;
    private TripoliSessionAnalysisMap mapOfAnalyses;
    private boolean mutable;
    private TripoliSpeciesColorMap sessionDefaultMapOfSpeciesToColors;

    // Color Strings for ratio plots
    private String twoSigmaHexColorString;
    private String oneSigmaHexColorString;
    private String twoStdErrHexColorString;
    private String meanHexColorString;
    // END OF ratio plot Color Strings

    private Session() {
        this("New Session");
    }

    private Session(String sessionName) {
        this(sessionName, new TripoliSessionAnalysisMap());
    }

    private Session(String sessionName, Map<String, AnalysisInterface> mapOfAnalyses) {
        this.sessionName = sessionName;
        this.mapOfAnalyses = ((TripoliSessionAnalysisMap) mapOfAnalyses);
        this.mapOfAnalyses.setSession(this);
        analystName = MISSING_STRING_FIELD;
        sessionNotes = MISSING_STRING_FIELD;
        sessionFilePathAsString = "";
        mutable = true;
        sessionChanged = false;
    }

    public static Session initializeDefaultSession() throws JAXBException {
        Session session = new Session();
//        session.addAnalysis(initializeNewAnalysis(1));
        try {
            TripoliPersistentState tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
            session.sessionDefaultMapOfSpeciesToColors = tripoliPersistentState.getMapOfSpeciesToColors();
            session.twoSigmaHexColorString = tripoliPersistentState.getTwoSigmaHexColorString();
            session.oneSigmaHexColorString = tripoliPersistentState.getOneSigmaHexColorString();
            session.twoStdErrHexColorString = tripoliPersistentState.getTwoStdErrHexColorString();
            session.meanHexColorString = tripoliPersistentState.getMeanHexColorString();
        } catch (TripoliException e) {
            e.printStackTrace();
        }
        return session;
    }

    public static Session initializeSession(String sessionName) {
        Session session = new Session(sessionName);
        try {
            TripoliPersistentState tripoliPersistentState = TripoliPersistentState.getExistingPersistentState();
            session.sessionDefaultMapOfSpeciesToColors = tripoliPersistentState.getMapOfSpeciesToColors();
            session.twoSigmaHexColorString = tripoliPersistentState.getTwoSigmaHexColorString();
            session.oneSigmaHexColorString = tripoliPersistentState.getOneSigmaHexColorString();
            session.twoStdErrHexColorString = tripoliPersistentState.getTwoStdErrHexColorString();
            session.meanHexColorString = tripoliPersistentState.getMeanHexColorString();
        } catch (TripoliException e) {
            e.printStackTrace();
        }
        return session;
    }

    public static boolean isSessionChanged() {
        return sessionChanged;
    }

    public static void setSessionChanged(boolean mySessionChanged) {
        sessionChanged = mySessionChanged;
    }

    public void addAnalysis(AnalysisInterface analysis) {
        if (!mapOfAnalyses.containsKey(analysis.getAnalysisName())) {
            mapOfAnalyses.put(analysis.getAnalysisName(), analysis);
        }
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionDefaultMapOfSpeciesToColors(TripoliSpeciesColorMap sessionDefaultMapOfSpeciesToColors) {
        this.sessionDefaultMapOfSpeciesToColors = sessionDefaultMapOfSpeciesToColors;
    }


    public TripoliSpeciesColorMap getSessionDefaultMapOfSpeciesToColors() {
        return sessionDefaultMapOfSpeciesToColors;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getAnalystName() {
        return analystName;
    }

    public void setAnalystName(String analystName) {
        this.analystName = analystName;
    }

    public String getSessionFilePathAsString() {
        return sessionFilePathAsString;
    }

    public void setSessionFilePathAsString(String sessionFilePathAsString) {
        this.sessionFilePathAsString = sessionFilePathAsString;
    }

    public Map<String, AnalysisInterface> getMapOfAnalyses() {
        return mapOfAnalyses;
    }

    public void setMapOfAnalyses(Map<String, AnalysisInterface> mapOfAnalyses) {
        this.mapOfAnalyses = (TripoliSessionAnalysisMap) mapOfAnalyses;
    }

    public String getSessionNotes() {
        return sessionNotes;
    }

    public void setSessionNotes(String sessionNotes) {
        this.sessionNotes = sessionNotes;
    }

    public boolean isMutable() {
        return mutable;
    }

    public void setMutable(boolean mutable) {
        this.mutable = mutable;
    }

    public String getTwoSigmaHexColorString() {
        return twoSigmaHexColorString;
    }

    public String getOneSigmaHexColorString() {
        return oneSigmaHexColorString;
    }

    public String getTwoStdErrHexColorString() {
        return twoStdErrHexColorString;
    }

    public String getMeanHexColorString() {
        return meanHexColorString;
    }

    public void setTwoSigmaHexColorString(String twoSigmaHexColorString) {
        this.twoSigmaHexColorString = twoSigmaHexColorString;
    }

    public void setOneSigmaHexColorString(String oneSigmaHexColorString) {
        this.oneSigmaHexColorString = oneSigmaHexColorString;
    }

    public void setTwoStdErrHexColorString(String twoStdErrHexColorString) {
        this.twoStdErrHexColorString = twoStdErrHexColorString;
    }

    public void setMeanHexColorString(String meanHexColorString) {
        this.meanHexColorString = meanHexColorString;
    }

    /**
     * For JUnit Testing by My Nguyen
     */
    @Override
    public boolean equals(Object session) {
        if (this == session) {
            return true;
        }
        if (session == null || getClass() != session.getClass()) {
            return false;
        }
        Session otherSession = (Session) session;
        return Objects.equals(sessionName, otherSession.sessionName)
                && Objects.equals(analystName, otherSession.analystName)
                && Objects.equals(sessionNotes, otherSession.sessionNotes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionName, analystName, sessionNotes);
    }
}