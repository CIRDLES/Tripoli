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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc;

import org.cirdles.tripoli.species.IsotopicRatio;

import java.io.Serializable;
import java.util.List;

/**
 * @author James F. Bowring
 */
public class EnsemblesStore implements Serializable {

    private final List<EnsembleRecord> ensembles;
    private final SingleBlockModelRecord lastDataModelInit;

    public EnsemblesStore(List<EnsembleRecord> ensembles, SingleBlockModelRecord lastDataModelInit) {
        this.ensembles = ensembles;
        this.lastDataModelInit = lastDataModelInit;
    }

    public List<EnsembleRecord> getEnsembles() {
        return ensembles;
    }

    public SingleBlockModelRecord getLastDataModelInit() {
        return lastDataModelInit;
    }

    public record EnsembleRecord(
            double[] logRatios,
            double[] I0,
            double[] baseLine,
            double dfGain,
            double[] signalNoise,
            double errorWeighted,
            double errorUnWeighted
    ) implements Serializable {
        public String prettyPrintHeaderAsCSV(String indexTitle, List<IsotopicRatio> isotopicRatiosList) {
            String header = "";
            for (int i = 0; i < logRatios.length; i++) {
                header += isotopicRatiosList.get(i).prettyPrint().replaceAll(" ", "") + ",";
            }
            for (int i = 0; i < I0.length; i++) {
                header += "I-" + i + ",";
            }
            for (int i = 0; i < baseLine.length; i++) {
                header += "BL-" + i + ",";
            }
            header += "DFGain,";
            for (int i = 0; i < signalNoise.length; i++) {
                header += "SigNoise-" + i + ",";
            }
            header += "errorWeighted,";
            header += "errorUnWeighted \n";

            return header;
        }

        public String prettyPrintAsCSV() {
            String data = "";
            for (int i = 0; i < logRatios.length; i++) {
                data += logRatios[i] + ",";
            }
            for (int i = 0; i < I0.length; i++) {
                data += I0[i] + ",";
            }
            for (int i = 0; i < baseLine.length; i++) {
                data += baseLine[i] + ",";
            }
            data += dfGain() + ",";
            for (int i = 0; i < signalNoise.length; i++) {
                data += signalNoise[i] + ",";
            }
            data += errorWeighted + ",";
            data += errorUnWeighted + "\n";

            return data;
        }
    }
}