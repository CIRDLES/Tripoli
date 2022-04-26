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

package org.cirdles.tripoli.sessions.analysis.methods.baselineTables;

import org.cirdles.tripoli.sessions.Session;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author James F. Bowring
 */
public class Baseline implements Serializable {
//    @Serial
//    private static final long serialVersionUID = 6597752272434171800L;
        private void readObject ( ObjectInputStream stream ) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();

        ObjectStreamClass myObject = ObjectStreamClass.lookup(
                Class.forName( Baseline.class.getCanonicalName()) );
        long theSUID = myObject.getSerialVersionUID();

        System.err.println( "Customized De-serialization of Baseline "
                + theSUID );
    }

    private String baselineName;
    private Map<Detector, BaselineCell> baselineCellsMap;

    private Baseline() {
    }

    private Baseline(String baselineName) {
        this.baselineName = baselineName;
        baselineCellsMap = new LinkedHashMap<>();
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Baseline initializeBaseline(String baselineName){
        return new Baseline(baselineName);
    }

    public void addBaselineCell(Detector detector, BaselineCell baselineCell){
        baselineCellsMap.put(detector, baselineCell);
    }

    public String getBaselineName() {
        return baselineName;
    }

    public void setBaselineName(String baselineName) {
        this.baselineName = baselineName;
    }

    public Map<Detector, BaselineCell> getBaselineCellsMap() {
        return baselineCellsMap;
    }

    public void setBaselineCellsMap(Map<Detector, BaselineCell> baselineCellsMap) {
        this.baselineCellsMap = baselineCellsMap;
    }
}