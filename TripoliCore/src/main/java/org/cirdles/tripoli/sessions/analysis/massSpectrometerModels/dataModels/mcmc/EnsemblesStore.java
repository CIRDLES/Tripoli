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
            double[] intensities,
            double[] baseLine,
            double dfGain,
            double[] signalNoise,
            double errorWeighted,
            double errorUnWeighted
    ) implements Serializable {
    }
}