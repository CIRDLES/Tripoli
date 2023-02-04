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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmcV2;

import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.DataModellerOutputRecord;

import java.io.Serializable;
import java.util.List;

/**
 * @author James F. Bowring
 */
public class EnsemblesStoreV2 implements Serializable {

    private final List<EnsembleRecord> ensembles;
    private final DataModellerOutputRecord lastDataModelInit;

    public EnsemblesStoreV2(List<EnsembleRecord> ensembles, DataModellerOutputRecord lastDataModelInit) {
        this.ensembles = ensembles;
        this.lastDataModelInit = lastDataModelInit;
    }

    public List<EnsembleRecord> getEnsembles() {
        return ensembles;
    }

    public DataModellerOutputRecord getLastDataModelInit() {
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