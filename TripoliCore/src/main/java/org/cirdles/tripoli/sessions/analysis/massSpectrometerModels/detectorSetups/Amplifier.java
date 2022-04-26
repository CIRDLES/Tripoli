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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups;

import java.io.Serializable;

/**
 * @author James F. Bowring
 */
public class Amplifier implements Serializable {
    private AmplifierTypeEnum amplifierType;

    private Amplifier() {
        this(AmplifierTypeEnum.RESISTANCE);
    }

    private Amplifier(AmplifierTypeEnum amplifierType) {
        this.amplifierType = amplifierType;
    }

    static Amplifier initializeAmplifier() {
        Amplifier amplifier = new Amplifier();
        return amplifier;
    }

    public AmplifierTypeEnum getAmplifierType() {
        return amplifierType;
    }

    public void setAmplifierType(AmplifierTypeEnum amplifierType) {
        this.amplifierType = amplifierType;
    }

    public enum AmplifierTypeEnum {
        RESISTANCE("RESISTANCE"),
        CAPACITANCE("CAPACITANCE"),
        NA("NA");

        final String name;

        AmplifierTypeEnum(String name) {
            this.name = name;
        }
    }
}