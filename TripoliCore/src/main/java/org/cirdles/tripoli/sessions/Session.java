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

import org.cirdles.tripoli.sessions.analysis.Analysis;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author James F. Bowring
 */
public class Session implements Serializable {

    @Serial
    private static final long serialVersionUID = 6597752272434171800L;


    private String sessionName;
    private Map<String, Analysis> mapOfAnalyses;

    private Session() {
        this("Default Session");
    }

    private Session(String sessionName) {
        this(sessionName, new LinkedHashMap<>());
    }

    private Session(String sessionName, Map<String, Analysis> mapOfAnalyses) {
        this.sessionName = sessionName;
        this.mapOfAnalyses = mapOfAnalyses;
    }

    public static Session initializeDefaultSession() {
        return new Session();
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public Map<String, Analysis> getMapOfAnalyses() {
        return mapOfAnalyses;
    }

    public void setMapOfAnalyses(Map<String, Analysis> mapOfAnalyses) {
        this.mapOfAnalyses = mapOfAnalyses;
    }
}